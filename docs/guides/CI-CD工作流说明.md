# CI/CD 工作流说明

本文档说明项目的 GitHub Actions 自动化构建、测试和发布流程。

## 触发条件

GitHub Actions 在以下情况下会自动运行：

- **Push 到 main 分支**：自动运行测试和构建
- **Pull Request**：自动运行测试和构建（用于代码审查）
- **推送 tag（格式：`vX.Y.Z`）**：自动构建 release APK 并创建 GitHub Release

## 工作流程详解

### Push/PR 触发时

当代码推送到 main 分支或创建 Pull Request 时，workflow 会执行以下步骤：

1. **环境设置**
   - 检出代码
   - 设置 JDK 17
   - 配置 Gradle 环境（启用缓存加速构建）

2. **代码质量检查**
   - 运行单元测试（`./gradlew test`）
   - 运行 Lint 检查（`./gradlew lint`）

3. **构建 Debug APK**
   - 构建 debug 版本的 APK（使用调试密钥签名）
   - 上传 debug APK 作为 artifact（保留 7 天，Dependabot PR 不上传）

**用途**：用于开发测试和代码审查验证

### Tag 触发时（创建 Release）

当推送以 `v` 开头的 tag（如 `v1.0.0`）时，workflow 会执行以下步骤：

1. **环境设置**
   - 检出代码
   - 设置 JDK 17
   - 配置 Gradle 环境

2. **版本号处理**
   - 验证 tag 格式（必须是 `vX.Y.Z`，如 `v1.2.3`）
   - 从 tag 自动提取版本号（`v1.2.3` → `1.2.3`）
   - 自动更新 `app/build.gradle` 中的 `versionName`
   - 自动计算并更新 `versionCode`（计算公式见下方）

3. **签名配置**
   - 从 GitHub Secrets 恢复正式 keystore
   - 注入签名配置到 `gradle.properties`

4. **构建 Release APK**
   - 使用正式密钥签名构建 release APK
   - 验证 APK 签名（确保使用正式密钥而非调试密钥）

5. **创建 GitHub Release**
   - 自动创建 GitHub Release
   - 上传 release APK 作为附件
   - 自动生成 Release notes（基于 commits）
   - 设置 Release 标题为 "VoiceDAO X.Y.Z" 格式

**注意**：Tag 触发时**不会**构建 debug APK，只构建 release APK。

## 版本管理

### 版本号格式

- **Tag 格式**：必须遵循语义化版本规范 `vX.Y.Z`
  - `X`：主版本号（重大变更）
  - `Y`：次版本号（新功能）
  - `Z`：修订版本号（bug 修复）
  - 示例：`v1.0.0`、`v2.1.3`、`v1.2.0`

- **验证规则**：如果 tag 格式不正确，构建会失败并提示错误

### 版本号自动同步

- **versionName**：从 tag 自动提取（`v1.2.3` → `1.2.3`）
- **versionCode**：根据版本号自动计算

### versionCode 计算公式

**本项目使用的公式**（自定义）：
```
versionCode = 主版本 * 10000 + 次版本 * 100 + 修订版本
```

**示例**：
- `v1.0.0` → `versionCode = 1 * 10000 + 0 * 100 + 0 = 10000`
- `v2.0.0` → `versionCode = 2 * 10000 + 0 * 100 + 0 = 20000`
- `v1.2.3` → `versionCode = 1 * 10000 + 2 * 100 + 3 = 10203`
- `v2.1.0` → `versionCode = 2 * 10000 + 1 * 100 + 0 = 20100`

**说明**：
- 这是**项目自定义**的计算方式，不是 Android 官方标准
- Android 的 `versionCode` 只是一个递增整数，没有官方计算公式
- 不同项目有不同的计算方式，常见的有：
  - **简单递增**：1, 2, 3, 4...（最简单，但无法从 versionCode 反推版本号）
  - **时间戳**：使用构建时间戳（如 `20240101`）
  - **版本号转换**：将版本号转换为数字（本项目采用此方式）
  - **其他公式**：如 `major*1000 + minor*100 + patch`（更紧凑，但限制更大）

### 版本号更新流程

1. 在 `app/build.gradle` 中手动更新版本号（可选，用于本地构建）
2. 提交代码并推送到 main 分支
3. 创建并推送 tag：`git tag v1.2.3 && git push origin v1.2.3`
4. GitHub Actions 自动：
   - 验证 tag 格式
   - 从 tag 提取版本号并更新 `build.gradle`
   - 构建并发布

## 签名配置

Release APK 使用正式密钥签名，配置说明请参考：

- [应用签名与 GitHub Actions 配置指南](./release-signing.md)

## 常见问题

### Q: 为什么 tag 触发时不构建 debug APK？

A: Release 是正式发布版本，只需要 release APK。Debug APK 仅用于开发和测试，在 Push/PR 时构建即可。

### Q: 如果 tag 格式错误会怎样？

A: 构建会失败，并显示错误信息提示正确的格式。必须使用 `vX.Y.Z` 格式（如 `v1.2.3`）。

### Q: 如何查看 workflow 执行日志？

A: 在 GitHub 仓库页面，点击 "Actions" 标签页，选择对应的 workflow 运行记录即可查看详细日志。

### Q: Artifacts 会保留多久？

A: Debug APK artifacts 保留 7 天。Release APK 会永久保存在 GitHub Release 中。

## 相关文档

- [应用签名与 GitHub Actions 配置指南](./release-signing.md) - 详细的签名配置和 Secrets 设置说明
- [文档索引](../文档索引.md) - 项目文档导航中心

