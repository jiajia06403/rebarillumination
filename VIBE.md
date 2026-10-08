# Rebar 升级迁移指南：0.42.0-26.1 → 0.44.4-26.2

> 本文档汇总将本插件（RebarIllumination）从 Rebar `0.42.0-26.1`（对应 Paper API 26.1）  
> 升级到最新版 `0.44.4-26.2`（对应 Paper API 26.2）所需完成的所有事项。  
> 所有“已修改”条目均已在仓库中实际改动完毕；“待评估”条目为行为/新特性变更，  
> 不影响编译但建议在升级后验证。

---

## 1. 升级概览

| 项目              | 升级前           | 升级后                                             |
| --------------- | ------------- | ----------------------------------------------- |
| Rebar 版本        | `0.42.0-26.1` | `0.44.4-26.2`（最新，2026-10-06 发布）                 |
| Paper / 服务端 API | `26.1.2`      | `26.2`                                          |
| Kotlin          | `2.3.0`       | `2.3.0`（不变）                                     |
| InvUI           | `2.1.0`       | `2.3.0`                                         |
| 依赖来源            | JitPack       | **Maven Central**（JitPack 对 0.44.x 构建失败，见 §2.3） |
| Java 工具链        | 25            | 25（不变）                                          |

版本后缀 `-26.1` / `-26.2` 表示 Rebar 编译所针对的 Minecraft/Paper 大版本。  
0.43.0 起 Rebar 整体迁移到 **26.2**（PR #887 `feat: update to 26.2`），因此本次升级  
同时是一次 **Paper 服务端大版本升级**，必须配套更新 `paper-api`、`api-version` 与运行时服务端。

---

## 2. 依赖与构建配置（已全部修改）

### 2.1 `gradle.properties`

```properties
# 升级前
rebar.version=0.42.0-26.1
# 升级后
rebar.version=0.44.4-26.2
```

### 2.2 `build.gradle.kts`

- **Rebar 来源**：移除 JitPack 仓库，改为从 Maven Central 解析  
  （Rebar 0.44 起已发布到 Maven Central，坐标 `io.github.pylonmc:rebar`）。
- **Paper API**：`io.papermc.paper:paper-api:26.1.2.build.+` → `26.2.build.+`
- **InvUI**：`xyz.xenondevs.invui:invui` / `invui-kotlin` 均由 `2.1.0` → `2.3.0`  
  （Rebar 0.44 编译即依赖 InvUI 2.3.0，需保持一致）
- **bukkit 块**：`apiVersion = "26.1.2"` → `"26.2"`
- **runServer**：`minecraftVersion("26.1.2")` → `"26.2"`

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc" }
    // 注意：已移除 maven("https://jitpack.io")，Rebar 现在从 Maven Central 获取
    maven("https://repo.xenondevs.xyz/releases") { name = "InvUI" }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    compileOnly("io.github.pylonmc.rebar:rebar:$rebarVersion")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect:2.3.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    compileOnly("xyz.xenondevs.invui:invui:2.3.0")
    compileOnly("xyz.xenondevs.invui:invui-kotlin:2.3.0")
}
```

### 2.3 `plugin.yml`

```yaml
# 升级前
api-version: 26.1.2
# 升级后
api-version: 26.2
```

### 2.4 ⚠️ JitPack 故障（重要背景）

Rebar `0.44.x` 在 JitPack 上的构建当前**失败**（JitPack 构建镜像无法打开  
Gradle 9.7.1 的 `gradle-wrapper.jar`，报 `Error: An unexpected error occurred while
trying to open file .../gradle-wrapper.jar`）。因此 **0.44.3 / 0.44.4 无法从 JitPack 解析**。  
Rebar 官方构建现已通过 Sonatype / Maven Central 发布对应版本，故本次升级直接改用  
Maven Central，无需本地 `publishToMavenLocal` 兜底。`runServer` 仍从 GitHub Releases  
下载 `rebar-0.44.4-26.2.jar` 作为运行时插件，不受影响。

---

## 3. 必须修改的破坏性 API 变更（已全部修改源码）

### 3.1 `RebarAddon.languages` 被移除，改为 `defaultLanguage`

`addon` 不再声明所支持的语言集合，改为声明**缺失翻译时的回退语言**。语言检测改为自动。

```kotlin
// 升级前  RebarIlluminationAddon.kt
override val languages: Set<Locale>
    get() = setOf(Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE)

// 升级后
override val defaultLanguage: Locale
    get() = Locale.ENGLISH
