package com.o3de.testutil

import com.intellij.openapi.project.Project
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.io.path.createFile
import kotlin.io.path.writeText

/**
 * Utility functions and helpers for testing O3DE Rider Plugin
 * Provides common test setup, assertions, and mock data creation
 */
object TestUtils {
    
    /**
     * Creates a temporary O3DE installation structure for testing
     */
    fun createMockO3DEInstallation(basePath: Path): MockO3DEInstallation {
        val o3dePath = basePath.resolve("o3de")
        Files.createDirectories(o3dePath)
        
        // Create O3DE executable files
        val windowsExe = o3dePath.resolve("o3de.exe")
        val unixExe = o3dePath.resolve("o3de")
        windowsExe.createFile()
        unixExe.createFile()
        
        // Create bin directory
        val binPath = o3dePath.resolve("bin")
        Files.createDirectories(binPath)
        
        // Create scripts directory
        val scriptsPath = o3dePath.resolve("scripts")
        Files.createDirectories(scriptsPath)
        
        // Create engine.json
        val engineJson = o3dePath.resolve("engine.json")
        engineJson.writeText("""
            {
                "engine_name": "o3de",
                "version": "23.10.0",
                "O3DE_VERSION_MAJOR": 23,
                "O3DE_VERSION_MINOR": 10,
                "O3DE_VERSION_PATCH": 0
            }
        """.trimIndent())
        
        return MockO3DEInstallation(
            rootPath = o3dePath,
            executablePath = windowsExe,
            binPath = binPath,
            scriptsPath = scriptsPath,
            engineJsonPath = engineJson
        )
    }
    
    /**
     * Creates a mock O3DE project structure for testing
     */
    fun createMockO3DEProject(
        basePath: Path, 
        projectName: String,
        enginePath: String? = null
    ): MockO3DEProject {
        val projectPath = basePath.resolve(projectName)
        Files.createDirectories(projectPath)
        
        // Create project.json
        val projectJson = projectPath.resolve("project.json")
        val projectContent = buildString {
            appendLine("{")
            appendLine("  \"project_name\": \"$projectName\",")
            appendLine("  \"version\": \"1.0.0\",")
            appendLine("  \"engine\": \"o3de\",")
            if (enginePath != null) {
                appendLine("  \"engine_path\": \"$enginePath\",")
            }
            appendLine("  \"project_id\": \"{12345678-1234-1234-1234-123456789012}\"")
            appendLine("}")
        }
        projectJson.writeText(projectContent)
        
        // Create CMakeLists.txt
        val cmakeFile = projectPath.resolve("CMakeLists.txt")
        cmakeFile.writeText("""
            cmake_minimum_required(VERSION 3.22)
            project($projectName)
            
            find_package(o3de REQUIRED)
            o3de_initialize()
        """.trimIndent())
        
        // Create Code directory
        val codePath = projectPath.resolve("Code")
        Files.createDirectories(codePath)
        
        // Create Gem directory
        val gemPath = projectPath.resolve("Gem")
        Files.createDirectories(gemPath)
        
        return MockO3DEProject(
            rootPath = projectPath,
            projectJsonPath = projectJson,
            cmakeListsPath = cmakeFile,
            codePath = codePath,
            gemPath = gemPath,
            projectName = projectName
        )
    }
    
    /**
     * Creates a mock gem structure for testing
     */
    fun createMockGem(
        basePath: Path,
        gemName: String,
        version: String = "1.0.0"
    ): MockGem {
        val gemPath = basePath.resolve(gemName)
        Files.createDirectories(gemPath)
        
        // Create gem.json
        val gemJson = gemPath.resolve("gem.json")
        val gemContent = """
            {
                "gem_name": "$gemName",
                "version": "$version",
                "display_name": "$gemName",
                "license": "Apache-2.0 OR MIT",
                "origin": "Open 3D Engine - o3de.org",
                "type": "Code",
                "summary": "Test gem for $gemName",
                "canonical_tags": [
                    "Gem"
                ],
                "user_tags": [
                    "Test"
                ],
                "icon_path": "preview.png",
                "requirements": "",
                "documentation_url": "",
                "dependencies": []
            }
        """.trimIndent()
        gemJson.writeText(gemContent)
        
        // Create Code directory
        val codePath = gemPath.resolve("Code")
        Files.createDirectories(codePath)
        
        // Create Include directory
        val includePath = codePath.resolve("Include")
        Files.createDirectories(includePath)
        
        // Create Source directory
        val sourcePath = codePath.resolve("Source")
        Files.createDirectories(sourcePath)
        
        return MockGem(
            rootPath = gemPath,
            gemJsonPath = gemJson,
            codePath = codePath,
            includePath = includePath,
            sourcePath = sourcePath,
            gemName = gemName,
            version = version
        )
    }
    
