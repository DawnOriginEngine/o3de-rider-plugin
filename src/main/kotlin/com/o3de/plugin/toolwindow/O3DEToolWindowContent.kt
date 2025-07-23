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
import java.awt.*
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
        val mainPanel = JPanel(BorderLayout())
        val contentPanel = JPanel()
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.Y_AXIS)
        
        // Configuration Section
        val configPanel = createTitledPanel("Configuration")
        val configForm = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()
        gbc.insets = Insets(5, 5, 5, 5)
        gbc.anchor = GridBagConstraints.WEST
        
        // Engine Path
        val enginePathField = JBTextField(configService.getEnginePath())
        gbc.gridx = 0; gbc.gridy = 0
        configForm.add(JLabel("Engine Path:"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        configForm.add(enginePathField, gbc)
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0
        val engineBrowseBtn = JButton("Browse")
        engineBrowseBtn.addActionListener { selectEnginePath(enginePathField) }
        configForm.add(engineBrowseBtn, gbc)
        
        // Project Path
        val projectPathField = JBTextField(configService.getProjectPath())
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0
        configForm.add(JLabel("Project Path:"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        configForm.add(projectPathField, gbc)
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0
        val projectBrowseBtn = JButton("Browse")
        projectBrowseBtn.addActionListener { selectProjectPath(projectPathField) }
        configForm.add(projectBrowseBtn, gbc)
        
        // Gems Path
        val gemsPathField = JBTextField(configService.getDefaultGemsPath())
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0
        configForm.add(JLabel("Default Gems Path:"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        configForm.add(gemsPathField, gbc)
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0
        val gemsBrowseBtn = JButton("Browse")
        gemsBrowseBtn.addActionListener { selectGemsPath(gemsPathField) }
        configForm.add(gemsBrowseBtn, gbc)
        
        configPanel.add(configForm, BorderLayout.CENTER)
        contentPanel.add(configPanel)
        
        // Status Section
        val statusPanel = createTitledPanel("Status")
        val statusForm = JPanel(FlowLayout(FlowLayout.LEFT))
        val statusLabel = JBLabel(getStatusText())
        val refreshBtn = JButton("Refresh")
        refreshBtn.addActionListener { refreshStatus(statusLabel) }
        statusForm.add(statusLabel)
        statusForm.add(refreshBtn)
        statusPanel.add(statusForm, BorderLayout.CENTER)
        contentPanel.add(statusPanel)
        
        // Quick Actions Section
        val actionsPanel = createTitledPanel("Quick Actions")
        val actionsForm = JPanel(GridLayout(2, 2, 5, 5))
        val createGemBtn = JButton("Create Gem")
        createGemBtn.addActionListener { showCreateGemDialog() }
        val createComponentBtn = JButton("Create Component")
        createComponentBtn.addActionListener { showCreateComponentDialog() }
        val createTemplateBtn = JButton("Create from Template")
        createTemplateBtn.addActionListener { showCreateFromTemplateDialog() }
        val registerEngineBtn = JButton("Register Engine")
        registerEngineBtn.addActionListener { registerEngine() }
        actionsForm.add(createGemBtn)
        actionsForm.add(createComponentBtn)
        actionsForm.add(createTemplateBtn)
        actionsForm.add(registerEngineBtn)
        actionsPanel.add(actionsForm, BorderLayout.CENTER)
        contentPanel.add(actionsPanel)
        
        // Gem Management Section
        val gemMgmtPanel = createTitledPanel("Gem Management")
        val gemMgmtForm = JPanel(GridLayout(2, 2, 5, 5))
        val enableGemBtn = JButton("Enable Gem")
        enableGemBtn.addActionListener { showEnableGemDialog() }
        val disableGemBtn = JButton("Disable Gem")
        disableGemBtn.addActionListener { showDisableGemDialog() }
        val listGemsBtn = JButton("List Registered Gems")
        listGemsBtn.addActionListener { listRegisteredGems() }
        gemMgmtForm.add(enableGemBtn)
        gemMgmtForm.add(disableGemBtn)
        gemMgmtForm.add(listGemsBtn)
        gemMgmtPanel.add(gemMgmtForm, BorderLayout.CENTER)
        contentPanel.add(gemMgmtPanel)
        
        // Templates Section
        val templatesPanel = createTitledPanel("Available Templates")
        val templatesList = createTemplatesList()
        val templatesScrollPane = JBScrollPane(templatesList)
        templatesScrollPane.preferredSize = Dimension(300, 150)
        templatesPanel.add(templatesScrollPane, BorderLayout.CENTER)
        contentPanel.add(templatesPanel)
        
        mainPanel.add(JBScrollPane(contentPanel), BorderLayout.CENTER)
        return mainPanel
    }
    
    private fun createTitledPanel(title: String): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(10, 10, 10, 10)
        val titleLabel = JBLabel(title)
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD)
        panel.add(titleLabel, BorderLayout.NORTH)
        return panel
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