```

> 说明：插件仍应保留 `src/main/resources/lang/{en,zh_CN}.yml` 翻译文件；Rebar 会  
> 自动识别 `translator.languages`。官方建议将 `defaultLanguage` 设为可在插件  
> `config.yml` 中配置的项。本项目当前硬编码为 `Locale.ENGLISH`（英语回退）。

### 3.2 `RebarItemSchema.getItemStack()` 重命名为 `createNewItemStack()`

schema 上获取物品副本的方法改名（同时内部改用 `ItemStack.asOne()`，数量为 1 时不再克隆）。

需替换的调用点（全部已改）：

- `RebarIlluminationRecipes.kt`：所有 `schema.getItemStack()`、`RebarRegistry.ITEMS[key]?.getItemStack()`
- `RebarIlluminationGuide.kt`：所有 `RebarRegistry.ITEMS[key]?.getItemStack()`
- `block/*.kt`（`LampBlock`/`BarLampBlock`/`PatchLampBlock`/`PillarLampBlock`/`SphereLampBlock`）：  
  `defaultItem?.getItemStack()`

```kotlin
// 统一替换
.getOrCreate...getItemStack()  ->  .createNewItemStack()
defaultItem?.getItemStack()    ->  defaultItem?.createNewItemStack()
```

### 3.3 配方 API 重写：`ShapedRecipeType` / `ShapelessRecipeType.addRecipe` 不再接受原生 Bukkit 配方

Rebar 0.44 重写了整个配方系统（新增 dummy recipe、RecipeMatchingService 等）。  
`ShapedRecipeType` / `ShapelessRecipeType` 现在是 `VanillaRecipeType<ShapedRebarRecipe, ...>`，  
其 `addRecipe` 只接受 Rebar 封装类型，不再接受 `org.bukkit.inventory.ShapedRecipe` /  
`ShapelessRecipe`。需通过 `fromVanilla(...)` 工厂包装。

```kotlin
// 升级前  RebarIlluminationRecipes.kt
ShapedRecipeType.addRecipe(recipe)     // recipe: org.bukkit.inventory.ShapedRecipe
ShapelessRecipeType.addRecipe(recipe) // recipe: org.bukkit.inventory.ShapelessRecipe

// 升级后（新增 import）
import io.github.pylonmc.rebar.recipe.vanilla.ShapedRebarRecipe
import io.github.pylonmc.rebar.recipe.vanilla.ShapelessRebarRecipe

ShapedRecipeType.addRecipe(ShapedRebarRecipe.fromVanilla(recipe))
ShapelessRecipeType.addRecipe(ShapelessRebarRecipe.fromVanilla(recipe))
```

> `fromVanilla` 会完整复制原 Bukkit 配方的结果、形状、材料/精确配方选择（含  
> `RecipeChoice.ExactChoice` / `MaterialChoice`），因此现有配方逻辑无需改动。  
> 配置文件加载配方的方式（`loadRecipe`）同步改为返回 `ShapedRebarRecipe` /  
> `ShapelessRebarRecipe`，本项目未使用配置加载配方，不受影响。

---

## 4. 行为变更与新特性（建议评估，非必须改）

以下变更已确认**不影响本项目编译/运行逻辑**，但升级后建议验证其行为符合预期。

### 4.1 i18n 翻译系统

- 新增**默认语言回退**与**按语言级文件回退**（`fix(i18n): fall back to language-level
  file before default` 等），缺失 key 的告警现在按 locale 文件分别统计。
- 新增**自动语言检测**（运行时按玩家客户端语言选择）。
- `translator.languages` / `canTranslateExact(...)` 取代旧的内部 `languages` /  
  `canTranslate(...)`。本插件仅通过 `defaultLanguage` 暴露，无需额外处理。

### 4.2 `RebarBlock`

- `RebarBlock` 现在实现 `WailaSupplier` 接口；`getWaila(player)` 变为 `override`  
  （本项目各 block 已用 `WailaDisplay.of(this, player)` 工厂，兼容）。
- `WailaDisplay` 构造函数变为 **private**，必须改用 `WailaDisplay.of(...)` 工厂  
  （新增 `WailaDisplay.of(text, color, overlay, progress)`）。本项目未直接 new，兼容。
- 新增 `getBlockData()` / `editBlockData(...)` 便捷方法。
- 新增 `RebarBlockInitializeEvent`（在 `postInitialise` 之前触发），可挂初始化监听。
- `getBlockTextureItem()` 现在返回**非空** `ItemStack`，且当没有匹配 rebar item 时  
  回退到 `schema.material`；内部不再用 `BARRIER` 兜底。本项目未覆写该方法。
- `getDropItem` / `getPickItem` 内部改用 `createNewItemStack()`。

### 4.3 `EntityHolderRebarBlock`

- `addEntity(name, entity)` 现在**返回该实体**（`fun <T : Entity> addEntity(...): T`），  
  并新增 `addEntity(name, RebarEntity)` 重载。本项目 `addEntity("light", ...)` 未使用  
  返回值，兼容。
- `tryRemoveEntity` 改用 `remove` 而非 `get`，语义不变。

### 4.4 `DirectionalRebarBlock`

- 新增 `setFacingIfAbsent(facing)`：仅在尚未设置朝向时写入，便于在 place 构造器中安全初始化。  
  本项目在构造器直接赋值 `override var facing`，未受影响。

### 4.5 `ItemDisplayBuilder` / `TransformBuilder`

- `ItemDisplayBuilder.transformation(Consumer<TransformBuilder>)` 新增重载。
- `TransformBuilder` 新增更多 `rotate(...)` 重载（`AxisAngle4f`、轴+角度等）。  
  本项目使用 `.scale(...).buildForItemDisplay()`，兼容。

### 4.6 `ConfigAdapter` / `ConfigSection`

- `ConfigAdapter` 新增：`LOCALE`、`ITEM_CHOICE`、`FLUID_CHOICE`、`FLUID_WITH_AMOUNT`、  
  `BLOCK_TYPE_WRAPPER`、`BLOCK_TAG`、`ITEM_TYPE_WRAPPER`、`WEIGHTED_SET`、  
  `WAILA_DISPLAY`、`CULLING_PRESET`、`CONTRIBUTOR` 等。
- `ConfigSection` 新增 `has(key)`；`fromSettings` 内部改为合并 Rebar 资源（公开签名不变）；  
  `copyResource` 改为接收 `RebarAddon`。
- 本项目 `FaultyLampMixin` / `RainbowLampMixin` 使用的 `ConfigAdapter.INTEGER` /  
  `ConfigAdapter.DOUBLE` 仍存在，`ConfigSection.fromSettings(...).get(...)` 签名不变，兼容。

### 4.7 `ItemStackBuilder`

- `editCustomModelData` 的 lambda 参数类型由 `Consumer<CustomModelData.Builder>` 改为  
  `Consumer<CustomModelDataBuilder>`（内部改用 `CustomModelDataBuilder.of(this).build()`）。  
  本项目仅用 `ItemStackBuilder.rebar(...)` 与 `.build()`，兼容。

### 4.8 研究系统（Research）

- **移除了 `researchBypassPermission`**：`RebarItemSchema.researchBypassPermission` 与  
  `Research` 中的绕过权限逻辑被删除；`Player.canCraft` 不再接受 `respectBypass` 参数。  
  研究解锁现在**仅**由 `RebarConfig.ResearchConfig.ENABLED` 控制。  
  本项目 `RebarIlluminationResearch` 未设置/使用绕过权限，兼容，但需知悉此行为变化。

### 4.9 版本不匹配保护

- Rebar 现在会在**版本不匹配时拒绝启动 addon**（`don't start if version mismatched`）。  
  务必保证 `gradle.properties` 的 `rebar.version` 与运行时安装的 Rebar 插件版本一致。

### 4.10 其他新增能力（供后续优化参考）

- 全新物品：`VanillaBlockItem` 接口（让普通原版方块也能作为 rebar item 放置）。
- `BlockTypeWrapper` / `RebarBlockTag`：方块类型与标签封装。
- 电力系统（ElectricRebarBlock / SimpleElectricRebarBlock）等大量新 API（本项目暂未使用）。
- 配方性能大幅优化、新增 loot table API、`CustomModelDataBuilder` 改进等。

---

## 5. 运行环境要求

1. **服务端必须升级到 Paper 26.2**（或与 26.2 API 兼容的下游服务端）。  
   在 26.1 服务端上加载会导致 Rebar / 本插件无法启动（见 §4.9）。
2. **构建工具链 JDK 25**：本项目 `java { toolchain.languageVersion = JavaLanguageVersion.of(25) }`  
   与 Rebar 0.44 一致，无需改动。
3. 运行时 Rebar 插件：`runServer` 会自动下载 `rebar-0.44.4-26.2.jar`；  
   手动部署时请将服务端 `plugins/` 下的 Rebar 同步升级到 `0.44.4-26.2`。

---

## 6. 清理与遗留项

- **`bin/` 目录为陈旧副本，建议删除**。该目录包含旧版源码（如 `WallLampBlock.kt`，  
  而 `src` 中已演进为 `PatchLampBlock.kt`），且**不在 Gradle 默认 source set  
  （`src/main/kotlin`）内**，不会参与编译。保留它只会造成混淆与误改，建议清理。
- 升级后建议删除构建缓存并重跑：`./gradlew clean build`。

---

## 7. 验证步骤

```bash
# 1) 解析依赖并编译（验证 Maven Central 上的 rebar 0.44.4-26.2 可正常解析、源码可编译）
./gradlew clean build

# 2) 启动本地 Paper 26.2 服务端进行冒烟测试
./gradlew runServer
```

冒烟测试检查项：

- 插件在 STARTUP 阶段随 Rebar 正常加载，无版本不匹配报错；
- 各类灯（普通 / 彩虹 / 无色）可放置、开关、故障模式正常；
- 配方（荧石粉、灯、柱灯、条灯、球灯、壁灯）在合成台可正常合成；
- 研究（illumination）解锁后配方可 discovery；
- 指南（RebarGuide）页面正常显示各物品；
- WAILA 对准方块时显示正确状态文本。
