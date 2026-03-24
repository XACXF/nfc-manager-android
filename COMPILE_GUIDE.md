# NFC信息读取与本地管理软件 - 编译指南

## 项目概述
这是一个用于读取、存储和管理NFC标签数据的Android应用程序。

## 编译方法

### 方法1：本地编译（需要Android开发环境）

#### 要求：
1. Android Studio Flamingo 或更高版本
2. Android SDK 33+
3. JDK 17+
4. Gradle

#### 步骤：
1. 使用Android Studio打开项目
2. 等待Gradle同步完成
3. 连接Android设备或创建模拟器
4. 点击运行按钮或执行：
   ```bash
   ./gradlew assembleDebug
   ```
5. APK文件将生成在：`app/build/outputs/apk/debug/app-debug.apk`

### 方法2：云端编译（使用GitHub Actions）

#### 步骤：
1. 将项目推送到GitHub：
   ```bash
   # 使用提供的脚本
   push_to_github.bat
   ```

2. GitHub Actions将自动开始编译：
   - 访问：https://github.com/XACXF/nfc-manager-android/actions
   - 查看编译状态

3. 下载编译后的APK：
   - 在Actions页面中，找到成功的构建
   - 点击"nfc-manager-apk"工件下载APK文件

### 方法3：使用在线编译服务

#### 选项A：AppCircle（免费）
1. 注册AppCircle账号
2. 导入GitHub仓库
3. 配置Android构建环境
4. 开始自动构建

#### 选项B：Bitrise（免费计划）
1. 注册Bitrise账号
2. 连接GitHub账号
3. 选择仓库并配置工作流
4. 触发构建

#### 选项C：GitLab CI/CD
1. 将项目导入GitLab
2. 使用共享Runner或配置自己的Runner
3. 使用提供的.gitlab-ci.yml配置文件

## 项目结构

```
NFCManager/
├── app/                    # Android应用模块
│   ├── src/main/          # 主源代码
│   ├── build.gradle       # 模块构建配置
│   └── proguard-rules.pro # 混淆规则
├── .github/workflows/     # GitHub Actions工作流
│   └── android-build.yml  # Android构建配置
├── build.gradle           # 项目构建配置
├── settings.gradle        # 项目设置
├── gradle.properties      # Gradle属性
├── gradlew                # Gradle包装器（Unix）
├── gradlew.bat            # Gradle包装器（Windows）
├── push_to_github.bat     # 推送脚本
└── README.md              # 项目说明
```

## 依赖项

项目使用以下主要依赖：
- Android SDK 34
- Jetpack Compose (UI框架)
- Room (数据库)
- Material Design 3
- Kotlin Coroutines

## 常见问题

### Q1：编译失败，提示缺少Android SDK
A：确保安装了Android SDK 33或更高版本，并配置了正确的SDK路径。

### Q2：GitHub Actions编译失败
A：检查工作流文件配置，确保JDK版本和Gradle配置正确。

### Q3：如何生成签名APK？
A：创建签名密钥并配置`build.gradle`：
```gradle
android {
    signingConfigs {
        release {
            storeFile file("your-keystore.jks")
            storePassword "your-password"
            keyAlias "your-alias"
            keyPassword "your-password"
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}
```

### Q4：如何测试NFC功能？
A：需要真实支持NFC的Android设备，或在模拟器中配置NFC模拟。

## 技术支持

如需帮助，请：
1. 查看GitHub Issues
2. 检查编译日志
3. 确保所有依赖项正确安装

## 许可证

本项目仅供学习和开发使用。