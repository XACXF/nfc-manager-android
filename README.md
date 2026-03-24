# NFC信息读取与本地管理软件

## 项目概述
一个用于读取、存储和管理NFC标签数据的Android应用程序。

## 功能特性

### 核心功能
1. **NFC标签读取**
   - 支持读取NDEF格式的NFC标签内容
   - 支持文本、URL、名片等多种数据类型
   - 自动识别标签类型并解析数据

2. **数据本地存储**
   - 保存到本地Room数据库
   - 每条记录包含：数据内容、读取时间、标签类型、自定义备注
   - 支持数据导出为TXT/CSV格式

3. **本地数据调用**
   - 列表展示所有保存的数据
   - 支持按时间、类型、关键词搜索
   - 模拟NFC标签触发对应功能
   - 一键复制数据到剪贴板

4. **数据管理**
   - 编辑、删除、分类管理
   - 数据备份与恢复功能
   - 自动清理规则设置

### 技术栈
- Android SDK 26+ (Android 8.0+)
- Kotlin
- Jetpack Compose (UI框架)
- Room (数据库)
- NFC API
- Material Design 3

## 项目结构
```
app/
├── src/main/
│   ├── java/com/nfcmanager/
│   │   ├── MainActivity.kt
│   │   ├── nfc/
│   │   │   ├── NFCManager.kt
│   │   │   └── NFCReader.kt
│   │   ├── data/
│   │   │   ├── database/
│   │   │   │   ├── AppDatabase.kt
│   │   │   │   └── entities/
│   │   │   ├── repository/
│   │   │   └── model/
│   │   ├── ui/
│   │   │   ├── screen/
│   │   │   │   ├── MainScreen.kt
│   │   │   │   ├── ReadScreen.kt
│   │   │   │   └── DataScreen.kt
│   │   │   └── component/
│   │   └── viewmodel/
└── res/
```

## 开发环境要求
- Android Studio Flamingo 或更高版本
- Android SDK 33+
- JDK 17+

## 构建说明

### 本地构建
1. 克隆项目
2. 使用Android Studio打开
3. 同步Gradle依赖
4. 连接Android设备或使用模拟器
5. 运行应用

### 云端构建（推荐）
项目已配置GitHub Actions自动构建：

1. **将项目推送到GitHub**
   ```bash
   # 运行推送脚本
   push_to_github.bat
   ```

2. **查看构建状态**
   - 访问：https://github.com/XACXF/nfc-manager-android/actions
   - GitHub Actions将自动构建APK

3. **下载APK文件**
   - 在Actions页面中找到成功的构建
   - 下载"nfc-manager-apk"工件

### 快速开始
1. 双击运行 `push_to_github.bat` 脚本
2. 使用您的GitHub Token推送项目
3. 等待GitHub Actions完成构建
4. 下载生成的APK文件到Android设备安装

## 云编译配置
项目包含：
- `.github/workflows/android-build.yml` - GitHub Actions工作流
- 自动构建、测试和打包
- 支持Android SDK 34和JDK 17