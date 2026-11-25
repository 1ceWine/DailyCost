# 物尽其用 DailyCost

一个离线优先的 Android 物品日均成本记账 App：记下每件东西的买入价、预期使用天数，App 帮你算出它「每天花你多少钱」，二手卖出后自动算回本进度和「赚了多少」。

## 下载安装

前往 [Releases 页面](https://github.com/1ceWine/DailyCost/releases/latest) 下载最新的 APK，安装即可。

> 安装时系统可能提示「未知来源应用」，允许后正常安装。App 无任何网络权限，数据只存在手机本地。

## 功能

- **记一笔**：名称、价格、购买日期、分类、预期使用天数、二手价
- **每天成本**：按 `净成本 ÷ 剩余预期天数` 实时折算，首页汇总总投入与全体日均
- **回本进度**：按 `赚的钱 : 净成本` 真实比例的单条双段进度条，回本后显示赚了多少
- **退环境**：物品卖出/淘汰时填二手回收价，单件自动重算净投入（总投入保持累计买入口径不变，另示「已回收 / 净投入」）
- **明细页**：按分类分组、搜索、排序（日均 / 价格 / 时间 / 名称）、批量改分类、批量删除、视图筛选（显示状态 / 回本状态 / 分类）
- **统计**：投入走势折线图、分类占比、月度对比，全部图表支持触摸读数
- **深色模式**：跟随系统或手动切换，主题色可换
- **备份**：一键导出 / 导入 JSON，数据完全本地，不上传任何服务器

## 技术栈

- 原生 Android 壳（Kotlin）+ WebView 加载本地 `assets/www`
- 前端：Vue 3（离线打包，无构建步骤）+ 原生 CSS，全部资源自适应屏幕尺寸
- 数据：`SharedPreferences` JSON 序列化，结构向后兼容
- 图标：Y2K 像素金币，代码逐格绘制，附完整 adaptive icon 分层

## 构建

```bash
# 需要 JDK 17 与 Android SDK
./gradlew assembleRelease
```

签名文件（`signing/`、`keystore.properties`）不含在仓库中，自用构建可用 debug 签名：`./gradlew assembleDebug`。

## 项目结构

```
app/src/main/
├── java/...        # MainActivity、备份导入导出桥接（Kotlin）
├── assets/www/     # 前端：index.html / app.js / style.css（Vue 3 离线）
└── res/            # 图标、adaptive icon、主题资源
```

## License

[MIT](LICENSE)
