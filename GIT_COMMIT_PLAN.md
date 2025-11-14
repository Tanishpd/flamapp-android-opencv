# Git Commit Plan for FlamApp

This document outlines the recommended git workflow for the FlamApp project.

## Initial Setup

```bash
cd flamapp
git init
git branch -M main
```

## Commit Sequence

### 1. Initial Project Structure
```bash
git add README.md .gitignore
git commit -m "docs: add README and gitignore"
```

### 2. Add Build Configuration
```bash
git add build.gradle settings.gradle app/build.gradle app/proguard-rules.pro local.properties
git commit -m "build: add Gradle build configuration"
```

### 3. Add Android Manifest and Resources
```bash
git add app/src/main/AndroidManifest.xml
git add app/src/main/res/
git commit -m "feat: add Android manifest and resources"
```

### 4. Add Camera Activity
```bash
git add app/src/main/java/com/flamapp/CameraActivity.java
git commit -m "feat: implement Camera2 API for live preview"
```

### 5. Add OpenGL Renderer
```bash
git add app/src/main/java/com/flamapp/GLRenderer.java
git commit -m "feat: implement OpenGL ES 2.0 renderer"
```

### 6. Add JNI Bridge
```bash
git add app/src/main/java/com/flamapp/NativeProcessor.java
git commit -m "feat: add JNI bridge for native processing"
```

### 7. Add CMake Configuration
```bash
git add jni/CMakeLists.txt
git commit -m "build: add CMake configuration for native build"
```

### 8. Add Native Processor Header
```bash
git add jni/native_processor.h
git commit -m "feat: add native processor header"
```

### 9. Add Native Processor Implementation
```bash
git add jni/native_processor.cpp
git commit -m "feat: implement OpenCV processing (Canny, Grayscale)"
```

### 10. Add OpenGL Native Renderer
```bash
git add gl/renderer.h gl/renderer.cpp
git commit -m "feat: implement native OpenGL renderer"
```

### 11. Add GLSL Shaders
```bash
git add gl/shaders/vertex.glsl gl/shaders/fragment.glsl
git commit -m "feat: add OpenGL ES vertex and fragment shaders"
```

### 12. Add TypeScript Configuration
```bash
git add web/tsconfig.json web/package.json web/.gitignore
git commit -m "build: add TypeScript configuration for web viewer"
```

### 13. Add Web Viewer HTML
```bash
git add web/index.html
git commit -m "feat: add web viewer HTML interface"
```

### 14. Add Web Viewer TypeScript
```bash
git add web/src/main.ts
git commit -m "feat: implement TypeScript web viewer logic"
```

### 15. Add Web Documentation
```bash
git add web/README.md
git commit -m "docs: add web viewer documentation"
```

### 16. Add Setup Guide
```bash
git add SETUP.md
git commit -m "docs: add comprehensive setup guide"
```

### 17. Add Git Plan
```bash
git add GIT_COMMIT_PLAN.md
git commit -m "docs: add git commit plan"
```

## Complete Initial Commit (Alternative)

If you prefer a single initial commit:

```bash
git add .
git commit -m "feat: initial FlamApp project with Android, OpenCV C++, OpenGL ES, and TypeScript web viewer

- Android Camera2 API integration
- JNI bridge for native processing
- OpenCV C++ for image processing (Canny, Grayscale)
- OpenGL ES 2.0 rendering pipeline
- TypeScript web viewer for frame display
- Complete build configuration and documentation"
```

## Branch Strategy

### Feature Branches
```bash
# Create feature branch
git checkout -b feature/new-opencv-algorithm
# Make changes
git add .
git commit -m "feat: add new OpenCV algorithm"
# Merge to main
git checkout main
git merge feature/new-opencv-algorithm
```

### Hotfix Branches
```bash
git checkout -b hotfix/camera-crash
# Fix bug
git add .
git commit -m "fix: resolve camera crash on Android 11+"
git checkout main
git merge hotfix/camera-crash
```

## Commit Message Convention

Use conventional commits format:

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting)
- **refactor**: Code refactoring
- **perf**: Performance improvements
- **test**: Add or update tests
- **build**: Build system changes
- **ci**: CI/CD changes
- **chore**: Other changes

### Examples:
```bash
git commit -m "feat: add new color filter processing mode"
git commit -m "fix: resolve memory leak in frame processing"
git commit -m "docs: update setup instructions for macOS"
git commit -m "perf: optimize Canny edge detection parameters"
git commit -m "refactor: extract texture loading to separate class"
```

## Tagging Releases

```bash
# Tag version
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# List tags
git tag -l
```

## Remote Repository

```bash
# Add remote
git remote add origin https://github.com/yourusername/flamapp.git

# Push to remote
git push -u origin main

# Push all tags
git push --tags
```

## Daily Workflow

```bash
# Start of day - update from remote
git pull origin main

# Create feature branch
git checkout -b feature/my-feature

# Make changes and commit frequently
git add .
git commit -m "feat: add feature component"

# Push feature branch
git push -u origin feature/my-feature

# Create pull request on GitHub
# After review and approval, merge to main
```

## Useful Git Commands

```bash
# View commit history
git log --oneline --graph --all

# View changes
git diff

# Discard changes
git checkout -- filename

# Amend last commit
git commit --amend

# Stash changes
git stash
git stash pop

# Cherry-pick commit
git cherry-pick <commit-hash>

# Rebase interactive
git rebase -i HEAD~3
```

## Best Practices

1. **Commit Often**: Small, focused commits are easier to review
2. **Write Clear Messages**: Describe what and why, not how
3. **Test Before Commit**: Ensure code builds and runs
4. **Keep Commits Atomic**: One logical change per commit
5. **Use Branches**: Keep main branch stable
6. **Review Before Push**: Double-check what you're pushing
7. **Pull Before Push**: Stay in sync with team

## Pre-commit Checklist

- [ ] Code builds successfully
- [ ] Tests pass (if applicable)
- [ ] Code follows style guidelines
- [ ] No sensitive data (keys, passwords)
- [ ] Commit message follows convention
- [ ] Files staged are correct

---

**Remember**: Good git hygiene makes collaboration easier!
