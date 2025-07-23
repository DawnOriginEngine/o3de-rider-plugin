package com.o3de.plugin.ui.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.layout.panel
import com.o3de.plugin.services.O3DEConfigService
import com.o3de.plugin.services.O3DECommandService
import java.awt.*
import javax.swing.*

class GemManagementDialog(private val project: Project) : DialogWrapper(project) {
    
    private val configService = O3DEConfigService.getInstance(project)
    private val commandService = O3DECommandService.getInstance(project)
    
    private val availableGemsModel = DefaultListModel<String>()
    private val enabledGemsModel = DefaultListModel<String>()
    
    private val availableGemsList = JBList(availableGemsModel)
    private val enabledGemsList = JBList(enabledGemsModel)
    
    private val enableButton = JButton("Enable →")
    private val disableButton = JButton("← Disable")
    private val refreshButton = JButton("Refresh")
    
    init {
        title = "O3DE Gem Management"
        init()
        setupUI()
        loadGems()
    }
    
    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(java.awt.BorderLayout())
        
        // Top panel with labels
        val topPanel = JPanel(java.awt.GridLayout(1, 3))
        topPanel.add(JLabel("Available Gems", SwingConstants.CENTER))
        topPanel.add(JLabel("")) // Empty space
        topPanel.add(JLabel("Enabled Gems", SwingConstants.CENTER))
        
        // Center panel with lists and buttons
        val centerPanel = JPanel(java.awt.BorderLayout())
        
        // Available gems list
        val availableScrollPane = JBScrollPane(availableGemsList)
        availableScrollPane.preferredSize = Dimension(300, 200)
        
        // Enabled gems list
        val enabledScrollPane = JBScrollPane(enabledGemsList)
        enabledScrollPane.preferredSize = Dimension(300, 200)
        
        // Button panel
        val buttonPanel = JPanel(java.awt.GridBagLayout())
        val gbc = java.awt.GridBagConstraints()
        gbc.insets = java.awt.Insets(5, 5, 5, 5)
        gbc.gridx = 0
        gbc.gridy = 0
        buttonPanel.add(enableButton, gbc)
        gbc.gridy = 1
        buttonPanel.add(disableButton, gbc)
        gbc.gridy = 2
        buttonPanel.add(refreshButton, gbc)
        
        // Layout the center panel
        val listsPanel = JPanel(java.awt.BorderLayout())
        listsPanel.add(availableScrollPane, java.awt.BorderLayout.WEST)
        listsPanel.add(buttonPanel, java.awt.BorderLayout.CENTER)
        listsPanel.add(enabledScrollPane, java.awt.BorderLayout.EAST)
        
        // Help text
        val helpText = JLabel("""<html>
                    <b>Gem Management:</b><br>
                    • <b>Available Gems:</b> Gems that are registered but not enabled in this project<br>
                    • <b>Enabled Gems:</b> Gems that are currently enabled in this project<br>
                    • Use Enable/Disable buttons to manage gems for your project<br>
                    • Click Refresh to update the gem lists
                    </html>""")
        
        mainPanel.add(topPanel, java.awt.BorderLayout.NORTH)
        mainPanel.add(listsPanel, java.awt.BorderLayout.CENTER)
        mainPanel.add(helpText, java.awt.BorderLayout.SOUTH)
        
