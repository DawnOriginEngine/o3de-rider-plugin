package com.o3de.plugin.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.*
import com.intellij.ui.layout.panel
import com.intellij.util.ui.JBUI
import com.o3de.plugin.services.O3DEConfigService
import com.o3de.plugin.services.O3DECommandService
import com.o3de.plugin.ui.dialogs.CreateGemDialog
import com.o3de.plugin.ui.dialogs.CreateComponentDialog
import com.o3de.plugin.ui.dialogs.CreateFromTemplateDialog
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import javax.swing.*

class O3DEToolWindowContent(private val project: Project) {
    
    private val configService = O3DEConfigService.getInstance(project)
    private val commandService = O3DECommandService.getInstance(project)
    
    private val mainPanel = JPanel(BorderLayout())
    
    init {
        createContent()
    }
    
    fun getContent(): JComponent = mainPanel
    
    private fun createContent() {
        val scrollPane = JBScrollPane(createMainPanel())
        scrollPane.border = JBUI.Borders.empty()
        mainPanel.add(scrollPane, BorderLayout.CENTER)
    }
    
    private fun createMainPanel(): JPanel {
        return panel {
            // Configuration Section
            titledRow("Configuration") {
                row("Engine Path:") {
                    val enginePathField = JBTextField(configService.getEnginePath())
                    enginePathField(growX)
                    button("Browse") {
                        selectEnginePath(enginePathField)
                    }
                }
                
                row("Project Path:") {
                    val projectPathField = JBTextField(configService.getProjectPath())
                    projectPathField(growX)
                    button("Browse") {
                        selectProjectPath(projectPathField)
                    }
                }
                
                row("Default Gems Path:") {
                    val gemsPathField = JBTextField(configService.getDefaultGemsPath())
                    gemsPathField(growX)
                    button("Browse") {
                        selectGemsPath(gemsPathField)
                    }
                }
            }
            
            // Status Section
            titledRow("Status") {
                row {
                    val statusLabel = JBLabel(getStatusText())
                    statusLabel()
                    button("Refresh") {
                        refreshStatus(statusLabel)
                    }
                }
            }
            
            // Quick Actions Section
            titledRow("Quick Actions") {
                row {
                    button("Create Gem") {
                        showCreateGemDialog()
                    }
                    button("Create Component") {
                        showCreateComponentDialog()
                    }
                }
                
                row {
                    button("Create from Template") {
                        showCreateFromTemplateDialog()
                    }
                    button("Register Engine") {
                        registerEngine()
                    }
                }
            }
            
            // Gem Management Section
            titledRow("Gem Management") {
                row {
                    button("Enable Gem") {
                        showEnableGemDialog()
                    }
                    button("Disable Gem") {
                        showDisableGemDialog()
                    }
                }
                
                row {
                    button("List Registered Gems") {
                        listRegisteredGems()
                    }
                }
            }
            
            // Templates Section
            titledRow("Available Templates") {
                row {
                    val templatesList = createTemplatesList()
                    JBScrollPane(templatesList)(growX, growY)
                }
            }
        }
    }
    
    private fun getStatusText(): String {
        val engineValid = configService.isEnginePathValid()
        val projectValid = configService.isProjectPathValid()
        
        return buildString {
            append("Engine: ")
            append(if (engineValid) "✓ Valid" else "✗ Invalid")
            append(" | Project: ")
            append(if (projectValid) "✓ Valid" else "✗ Invalid")
        }
    }
    
    private fun refreshStatus(statusLabel: JBLabel) {
        statusLabel.text = getStatusText()
    }
    
