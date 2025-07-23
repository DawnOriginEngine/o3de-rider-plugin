package com.o3de.plugin.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.components.service
import com.intellij.openapi.wm.ToolWindowManager
import java.io.File

@Service(Service.Level.PROJECT)
class O3DECommandService(private val project: Project) {
    
    private val configService = O3DEConfigService.getInstance(project)
    
    companion object {
        fun getInstance(project: Project): O3DECommandService {
            return project.service<O3DECommandService>()
        }
    }
    
    /**
     * Execute O3DE create-gem command
     */
    fun createGem(
        gemName: String,
        gemPath: String,
        templateName: String = "DefaultGem",
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val command = buildO3DECommand("create-gem")
            .withParameters("--gem-name", gemName)
            .withParameters("--gem-path", gemPath)
            .withParameters("--template-name", templateName)
        
        executeCommand(command, "Creating Gem: $gemName", onComplete)
    }
    
    /**
     * Execute O3DE create-from-template command for components
     * Uses create-from-template command to create components according to O3DE CLI reference documentation
     */
    fun createComponent(
        componentName: String,
        destinationPath: String,
        gemName: String,
        templateName: String = "DefaultComponent",
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val command = buildO3DECommand("create-from-template")
            .withParameters("--destination-path", destinationPath)
            .withParameters("--destination-name", componentName)
            .withParameters("--template-name", templateName)
            .withParameters("--keep-restricted-in-instance")
            .withParameters("--replace", "'\${GemName}'", gemName)
            .withParameters("--force")
                
        executeCommand(command, "Creating Component: $componentName", onComplete)
    }
    
    /**
     * Execute O3DE create-from-template command
     * Implementation of create-from-template command according to O3DE CLI reference documentation
     */
    fun createFromTemplate(
        templateName: String,
        destinationPath: String,
        destinationName: String,
        replacements: Map<String, String> = emptyMap(),
        keepRestrictedFiles: Boolean = true,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val command = buildO3DECommand("create-from-template")
            .withParameters("--template-name", templateName)
            .withParameters("--destination-path", destinationPath)
            .withParameters("--destination-name", destinationName)
            .withParameters("--force")

        
        // Add template variable replacement parameters
        replacements.forEach { (key, value) ->
            command.withParameters("--replace", key, value)
        }
        
        executeCommand(command, "Creating from template: $templateName", onComplete)
    }
    
    /**
     * Execute O3DE enable-gem command
     */
    fun enableGem(
        gemName: String,
        projectPath: String = configService.getProjectPath(),
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val command = buildO3DECommand("enable-gem")
            .withParameters("--gem-name", gemName)
            .withParameters("--project-path", projectPath)
        
        executeCommand(command, "Enabling Gem: $gemName", onComplete)
    }
    
    /**
     * Execute O3DE disable-gem command
     */
    fun disableGem(
        gemName: String,
        projectPath: String = configService.getProjectPath(),
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val command = buildO3DECommand("disable-gem")
            .withParameters("--gem-name", gemName)
            .withParameters("--project-path", projectPath)
        
        executeCommand(command, "Disabling Gem: $gemName", onComplete)
    }
    
    /**
     * Execute O3DE register command
     */
    fun registerEngine(
        enginePath: String = configService.getEnginePath(),
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val command = buildO3DECommand("register")
            .withParameters("--this-engine")
        
        executeCommand(command, "Registering Engine", onComplete)
    }
    
    /**
     * Execute O3DE register command for gems
     */
    fun registerGem(
        gemPath: String,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val command = buildO3DECommand("register")
            .withParameters("--gem-path", gemPath)
        
        executeCommand(command, "Registering Gem", onComplete)
    }
    
    /**
     * Get registered gems
     */
    fun getRegisteredGems(onComplete: (List<String>) -> Unit) {
        val command = buildO3DECommand("register-show")
        
        executeCommand(command, "Getting registered gems") { success, output ->
            if (success) {
                val gems = parseRegisteredGems(output)
                onComplete(gems)
            } else {
                onComplete(emptyList())
            }
        }
    }
    
    private fun parseRegisteredGems(output: String): List<String> {
        // Parse the output to extract gem names
        // This is a simplified parser - you might need to adjust based on actual output format
        return output.lines()
            .filter { it.contains("gem", ignoreCase = true) }
            .mapNotNull { line ->
                // Extract gem name from the line
                val parts = line.split(":")
                if (parts.size >= 2) parts[1].trim() else null
            }
    }
    
    private fun buildO3DECommand(subcommand: String): GeneralCommandLine {
        val scriptPath = configService.getO3DEScriptPath()
        if (scriptPath.isEmpty()) {
            throw IllegalStateException("O3DE engine path not configured or invalid")
        }
        
        return GeneralCommandLine(scriptPath, subcommand)
            .withWorkDirectory(project.basePath)
    }
    
    private fun executeCommand(
        command: GeneralCommandLine,
        description: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        try {
            val processHandler = ProcessHandlerFactory.getInstance().createProcessHandler(command)
            val output = StringBuilder()
            
            processHandler.addProcessListener(object : com.intellij.execution.process.ProcessAdapter() {
                override fun onTextAvailable(event: com.intellij.execution.process.ProcessEvent, outputType: com.intellij.openapi.util.Key<*>) {
                    output.append(event.text)
                }
                
                override fun processTerminated(event: com.intellij.execution.process.ProcessEvent) {
                    val success = event.exitCode == 0
                    onComplete(success, output.toString())
                }
            })
            
            ProcessTerminatedListener.attach(processHandler)
            processHandler.startNotify()
            
            // Show in terminal if available
            showInTerminal(command.commandLineString, description)
            
        } catch (e: Exception) {
            onComplete(false, "Error executing command: ${e.message}")
        }
    }
    
    private fun showInTerminal(commandLine: String, description: String) {
        try {
            val toolWindowManager = ToolWindowManager.getInstance(project)
            val terminalToolWindow = toolWindowManager.getToolWindow("Terminal")
            
            terminalToolWindow?.let { toolWindow ->
                if (!toolWindow.isVisible) {
                    toolWindow.show()
                }
                
                // Add a comment about what we're doing
                val terminalView = toolWindow.contentManager.selectedContent?.component
                // Note: Actual terminal integration would require more complex implementation
                // This is a simplified version
            }
        } catch (e: Exception) {
            // Fallback - just log the command
            println("Executing: $commandLine")
        }
    }
}