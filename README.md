# Rebar Illumination

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.1.2-blue.svg)](https://www.minecraft.net/)
[![Rebar](https://img.shields.io/badge/Rebar-0.40.0--26.1-blue.svg)](https://github.com/pylonmc/rebar)
[![Pylon](https://img.shields.io/badge/Pylon-0.38.0--26.1-blue.svg)](https://github.com/pylonmc/pylon)

[English Version](README.en.md)

---

## 📖 中文

Rebar Illumination 是 Rebar 框架的一个附属组件，为 Minecraft 提供丰富的彩色照明系统。

### ✨ 功能特性

- **5 种灯类型**：方块灯、柱状灯、球形灯、壁灯、条状灯
- **16 种颜色**：支持 Minecraft 全部 16 种染料颜色
- **交互式控制**：潜行 + 右键点击切换灯的开关状态
- **故障模式**：模拟故障的灯的随机闪烁效果
- **注意**：所有灯具需要安装 DynamicLight 插件或使用光影才能发光，并且不影响实际光照

### 📦 安装

1. 确保服务器已安装 [Rebar](https://github.com/pylonmc/rebar) 和 [Pylon](https://github.com/pylonmc/pylon)
2. 将 `RebarIllumination.jar` 放入服务器的 `plugins` 目录
3. 重启服务器

### 🎮 使用方法

- **合成灯**：先合成指定颜色的荧光石，再根据配方合成不同种类的灯
- **切换开关状态**：潜行 + 右键点击已放置的灯
- **开启故障模式**：手持燧石右键点击已放置的灯
- **解除故障模式**：手持对应颜色的荧光粉右键点击故障的灯

### 📝 配置

插件配置文件位于 `plugins/Rebar/settings/rebarillumination/` 目录下。

### 📜 许可证

本项目采用 MIT 许可证，详见 [LICENSE](LICENSE) 文件
