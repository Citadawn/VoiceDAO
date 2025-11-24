# 应用签名与 GitHub Actions 配置指南

> 本文记录如何在本地和 GitHub Actions 中使用同一份 keystore 执行正式签名，方便后续维护或在项目重建后快速恢复。

## 1. 准备 keystore

1. 使用 Android Studio 或 `keytool` 生成正式 keystore，例如：
   
   ```bash
   keytool -genkeypair -v -keystore D:/DEV/tools/android-dev/Keystore/VoiceDAO_release_key.jks \
     -alias voicedao -keyalg RSA -keysize 2048 -validity 36500
   ```

2. 妥善保存 keystore 文件与以下信息：
   
   - keystore 路径（建议放在加密磁盘或安全目录）
   - store password
   - key alias
   - key password

3. 切勿将 `.jks` 提交到仓库。需要共享时，请通过安全渠道传输。

## 2. 本地构建配置

1. 将仓库根目录下的 `keystore.properties.sample` 复制为 `keystore.properties`（该文件已在 `.gitignore` 中忽略）。

2. 修改 `keystore.properties`，填入真实路径与密码。例如：
   
   ```
   VOICE_DAO_RELEASE_STORE_FILE=D:/DEV/tools/android-dev/Keystore/VoiceDAO_release_key.jks
   VOICE_DAO_RELEASE_STORE_PASSWORD=your-store-password
   VOICE_DAO_RELEASE_KEY_ALIAS=voicedao
   VOICE_DAO_RELEASE_KEY_PASSWORD=your-key-password
   ```

3. 之后无论使用 Android Studio 的 “Generate Signed Bundle/APK” 还是命令行 `./gradlew assembleRelease`，都会自动读取该配置并使用正式签名。

## 3. GitHub Actions 配置

Actions 运行在远端 runner，需要通过 Secrets 注入 keystore 与密码。

1. **Base64 化 keystore**
   
   - PowerShell：
     
     ```powershell
     [Convert]::ToBase64String([IO.File]::ReadAllBytes("D:\path\VoiceDAO_release_key.jks")) > VoiceDAO_release_key.jks.b64
     ```
   
   - macOS/Linux：
     
     ```bash
     base64 VoiceDAO_release_key.jks > VoiceDAO_release_key.jks.b64
     ```
   
   - 打开 `.b64` 文件，复制整段 Base64 文本。

2. **创建 Secrets（Settings → Secrets and variables → Actions）**
   
   - `VOICE_DAO_RELEASE_KEYSTORE`：粘贴 Base64 字符串
   - `VOICE_DAO_RELEASE_STORE_PASSWORD`：store password
   - `VOICE_DAO_RELEASE_KEY_ALIAS`：key alias
   - `VOICE_DAO_RELEASE_KEY_PASSWORD`：key password

3. `android-ci.yml` 中的“Restore release keystore / Inject release signing secrets”步骤会：
   
   - 解码 Base64 到 `~/.android/voicedao-release.jks`
   - 将四个值写入临时 `gradle.properties`
   - 触发 `assembleRelease` 时自动使用正式 keystore

4. Secrets 仅在推送 `v*` tag 时读取，普通 push/PR 不会使用正式证书。如需在其他场景签名，可自行调整 workflow 的 `if` 条件。

## 4. 恢复与注意事项

- 若仓库被重建或 Secrets 丢失，可按本指南重新配置：准备 keystore → 生成 Base64 → 重新创建四个 Secrets。
- 永远不要修改或替换正式 keystore，一旦更换，用户端将无法升级已有安装包。
- 对于只需要调试的贡献者，可忽略 `keystore.properties`，他们仍可构建 debug 版本；正式发布由具备 keystore 的成员或 CI 完成。
- 定期在安全位置备份 `.jks` 与本指南，以备不时之需。