    private fun createTemplatesList(): JList<String> {
        val templates = configService.getAvailableTemplates()
        val listModel = DefaultListModel<String>()
        templates.forEach { listModel.addElement(it) }
        
        return JBList(listModel).apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
        }
    }
    
    private fun selectEnginePath(field: JBTextField) {
        val fileChooser = JFileChooser()
        fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        fileChooser.dialogTitle = "Select O3DE Engine Directory"
        
        if (fileChooser.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            val selectedPath = fileChooser.selectedFile.absolutePath
            field.text = selectedPath
            configService.setEnginePath(selectedPath)
        }
    }
    
    private fun selectProjectPath(field: JBTextField) {
        val fileChooser = JFileChooser()
        fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        fileChooser.dialogTitle = "Select O3DE Project Directory"
        
        if (fileChooser.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            val selectedPath = fileChooser.selectedFile.absolutePath
            field.text = selectedPath
            configService.setProjectPath(selectedPath)
        }
    }
    
    private fun selectGemsPath(field: JBTextField) {
        val fileChooser = JFileChooser()
        fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        fileChooser.dialogTitle = "Select Default Gems Directory"
        
        if (fileChooser.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            val selectedPath = fileChooser.selectedFile.absolutePath
            field.text = selectedPath
            configService.setDefaultGemsPath(selectedPath)
        }
    }
    
    private fun showCreateGemDialog() {
        if (!configService.isEnginePathValid()) {
            Messages.showErrorDialog(project, "Please configure a valid O3DE engine path first.", "Configuration Error")
            return
        }
        
        val dialog = CreateGemDialog(project)
        if (dialog.showAndGet()) {
            // Dialog handles the gem creation
        }
    }
    
    private fun showCreateComponentDialog() {
        if (!configService.isEnginePathValid()) {
            Messages.showErrorDialog(project, "Please configure a valid O3DE engine path first.", "Configuration Error")
            return
        }
        
        val dialog = CreateComponentDialog(project)
        if (dialog.showAndGet()) {
            // Dialog handles the component creation
        }
    }
    
    private fun showCreateFromTemplateDialog() {
        if (!configService.isEnginePathValid()) {
            Messages.showErrorDialog(project, "Please configure a valid O3DE engine path first.", "Configuration Error")
            return
        }
        
        val dialog = CreateFromTemplateDialog(project)
        if (dialog.showAndGet()) {
            // Dialog handles the template creation
        }
    }
    
    private fun registerEngine() {
        if (!configService.isEnginePathValid()) {
            Messages.showErrorDialog(project, "Please configure a valid O3DE engine path first.", "Configuration Error")
            return
        }
        
        commandService.registerEngine { success, output ->
            SwingUtilities.invokeLater {
                if (success) {
                    Messages.showInfoMessage(project, "Engine registered successfully.", "Success")
                } else {
                    Messages.showErrorDialog(project, "Failed to register engine:\n$output", "Error")
                }
            }
        }
    }
    
    private fun showEnableGemDialog() {
        val gemName = Messages.showInputDialog(
            project,
            "Enter gem name to enable:",
            "Enable Gem",
            Messages.getQuestionIcon()
        )
        
        if (!gemName.isNullOrBlank()) {
            commandService.enableGem(gemName) { success, output ->
                SwingUtilities.invokeLater {
                    if (success) {
                        Messages.showInfoMessage(project, "Gem '$gemName' enabled successfully.", "Success")
                    } else {
                        Messages.showErrorDialog(project, "Failed to enable gem:\n$output", "Error")
                    }
                }
            }
        }
    }
    
    private fun showDisableGemDialog() {
        val gemName = Messages.showInputDialog(
            project,
            "Enter gem name to disable:",
            "Disable Gem",
            Messages.getQuestionIcon()
        )
        
        if (!gemName.isNullOrBlank()) {
            commandService.disableGem(gemName) { success, output ->
                SwingUtilities.invokeLater {
                    if (success) {
                        Messages.showInfoMessage(project, "Gem '$gemName' disabled successfully.", "Success")
                    } else {
                        Messages.showErrorDialog(project, "Failed to disable gem:\n$output", "Error")
                    }
                }
            }
        }
    }
    
    private fun listRegisteredGems() {
        commandService.getRegisteredGems { gems ->
            SwingUtilities.invokeLater {
                val gemsList = if (gems.isNotEmpty()) {
                    gems.joinToString("\n")
                } else {
                    "No registered gems found."
                }
                
                Messages.showInfoMessage(project, gemsList, "Registered Gems")
            }
        }
    }
}