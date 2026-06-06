# Rebar Illumination

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.1.2-blue.svg)](https://www.minecraft.net/)
[![Rebar](https://img.shields.io/badge/Rebar-0.40.0--26.1-blue.svg)](https://github.com/pylonmc/rebar)
[![Pylon](https://img.shields.io/badge/Pylon-0.38.0--26.1-blue.svg)](https://github.com/pylonmc/pylon)

[中文版](README.md)

---

## 📖 English

Rebar Illumination is an addon for the Rebar framework that provides a rich colored lighting system for Minecraft.

### ✨ Features

- **5 Lamp Types**: Block Lamps, Pillar Lamps, Sphere Lamps, Wall Lamps, Bar Lamps
- **16 Colors**: Supports all 16 Minecraft dye colors
- **Interactive Control**: Sneak + Right Click to toggle lamp state
- **Faulty Mode**: Lamps can enter faulty mode, fixable with Illumar
- **Research System**: Integrated with Rebar's research system
- **Note**: All lamps require DynamicLight plugin or shader pack to emit light

### 📦 Installation

1. Ensure [Rebar](https://github.com/pylonmc/rebar) and [Pylon](https://github.com/pylonmc/pylon) are installed on your server
2. Place `RebarIllumination.jar` in your server's `plugins` directory
3. Restart your server

### 🛠️ Recipes

#### Illumar
```
空  | 红石 | 空
空  | 玻璃 | 空
染料 | 空   | 空
```

#### Block Lamp
```
荧光粉 | 荧光粉 | 荧光粉
荧光粉 | 玻璃   | 荧光粉
荧光粉 | 荧光粉 | 荧光粉
```

### 🎮 Usage

- **Place Lamp**: Right-click to place while holding the lamp
- **Toggle Lamp**: Sneak + Right-click on a placed lamp
- **Fix Faulty Mode**: Right-click a faulty lamp with Illumar

### 📁 Project Structure

```
RebarIllumination/
├── src/main/
│   ├── kotlin/io/github/rebarillumination/
│   │   ├── block/          # Block definitions
│   │   ├── item/           # Item definitions
│   │   └── util/           # Utility classes
│   └── resources/
│       ├── lang/           # Language files
│       ├── settings/       # Configuration files
│       └── plugin.yml      # Plugin description
├── build.gradle.kts        # Gradle build configuration
└── gradle.properties       # Project properties
```

### 📝 Configuration

Plugin configuration files are located in `plugins/Rebar/settings/rebarillumination/`.

### 📜 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.