# NFC项目云编译指南

## 快速开始

### 方法1：使用Python脚本（推荐）
1. 确保安装了Python 3.x
2. 双击运行 `start_cloud_build.bat`
3. 选择选项1
4. 脚本将自动上传项目到GitHub
5. GitHub Actions将自动开始编译

### 方法2：手动上传到GitHub
1. 在GitHub创建新仓库：`nfc-manager-android`
2. 上传所有项目文件（排除`build/`等目录）
3. GitHub Actions将自动触发构建
4. 在Actions页面下载APK

## 云编译平台对比

### GitHub Actions（已配置）
- ✅ 免费
- ✅ 已配置工作流文件
- ✅ 自动构建、测试
- ✅ 支持Android SDK 34 + JDK 17

### GitLab CI/CD
- ✅ 免费私有仓库
- ✅ 强大的CI/CD功能
- ⚠️ 需要配置Runner

### Jenkins
- ✅ 高度可定制
- ✅ 支持复杂流水线
- ⚠️ 需要自建服务器

### 在线编译服务
- **AppCircle**: 免费计划，友好的UI
- **Bitrise**: 免费计划，移动应用专用
- **Codemagic**: Flutter专用，免费计划

## 已配置的GitHub Actions工作流

### 功能
1. **自动构建**：推送到main分支时自动构建
2. **测试运行**：执行单元测试
3. **APK生成**：生成debug APK
4. **工件上传**：APK和构建报告保存为工件

### 工作流程
```yaml
on: [push, pull_request]
jobs:
  build:
    - 设置JDK 17
    - 构建APK
    - 上传APK工件
  test:
    - 运行测试
```

## 项目编译配置

### Android配置
- **minSdk**: 26 (Android 8.0)
- **targetSdk**: 34
- **编译工具**: Gradle 8.x

### 依赖项
- Jetpack Compose (UI)
- Room (数据库)
- Material Design 3
- NFC API支持

## 常见问题

### Q: GitHub Actions构建失败
**可能原因**：
1. JDK版本不匹配 → 已配置JDK 17
2. Android SDK缺失 → 使用ubuntu-latest包含SDK
3. 内存不足 → 使用标准Runner

**解决方案**：
1. 检查Actions日志
2. 确保`gradle.properties`配置正确
3. 简化构建步骤

### Q: 如何生成签名APK？
修改`app/build.gradle`：
```gradle
android {
    signingConfigs {
        release {
            storeFile file("keystore.jks")
            storePassword System.getenv("STORE_PASSWORD")
            keyAlias System.getenv("KEY_ALIAS")
            keyPassword System.getenv("KEY_PASSWORD")
        }
    }
}
```

### Q: 如何添加环境变量？
在GitHub仓库设置中：
1. Settings → Secrets and variables → Actions
2. 添加`STORE_PASSWORD`、`KEY_ALIAS`等

## 性能优化建议

### 构建缓存
```yaml
# 在workflow中添加
- name: Gradle Cache
  uses: actions/cache@v3
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
```

### 并行构建
```yaml
jobs:
  build:
    strategy:
      matrix:
        variant: [debug, release]
```

## 监控和通知

### 构建状态
- 在README中添加徽章：
  ```
  ![Android CI](https://github.com/XACXF/nfc-manager-android/actions/workflows/android-build.yml/badge.svg)
  ```

### 通知配置
```yaml
# 在workflow中添加
- name: Notify Slack
  if: failure()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
```

## 安全注意事项

1. **Token安全**：不要将GitHub Token硬编码在代码中
2. **签名密钥**：使用环境变量存储密码
3. **依赖扫描**：启用Dependabot安全更新
4. **代码扫描**：启用CodeQL分析

## 扩展功能

### 多环境构建
```yaml
jobs:
  build:
    strategy:
      matrix:
        environment: [development, staging, production]
```

### 发布到测试平台
- Firebase App Distribution
- Microsoft App Center
- Google Play Internal Testing

## 支持与帮助

### 文档
- `COMPILE_GUIDE.md` - 完整编译指南
- `README.md` - 项目说明

### 故障排除
1. 检查GitHub Actions日志
2. 验证Gradle配置
3. 确保依赖项版本兼容

### 联系我们
- GitHub Issues: 报告问题
- 项目讨论: 功能建议