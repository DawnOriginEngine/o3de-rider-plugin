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

## [1.0.0] - TBD

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