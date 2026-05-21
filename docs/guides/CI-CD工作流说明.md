# CI/CD 工作流说明

本文档说明项目的 GitHub Actions 自动化构建、测试和发布流程。

## 触发条件

GitHub Actions 在以下情况下会自动运行：

- **Push 到 main 分支**：运行 Lint 与单元测试
- **Pull Request**：运行 Lint 与单元测试（合并前检查）
- **推送 tag（格式：`vX.Y.Z`）**：构建 Release APK 并创建 GitHub Release

## 工作流程详解

### Push/PR 触发时

当代码推送到 main 分支或创建 Pull Request 时，workflow 执行：

1. **环境设置**
   
   - 检出代码
   - 设置 JDK 17
   - 配置 Gradle 环境（启用缓存加速构建）

2. **代码质量检查**
   
   - 单元测试（`./gradlew test`）：执行 `src/test` 下的单元测试
   - Lint（`./gradlew lint`）：按 Android Lint 规则扫描资源、Manifest、API 用法等

3. **产物范围**
   
   - CI 不构建 APK；安装包在本机通过 `./gradlew assembleDebug` 或 `./gradlew assembleRelease` 生成

### 依赖管理

仓库**未包含** `.github/dependabot.yml`，不启用 Dependabot 自动升级 PR。Gradle 依赖与 GitHub Actions 版本由维护者按需手动更新。

### Tag 触发时（创建 Release）

当推送以 `v` 开头的 tag（如 `v1.0.0`）时，workflow 执行：

1. **环境设置**
   
   - 检出代码
   - 设置 JDK 17
   - 配置 Gradle 环境

2. **版本号处理**
   
   - 验证 tag 格式（必须是 `vX.Y.Z`，如 `v1.2.3`）
   - 从 tag 提取版本号（`v1.2.3` → `1.2.3`）
   - 更新 `app/build.gradle` 中的 `versionName`
   - 按下方公式计算并更新 `versionCode`

3. **签名配置**
   
   - 从 GitHub Secrets 载入正式 keystore 到 runner
   - 将签名相关项写入 `gradle.properties`

4. **构建 Release APK**
   
   - 使用正式密钥执行 `assembleRelease`
   - 校验 APK 签名（须为正式密钥，非调试密钥）

5. **创建 GitHub Release**
   
   - 创建 GitHub Release
   - 附上 Release APK
   - 根据 commits 生成 Release notes
   - Release 标题格式：`VoiceDAO X.Y.Z`

Tag 流程仅产出 Release APK；正式包保存在 GitHub Release，不使用 Actions Artifacts。

## 版本管理

### 版本号格式

- **Tag 格式**：语义化版本 `vX.Y.Z`
  
  - `X`：主版本号
  - `Y`：次版本号
  - `Z`：修订版本号
  - 示例：`v1.0.0`、`v2.1.3`、`v1.2.0`

- **校验**：tag 不符合 `vX.Y.Z` 时，workflow 在步骤「Validate and extract version from tag」失败，不创建 GitHub Release

### 版本号自动同步

- **versionName**：从 tag 提取（`v1.2.3` → `1.2.3`）
- **versionCode**：按下列公式计算

### versionCode 计算公式

```
versionCode = 主版本 * 10000 + 次版本 * 100 + 修订版本
```

**示例**：

- `v1.0.0` → `10000`
- `v2.0.0` → `20000`
- `v1.2.3` → `10203`
- `v2.1.0` → `20100`

**约束**：

- 项目自定义公式；Android 仅要求 `versionCode` 为单调递增的整数
- `minor`、`patch` 宜小于 100，避免与相邻版本撞号

### 发版流程

1. 可选：在 `app/build.gradle` 中填写版本号（便于本地构建）
2. 提交并推送到 main
3. 创建并推送 tag：`git tag v1.2.3 && git push origin v1.2.3`
4. Actions 校验 tag、更新 `build.gradle`、构建并发布 Release

## 签名配置

Release APK 使用正式密钥签名，配置见 [应用签名与 GitHub Actions 配置指南](./应用签名与%20GitHub%20Actions%20配置指南.md)。

## 常见问题

### Q: Push/PR 的 CI 包含哪些步骤？

A: 仅 `clean`、`lint`、`test`。APK 在本机构建；正式发布通过 tag 触发 Release 构建。

### Q: 如何关闭 Lint / test？

A: 在 `.github/workflows/android-ci.yml` 中移除或调整 `Run unit tests and lint` 步骤。移除后 Push/PR 不再执行 Gradle 质量检查；tag 发版流程在 `assembleRelease` 前仍会执行该步骤（除非同步修改 tag 分支逻辑）。

### Q: tag 格式错误时有什么提示？

A: workflow 失败，不创建 GitHub Release。在 Actions 日志「Validate and extract version from tag」中查看中文提示，例如：

```text
❌ 错误：标签格式无效。要求格式：vX.Y.Z（例如 v1.2.3）
当前标签：……
解析出的版本号：……
说明：仅当标签为 v主版本.次版本.修订号 时才会构建 Release 并创建 GitHub Release。
```

须使用 `vX.Y.Z`（如 `v1.2.3`）。错误 tag 需先在 GitHub 或本地删除，再推送正确 tag。

### Q: 如何查看 workflow 日志？

A: 仓库 **Actions** 页 → 选择对应运行记录。

### Q: GitHub Actions 如何计费？

A: 按托管 runner 上的**运行分钟数**计费。公开仓库通常可免费使用（以 GitHub 当前政策为准）；私有仓库有每月免费分钟额度。本 workflow：Push/PR 仅 lint/test；tag 发版才构建 Release，用于控制单次运行时长。

### Q: Release APK 在哪里下载？

A: 仓库 **Releases** 页，附件为正式签名的 Release APK。

## 相关文档

- [应用签名与 GitHub Actions 配置指南](./应用签名与%20GitHub%20Actions%20配置指南.md)
- [文档索引](../文档索引.md)
