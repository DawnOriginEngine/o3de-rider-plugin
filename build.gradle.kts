plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.intellij.platform)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.serialization)
    id("java")
    id("jacoco")
}

group = "com.o3de"
version = "1.0.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    // IntelliJ Platform
    intellijPlatform {
        rider(libs.versions.intellij.platform.asProvider().get(), useInstaller = false)
        
        // Bundled plugins for Rider
        // bundledPlugins() - will be added as needed
    }
    
    // Kotlin dependencies
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.coroutines)
    
    // Serialization
    implementation(libs.bundles.serialization)
    
    // Utilities
    implementation(libs.bundles.utilities)
    
    // Logging
    implementation(libs.bundles.logging)
    
    // HTTP Client (for future O3DE API integration)
    implementation(libs.bundles.http.client)
    
    // Testing dependencies
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.integration.testing)
    
    // Test runtime
    testRuntimeOnly(libs.junit.platform.launcher)
}

intellijPlatform {
    buildSearchableOptions = false
    instrumentCode = false
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    
    // Enable preview features if needed
    // withSourcesJar()
    // withJavadocJar()
}

kotlin {
    jvmToolchain(17)
    
    compilerOptions {
        // Enable explicit API mode for better API design
        // explicitApi.set(org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Warning)
        
        // Enable progressive mode for new language features
        progressiveMode.set(true)
        
        // Opt-in to experimental APIs
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
        )
    }
}

tasks {

    patchPluginXml {
        sinceBuild.set("242")
        untilBuild.set("252.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
    
    // 测试任务配置
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    
    // 构建任务依赖
    // buildPlugin {
    //     dependsOn("test")
    // }
}

// Detekt 配置
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/config/detekt/detekt.yml")
    baseline = file("$projectDir/config/detekt/baseline.xml")
    
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

// 创建detekt配置文件的任务
tasks.register("createDetektConfig") {
    val configDir = layout.projectDirectory.dir("config/detekt")
    val detektConfigFile = configDir.file("detekt.yml")
    
    outputs.file(detektConfigFile)
    
    doLast {
        configDir.asFile.mkdirs()
        
        if (!detektConfigFile.asFile.exists()) {
            detektConfigFile.asFile.writeText("""
                build:
                  maxIssues: 0
                  excludeCorrectable: false
                  weights:
                    complexity: 2
                    LongParameterList: 1
                    style: 1
                    comments: 1
                
                config:
                  validation: true
                  warningsAsErrors: false
                  checkExhaustiveness: false
                
                processors:
                  active: true
                
                console-reports:
                  active: true
                
                output-reports:
                  active: true
                
                comments:
                  active: true
                  CommentOverPrivateFunction:
                    active: false
                  CommentOverPrivateProperty:
                    active: false
                  EndOfSentenceFormat:
                    active: false
                  UndocumentedPublicClass:
                    active: false
                  UndocumentedPublicFunction:
                    active: false
                
                complexity:
                  active: true
                  ComplexCondition:
                    threshold: 4
                  ComplexInterface:
                    threshold: 10
                  ComplexMethod:
                    threshold: 15
                  LargeClass:
                    threshold: 600
                  LongMethod:
                    threshold: 60
                  LongParameterList:
                    functionThreshold: 6
                    constructorThreshold: 7
                  NestedBlockDepth:
                    threshold: 4
                  StringLiteralDuplication:
                    threshold: 3
                  TooManyFunctions:
                    thresholdInFiles: 11
                    thresholdInClasses: 11
                    thresholdInInterfaces: 11
                    thresholdInObjects: 11
                    thresholdInEnums: 11
                    ignoreDeprecated: false
                    ignorePrivate: false
                    ignoreOverridden: false
            """.trimIndent())
        }
    }
}

// 确保detekt任务在配置文件创建后运行
tasks.named("detekt") {
    dependsOn("createDetektConfig")
}