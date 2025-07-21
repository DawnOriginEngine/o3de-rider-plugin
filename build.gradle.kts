plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    id("org.jetbrains.intellij") version "1.15.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

group = "com.o3de"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}

intellij {
    version.set("2023.2")
    type.set("RD")
    plugins.set(listOf("terminal"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("241.*")
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
    buildPlugin {
        dependsOn("test")
    }
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
    doLast {
        val configDir = file("$projectDir/config/detekt")
        configDir.mkdirs()
        
        val detektConfig = file("$projectDir/config/detekt/detekt.yml")
        if (!detektConfig.exists()) {
            detektConfig.writeText("""
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