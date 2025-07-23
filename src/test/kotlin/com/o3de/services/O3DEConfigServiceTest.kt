package com.o3de.services

import com.o3de.plugin.services.O3DEConfigService
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.createFile
import kotlin.io.path.writeText

/**
 * Test class for O3DEConfigService
 * Demonstrates comprehensive testing patterns for the O3DE Rider Plugin
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("O3DE Configuration Service Tests")
class O3DEConfigServiceTest {

    private lateinit var configService: O3DEConfigService
    
    @TempDir
    lateinit var tempDir: Path
    
    @BeforeAll
    fun setUpAll() {
        // Global setup for all tests
    }
    
    @BeforeEach
    fun setUp() {
        // Create a new instance for testing
        configService = O3DEConfigService()
        // Initialize with empty state
        configService.loadState(O3DEConfigService.State())
    }
    
    @AfterEach
    fun tearDown() {
        // Cleanup after each test
    }
    
    @Nested
    @DisplayName("O3DE Installation Detection")
    inner class InstallationDetectionTests {
        
        @Test
        @DisplayName("Should detect valid O3DE installation")
        fun `should detect valid O3DE installation when all required files present`() {
            // Given
            val o3dePath = createValidO3DEInstallation()
            
            // When
            configService.setEnginePath(o3dePath.toString())
            val isValid = configService.isEnginePathValid()
            
            // Then
            assertTrue(isValid, "Should detect valid O3DE installation")
        }
        
        @Test
        @DisplayName("Should reject invalid O3DE installation")
        fun `should reject invalid O3DE installation when executable missing`() {
            // Given
            val invalidPath = tempDir.resolve("invalid-o3de")
            Files.createDirectories(invalidPath)
            
            // When
            configService.setEnginePath(invalidPath.toString())
            val isValid = configService.isEnginePathValid()
            
            // Then
            assertFalse(isValid, "Should reject invalid O3DE installation")
        }
        
        @Test
        @DisplayName("Should handle non-existent path gracefully")
        fun `should handle non-existent path gracefully`() {
            // Given
            val nonExistentPath = "/path/that/does/not/exist"
            
            // When & Then
            assertDoesNotThrow {
                configService.setEnginePath(nonExistentPath)
                val isValid = configService.isEnginePathValid()
                assertFalse(isValid, "Should return false for non-existent path")
            }
        }
        
        @ParameterizedTest
        @DisplayName("Should handle various invalid path formats")
        @ValueSource(strings = ["", " ", "\t", "\n", "null"])
        fun `should handle invalid path formats`(invalidPath: String) {
            // When
            configService.setEnginePath(invalidPath)
            val isValid = configService.isEnginePathValid()
            
            // Then
            assertFalse(isValid, "Should handle invalid path format: '$invalidPath'")
        }
    }
    
    @Nested
    @DisplayName("Template Management")
    inner class TemplateManagementTests {
        
        @Test
        @DisplayName("Should get available templates when engine path is valid")
        fun `should get available templates when engine path is valid`() {
            // Given
            val enginePath = createValidO3DEInstallation()
            configService.setEnginePath(enginePath.toString())
            
            // When
            val templates = configService.getAvailableTemplates()
            
            // Then
            assertNotNull(templates, "Should return template list")
        }
        
        @Test
        @DisplayName("Should return empty list when engine path is invalid")
        fun `should return empty list when engine path is invalid`() {
            // Given
            configService.setEnginePath("/invalid/path")
            
            // When
            val templates = configService.getAvailableTemplates()
            
            // Then
            assertTrue(templates.isEmpty(), "Should return empty list for invalid engine path")
        }
        
        @Test
        @DisplayName("Should filter gem templates correctly")
        fun `should filter gem templates correctly`() {
            // Given
            val enginePath = createValidO3DEInstallation()
            configService.setEnginePath(enginePath.toString())
            
            // When
            val gemTemplates = configService.getGemTemplates()
            
            // Then
            assertNotNull(gemTemplates, "Should return gem template list")
        }
    }
    
    @Nested
    @DisplayName("Project Configuration")
    inner class ProjectConfigurationTests {
        
        @Test
        @DisplayName("Should detect O3DE project correctly")
        fun `should detect O3DE project when project json exists`() {
            // Given
            val projectPath = createO3DEProject("TestProject")
            
            // When
            configService.setProjectPath(projectPath.toString())
            val isO3DEProject = configService.isProjectPathValid()
            
            // Then
            assertTrue(isO3DEProject, "Should detect valid O3DE project")
        }
        
        @Test
        @DisplayName("Should reject non-O3DE project")
        fun `should reject non-O3DE project when project json missing`() {
            // Given
            val regularPath = tempDir.resolve("regular-project")
            Files.createDirectories(regularPath)
            
            // When
            configService.setProjectPath(regularPath.toString())
            val isO3DEProject = configService.isProjectPathValid()
            
            // Then
            assertFalse(isO3DEProject, "Should reject non-O3DE project")
        }
    }
    
    @Nested
    @DisplayName("Performance Tests")
    inner class PerformanceTests {
        
        @Test
        @DisplayName("Should handle template detection performance")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        fun `should handle template detection performance`() {
            // Given
            val enginePath = createValidO3DEInstallation()
            configService.setEnginePath(enginePath.toString())
            
            // When - First call
            val startTime = System.currentTimeMillis()
            val firstCall = configService.getAvailableTemplates()
            val firstCallTime = System.currentTimeMillis() - startTime
            
            // When - Second call
            val secondStartTime = System.currentTimeMillis()
            val secondCall = configService.getAvailableTemplates()
            val secondCallTime = System.currentTimeMillis() - secondStartTime
            
            // Then
            assertEquals(firstCall.size, secondCall.size, "Both calls should return same number of templates")
            assertTrue(firstCallTime < 1000, "Template detection should be reasonably fast")
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    inner class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle file system errors gracefully")
        fun `should handle file system errors gracefully`() {
            // Given - A path that will cause I/O errors
            val problematicPath = "/dev/null/invalid/path"
            
            // When & Then
            assertDoesNotThrow {
                configService.setEnginePath(problematicPath)
                val result = configService.isEnginePathValid()
                assertFalse(result, "Should handle I/O errors gracefully")
            }
        }
        
        @Test
        @DisplayName("Should handle permission denied errors")
        fun `should handle permission denied errors`() {
            // Given
            val restrictedPath = createRestrictedDirectory()
            
            // When & Then
            assertDoesNotThrow {
                configService.setEnginePath(restrictedPath.toString())
                val result = configService.isEnginePathValid()
                // Result may vary based on system, but should not throw
            }
        }
    }
    
    // Helper methods for test setup
    
    private fun createValidO3DEInstallation(): Path {
        val o3dePath = tempDir.resolve("o3de")
        Files.createDirectories(o3dePath)
        
        // Create required O3DE files
        o3dePath.resolve("o3de.exe").createFile()
        o3dePath.resolve("o3de").createFile() // Unix executable
        
        val binPath = o3dePath.resolve("bin")
        Files.createDirectories(binPath)
        
        // Create Templates directory for template tests
        val templatesPath = o3dePath.resolve("Templates")
        Files.createDirectories(templatesPath)
        
        // Create some sample template directories
        Files.createDirectories(templatesPath.resolve("DefaultGem"))
        Files.createDirectories(templatesPath.resolve("DefaultComponent"))
        
        return o3dePath
    }
    

    
    private fun createO3DEProject(projectName: String): Path {
        val projectPath = tempDir.resolve(projectName)
        Files.createDirectories(projectPath)
        
        val projectFile = projectPath.resolve("project.json")
        val projectContent = """
            {
                "project_name": "$projectName",
                "version": "1.0.0",
                "engine": "o3de"
            }
        """.trimIndent()
        
        projectFile.writeText(projectContent)
        return projectPath
    }
    

    
    private fun createRestrictedDirectory(): Path {
        val restrictedPath = tempDir.resolve("restricted")
        Files.createDirectories(restrictedPath)
        
        // Attempt to make directory read-only (may not work on all systems)
        try {
            restrictedPath.toFile().setReadOnly()
        } catch (e: Exception) {
            // Ignore if we can't set permissions
        }
        
        return restrictedPath
    }
}