    /**
     * Creates a mock O3DE registry for testing
     */
    fun createMockO3DERegistry(
        basePath: Path,
        engines: List<MockEngineInfo> = emptyList(),
        projects: List<MockProjectInfo> = emptyList(),
        gems: List<MockGemInfo> = emptyList()
    ): Path {
        val registryPath = basePath.resolve(".o3de")
        Files.createDirectories(registryPath)
        
        val registryFile = registryPath.resolve("registry.json")
        val registryContent = buildString {
            appendLine("{")
            
            // Engines section
            appendLine("  \"engines\": [")
            engines.forEachIndexed { index, engine ->
                appendLine("    {")
                appendLine("      \"name\": \"${engine.name}\",")
                appendLine("      \"path\": \"${engine.path}\",")
                appendLine("      \"version\": \"${engine.version}\"")
                append("    }")
                if (index < engines.size - 1) appendLine(",")
                else appendLine()
            }
            appendLine("  ],")
            
            // Projects section
            appendLine("  \"projects\": [")
            projects.forEachIndexed { index, project ->
                appendLine("    {")
                appendLine("      \"name\": \"${project.name}\",")
                appendLine("      \"path\": \"${project.path}\"")
                append("    }")
                if (index < projects.size - 1) appendLine(",")
                else appendLine()
            }
            appendLine("  ],")
            
            // Gems section
            appendLine("  \"gems\": [")
            gems.forEachIndexed { index, gem ->
                appendLine("    {")
                appendLine("      \"name\": \"${gem.name}\",")
                appendLine("      \"path\": \"${gem.path}\",")
                appendLine("      \"version\": \"${gem.version}\"")
                append("    }")
                if (index < gems.size - 1) appendLine(",")
                else appendLine()
            }
            appendLine("  ]")
            
            appendLine("}")
        }
        
        registryFile.writeText(registryContent)
        return registryFile
    }
    
    /**
     * Performance testing utilities
     */
    object Performance {
        
        /**
         * Measures execution time of a block of code
         */
        inline fun <T> measureTime(block: () -> T): Pair<T, Duration> {
            val startTime = System.nanoTime()
            val result = block()
            val endTime = System.nanoTime()
            val duration = Duration.ofNanos(endTime - startTime)
            return Pair(result, duration)
        }
        
        /**
         * Asserts that a block of code executes within a specified time limit
         */
        inline fun <T> assertExecutionTime(
            maxDuration: Duration,
            message: String = "Execution took too long",
            block: () -> T
        ): T {
            val (result, actualDuration) = measureTime(block)
            assertTrue(
                actualDuration <= maxDuration,
                "$message. Expected: <= $maxDuration, Actual: $actualDuration"
            )
            return result
        }
        
        /**
         * Runs a performance benchmark multiple times and returns statistics
         */
        inline fun benchmark(
            iterations: Int = 10,
            warmupIterations: Int = 3,
            block: () -> Unit
        ): BenchmarkResult {
            // Warmup
            repeat(warmupIterations) { block() }
            
            // Actual measurements
            val durations = mutableListOf<Duration>()
            repeat(iterations) {
                val (_, duration) = measureTime(block)
                durations.add(duration)
            }
            
            return BenchmarkResult(
                iterations = iterations,
                durations = durations,
                averageDuration = Duration.ofNanos(durations.map { it.toNanos() }.average().toLong()),
                minDuration = durations.minOrNull() ?: Duration.ZERO,
                maxDuration = durations.maxOrNull() ?: Duration.ZERO
            )
        }
    }
    
