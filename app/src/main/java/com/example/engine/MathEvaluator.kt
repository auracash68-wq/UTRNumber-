package com.example.engine

import java.text.DecimalFormat
import kotlin.math.*

class MathEvaluator(private val isDegreeMode: Boolean = false) {
    
    fun evaluate(expression: String): EvaluationResult {
        if (expression.isBlank()) return EvaluationResult.Success(0.0, "")
        
        val sanitized = sanitizeExpression(expression)
        return try {
            val parser = Parser(sanitized, isDegreeMode)
            val value = parser.parse()
            if (value.isNaN()) {
                EvaluationResult.Error("Undefined")
            } else if (value.isInfinite()) {
                EvaluationResult.Error(if (value < 0) "-Infinity" else "Infinity")
            } else {
                EvaluationResult.Success(value, formatResult(value))
            }
        } catch (e: Exception) {
            EvaluationResult.Error(e.message ?: "Syntax Error")
        }
    }
    
    private fun sanitizeExpression(expr: String): String {
        return expr
            // Replace visual operational characters with parser symbols
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", "PI")
            .replace("e", "E")
            .replace("−", "-") // replace unicode minus
            // Ensure proper parentheses layout
            .replace(" ", "")
    }
    
    private fun formatResult(value: Double): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value < 0) "-Infinity" else "Infinity"
        
        // Handle scientific notation for extremely large/small values to avoid long fractional strings
        val absVal = abs(value)
        return if (absVal > 0 && (absVal >= 1e15 || absVal < 1e-11)) {
            val formatter = DecimalFormat("0.########E0")
            formatter.format(value)
        } else {
            // Max out display formatting at 12 decimal places
            val formatter = DecimalFormat("0.############")
            formatter.format(value)
        }
    }
}

sealed class EvaluationResult {
    data class Success(val value: Double, val formatted: String) : EvaluationResult()
    data class Error(val message: String) : EvaluationResult()
}

private class Parser(private val str: String, private val isDegreeMode: Boolean) {
    private var pos = -1
    private var ch = 0

    private fun nextChar() {
        pos++
        ch = if (pos < str.length) str[pos].code else -1
    }

    private fun eat(charToEat: Int): Boolean {
        while (ch == ' '.code) nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    fun parse(): Double {
        nextChar()
        val x = parseExpression()
        if (pos < str.length) throw RuntimeException("Syntax Error")
        return x
    }

    private fun parseExpression(): Double {
        var x = parseTerm()
        while (true) {
            if (eat('+'.code)) x += parseTerm()
            else if (eat('-'.code)) x -= parseTerm()
            else return x
        }
    }

    private fun parseTerm(): Double {
        var x = parseFactor()
        while (true) {
            if (eat('*'.code)) x *= parseFactor()
            else if (eat('/'.code)) {
                val divisor = parseFactor()
                if (divisor == 0.0) throw RuntimeException("Div by 0")
                x /= divisor
            } else if (eat('m'.code)) {
                if (ch == 'o'.code) {
                    nextChar()
                    if (ch == 'd'.code) {
                        nextChar()
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw RuntimeException("Div by 0")
                        x %= divisor
                    } else {
                        throw RuntimeException("Syntax Error: 'mo' is unknown")
                    }
                } else {
                    throw RuntimeException("Syntax Error: 'm' is unknown")
                }
            } else {
                // Implicit multiplication: if next token is (, constant, or start of a function/number
                if (ch == '('.code || ch == 'P'.code || ch == 'E'.code || 
                    (ch >= 'a'.code && ch <= 'z'.code) || (ch >= 'A'.code && ch <= 'Z'.code)) {
                    x *= parseFactor()
                } else {
                    return x
                }
            }
        }
    }

    private fun parseFactor(): Double {
        if (eat('+'.code)) return +parseFactor()
        if (eat('-'.code)) return -parseFactor()

        var x: Double
        val startPos = this.pos
        if (eat('('.code)) {
            x = parseExpression()
            if (!eat(')'.code)) throw RuntimeException("Mismatched Parentheses")
        } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
            while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
            x = str.substring(startPos, this.pos).toDouble()
        } else if ((ch >= 'A'.code && ch <= 'Z'.code) || (ch >= 'a'.code && ch <= 'z'.code)) {
            while ((ch >= 'A'.code && ch <= 'Z'.code) || (ch >= 'a'.code && ch <= 'z'.code)) nextChar()
            val name = str.substring(startPos, this.pos)
            
            if (name == "PI") {
                x = PI
            } else if (name == "E") {
                x = E
            } else {
                if (!eat('('.code)) throw RuntimeException("Missing '(' for '$name'")
                val arg = parseExpression()
                if (!eat(')'.code)) throw RuntimeException("Unclosed function '$name'")
                
                x = when (name.lowercase()) {
                    "sin" -> {
                        val rad = if (isDegreeMode) Math.toRadians(arg) else arg
                        sin(rad)
                    }
                    "cos" -> {
                        val rad = if (isDegreeMode) Math.toRadians(arg) else arg
                        cos(rad)
                    }
                    "tan" -> {
                        val rad = if (isDegreeMode) Math.toRadians(arg) else arg
                        tan(rad)
                    }
                    "asin" -> {
                        val radVal = asin(arg)
                        if (isDegreeMode) Math.toDegrees(radVal) else radVal
                    }
                    "acos" -> {
                        val radVal = acos(arg)
                        if (isDegreeMode) Math.toDegrees(radVal) else radVal
                    }
                    "atan" -> {
                        val radVal = atan(arg)
                        if (isDegreeMode) Math.toDegrees(radVal) else radVal
                    }
                    "log" -> log10(arg)
                    "ln" -> ln(arg)
                    "sqrt" -> {
                        if (arg < 0.0) throw RuntimeException("Domain Error: sqrt limit")
                        sqrt(arg)
                    }
                    "cbrt" -> Math.cbrt(arg)
                    "abs" -> abs(arg)
                    else -> throw RuntimeException("Unknown function: $name")
                }
            }
        } else {
            throw RuntimeException("Syntax Error")
        }

        // Postfix operations like indices/exponents (power, factorial)
        while (true) {
            if (eat('^'.code)) {
                x = x.pow(parseFactor())
            } else if (eat('!'.code)) {
                x = factorial(x)
            } else if (eat('%'.code)) {
                x /= 100.0
            } else {
                break
            }
        }

        return x
    }

    private fun factorial(n: Double): Double {
        if (n < 0.0 || n > 170.0) throw RuntimeException("Domain Error: factorial")
        val intN = n.roundToInt()
        if (abs(n - intN) > 1e-9) {
            throw RuntimeException("Integer Factorials only")
        }
        if (intN == 0) return 1.0
        var result = 1.0
        for (i in 1..intN) {
            result *= i
        }
        return result
    }
}
