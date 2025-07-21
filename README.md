# O3DE JetBrains Rider Plugin

A comprehensive JetBrains Rider plugin for O3DE (Open 3D Engine) development that provides quick access to O3DE CLI commands for creating and managing Gems, Components, and EBus functionality.

## Features

### 🚀 Quick Creation Tools
- **Create Gems**: Quickly create new O3DE Gems with various templates (AssetGem, DefaultGem, CppToolGem, PythonToolGem)
- **Create Components**: Generate new O3DE Components with proper structure and boilerplate code
- **Create from Templates**: Use any available O3DE template to create new items

### 🔧 Project Management
- **Gem Management**: Enable and disable Gems for your project
- **Engine Registration**: Register O3DE engines with the global registry
- **Project Building**: Quick access to build instructions and project management

### 🎯 Integrated Workflow
- **Tool Window**: Dedicated O3DE tool window with configuration and quick actions
- **Context Actions**: Right-click actions for O3DE operations
- **Path Management**: Automatic detection and configuration of O3DE paths

## Installation

1. Clone this repository to your local machine
2. Open the project in IntelliJ IDEA or JetBrains Rider
3. Build the plugin using Gradle:
   ```bash
   ./gradlew buildPlugin
   ```
4. Install the generated plugin file from `build/distributions/` in your JetBrains Rider

## Configuration

### First Time Setup
1. Open JetBrains Rider
2. Go to **View → Tool Windows → O3DE**
3. Configure the following paths:
   - **Engine Path**: Path to your O3DE engine installation
   - **Project Path**: Path to your current O3DE project
   - **Default Gem Path**: Default location for creating new Gems

### Auto-Detection
The plugin automatically attempts to detect:
- Python executable path
- O3DE script locations
- Project structure and existing Gems

## Usage

### Creating a New Gem
1. Open the O3DE tool window
2. Click **Create Gem** or use the action from the Tools menu
3. Fill in the gem details:
   - **Gem Name**: Name for your new gem
   - **Gem Path**: Where to create the gem
   - **Template**: Choose from available gem templates
4. Click **OK** to create the gem
5. Follow the post-creation instructions to register the gem

### Creating a New Component
1. Click **Create Component** in the O3DE tool window
2. Specify:
   - **Component Name**: Name for your component ("Component" will be appended)
   - **Destination Path**: Target directory (usually a gem's Code directory)
   - **Gem Name**: Namespace for the component
   - **Template**: Component template to use
3. The plugin will generate the necessary .h and .cpp files

### Managing Gems
1. Click **Manage Gems** to open the Gem Management dialog
2. View available and enabled gems
3. Use the **Enable →** and **← Disable** buttons to manage gems
4. Click **Refresh** to update the gem lists

### Using Templates
1. Click **Create from Template**
2. Select a template type (Gem, Component, etc.)
3. Provide a name and destination path
4. The template will be instantiated with your specifications

## Available Templates

### Gem Templates
- **AssetGem**: For gems that primarily handle assets
- **DefaultGem**: Standard gem with basic C++ structure
- **CppToolGem**: C++ tool gem for editor functionality
- **PythonToolGem**: Python-based tool gem

### Component Templates
- **DefaultComponent**: Standard component with basic structure

## Tool Window Features

### Configuration Section
- Engine path configuration with file browser
- Project path setup
- Default gem path setting
- Status indicators for path validation

### Quick Actions
- One-click gem creation
- Component generation
- Template instantiation
- Engine registration

### Status Display
- Current configuration status
- Path validation results
- Recent operation feedback

## Commands Integration

The plugin integrates with the following O3DE CLI commands:

- `o3de create-gem` - Create new gems
- `o3de create-component` - Generate components
- `o3de create-from-template` - Use templates
- `o3de enable-gem` - Enable gems in projects
- `o3de disable-gem` - Disable gems
- `o3de register` - Register engines and projects
- `o3de register --show` - List registered items

## Requirements

- JetBrains Rider 2023.1 or later
- O3DE engine installation
- Python 3.7+ (for O3DE CLI)
- Windows, macOS, or Linux

## Development

### Building from Source
```bash
git clone <repository-url>
cd o3de-rider-plugin
./gradlew buildPlugin
```

### Running in Development
```bash
./gradlew runIde
```

### Testing
```bash
./gradlew test
```

## File Structure

```
src/main/kotlin/com/o3de/plugin/
├── services/
│   ├── O3DEConfigService.kt      # Configuration management
│   └── O3DECommandService.kt     # CLI command execution
├── ui/
│   ├── O3DEToolWindowFactory.kt  # Tool window factory
│   ├── O3DEToolWindowContent.kt  # Main tool window UI
│   └── dialogs/
│       ├── CreateGemDialog.kt    # Gem creation dialog
│       ├── CreateComponentDialog.kt # Component creation dialog
│       ├── CreateFromTemplateDialog.kt # Template dialog
│       └── GemManagementDialog.kt # Gem management UI
└── actions/
    └── O3DEActions.kt            # Plugin actions
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and feature requests, please use the GitHub issue tracker.

## Acknowledgments

- O3DE Community for the excellent documentation
- JetBrains for the IntelliJ Platform SDK
- Contributors and testers