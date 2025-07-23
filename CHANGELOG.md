# Changelog

All notable changes to the O3DE Rider Plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial release of O3DE Rider Plugin
- Integration with O3DE CLI commands
- Create Gem functionality
- Create Component functionality
- Create from Template functionality
- Gem Management (enable/disable gems)
- Engine Registration support
- Project Management features
- Dedicated O3DE Tool Window
- Context-sensitive actions
- Smart path detection
- Input validation for all dialogs
- Cross-platform compatibility (Windows, macOS, Linux)

### Features
- **Quick Creation Tools**:
  - Create new O3DE Gems with customizable templates
  - Generate components with automatic namespace detection
  - Create projects from O3DE templates
  
- **Project Management**:
  - Enable/disable gems in current project
  - Register O3DE engines
  - View registered projects and engines
  - Build project integration
  
- **Developer Experience**:
  - Intuitive dialog interfaces
  - Real-time input validation
  - Automatic path detection
  - Template selection support
  - Progress indicators for long-running operations

### Technical Details
- Built with Kotlin and IntelliJ Platform SDK
- Supports JetBrains Rider 2023.1 and later
- Integrates with O3DE CLI commands:
  - `o3de create-gem`
  - `o3de create-component`
  - `o3de create-from-template`
  - `o3de enable-gem` / `o3de disable-gem`
  - `o3de register`
  - `o3de build-project`

### Requirements
- JetBrains Rider 2023.1 or later
- O3DE Engine installed and configured
- Java 17 or later

## [1.0.2] - 2024-01-15

### Changed
- Enhanced CreateComponentDialog to use O3DE's create-from-template command
- Improved component creation with template variable replacement support
- Added "Keep restricted files" option for template processing
- Updated UI text and help documentation from Chinese to English

### Added
- Template variable replacement panel in CreateComponentDialog
- Support for custom template variables (GemName, ComponentName, COMPONENT_NAME)
- Enhanced createFromTemplate method with keepRestrictedFiles parameter
- Comprehensive template variable substitution functionality

### Fixed
- Component creation now properly follows O3DE CLI reference documentation
- Improved parameter ordering in create-from-template command execution
- Better error handling and user feedback for component creation

## [1.0.0] - 2024-01-01

### Added
- Initial stable release

---

## Release Notes Template

For future releases, use this template:

```markdown
## [X.Y.Z] - YYYY-MM-DD

### Added
- New features

### Changed
- Changes in existing functionality

### Deprecated
- Soon-to-be removed features

### Removed
- Removed features

### Fixed
- Bug fixes

### Security
- Security improvements
```