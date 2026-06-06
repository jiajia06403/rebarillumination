# Rebar Illumination

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-blue.svg)](https://www.minecraft.net/)

---

## 📖 中文

Rebar Illumination 是 Rebar 框架的一个附属组件，为 Minecraft 提供丰富的彩色照明系统。

### ✨ 功能特性

- **5 种灯类型**：方块灯、柱状灯、球形灯、壁灯、条状灯
- **16 种颜色**：支持 Minecraft 全部 16 种染料颜色
- **交互式控制**：潜行 + 右键点击切换灯的开关状态
- **故障模式**：灯具有故障模式，可以使用荧光粉解除
- **研究系统**：集成 Rebar 的研究系统

### 📦 安装

1. 确保服务器已安装 [Rebar](https://github.com/pylonmc/rebar) 和 [Pylon](https://github.com/pylonmc/pylon)
2. 将 `RebarIllumination.jar` 放入服务器的 `plugins` 目录
3. 重启服务器

### 🛠️ 配方

#### 荧光粉（Illumar）
```
空  | 红石 | 空
空  | 玻璃 | 空
染料 | 空   | 空
```

#### 方块灯
```
荧光粉 | 荧光粉 | 荧光粉
荧光粉 | 玻璃   | 荧光粉
荧光粉 | 荧光粉 | 荧光粉
```

### 🎮 使用方法

- **放置灯**：手持灯右键放置
- **切换开关**：潜行 + 右键点击已放置的灯
- **解除故障模式**：手持荧光粉右键点击故障的灯

### 📁 项目结构

```
RebarIllumination/
├── src/main/
│   ├── kotlin/io/github/rebarillumination/
│   │   ├── block/          # 方块定义
│   │   ├── item/           # 物品定义
│   │   └── util/           # 工具类
│   └── resources/
│       ├── lang/           # 语言文件
│       ├── settings/       # 配置文件
│       └── plugin.yml      # 插件描述
├── build.gradle.kts        # Gradle 构建配置
└── gradle.properties       # 项目属性
```

### 📝 配置

插件配置文件位于 `plugins/Rebar/settings/rebarillumination/` 目录下。

### 📜 许可证

本项目采用 MIT 许可证，详见 [LICENSE](LICENSE) 文件。

---

[English Version](README.en.md)