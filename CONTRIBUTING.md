# Contributing to O3DE Rider Plugin

Thank you for your interest in contributing to the O3DE Rider Plugin! This document provides guidelines and information for contributors.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Contributing Process](#contributing-process)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)
- [Issue Guidelines](#issue-guidelines)
- [Pull Request Process](#pull-request-process)
- [Release Process](#release-process)
- [Community](#community)

## Code of Conduct

This project adheres to the [O3DE Code of Conduct](https://github.com/o3de/o3de/blob/development/CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

## Getting Started

### Prerequisites

- **Java 17 or later**
- **JetBrains Rider 2023.1 or later**
- **O3DE Engine** (latest stable version recommended)
- **Git**
- **Gradle 8.4 or later**

### Development Environment

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/o3de-rider-plugin.git
   cd o3de-rider-plugin
   ```
3. **Set up the upstream remote**:
   ```bash
   git remote add upstream https://github.com/o3de/o3de-rider-plugin.git
   ```

## Development Setup

### Building the Plugin

```bash
# Build the plugin
./gradlew buildPlugin

# Run tests
./gradlew test

# Run code quality checks
./gradlew detekt

# Verify plugin compatibility
./gradlew verifyPlugin
```

### Running in Development

```bash
# Run Rider with the plugin in development mode
./gradlew runIde
```

### IDE Setup

1. **Import the project** into IntelliJ IDEA or JetBrains Rider
2. **Configure the SDK** to use Java 17
3. **Install recommended plugins**:
   - Kotlin
   - Gradle
   - Detekt

## Contributing Process

### 1. Choose an Issue

- Look for issues labeled `good first issue` for beginners
- Check issues labeled `help wanted` for areas needing contribution
- Comment on the issue to indicate you're working on it

### 2. Create a Branch

```bash
# Create and switch to a new branch
git checkout -b feature/your-feature-name
# or
git checkout -b fix/issue-number-description
```

### 3. Make Changes

- Follow the [coding standards](#coding-standards)
- Write tests for new functionality
- Update documentation as needed
- Ensure all tests pass

### 4. Commit Changes

```bash
# Stage your changes
git add .

# Commit with a descriptive message
git commit -m "feat: add new O3DE component creation dialog"
```

#### Commit Message Format

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks
- `ci`: CI/CD changes

**Examples:**
```
feat(ui): add gem management dialog
fix(services): resolve O3DE CLI command timeout
docs: update installation instructions
test(actions): add unit tests for O3DE actions
```

### 5. Push and Create PR

```bash
# Push your branch
git push origin feature/your-feature-name

# Create a pull request on GitHub
```

## Coding Standards

### Kotlin Style Guide

- Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions small and focused
- Use data classes for simple data containers

### Code Organization

```
src/main/kotlin/com/o3de/
├── actions/          # User actions and menu items
├── services/         # Core services and business logic
├── ui/
│   ├── dialogs/      # Dialog implementations
│   └── components/   # Reusable UI components
├── toolwindow/       # Tool window implementations
└── utils/            # Utility classes and extensions
```

### Naming Conventions

- **Classes**: PascalCase (`CreateGemDialog`)
- **Functions**: camelCase (`createNewGem`)
- **Constants**: UPPER_SNAKE_CASE (`DEFAULT_TIMEOUT`)
- **Packages**: lowercase (`com.o3de.services`)

### Error Handling

- Use proper exception handling
- Provide meaningful error messages
- Log errors appropriately
- Don't expose sensitive information in error messages

## Testing Guidelines

### Test Structure

```
src/test/kotlin/com/o3de/
├── actions/          # Action tests
├── services/         # Service tests
├── ui/               # UI component tests
└── utils/            # Utility tests
```

### Writing Tests

- Write unit tests for all new functionality
- Use descriptive test names
- Follow the AAA pattern (Arrange, Act, Assert)
- Mock external dependencies
- Test both success and failure scenarios

### Test Example

```kotlin
class O3DEConfigServiceTest {
    @Test
    fun `should detect O3DE installation when valid path provided`() {
        // Arrange
        val validPath = "/path/to/o3de"
        val service = O3DEConfigService()
        
        // Act
        val result = service.isValidO3DEPath(validPath)
        
        // Assert
        assertTrue(result)
    }
}
```

## Documentation

### Code Documentation

- Add KDoc comments for public classes and functions
- Include parameter descriptions and return value information
- Provide usage examples for complex APIs

### User Documentation

- Update README.md for new features
- Add screenshots for UI changes
- Update CHANGELOG.md for all changes

## Issue Guidelines

### Reporting Bugs

- Use the bug report template
- Provide detailed reproduction steps
- Include environment information
- Attach relevant logs or screenshots

### Requesting Features

- Use the feature request template
- Explain the use case and benefits
- Consider implementation complexity
- Discuss with maintainers before starting work

## Pull Request Process

### Before Submitting

- [ ] All tests pass locally
- [ ] Code follows style guidelines
- [ ] Documentation is updated
- [ ] CHANGELOG.md is updated
- [ ] Commit messages follow conventions

### PR Requirements

- [ ] Descriptive title and description
- [ ] Links to related issues
- [ ] Screenshots for UI changes
- [ ] Test coverage for new code
- [ ] No merge conflicts

### Review Process

1. **Automated Checks**: CI/CD pipeline runs automatically
2. **Code Review**: Maintainers review the code
3. **Testing**: Manual testing if needed
4. **Approval**: At least one maintainer approval required
5. **Merge**: Squash and merge to main branch

## Release Process

### Version Numbering

We follow [Semantic Versioning](https://semver.org/):

- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Release Steps

1. Update version in `build.gradle.kts`
2. Update CHANGELOG.md
3. Create release PR
4. Tag release after merge
5. GitHub Actions handles the rest

## Community

### Communication Channels

- **GitHub Discussions**: General questions and ideas
- **GitHub Issues**: Bug reports and feature requests
- **O3DE Discord**: Real-time community chat
- **Email**: For security issues or private matters

### Getting Help

- Check existing documentation
- Search closed issues
- Ask in GitHub Discussions
- Join the O3DE Discord community

### Recognition

Contributors are recognized in:

- CHANGELOG.md for significant contributions
- GitHub contributor graphs
- Release notes for major features
- Special mentions in project updates

## Development Tips

### Debugging

- Use the IntelliJ debugger with `runIde` task
- Enable plugin development mode in Rider
- Check the IDE logs for errors
- Use logging statements for complex logic

### Performance

- Avoid blocking the UI thread
- Use background tasks for long operations
- Cache expensive computations
- Profile memory usage for large operations

### UI/UX

- Follow JetBrains UI guidelines
- Test with different themes (light/dark)
- Ensure accessibility compliance
- Test on different screen sizes

## Questions?

If you have questions about contributing, please:

1. Check this document first
2. Search existing issues and discussions
3. Create a new discussion if needed
4. Contact maintainers directly for urgent matters

Thank you for contributing to the O3DE Rider Plugin! 🚀