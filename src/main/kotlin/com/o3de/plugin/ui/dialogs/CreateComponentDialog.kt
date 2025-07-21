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

class CreateComponentDialog(private val project: Project) : DialogWrapper(project) {
    
    private val configService = O3DEConfigService.getInstance(project)
    private val commandService = O3DECommandService.getInstance(project)
    
    private val componentNameField = JBTextField()
    private val destinationPathField = JBTextField()
    private val gemNameField = JBTextField()
    private val templateComboBox = JComboBox<String>()
    
    init {
        title = "Create O3DE Component"
        init()
        loadTemplates()
        setupDefaults()
    }
    
    override fun createCenterPanel(): JComponent {
        return panel {
            row("Component Name:") {
                componentNameField(growX)
            }
            
            row("Destination Path:") {
                destinationPathField(growX)
                button("Browse") {
                    selectDestinationPath()
                }
            }
            
            row("Gem Name:") {
                gemNameField(growX)
            }
            
            row("Template:") {
                templateComboBox(growX)
            }
            
            row {
                comment("""<html>
                    <b>Component Creation:</b><br>
                    • Component name will have "Component" appended automatically<br>
                    • Destination path should point to the gem's Code directory<br>
                    • Gem name is used as the namespace for the component<br>
                    • Template determines the component structure and features
                    </html>""")
            }
            
            row {
                comment("""<html>
                    <b>Example:</b><br>
                    Component Name: MyTest<br>
                    Result: MyTestComponent.h, MyTestComponent.cpp, MyTestInterface.h
                    </html>""")
            }
        }
    }
    
    override fun doValidate(): ValidationInfo? {
        val componentName = componentNameField.text.trim()
        val destinationPath = destinationPathField.text.trim()
        val gemName = gemNameField.text.trim()
        val template = templateComboBox.selectedItem as? String
        
        when {
            componentName.isEmpty() -> return ValidationInfo("Component name is required", componentNameField)
            !isValidComponentName(componentName) -> return ValidationInfo(
                "Component name must be alphanumeric and can contain underscores", 
                componentNameField
            )
            destinationPath.isEmpty() -> return ValidationInfo("Destination path is required", destinationPathField)
            !isValidDestinationPath(destinationPath) -> return ValidationInfo(
                "Destination path must exist", 
                destinationPathField
            )
            gemName.isEmpty() -> return ValidationInfo("Gem name is required", gemNameField)
            !isValidGemName(gemName) -> return ValidationInfo(
                "Gem name must be alphanumeric and can contain underscores", 
                gemNameField
            )
            template.isNullOrEmpty() -> return ValidationInfo("Template selection is required", templateComboBox)
            componentAlreadyExists(destinationPath, componentName) -> return ValidationInfo(
                "Component already exists at the specified path", 
                componentNameField
            )
        }
        
        return null
    }
    
    override fun doOKAction() {
        val componentName = componentNameField.text.trim()
        val destinationPath = destinationPathField.text.trim()
        val gemName = gemNameField.text.trim()
        val template = templateComboBox.selectedItem as String
        
        // Close dialog first
        super.doOKAction()
        
        // Execute command
        commandService.createComponent(
            componentName, 
            destinationPath, 
            gemName, 
            template
        ) { success, output ->
            SwingUtilities.invokeLater {
                if (success) {
                    Messages.showInfoMessage(
                        project,
                        "Component '${componentName}Component' created successfully at:\n$destinationPath",
                        "Component Created"
                    )
                    
                    // Show next steps
                    val nextSteps = """Component created successfully!
                        
                        Next steps:
                        1. Add the component files to your Gem's CMakeLists.txt
                        2. Register the component in your Gem's module
                        3. Implement the component's functionality
                        4. Build your project
                        
                        Files created:
                        • ${componentName}Component.h
                        • ${componentName}Component.cpp  
                        • ${componentName}Interface.h
                    """.trimIndent()
                    
                    Messages.showInfoMessage(
                        project,
                        nextSteps,
                        "Next Steps"
                    )
                } else {
                    Messages.showErrorDialog(
                        project,
                        "Failed to create component:\n$output",
                        "Creation Error"
                    )
                }
            }
        }
    }
    
    private fun loadTemplates() {
        val templates = configService.getComponentTemplates()
        
        // Add default component templates
        val defaultTemplates = listOf(
            "DefaultComponent"
        )
        
        val allTemplates = if (templates.isNotEmpty()) templates else defaultTemplates
        
        templateComboBox.removeAllItems()
        allTemplates.forEach { templateComboBox.addItem(it) }
        
        // Select DefaultComponent by default
        if (allTemplates.contains("DefaultComponent")) {
            templateComboBox.selectedItem = "DefaultComponent"
        }
    }
    
    private fun setupDefaults() {
        // Try to detect if we're in a gem directory
        val projectPath = project.basePath
        if (projectPath != null) {
            val projectDir = File(projectPath)
            
            // Look for gem.json to detect if we're in a gem
            val gemJsonFile = File(projectDir, "gem.json")
            if (gemJsonFile.exists()) {
                // We're in a gem directory
                val codeDir = File(projectDir, "Code")
                if (codeDir.exists()) {
                    destinationPathField.text = codeDir.absolutePath
                }
                
                // Try to extract gem name from gem.json
                try {
                    val gemName = extractGemNameFromJson(gemJsonFile)
                    if (gemName.isNotEmpty()) {
                        gemNameField.text = gemName
                    }
                } catch (e: Exception) {
                    // Ignore errors in parsing
                }
            } else {
                // Look for Gems subdirectory
                val gemsDir = File(projectDir, "Gems")
                if (gemsDir.exists()) {
                    destinationPathField.text = gemsDir.absolutePath
                }
            }
        }
    }
    
    private fun extractGemNameFromJson(gemJsonFile: File): String {
        // Simple JSON parsing to extract gem name
        val content = gemJsonFile.readText()
        val nameRegex = """"gem_name"\s*:\s*"([^"]+)""".toRegex()
        val match = nameRegex.find(content)
        return match?.groupValues?.get(1) ?: ""
    }
    
    private fun selectDestinationPath() {
        val fileChooser = javax.swing.JFileChooser()
        fileChooser.fileSelectionMode = javax.swing.JFileChooser.DIRECTORIES_ONLY
        fileChooser.dialogTitle = "Select Component Destination Directory"
        
        val currentPath = destinationPathField.text
        if (currentPath.isNotEmpty()) {
            fileChooser.currentDirectory = File(currentPath)
        }
        
        if (fileChooser.showOpenDialog(contentPanel) == javax.swing.JFileChooser.APPROVE_OPTION) {
            destinationPathField.text = fileChooser.selectedFile.absolutePath
        }
    }
    
    private fun isValidComponentName(name: String): Boolean {
        return name.matches(Regex("[a-zA-Z][a-zA-Z0-9_]*"))
    }
    
    private fun isValidGemName(name: String): Boolean {
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
    
    private fun componentAlreadyExists(destinationPath: String, componentName: String): Boolean {
        val headerFile = File(destinationPath, "${componentName}Component.h")
        val sourceFile = File(destinationPath, "${componentName}Component.cpp")
        return headerFile.exists() || sourceFile.exists()
    }
}