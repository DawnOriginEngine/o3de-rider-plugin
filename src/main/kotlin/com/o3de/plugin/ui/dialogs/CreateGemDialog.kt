package com.o3de.plugin.ui.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.panel
import com.o3de.plugin.services.O3DEConfigService
import com.o3de.plugin.services.O3DECommandService
import java.io.File
import javax.swing.*
import java.awt.*

class CreateGemDialog(private val project: Project) : DialogWrapper(project) {
    
    private val configService = O3DEConfigService.getInstance(project)
    private val commandService = O3DECommandService.getInstance(project)
    
    private val gemNameField = JBTextField()
    private val gemPathField = JBTextField(configService.getDefaultGemsPath())
    private val templateComboBox = JComboBox<String>()
    
    init {
        title = "Create O3DE Gem"
        init()
        loadTemplates()
    }
    
    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(java.awt.BorderLayout())
        val formPanel = JPanel(java.awt.GridBagLayout())
        val gbc = java.awt.GridBagConstraints()
        
        gbc.insets = java.awt.Insets(5, 5, 5, 5)
        gbc.anchor = java.awt.GridBagConstraints.WEST
        
        // Gem Name
        gbc.gridx = 0; gbc.gridy = 0
        formPanel.add(JLabel("Gem Name:"), gbc)
        gbc.gridx = 1; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        formPanel.add(gemNameField, gbc)
        
        // Gem Path
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = java.awt.GridBagConstraints.NONE; gbc.weightx = 0.0
        formPanel.add(JLabel("Gem Path:"), gbc)
        gbc.gridx = 1; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        formPanel.add(gemPathField, gbc)
        gbc.gridx = 2; gbc.fill = java.awt.GridBagConstraints.NONE; gbc.weightx = 0.0
        val browseButton = JButton("Browse")
        browseButton.addActionListener { selectGemPath() }
        formPanel.add(browseButton, gbc)
        
        // Template
        gbc.gridx = 0; gbc.gridy = 2
        formPanel.add(JLabel("Template:"), gbc)
        gbc.gridx = 1; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        formPanel.add(templateComboBox, gbc)
        
        // Help text
        val helpText = JLabel("""<html>
                    <b>Available Templates:</b><br>
                    • <b>DefaultGem</b> - Full-featured gem with code and assets<br>
                    • <b>AssetGem</b> - Asset-only gem (no code compilation)<br>
                    • <b>CppToolGem</b> - C++ tool for the Editor<br>
                    • <b>PythonToolGem</b> - Python tool for the Editor<br>
                    • <b>GraphicsGem</b> - Gem for Atom rendering features<br>
                    • <b>UnifiedMultiplayerGem</b> - Multiplayer system gem
                    </html>""")
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL
        formPanel.add(helpText, gbc)
        
        mainPanel.add(formPanel, java.awt.BorderLayout.CENTER)
        return mainPanel
    }
    
    override fun doValidate(): ValidationInfo? {
        val gemName = gemNameField.text.trim()
        val gemPath = gemPathField.text.trim()
        val template = templateComboBox.selectedItem as? String
        
        when {
            gemName.isEmpty() -> return ValidationInfo("Gem name is required", gemNameField)
            !isValidGemName(gemName) -> return ValidationInfo(
                "Gem name must be alphanumeric and can contain underscores and hyphens", 
                gemNameField
            )
            gemPath.isEmpty() -> return ValidationInfo("Gem path is required", gemPathField)
            !isValidPath(gemPath) -> return ValidationInfo("Invalid gem path", gemPathField)
            template.isNullOrEmpty() -> return ValidationInfo("Template selection is required", templateComboBox)
            gemAlreadyExists(gemPath, gemName) -> return ValidationInfo(
                "Gem already exists at the specified path", 
                gemPathField
            )
        }
        
        return null
    }
    
    override fun doOKAction() {
        val gemName = gemNameField.text.trim()
        val gemPath = getFullGemPath()
        val template = templateComboBox.selectedItem as String
        
        // Close dialog first
        super.doOKAction()
        
        // Execute command
        commandService.createGem(gemName, gemPath, template) { success, output ->
            SwingUtilities.invokeLater {
                if (success) {
                    Messages.showInfoMessage(
                        project,
                        "Gem '$gemName' created successfully at:\n$gemPath",
                        "Gem Created"
                    )
                    
                    // Ask if user wants to register the gem
                    val register = Messages.showYesNoDialog(
                        project,
                        "Would you like to register this gem?",
                        "Register Gem",
                        Messages.getQuestionIcon()
                    )
                    
                    if (register == Messages.YES) {
                        commandService.registerGem(gemPath) { regSuccess, regOutput ->
                            SwingUtilities.invokeLater {
                                if (regSuccess) {
                                    Messages.showInfoMessage(
                                        project,
                                        "Gem registered successfully.",
                                        "Registration Complete"
                                    )
                                } else {
                                    Messages.showErrorDialog(
                                        project,
                                        "Failed to register gem:\n$regOutput",
                                        "Registration Error"
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Messages.showErrorDialog(
                        project,
                        "Failed to create gem:\n$output",
                        "Creation Error"
                    )
                }
            }
        }
    }
    
    private fun loadTemplates() {
        val templates = configService.getGemTemplates()
        
        // Add default templates if none found
        val defaultTemplates = listOf(
            "DefaultGem",
            "AssetGem", 
            "CppToolGem",
            "PythonToolGem",
            "GraphicsGem",
            "UnifiedMultiplayerGem",
            "PrebuiltGem"
        )
        
        val allTemplates = if (templates.isNotEmpty()) templates else defaultTemplates
        
        templateComboBox.removeAllItems()
        allTemplates.forEach { templateComboBox.addItem(it) }
        
        // Select DefaultGem by default
        if (allTemplates.contains("DefaultGem")) {
            templateComboBox.selectedItem = "DefaultGem"
        }
    }
    
    private fun selectGemPath() {
        val fileChooser = javax.swing.JFileChooser()
        fileChooser.fileSelectionMode = javax.swing.JFileChooser.DIRECTORIES_ONLY
        fileChooser.dialogTitle = "Select Gem Directory"
        fileChooser.currentDirectory = File(gemPathField.text)
        
        if (fileChooser.showOpenDialog(contentPanel) == javax.swing.JFileChooser.APPROVE_OPTION) {
            gemPathField.text = fileChooser.selectedFile.absolutePath
        }
    }
    
    private fun isValidGemName(name: String): Boolean {
        return name.matches(Regex("[a-zA-Z0-9_-]+"))
    }
    
    private fun isValidPath(path: String): Boolean {
        return try {
            val file = File(path)
            file.parentFile?.exists() ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getFullGemPath(): String {
        val basePath = gemPathField.text.trim()
        val gemName = gemNameField.text.trim()
        return File(basePath, gemName).absolutePath
    }
    
    private fun gemAlreadyExists(basePath: String, gemName: String): Boolean {
        val fullPath = File(basePath, gemName)
        return fullPath.exists() && fullPath.isDirectory
    }
}