import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.nanocalculate.calculator"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  // Register a task to generate release keystore at execution time before packaging/building
  val generateKeystoreTask = tasks.register("generateKeystore") {
    val jksFile = file("nanocalculate.jks")
    onlyIf { !jksFile.exists() }
    doLast {
      try {
        ProcessBuilder(
          "keytool", "-genkeypair", "-noprompt",
          "-keystore", jksFile.absolutePath,
          "-alias", "nanocalculate_alias",
          "-keyalg", "RSA",
          "-keysize", "2048",
          "-validity", "10000",
          "-storepass", "sahid123",
          "-keypass", "sahid123",
          "-dname", "CN=sahid, O=Aura green, L=Kolkata, S=West Bengal, C=IN"
        ).inheritIO().start().waitFor()
      } catch (e: Exception) {
        logger.warn("Keystore creation failed: ", e)
      }
    }
  }

  // Register a task to generate legacy and adaptive mipmaps from our single icon (ic_launcher_fgs.png) at build-time
  val generateMipmapsTask = tasks.register("generateMipmaps") {
    val srcPath = project.projectDir.resolve("src/main/res/drawable/ic_launcher_fgs.png")
    val resPath = project.projectDir.resolve("src/main/res")
    onlyIf { srcPath.exists() }
    doLast {
      try {
        val densities = mapOf(
          "mdpi" to 48,
          "hdpi" to 72,
          "xhdpi" to 96,
          "xxhdpi" to 144,
          "xxxhdpi" to 192
        )
        val img = javax.imageio.ImageIO.read(srcPath)
        if (img != null) {
          densities.forEach { (dirName, size) ->
            val dir = resPath.resolve("mipmap-$dirName")
            if (!dir.exists()) {
              dir.mkdirs()
            }
            val resized = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
            val g = resized.createGraphics()
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.drawImage(img, 0, 0, size, size, null)
            g.dispose()

            javax.imageio.ImageIO.write(resized, "png", dir.resolve("ic_launcher.png"))
            javax.imageio.ImageIO.write(resized, "png", dir.resolve("ic_launcher_round.png"))
          }
          println("Successfully generated and saved all adaptive multi-density launcher icons.")
        } else {
          println("Source launcher image read returned null!")
        }
      } catch (e: Exception) {
        println("Launcher icon multi-density copies generation failed: ${e.message}")
      }
    }
  }

  // Ensure all build tasks depend on our keystore generation and mipmap generation tasks (excluding themselves)
  tasks.matching { (it.name.startsWith("preBuild") || it.name.startsWith("generate")) && it.name != "generateKeystore" && it.name != "generateMipmaps" }.all {
    dependsOn(generateKeystoreTask)
    dependsOn(generateMipmapsTask)
  }

  signingConfigs {
    create("release") {
      storeFile = file("nanocalculate.jks")
      storePassword = "sahid123"
      keyAlias = "nanocalculate_alias"
      keyPassword = "sahid123"
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.play.services.ads)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

tasks.register<Copy>("copyReleaseApk") {
  dependsOn("assembleRelease")
  from(layout.buildDirectory.file("outputs/apk/release/app-release.apk"))
  into(file("${rootDir}/.build-outputs"))
  doLast {
    copy {
      from(layout.buildDirectory.file("outputs/apk/release/app-release.apk"))
      into(file("${rootDir}/release_apk"))
    }
  }
}

