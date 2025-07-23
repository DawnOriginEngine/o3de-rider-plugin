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

class CreateComponentDialog(private val project: Project) : DialogWrapper(project) {
    
    private val configService = O3DEConfigService.getInstance(project)
    private val commandService = O3DECommandService.getInstance(project)
    
    private val componentNameField = JBTextField()
    private val destinationPathField = JBTextField()
    private val gemNameField = JBTextField()
    private val templateComboBox = JComboBox<String>()
    private val keepRestrictedFilesCheckBox = JCheckBox("Keep restricted files", true)
    private val replacementsPanel = JPanel(GridBagLayout())
    
    init {
        title = "Create O3DE Component"
        init()
        loadTemplates()
        setupDefaults()
    }
    
    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout())
        val formPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()
        
        gbc.insets = Insets(5, 5, 5, 5)
        gbc.anchor = GridBagConstraints.WEST
        
        // Component Name
        gbc.gridx = 0; gbc.gridy = 0
        formPanel.add(JLabel("Component Name:"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        formPanel.add(componentNameField, gbc)
        
        // Destination Path
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0
        formPanel.add(JLabel("Destination Path:"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        formPanel.add(destinationPathField, gbc)
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0
        val browseButton = JButton("Browse")
        browseButton.addActionListener { selectDestinationPath() }
        formPanel.add(browseButton, gbc)
        
        // Gem Name
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0
        formPanel.add(JLabel("Gem Name:"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        formPanel.add(gemNameField, gbc)
        
        // Template
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0
        formPanel.add(JLabel("Template:"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        formPanel.add(templateComboBox, gbc)
        
        // Keep Restricted Files
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL
        formPanel.add(keepRestrictedFilesCheckBox, gbc)
        gbc.gridwidth = 1
        
        // Replacements section
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0
        formPanel.add(JLabel("Template Variables:"), gbc)
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.3
        setupReplacementsPanel()
        formPanel.add(JScrollPane(replacementsPanel), gbc)
        gbc.gridwidth = 1; gbc.weighty = 0.0
        
        // Help text panel
        val helpPanel = JPanel(GridLayout(3, 1))
        val helpText1 = JLabel("""<html>
                    <b>Component Creation (using create-from-template):</b><br>
                    • Uses O3DE create-from-template command to create components<br>
                    • Destination path should point to the gem's Code directory<br>
                    • Gem name is used as the namespace for the component<br>
                    • Template determines the component structure and features
                    </html>""")
        val helpText2 = JLabel("""<html>
                    <b>Template Variables:</b><br>
                    • GemName: Name of the gem, used for namespace<br>
                    • ComponentName: Name of the component<br>
                    • COMPONENT_NAME: Uppercase component name
                    </html>""")
        val helpText3 = JLabel("""<html>
                    <b>Example:</b><br>
                    Component Name: MyTest, Gem Name: MyGem<br>
                    Result: MyTestComponent.h, MyTestComponent.cpp
                    </html>""")
        helpPanel.add(helpText1)
        helpPanel.add(helpText2)
        helpPanel.add(helpText3)
        
        mainPanel.add(formPanel, BorderLayout.CENTER)
        mainPanel.add(helpPanel, BorderLayout.SOUTH)
        
        return mainPanel
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
        
        // Collect template variable replacement parameters
        val replacements = getReplacements()
        val keepRestrictedFiles = keepRestrictedFilesCheckBox.isSelected
        
        // Execute command using create-from-template
        commandService.createFromTemplate(
            template,
            destinationPath,
            componentName,
            replacements,
            keepRestrictedFiles
        ) { success, output ->
            SwingUtilities.invokeLater {
                if (success) {
                    Messages.showInfoMessage(
                        project,
                        "Component '$componentName' created successfully using template '$template'!\nDestination path: $destinationPath",
                        "Component Created Successfully"
                    )
                    
                    // Show next steps
                    val nextSteps = """Component created successfully!
                        
                        Command used: create-from-template
                        Template: $template
                        Template variable replacements: ${replacements.entries.joinToString(", ") { "\${${it.key}} = ${it.value}" }}
                        
                        Next steps:
                        1. Add component files to the Gem's CMakeLists.txt
                        2. Register the component in the Gem module
                        3. Implement component functionality
                        4. Build the project
                        
                        Created files:
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
    
    /**
     * Setup template variable replacements panel
     */
    private fun setupReplacementsPanel() {
        replacementsPanel.removeAll()
        val gbc = GridBagConstraints()
        gbc.insets = Insets(2, 2, 2, 2)
        gbc.anchor = GridBagConstraints.WEST
        
        // Add default replacement variables
        val defaultReplacements = mapOf(
            "GemName" to gemNameField,
            "ComponentName" to componentNameField,
            "COMPONENT_NAME" to JBTextField()
        )
        
        var row = 0
        defaultReplacements.forEach { (key, field) ->
            gbc.gridx = 0; gbc.gridy = row
            replacementsPanel.add(JLabel("\${$key}:"), gbc)
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
            
            if (key == "COMPONENT_NAME") {
                // Auto-fill uppercase component name
                field.text = componentNameField.text.uppercase()
                componentNameField.document.addDocumentListener(object : javax.swing.event.DocumentListener {
                    override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = updateUppercaseName(field)
                    override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = updateUppercaseName(field)
                    override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = updateUppercaseName(field)
                    
                    private fun updateUppercaseName(targetField: JBTextField) {
                        targetField.text = componentNameField.text.uppercase()
                    }
                })
            }
            
            replacementsPanel.add(field, gbc)
            gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0
            row++
        }
    }
    
    /**
     * Get all template variable replacement parameters
     */
    private fun getReplacements(): Map<String, String> {
        val replacements = mutableMapOf<String, String>()
        
        // Add basic replacement variables
        replacements["GemName"] = gemNameField.text.trim()
        replacements["ComponentName"] = componentNameField.text.trim()
        replacements["COMPONENT_NAME"] = componentNameField.text.trim().uppercase()
        
        // Collect other variables from replacement panel
        val components = replacementsPanel.components
        var i = 0
        while (i < components.size - 1) {
            val label = components[i] as? JLabel
            val field = components[i + 1] as? JBTextField
            
            if (label != null && field != null) {
                val key = label.text.removeSurrounding("\${", ":}")
                val value = field.text.trim()
                if (key.isNotEmpty() && value.isNotEmpty()) {
                    replacements[key] = value
                }
            }
            i += 2
        }
        
        return replacements
    }
}