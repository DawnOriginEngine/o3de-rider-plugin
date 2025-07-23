# O3DE Rider Plugin

A comprehensive JetBrains Rider plugin for Open 3D Engine (O3DE) development, providing seamless integration between Rider IDE and O3DE workflows with advanced tooling, debugging support, and automated CI/CD capabilities.

## 🚀 Features

### 🛠️ Core Development Tools
- **O3DE Project Detection**: Automatically detects and configures O3DE projects
- **Gem Management**: Browse, create, enable/disable, and manage O3DE gems
- **Component Generation**: Create new O3DE Components with proper structure and boilerplate code
- **Template System**: Use any available O3DE template to create new items
- **Build Integration**: Integrated build system with CMake support

### 🎯 Enhanced IDE Integration
- **Tool Window**: Dedicated O3DE tool window with configuration and quick actions
- **Context Actions**: Right-click actions for O3DE operations
- **Code Templates**: Pre-configured templates for O3DE components
- **Asset Management**: Enhanced asset browser and management tools
- **Path Management**: Automatic detection and configuration of O3DE paths

### 🐛 Advanced Development Support
- **Debugging Support**: Advanced debugging capabilities for O3DE applications
- **Live Coding**: Hot-reload support for rapid development
- **Code Quality**: Integrated static analysis and code quality checks
- **Testing Framework**: Comprehensive testing utilities and helpers

### 🚀 CI/CD & Automation
- **GitHub Actions**: Pre-configured workflows for automated testing and releases
- **Code Quality Gates**: Automated code quality checks with Detekt and security scanning
- **Multi-version Testing**: Compatibility testing across multiple Rider and Java versions
- **Automated Releases**: Semantic versioning and automated publishing to JetBrains Marketplace

## 📋 Requirements

- **JetBrains Rider**: 2023.2 or later
- **Java**: JDK 17 or later
- **O3DE**: Version 23.05 or later
- **Operating System**: Windows 10/11, macOS 10.15+, or Linux (Ubuntu 20.04+)

## 📦 Installation

### From JetBrains Marketplace

1. Open JetBrains Rider
2. Go to `File` → `Settings` → `Plugins`
3. Search for "O3DE"
4. Click `Install`

### Manual Installation

