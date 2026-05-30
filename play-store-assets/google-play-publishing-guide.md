# Google Play 上架完整操作指南
# Woodworking Calculator Pro

> 按此文档顺序操作，不要跳步骤。预计总耗时：**3–5 小时**（首次上架）。

---

## 前置准备清单

在打开任何网页之前，确保以下全部就绪：

- [ ] Google 账号（个人或公司均可）
- [ ] 一次性注册费：**US $25**（信用卡或 PayPal）
- [ ] 已生成正式签名的 AAB 文件（见步骤 0）
- [ ] 隐私政策公开 URL（见步骤 1）

---

## 步骤 0：构建发布 AAB

在 Android Studio 或终端操作，选一种：

### 方法 A — Android Studio（推荐首次）
1. 菜单 **Build → Generate Signed App Bundle / APK**
2. 选 **Android App Bundle**
3. **创建新 keystore**（如果没有）：
   - 路径：选一个**安全位置**并备份，例如 `~/keystores/wcp-release.jks`
   - 记下密码和别名——**丢失 keystore = 永远无法更新 App**
4. **Build Variants** 选 `release`
5. 等待构建完成，记下 AAB 输出路径（通常在 `app/release/app-release.aab`）

### 方法 B — 命令行
```bash
./gradlew bundleRelease \
  -Pandroid.injected.signing.store.file=/path/to/wcp-release.jks \
  -Pandroid.injected.signing.store.password=YOUR_STORE_PW \
  -Pandroid.injected.signing.key.alias=YOUR_KEY_ALIAS \
  -Pandroid.injected.signing.key.password=YOUR_KEY_PW
```
输出：`app/build/outputs/bundle/release/app-release.aab`

### 关于 Play App Signing（强烈推荐开启）
首次上传时，Google 会提示"使用 Play 应用签名"。**请接受**：
- Google 持有最终签名密钥，你的本地 keystore 只作为上传密钥
- 你的 keystore 丢失后仍可继续更新 App
- 对用户无感知

---

## 步骤 1：发布隐私政策（获得公开 URL）

隐私政策文件已写好，位置：`docs/privacy-policy.html`

### 通过 GitHub Pages 发布（免费，推荐）

1. 确保代码已推送到 GitHub，仓库默认分支是 `main`
2. **推送本次改动**（包含 `.github/workflows/pages.yml` 和 `docs/index.html`）：
   ```bash
   git add .
   git commit -m "chore: add GitHub Pages deployment and updated store assets"
   git push
   ```
3. 打开 GitHub 仓库页面 → **Settings → Pages**
4. **Source** 选 `GitHub Actions`（不要选 Deploy from a branch）
5. 等待 Actions 自动运行（约 1 分钟）
6. 完成后，**Privacy Policy 公开 URL** 为：
   ```
   https://YOUR_GITHUB_USERNAME.github.io/YOUR_REPO_NAME/privacy-policy.html
   ```
   例如：`https://johndoe.github.io/Woodworking-Calculator-Pro/privacy-policy.html`

7. **在浏览器验证**这个 URL 能正常打开，再继续后续步骤

> 如果你的 GitHub 仓库是私有仓库，GitHub Pages 需要 Pro 账号才能用于私有仓库。
> 替代方案：把 `docs/privacy-policy.html` 上传到任意静态托管（如 Netlify、Vercel、Cloudflare Pages）。

---

## 步骤 2：注册 Google Play Console

