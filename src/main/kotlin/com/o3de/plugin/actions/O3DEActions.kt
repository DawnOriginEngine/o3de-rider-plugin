package com.o3de.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.o3de.plugin.ui.dialogs.CreateGemDialog
import com.o3de.plugin.ui.dialogs.CreateComponentDialog
import com.o3de.plugin.ui.dialogs.CreateFromTemplateDialog
import com.o3de.plugin.ui.dialogs.GemManagementDialog
import com.o3de.plugin.services.O3DEConfigService
import com.o3de.plugin.services.O3DECommandService
import com.intellij.openapi.ui.Messages
import javax.swing.SwingUtilities

/**
 * Action to create a new O3DE Gem
 */
class CreateGemAction : AnAction("Create Gem", "Create a new O3DE Gem", null) {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        val dialog = CreateGemDialog(project)
        dialog.show()
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

/**
 * Action to create a new O3DE Component
 */
class CreateComponentAction : AnAction("Create Component", "Create a new O3DE Component", null) {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        val dialog = CreateComponentDialog(project)
        dialog.show()
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

/**
 * Action to create from O3DE template
 */
class CreateFromTemplateAction : AnAction("Create from Template", "Create from O3DE Template", null) {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        val dialog = CreateFromTemplateDialog(project)
        dialog.show()
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

/**
 * Action to manage gems (enable/disable)
 */
class ManageGemsAction : AnAction("Manage Gems", "Enable or disable O3DE Gems", null) {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        val dialog = GemManagementDialog(project)
        dialog.show()
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

/**
 * Action to register O3DE engine
 */
class RegisterEngineAction : AnAction("Register Engine", "Register O3DE Engine", null) {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val configService = O3DEConfigService.getInstance(project)
        val commandService = O3DECommandService.getInstance(project)
        
        val enginePath = configService.getEnginePath()
        if (enginePath.isEmpty()) {
            Messages.showErrorDialog(
                project,
                "Please configure the O3DE engine path in the tool window first.",
                "Engine Path Required"
            )
            return
        }
        
        commandService.registerEngine(enginePath) { success, output ->
            SwingUtilities.invokeLater {
                if (success) {
                    Messages.showInfoMessage(
                        project,
                        "O3DE engine registered successfully at:\n$enginePath",
                        "Engine Registered"
                    )
                } else {
                    Messages.showErrorDialog(
                        project,
                        "Failed to register O3DE engine:\n$output",
                        "Registration Error"
                    )
                }
            }
        }
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

/**
 * Action to show registered engines and projects
 */
class ShowRegisteredAction : AnAction("Show Registered", "Show registered O3DE engines and projects", null) {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val commandService = O3DECommandService.getInstance(project)
        
        commandService.getRegisteredGems { success, output ->
            SwingUtilities.invokeLater {
                if (success) {
                    Messages.showInfoMessage(
                        project,
                        "Registered O3DE Items:\n\n$output",
                        "Registered Items"
                    )
                } else {
                    Messages.showErrorDialog(
                        project,
                        "Failed to get registered items:\n$output",
                        "Query Error"
                    )
                }
            }
        }
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

/**
 * Action to build the current O3DE project
 */
class BuildProjectAction : AnAction("Build Project", "Build the current O3DE project", null) {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val configService = O3DEConfigService.getInstance(project)
        
        val projectPath = project.basePath
        if (projectPath == null) {
            Messages.showErrorDialog(
                project,
                "Unable to determine project path.",
                "Build Error"
            )
            return
        }
        
        // Check if this is an O3DE project
        val projectJsonFile = java.io.File(projectPath, "project.json")
        if (!projectJsonFile.exists()) {
            Messages.showErrorDialog(
                project,
                "This doesn't appear to be an O3DE project (no project.json found).",
                "Build Error"
            )
            return
        }
        
        // For now, show a message about building
        // In a real implementation, this would trigger the actual build process
        Messages.showInfoMessage(
            project,
            """To build your O3DE project:
                
                1. Open a terminal in your project directory
                2. Run: cmake -B build -S . -G "Visual Studio 16 2019" -A x64
                3. Run: cmake --build build --config profile --parallel
                
                Or use the O3DE Project Manager to build the project.
            """.trimIndent(),
            "Build Instructions"
        )
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

/**
 * Action to open O3DE Project Manager
 */
class OpenProjectManagerAction : AnAction("Open Project Manager", "Open O3DE Project Manager", null) {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val configService = O3DEConfigService.getInstance(project)
        
        val enginePath = configService.getEnginePath()
        if (enginePath.isEmpty()) {
            Messages.showErrorDialog(
                project,
                "Please configure the O3DE engine path first.",
                "Engine Path Required"
            )
            return
        }
        
        try {
            val projectManagerPath = java.io.File(enginePath, "bin/Windows/profile/Default/o3de.exe")
            if (projectManagerPath.exists()) {
                ProcessBuilder(projectManagerPath.absolutePath)
                    .directory(java.io.File(enginePath))
                    .start()
                    
                Messages.showInfoMessage(
                    project,
                    "O3DE Project Manager launched.",
                    "Project Manager"
                )
            } else {
                Messages.showErrorDialog(
                    project,
                    "O3DE Project Manager not found at expected location:\n${projectManagerPath.absolutePath}",
                    "Project Manager Not Found"
                )
            }
        } catch (e: Exception) {
            Messages.showErrorDialog(
                project,
                "Failed to launch O3DE Project Manager:\n${e.message}",
                "Launch Error"
            )
        }
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}