1. Download the latest release from [GitHub Releases](https://github.com/your-org/o3de-rider-plugin/releases)
2. In Rider, go to `File` → `Settings` → `Plugins`
3. Click the gear icon and select `Install Plugin from Disk...`
4. Select the downloaded `.zip` file

### Development Installation

1. Clone this repository to your local machine
2. Open the project in JetBrains Rider
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

## 🛠️ Development Setup

### Prerequisites

1. Install [JDK 17](https://adoptium.net/) or later
2. Install [JetBrains Rider](https://www.jetbrains.com/rider/)
3. Install [O3DE](https://o3de.org/download/)

### Building the Plugin

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-org/o3de-rider-plugin.git
   cd o3de-rider-plugin
   ```

2. **Build the plugin**:
   ```bash
   ./gradlew buildPlugin
   ```

3. **Run tests**:
   ```bash
   ./gradlew test
   ```

4. **Run code quality checks**:
   ```bash
   ./gradlew detekt
   ```

### Development Workflow

1. **Run the plugin in development mode**:
   ```bash
   ./gradlew runIde
   ```

2. **Debug the plugin**:
   ```bash
   ./gradlew runIde --debug-jvm
   ```

3. **Generate distribution**:
   ```bash
   ./gradlew buildPlugin
   ```

## 🧪 Testing

The project uses a comprehensive testing strategy:

### Unit Tests
```bash
./gradlew test
```

### Integration Tests
```bash
./gradlew integrationTest
```

### Test Coverage
```bash
./gradlew jacocoTestReport
```

## 📊 Code Quality

We maintain high code quality standards:

- **Static Analysis**: Detekt for Kotlin code analysis
- **Security Scanning**: Trivy for vulnerability detection
- **Test Coverage**: JaCoCo for coverage reporting
- **Code Formatting**: EditorConfig and Kotlin coding conventions

### Running Quality Checks

```bash
# Run all quality checks
./gradlew check

# Run static analysis
./gradlew detekt

# Run security scan
./gradlew securityScan

# Generate coverage report
./gradlew jacocoTestReport
```

## 🏗️ Project Structure

```
o3de-rider-plugin/
├── .github/                    # GitHub Actions workflows
│   ├── workflows/
│   │   ├── build.yml          # Main CI/CD pipeline
│   │   └── release.yml        # Release automation
│   ├── ISSUE_TEMPLATE/        # Issue templates
│   ├── PULL_REQUEST_TEMPLATE/ # PR templates
│   └── dependabot.yml         # Dependency updates
├── gradle/                     # Gradle configuration
│   ├── libs.versions.toml     # Version catalog
│   └── wrapper/               # Gradle wrapper
├── src/
│   ├── main/
│   │   ├── kotlin/            # Plugin source code
│   │   │   └── com/o3de/
│   │   │       ├── actions/   # IDE actions
│   │   │       ├── services/  # Core services
│   │   │       ├── ui/        # User interface
│   │   │       └── utils/     # Utilities
│   │   └── resources/         # Plugin resources
│   │       ├── META-INF/
│   │       │   └── plugin.xml # Plugin descriptor
│   │       └── icons/         # Plugin icons
│   └── test/
│       ├── kotlin/            # Test source code
│       │   ├── com/o3de/
│       │   │   ├── services/  # Service tests
│       │   │   └── ui/        # UI tests
│       │   └── testutil/      # Test utilities
│       └── resources/         # Test resources
├── docs/                      # Documentation
├── build.gradle.kts           # Build configuration
├── settings.gradle.kts        # Gradle settings
├── gradle.properties          # Gradle properties
├── .editorconfig             # Editor configuration
├── .gitignore                # Git ignore rules
├── CHANGELOG.md              # Version history
├── CONTRIBUTING.md           # Contribution guidelines
├── SECURITY.md               # Security policy
├── CODE_QUALITY_GUIDE.md     # Code quality guidelines
└── README.md                 # This file
```

## 🚀 CI/CD Pipeline

Our GitHub Actions pipeline includes:

- **Continuous Integration**: Automated testing on multiple Java versions
- **Code Quality**: Static analysis and security scanning
- **Compatibility Testing**: Testing against multiple Rider versions
- **Automated Releases**: Semantic versioning and automated publishing
- **Dependency Management**: Automated dependency updates via Dependabot

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

### Quick Start for Contributors

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes and add tests
4. Run quality checks: `./gradlew check`
5. Commit your changes: `git commit -m 'Add amazing feature'`
6. Push to the branch: `git push origin feature/amazing-feature`
7. Open a Pull Request

## 📝 Documentation

- [Code Quality Guide](CODE_QUALITY_GUIDE.md)
- [Contributing Guidelines](CONTRIBUTING.md)
- [Security Policy](SECURITY.md)
- [Changelog](CHANGELOG.md)

## 🐛 Bug Reports

Please use our [Bug Report Template](.github/ISSUE_TEMPLATE/bug_report.yml) when reporting issues.

## 💡 Feature Requests

We'd love to hear your ideas! Use our [Feature Request Template](.github/ISSUE_TEMPLATE/feature_request.yml).

## 🔒 Security

For security vulnerabilities, please see our [Security Policy](SECURITY.md).

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 📞 Support

- **Documentation**: [Plugin Wiki](https://github.com/your-org/o3de-rider-plugin/wiki)
- **Discussions**: [GitHub Discussions](https://github.com/your-org/o3de-rider-plugin/discussions)
- **Issues**: [GitHub Issues](https://github.com/your-org/o3de-rider-plugin/issues)
- **O3DE Community**: [O3DE Discord](https://discord.gg/o3de)

## 🙏 Acknowledgments

- [Open 3D Engine](https://o3de.org/) team for the amazing game engine
- [JetBrains](https://www.jetbrains.com/) for the excellent IDE platform
- O3DE Community for the excellent documentation
- All contributors who help make this plugin better

---

**Made with ❤️ for the O3DE community**