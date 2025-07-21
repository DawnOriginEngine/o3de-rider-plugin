# 构建说明

## 网络问题解决方案

如果遇到Gradle下载超时问题，请尝试以下解决方案：

### 方案1：手动下载Gradle
1. 访问 https://gradle.org/releases/
2. 下载 Gradle 8.4 二进制包 (gradle-8.4-bin.zip)
3. 将下载的文件放置到以下目录：
   ```
   %USERPROFILE%\.gradle\wrapper\dists\gradle-8.4-bin\<hash>\gradle-8.4-bin.zip
   ```
4. 重新运行构建命令：`./gradlew.bat buildPlugin`

### 方案2：使用代理或VPN
如果网络访问受限，请配置代理或使用VPN后重试。

### 方案3：使用本地Gradle安装
1. 安装Gradle到本地系统
2. 使用 `gradle buildPlugin` 代替 `./gradlew.bat buildPlugin`

## 构建命令

```bash
# Windows
.\gradlew.bat buildPlugin

# Linux/macOS
./gradlew buildPlugin
```

## 构建输出

成功构建后，插件文件将位于：
```
build/distributions/o3de-rider-plugin-1.0.0.zip
```

## 安装插件

1. 打开JetBrains Rider
2. 转到 File → Settings → Plugins
3. 点击齿轮图标 → Install Plugin from Disk
4. 选择生成的zip文件
5. 重启IDE