1. 打开 [play.google.com/console](https://play.google.com/console)
2. 用你的 Google 账号登录
3. 支付 **$25 注册费**（一次性，终身有效）
4. 填写开发者信息：
   - **账号名称**：显示在 Play 商店上的开发者名字，认真填，审核后改名很麻烦
   - **联系邮箱**：用户和 Google 都能看到，用正式邮箱
   - **电话**：Google 验证用，不对外显示

---

## 步骤 3：创建应用

1. Console 首页 → **创建应用**
2. 填写：
   | 字段 | 值 |
   |------|-----|
   | 默认语言 | English (United States) |
   | 应用名称 | `Woodworking Calculator Pro` |
   | 应用类型 | 应用（App） |
   | 免费还是付费 | **免费**（有 IAP，但下载本身免费） |
3. 勾选开发者政策声明 → **创建应用**

---

## 步骤 4：配置商店信息（Listing）

进入 **主页 → 商店展示 → 主要商店列表**

### 4.1 文字内容

复制 `play-store-assets/listing-copy.md` 中对应的字段：

| 字段 | 内容 |
|------|------|
| **应用名称** | `Woodworking Calculator Pro` |
| **简短说明** (80字符) | `Offline workshop calculators — board feet, cut lists, stairs, miter & more.` |
| **完整说明** (4000字符) | 复制 listing-copy.md 中 Full Description 部分 |

### 4.2 图片素材上传

所有图片在 `play-store-assets/` 目录：

| Play Console 字段 | 文件名 | 规格要求 |
|------------------|--------|----------|
| **应用图标** | `icon-512.png` | 512×512 px, PNG, 无透明度 |
| **特色图片** | `feature-graphic-1024x500.png` | 1024×500 px, JPG/PNG |
| **手机截图 1** | `phone-01-home-1080x1920.png` | ≥320px 宽, ≤3840px, 比例 9:16 到 1:2 |
| **手机截图 2** | `phone-02-board-optimizer-1080x1920.png` | 同上 |
| **手机截图 3** | `phone-03-board-feet-1080x1920.png` | 同上 |
| **手机截图 4** | `phone-04-equal-spacing-1080x1920.png` | 同上 |
| **手机截图 5** | `phone-05-privacy-history-1080x1920.png` | 同上 |
| **手机截图 6** | `phone-06-sheet-optimizer-1080x1920.png` | 同上 |
| **手机截图 7** | `phone-07-paywall-1080x1920.png` | 同上 |

> Play Store 要求最少 2 张、最多 8 张截图。7 张全上即可。

### 4.3 分类与联系信息

| 字段 | 值 |
|------|-----|
| **应用类别** | 工具（Tools） |
| **标签** | Construction calculator · Carpentry · Woodworking |
| **邮箱地址** | 你的支持邮箱 |
| **隐私政策** | 步骤 1 获得的 GitHub Pages URL |

---

## 步骤 5：设置内容分级

**主页 → 政策 → 应用内容 → 内容分级**

1. 点击"开始问卷"
2. 类别选 **实用工具（Utility）**
3. 全部问题选"否"（App 不含暴力、色情、赌博等内容）
4. 提交，系统会自动评定为 **所有年龄段（Everyone）**

---

## 步骤 6：完成数据安全申报

**主页 → 政策 → 应用内容 → 数据安全**

按以下回答（与你的隐私政策一致）：

| 问题 | 回答 |
|------|------|
| 您的应用是否收集或分享用户数据？ | **否** |
| 您的应用是否使用加密传输数据？ | 不适用（不传输） |
| 用户是否可以要求删除数据？ | **是**（卸载 App 即删除） |
| 应用是否包含广告？ | **否** |

关于 **应用内购买**：
- 这里填写"是，包含应用内购买"
- 说明：一次性解锁（非消耗型）

---

## 步骤 7：创建 Pro IAP 商品

**主页 → 盈利 → 应用内购商品 → 管理商品**

1. 点击 **创建商品**
2. 填写：
   | 字段 | 值 |
   |------|-----|
   | 商品 ID | `wcp_pro_unlock` |
   | 商品类型 | **托管型商品（Managed product）** |
   | 名称 | `Pro Unlock` |
   | 说明 | `Unlocks all 9 Pro calculators. One-time purchase.` |
3. **设置价格**：
   - 美国默认价格：**$4.99**
   - 点击"转换所有国家/地区的价格" → 选模板 → 按汇率自动换算
   - 手动检查：英国 £3.99 · 欧元区 €4.99 · 澳大利亚 A$7.99 · 日本 ¥600 · 印度 ₹399
4. 状态设为 **有效（Active）**
5. 保存

> ⚠️ 商品 ID 一旦保存**不能修改**。必须与代码里的 `BillingManager.PRO_PRODUCT_ID = "wcp_pro_unlock"` 完全一致。

---

## 步骤 8：上传 AAB 到正式版轨道

**主页 → 发布 → 正式版**

1. 点击 **创建新发布版本**
2. 如果提示"使用 Play 应用签名"→ **接受**（强烈推荐）
3. 上传 AAB 文件：`app/release/app-release.aab`
4. 填写发布说明（用户可见的更新内容）：
   ```
   en-US:
   First release — 4 free tools + 9 Pro tools with one-time unlock.
   Offline. No ads. No tracking.
   ```
5. **版本说明** 建议同时填中文（如果你打算面向中文市场）

---

## 步骤 9：配置上架国家

**主页 → 发布 → 正式版 → 国家/地区**

按 `play-store-assets/launch-countries.md` 中的 Phase 1 列表勾选：

Phase 1（立即上架，15 个国家）：
- 🇺🇸 美国 · 🇨🇦 加拿大 · 🇬🇧 英国 · 🇮🇪 爱尔兰 · 🇦🇺 澳大利亚 · 🇳🇿 新西兰
- 🇩🇪 德国 · 🇳🇱 荷兰 · 🇧🇪 比利时 · 🇦🇹 奥地利 · 🇨🇭 瑞士
- 🇸🇪 瑞典 · 🇳🇴 挪威 · 🇩🇰 丹麦 · 🇫🇮 芬兰

---

## 步骤 10：提交审核

1. 回到正式版发布页面
2. 检查所有模块的警告，修复 ❌ 错误（⚠️ 警告可忽略）
3. 点击 **发送以供审核**
4. 等待 Google 审核：通常 **1–3 个工作日**，首次上架有时需要 7 天

**常见驳回原因及预防：**

| 驳回原因 | 预防措施 |
|----------|----------|
| 隐私政策 URL 无法访问 | 提交前用无痕浏览器验证 URL 可以打开 |
| 截图与 App 实际功能不符 | 不要在截图上添加夸大功能的文字 |
| 商品价格高于允许范围 | $4.99 在所有目标市场均允许 |
| 目标 API 不符合要求 | `targetSdk = 35`（已在 build.gradle.kts 配置） |
| INTERNET 权限缺失但有 IAP | Play Billing 通过 IPC 工作，不需要 INTERNET 权限，这是正常的 |

---

## 步骤 11：审核通过后

1. App 在各国商店上线后，**发邮件给 5–10 个真实用户**请求评价
2. 头两周每天登录 Console 检查：
   - **崩溃率**（Android Vitals → 崩溃率 < 1.09% 是基线）
   - **ANR 率**（< 0.47%）
   - **卸载率**（首日卸载 > 40% 说明首次使用体验有问题）
3. 收到 Pro 购买后，在 Console → 盈利 → 收益 确认到账
4. **一个月后**：按 `launch-countries.md` Phase 2 扩展国家

---

## 资产汇总清单

| 文件 | 状态 | 说明 |
|------|------|------|
| `icon-512.png` | ✅ 已有 | 512×512 |
| `feature-graphic-1024x500.png` | ✅ 已更新 | 移除"NO IAP"字样 |
| `phone-01-home-1080x1920.png` | ✅ 已有 | 主页截图 |
| `phone-02-board-optimizer-1080x1920.png` | ✅ 已有 | |
| `phone-03-board-feet-1080x1920.png` | ✅ 已有 | |
| `phone-04-equal-spacing-1080x1920.png` | ✅ 已有 | |
| `phone-05-privacy-history-1080x1920.png` | ✅ 已更新 | 移除"No in-app purchases" |
| `phone-06-sheet-optimizer-1080x1920.png` | ✅ 新生成 | 新 Pro 功能截图 |
| `phone-07-paywall-1080x1920.png` | ✅ 新生成 | Paywall 解锁页截图 |
| `docs/privacy-policy.html` | ✅ 已有 | 已更新为 freemium 版本 |
| 隐私政策公开 URL | ⏳ 推送后自动生成 | 见步骤 1 |
| 正式签名 AAB | ⏳ 你来构建 | 见步骤 0 |
| IAP 商品 `wcp_pro_unlock` | ⏳ 在 Console 创建 | 见步骤 7 |

---

## 快速参考：重要链接

- Google Play Console：https://play.google.com/console
- Play 政策中心：https://support.google.com/googleplay/android-developer/topic/9858052
- Play 计费库文档：https://developer.android.com/google/play/billing
- GitHub Pages 文档：https://docs.github.com/en/pages
- App Bundle 签名指南：https://developer.android.com/studio/publish/app-signing
