package com.o3de.plugin.services

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import java.io.File

@State(
    name = "O3DEConfigService",
    storages = [Storage("o3de-config.xml")]
)
@Service(Service.Level.PROJECT)
class O3DEConfigService : PersistentStateComponent<O3DEConfigService.State> {
    
    data class State(
        var enginePath: String = "",
        var projectPath: String = "",
        var defaultGemsPath: String = "",
        var pythonExecutable: String = ""
    )
    
    private var state = State()
    
    override fun getState(): State = state
    
    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }
    
    companion object {
        fun getInstance(project: Project): O3DEConfigService {
            return project.service<O3DEConfigService>()
        }
    }
    
    // Engine path management
    fun getEnginePath(): String = state.enginePath
    
    fun setEnginePath(path: String) {
        state.enginePath = path
        // Auto-detect Python executable
        if (pythonExecutable.isEmpty()) {
            detectPythonExecutable()
        }
    }
    
    // Project path management
    fun getProjectPath(): String = state.projectPath
    
    fun setProjectPath(path: String) {
        state.projectPath = path
    }
    
    // Default gems path
    fun getDefaultGemsPath(): String {
        return if (state.defaultGemsPath.isNotEmpty()) {
            state.defaultGemsPath
        } else {
            // Default to user home O3DE directory
            System.getProperty("user.home") + File.separator + "O3DE" + File.separator + "Gems"
        }
    }
    
    fun setDefaultGemsPath(path: String) {
        state.defaultGemsPath = path
    }
    
    // Python executable
    val pythonExecutable: String
        get() = if (state.pythonExecutable.isNotEmpty()) {
            state.pythonExecutable
        } else {
            detectPythonExecutable()
        }
    
    private fun detectPythonExecutable(): String {
        if (state.enginePath.isEmpty()) return "python"
        
        val windowsPython = File(state.enginePath, "python\\python.cmd")
        val linuxPython = File(state.enginePath, "python/python.sh")
        
        return when {
            windowsPython.exists() -> windowsPython.absolutePath
            linuxPython.exists() -> linuxPython.absolutePath
            else -> "python"
        }.also {
            state.pythonExecutable = it
        }
    }
    
    // O3DE script paths
    fun getO3DEScriptPath(): String {
        if (state.enginePath.isEmpty()) return ""
        
        val windowsScript = File(state.enginePath, "scripts\\o3de.bat")
        val linuxScript = File(state.enginePath, "scripts/o3de.sh")
        
        return when {
            windowsScript.exists() -> windowsScript.absolutePath
            linuxScript.exists() -> linuxScript.absolutePath
            else -> ""
        }
    }
    
    // Validation methods
    fun isEnginePathValid(): Boolean {
        if (state.enginePath.isEmpty()) return false
        val engineDir = File(state.enginePath)
        return engineDir.exists() && engineDir.isDirectory && 
               File(engineDir, "scripts").exists()
    }
    
    fun isProjectPathValid(): Boolean {
        if (state.projectPath.isEmpty()) return false
        val projectDir = File(state.projectPath)
        return projectDir.exists() && projectDir.isDirectory &&
               File(projectDir, "project.json").exists()
    }
    
    // Template detection
    fun getAvailableTemplates(): List<String> {
        if (!isEnginePathValid()) return emptyList()
        
        val templatesDir = File(state.enginePath, "Templates")
        if (!templatesDir.exists()) return emptyList()
        
        return templatesDir.listFiles { file -> file.isDirectory }?.map { it.name } ?: emptyList()
    }
    
    fun getGemTemplates(): List<String> {
        return getAvailableTemplates().filter { template ->
            template.contains("Gem", ignoreCase = true)
        }
    }
    
    fun getComponentTemplates(): List<String> {
        return getAvailableTemplates().filter { template ->
            template.contains("Component", ignoreCase = true)
        }
    }
}