        return mainPanel
    }
    
    override fun createActions(): Array<Action> {
        return arrayOf(cancelAction)
    }
    
    private fun setupUI() {
        // Setup list selection modes
        availableGemsList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        enabledGemsList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        
        // Setup button actions
        enableButton.addActionListener {
            enableSelectedGem()
        }
        
        disableButton.addActionListener {
            disableSelectedGem()
        }
        
        refreshButton.addActionListener {
            loadGems()
        }
        
        // Enable/disable buttons based on selection
        availableGemsList.addListSelectionListener {
            enableButton.isEnabled = availableGemsList.selectedValue != null
        }
        
        enabledGemsList.addListSelectionListener {
            disableButton.isEnabled = enabledGemsList.selectedValue != null
        }
        
        // Initially disable buttons
        enableButton.isEnabled = false
        disableButton.isEnabled = false
    }
    
    private fun loadGems() {
        // Clear existing lists
        availableGemsModel.clear()
        enabledGemsModel.clear()
        
        // Load registered gems
        commandService.getRegisteredGems { gems ->
            SwingUtilities.invokeLater {
                gems.forEach { gemName ->
                    availableGemsModel.addElement(gemName)
                }
                loadEnabledGems()
            }
        }
    }
    
    private fun parseRegisteredGems(output: String) {
        // Parse the output from o3de register --show command
        // The output typically contains gem information in a structured format
        val lines = output.split("\n")
        val gems = mutableSetOf<String>()
        
        for (line in lines) {
            // Look for gem entries - this is a simplified parser
            // The actual format may vary, so this might need adjustment
            if (line.trim().isNotEmpty() && !line.startsWith("#") && !line.startsWith("[")) {
                // Try to extract gem name from various possible formats
                val gemName = extractGemName(line)
                if (gemName.isNotEmpty()) {
                    gems.add(gemName)
                }
            }
        }
        
        // For now, add all gems to available list
        // In a real implementation, we would check which gems are enabled in the project
        gems.forEach { gem ->
            availableGemsModel.addElement(gem)
        }
        
        // Add some example enabled gems (this should be replaced with actual project gem detection)
        loadEnabledGems()
    }
    
    private fun extractGemName(line: String): String {
        // Simple extraction - look for patterns that might contain gem names
        val patterns = listOf(
            """"gem_name"\s*:\s*"([^"]+)""".toRegex(),
            """name\s*=\s*"([^"]+)""".toRegex(),
            """([a-zA-Z][a-zA-Z0-9_]+)Gem""".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(line)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        
        return ""
    }
    
    private fun loadEnabledGems() {
        // Try to load enabled gems from project configuration
        val projectPath = project.basePath
        if (projectPath != null) {
            val projectJsonFile = java.io.File(projectPath, "project.json")
            if (projectJsonFile.exists()) {
                try {
                    val content = projectJsonFile.readText()
                    val enabledGems = parseEnabledGemsFromProject(content)
                    enabledGems.forEach { gem ->
                        enabledGemsModel.addElement(gem)
                        // Remove from available list if present
                        if (availableGemsModel.contains(gem)) {
                            availableGemsModel.removeElement(gem)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore parsing errors
                }
            }
        }
    }
    
    private fun parseEnabledGemsFromProject(content: String): List<String> {
        // Simple JSON parsing to extract enabled gems
        val gems = mutableListOf<String>()
        val gemPattern = """"gem_name"\s*:\s*"([^"]+)""".toRegex()
        
        gemPattern.findAll(content).forEach { match ->
            gems.add(match.groupValues[1])
        }
        
        return gems
    }
    
    private fun enableSelectedGem() {
        val selectedGem = availableGemsList.selectedValue
        if (selectedGem != null) {
            // For this example, we'll assume the gem path is in a standard location
            val gemPath = findGemPath(selectedGem)
            
            commandService.enableGem(gemPath) { success, output ->
                SwingUtilities.invokeLater {
                    if (success) {
                        // Move gem from available to enabled
                        availableGemsModel.removeElement(selectedGem)
                        enabledGemsModel.addElement(selectedGem)
                        
                        Messages.showInfoMessage(
                            project,
                            "Gem '$selectedGem' enabled successfully",
                            "Gem Enabled"
                        )
                    } else {
                        Messages.showErrorDialog(
                            project,
                            "Failed to enable gem '$selectedGem':\n$output",
                            "Enable Error"
                        )
                    }
                }
            }
        }
    }
    
    private fun disableSelectedGem() {
        val selectedGem = enabledGemsList.selectedValue
        if (selectedGem != null) {
            val gemPath = findGemPath(selectedGem)
            
            commandService.disableGem(gemPath) { success, output ->
                SwingUtilities.invokeLater {
                    if (success) {
                        // Move gem from enabled to available
                        enabledGemsModel.removeElement(selectedGem)
                        availableGemsModel.addElement(selectedGem)
                        
                        Messages.showInfoMessage(
                            project,
                            "Gem '$selectedGem' disabled successfully",
                            "Gem Disabled"
                        )
                    } else {
                        Messages.showErrorDialog(
                            project,
                            "Failed to disable gem '$selectedGem':\n$output",
                            "Disable Error"
                        )
                    }
                }
            }
        }
    }
    
    private fun findGemPath(gemName: String): String {
        // Try to find the gem path
        val projectPath = project.basePath
        if (projectPath != null) {
            // Check common gem locations
            val possiblePaths = listOf(
                java.io.File(projectPath, "Gems/$gemName"),
                java.io.File(projectPath, "$gemName"),
                java.io.File(configService.getDefaultGemsPath(), gemName)
            )
            
            for (path in possiblePaths) {
                if (path.exists() && java.io.File(path, "gem.json").exists()) {
                    return path.absolutePath
                }
            }
        }
        
        // Fallback to gem name (let O3DE resolve the path)
        return gemName
    }
}