    /**
     * File system testing utilities
     */
    object FileSystem {
        
        /**
         * Creates a temporary file with specified content
         */
        fun createTempFileWithContent(content: String, suffix: String = ".tmp"): File {
            val tempFile = File.createTempFile("o3de-test", suffix)
            tempFile.writeText(content)
            tempFile.deleteOnExit()
            return tempFile
        }
        
        /**
         * Creates a directory structure from a map
         */
        fun createDirectoryStructure(basePath: Path, structure: Map<String, Any>) {
            structure.forEach { (name, content) ->
                val path = basePath.resolve(name)
                when (content) {
                    is String -> {
                        // It's a file
                        path.writeText(content)
                    }
                    is Map<*, *> -> {
                        // It's a directory
                        Files.createDirectories(path)
                        @Suppress("UNCHECKED_CAST")
                        createDirectoryStructure(path, content as Map<String, Any>)
                    }
                }
            }
        }
    }
    
    /**
     * Assertion utilities for O3DE-specific testing
     */
    object Assertions {
        
        /**
         * Asserts that a path represents a valid O3DE installation
         */
        fun assertValidO3DEInstallation(path: Path, message: String = "Invalid O3DE installation") {
            assertTrue(Files.exists(path), "$message: Path does not exist")
            assertTrue(Files.isDirectory(path), "$message: Path is not a directory")
            
            val hasWindowsExe = Files.exists(path.resolve("o3de.exe"))
            val hasUnixExe = Files.exists(path.resolve("o3de"))
            assertTrue(
                hasWindowsExe || hasUnixExe,
                "$message: No O3DE executable found"
            )
        }
        
        /**
         * Asserts that a path represents a valid O3DE project
         */
        fun assertValidO3DEProject(path: Path, message: String = "Invalid O3DE project") {
            assertTrue(Files.exists(path), "$message: Path does not exist")
            assertTrue(Files.isDirectory(path), "$message: Path is not a directory")
            assertTrue(
                Files.exists(path.resolve("project.json")),
                "$message: project.json not found"
            )
        }
        
        /**
         * Asserts that a path represents a valid O3DE gem
         */
        fun assertValidO3DEGem(path: Path, message: String = "Invalid O3DE gem") {
            assertTrue(Files.exists(path), "$message: Path does not exist")
            assertTrue(Files.isDirectory(path), "$message: Path is not a directory")
            assertTrue(
                Files.exists(path.resolve("gem.json")),
                "$message: gem.json not found"
            )
        }
    }
}

// Data classes for mock objects

data class MockO3DEInstallation(
    val rootPath: Path,
    val executablePath: Path,
    val binPath: Path,
    val scriptsPath: Path,
    val engineJsonPath: Path
)

data class MockO3DEProject(
    val rootPath: Path,
    val projectJsonPath: Path,
    val cmakeListsPath: Path,
    val codePath: Path,
    val gemPath: Path,
    val projectName: String
)

data class MockGem(
    val rootPath: Path,
    val gemJsonPath: Path,
    val codePath: Path,
    val includePath: Path,
    val sourcePath: Path,
    val gemName: String,
    val version: String
)

data class MockEngineInfo(
    val name: String,
    val path: String,
    val version: String
)

data class MockProjectInfo(
    val name: String,
    val path: String
)

data class MockGemInfo(
    val name: String,
    val path: String,
    val version: String
)

data class BenchmarkResult(
    val iterations: Int,
    val durations: List<Duration>,
    val averageDuration: Duration,
    val minDuration: Duration,
    val maxDuration: Duration
) {
    override fun toString(): String {
        return "BenchmarkResult(iterations=$iterations, avg=${averageDuration.toMillis()}ms, " +
                "min=${minDuration.toMillis()}ms, max=${maxDuration.toMillis()}ms)"
    }
}