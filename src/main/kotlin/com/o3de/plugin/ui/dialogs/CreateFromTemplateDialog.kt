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
import javax.swing.JComponent
import javax.swing.JComboBox
import javax.swing.SwingUtilities

class CreateFromTemplateDialog(private val project: Project) : DialogWrapper(project) {
    
    private val configService = O3DEConfigService.getInstance(project)
    private val commandService = O3DECommandService.getInstance(project)
    
    private val templateComboBox = JComboBox<String>()
    private val destinationPathField = JBTextField()
    private val nameField = JBTextField()
    
    init {
        title = "Create from O3DE Template"
        init()
        loadTemplates()
        setupDefaults()
    }
    
    override fun createCenterPanel(): JComponent {
        return panel {
            row("Template:") {
                templateComboBox(growX)
            }
            
            row("Name:") {
                nameField(growX)
            }
            
            row("Destination Path:") {
                destinationPathField(growX)
                button("Browse") {
                    selectDestinationPath()
                }
            }
            
            row {
                comment("""<html>
                    <b>Template Creation:</b><br>
                    • Select a template type (Gem, Component, etc.)<br>
                    • Provide a name for the new item<br>
                    • Choose the destination directory<br>
                    • The template will be instantiated with your specified name
                    </html>""")
            }
            
            row {
                comment("""<html>
                    <b>Available Templates:</b><br>
                    • <b>Gem Templates:</b> AssetGem, DefaultGem, CppToolGem, PythonToolGem<br>
                    • <b>Component Templates:</b> DefaultComponent<br>
                    • <b>Other Templates:</b> Project templates and custom templates
                    </html>""")
            }
        }
    }
    
    override fun doValidate(): ValidationInfo? {
        val template = templateComboBox.selectedItem as? String
        val name = nameField.text.trim()
        val destinationPath = destinationPathField.text.trim()
        
        when {
            template.isNullOrEmpty() -> return ValidationInfo("Template selection is required", templateComboBox)
            name.isEmpty() -> return ValidationInfo("Name is required", nameField)
            !isValidName(name) -> return ValidationInfo(
                "Name must be alphanumeric and can contain underscores", 
                nameField
            )
            destinationPath.isEmpty() -> return ValidationInfo("Destination path is required", destinationPathField)
            !isValidDestinationPath(destinationPath) -> return ValidationInfo(
                "Destination path must exist", 
                destinationPathField
            )
            itemAlreadyExists(destinationPath, name) -> return ValidationInfo(
                "Item with this name already exists at the specified path", 
                nameField
            )
        }
        
        return null
    }
    
    override fun doOKAction() {
        val template = templateComboBox.selectedItem as String
        val name = nameField.text.trim()
        val destinationPath = destinationPathField.text.trim()
        
        // Close dialog first
        super.doOKAction()
        
        // Execute command
        commandService.createFromTemplate(
            template, 
            name, 
            destinationPath
        ) { success, output ->
            SwingUtilities.invokeLater {
                if (success) {
                    Messages.showInfoMessage(
                        project,
                        "'$name' created successfully from template '$template' at:\n$destinationPath",
                        "Template Created"
                    )
                    
                    // Show template-specific next steps
                    val nextSteps = getNextStepsForTemplate(template, name)
                    if (nextSteps.isNotEmpty()) {
                        Messages.showInfoMessage(
                            project,
                            nextSteps,
                            "Next Steps"
                        )
                    }
                } else {
                    Messages.showErrorDialog(
                        project,
                        "Failed to create from template:\n$output",
                        "Creation Error"
                    )
                }
            }
        }
    }
    
    private fun loadTemplates() {
        val templates = configService.getAvailableTemplates()
        
        // Add default templates if none found
        val defaultTemplates = listOf(
            "AssetGem",
            "DefaultGem", 
            "CppToolGem",
            "PythonToolGem",
            "DefaultComponent"
        )
        
        val allTemplates = if (templates.isNotEmpty()) templates else defaultTemplates
        
        templateComboBox.removeAllItems()
        allTemplates.forEach { templateComboBox.addItem(it) }
        
        // Select DefaultGem by default
        if (allTemplates.contains("DefaultGem")) {
            templateComboBox.selectedItem = "DefaultGem"
        }
    }
    
    private fun setupDefaults() {
        val projectPath = project.basePath
        if (projectPath != null) {
            val projectDir = File(projectPath)
            
            // Default to project directory or Gems subdirectory
            val gemsDir = File(projectDir, "Gems")
            if (gemsDir.exists()) {
                destinationPathField.text = gemsDir.absolutePath
            } else {
                destinationPathField.text = projectDir.absolutePath
            }
        }
    }
    
    private fun selectDestinationPath() {
        val fileChooser = javax.swing.JFileChooser()
        fileChooser.fileSelectionMode = javax.swing.JFileChooser.DIRECTORIES_ONLY
        fileChooser.dialogTitle = "Select Destination Directory"
        
        val currentPath = destinationPathField.text
        if (currentPath.isNotEmpty()) {
            fileChooser.currentDirectory = File(currentPath)
        }
        
        if (fileChooser.showOpenDialog(contentPanel) == javax.swing.JFileChooser.APPROVE_OPTION) {
            destinationPathField.text = fileChooser.selectedFile.absolutePath
        }
    }
    
    private fun isValidName(name: String): Boolean {
        return name.matches(Regex("[a-zA-Z][a-zA-Z0-9_]*"))
    }
    
    private fun isValidDestinationPath(path: String): Boolean {
        return try {
            val file = File(path)
            file.exists() && file.isDirectory
        } catch (e: Exception) {
            false
        }
    }
    
    private fun itemAlreadyExists(destinationPath: String, name: String): Boolean {
        val targetDir = File(destinationPath, name)
        return targetDir.exists()
    }
    
    private fun getNextStepsForTemplate(template: String, name: String): String {
        return when {
            template.contains("Gem", ignoreCase = true) -> {
                """Gem '$name' created successfully!
                    
                    Next steps:
                    1. Register the gem with your project:
                       o3de enable-gem --gem-path "path/to/$name"
                    2. Add the gem to your project's gem.json
                    3. Build your project to compile the gem
                    4. Start implementing your gem's functionality
                    
                    The gem includes:
                    • Basic module structure
                    • CMakeLists.txt configuration
                    • gem.json metadata
                    • Sample code (depending on template)
                """.trimIndent()
            }
            template.contains("Component", ignoreCase = true) -> {
                """Component '$name' created successfully!
                    
                    Next steps:
                    1. Add the component files to your Gem's CMakeLists.txt
                    2. Register the component in your Gem's module
                    3. Implement the component's functionality
                    4. Build your project
                    
                    Files created:
                    • ${name}Component.h
                    • ${name}Component.cpp
                    • ${name}Interface.h
                """.trimIndent()
            }
            else -> {
                """'$name' created successfully from template '$template'!
                    
                    Next steps:
                    1. Review the generated files
                    2. Customize the implementation as needed
                    3. Build your project
                    4. Test the functionality
                """.trimIndent()
            }
        }
    }
}