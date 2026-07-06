# PRD: Create 1.7.10 Unofficial — 从 1.20.1 向 1.7.10 的逆向移植

## Problem Statement

Minecraft 1.7.10 拥有庞大的科技模组生态（GregTech New Horizons、Thermal Expansion、IndustrialCraft 2 等），但缺少现代旋转动力机械模组 **Create** 的核心体验。Create 的旋转动力网络、应力系统、齿轮传动和基础机械处理在 1.7.10 生态中没有任何替代品。1.7.10 的玩家和整合包作者希望能在旧版 Minecraft 中使用 Create 风格的动力机械系统，以丰富 1.7.10 科技整合包的玩法深度。

Create 目前仅支持 Minecraft 1.20+ / 1.21+，其代码深度绑定现代 Forge API、Java 17+ 语法、Flywheel 实例化渲染、JSON 模型系统、Data Generation 等——这些在 1.7.10 中均不存在。需要一个完整的逆向移植工程来弥合这一鸿沟。

## Solution

基于 **GTNH 生态技术栈**（lwjgl3ify + UniMixins + ForgeGradle 5.x），从零构建一个面向 Minecraft 1.7.10 的 Create 实现，参考 Create 6.0.8 for 1.20.1 源码。项目采用 **Java 17 + Kotlin 混合** 语言、**UniMixins** 做字节码注入、**混合 Shim 层** 做 API 适配，第一阶段使用传统 ISBRH + TESR 渲染（Flywheel 实例化渲染作为后期迭代目标）。

## User Stories

### 动力网络核心

1. 作为玩家，我希望放置水车、风车或蒸汽机作为动力源，使其输出旋转动力，以便驱动后续的机械
2. 作为玩家，我希望使用齿轮、传动杆和转轴将动力从源头传输到远处的机械，以便灵活布置工厂
3. 作为玩家，我希望多个动力源和多个机械消耗能在同一个网络中自动合并计算转速和应力，使得网络中的速度一致
4. 作为玩家，我希望当网络中的应力消耗超过应力容量时，整个网络过载停滞，以便理解我的设计存在动力不足的问题
5. 作为玩家，我希望在方块放置或破坏时动力网络自动重新计算并实时反映到所有连接方块上，使得即时的网络变化得到正确反馈

### 基础机械

6. 作为玩家，我希望使用粉碎机（Millstone）将矿石/原材料粉碎成粉末/碎料，以便获得更高的产出倍率
7. 作为玩家，我希望使用动力钻（Drill）挖掘方块或在世界中开凿隧道，以便自动化采矿
8. 作为玩家，我希望使用鼓风机（Encased Fan）产生气流，对前方物品进行处理（洗涤、烟熏、熔炼等），以便自动化产线
9. 作为玩家，我希望使用传送带（Belt）在位置上运输物品实体，以便连接不同的处理机械
10. 作为玩家，我希望使用机械手（Mechanical Arm）在传送带和容器之间搬运物品，以便建立柔性产线

### 流体系统

11. 作为玩家，我希望使用动力泵（Pump）将液体从开放水池抽取到管道中，以便获取无限水源
12. 作为玩家，我希望使用流体管道（Pipe）在方块间传输液体，以便远距离输送
13. 作为玩家，我希望使用流体储罐（Fluid Tank）作为液体缓冲区，以便暂存和调配液体资源

### 附魔系统

14. 作为玩家，我希望使用 Create 风格的附魔台，通过注入液体经验来为工具附魔，以便获得指定附魔而非随机附魔
15. 作为玩家，我希望超平坦附魔机制能让我在工具上叠加更多附魔，以便实现 1.7.10 传统附魔台无法达到的组合

### 整合包作者

16. 作为整合包作者，我希望通过 MineTweaker 3 脚本自定义所有机械配方的输入输出，以便将 Create 机械融入我设计的科技树
17. 作为整合包作者，我希望所有方块和物品 ID 通过配置文件可调整，以便在与其他上百个模组共存时避免冲突
18. 作为附属模组开发者，我希望 Create 提供清晰稳定的 public API，以便我能开发自己的 Create 风格机械方块

### Ponder 教程（后期）

19. 作为新玩家，我希望按 W 键查看 Create 方块的交互式教程，以便快速理解每个机械的使用方法
20. 作为新玩家，我希望 Ponder 场景中用动画展示多方块组装方式，以便不需要查阅外部 wiki

## Implementation Decisions

### 运行时环境

- **Minecraft 版本**：1.7.10（Forge）
- **Java 版本**：Java 17（通过 lwjgl3ify 实现对 1.7.10 的 Java 17+ 运行时支持）
- **前置依赖**：lwjgl3ify（用户必须安装）、UniMixins（内嵌）
- **构建系统**：ForgeGradle 5.x + Gradle（基于 GTNH ExampleMod 1.7.10 模板）
- **编程语言**：Java 17 + Kotlin 混合

### 技术架构

- **Mixin 方案**：UniMixins（LegacyModdingMC 维护，当前 1.7.10 唯一活跃 Mixin 加载器）。支持 Early/Late Mixin、MixinExtras。极少数不兼容场景使用 Coremod（ASM Transformer）兜底
- **Shim 层（混合策略）**：
  - **进 Shim**：`MyBlockPos`、`MyVec3`、`MyDirection`、`MyMathHelper` 等轻量数据类，在 1.7.10 API 之上模拟现代 Minecraft 基础类型
  - **不进 Shim，手写 1.7.10 原生**：方块/物品注册、TileEntity 系统、渲染（ISBRH/TESR/Tessellator）、网络、GUI/Container、NBT 读写
- **Package 结构**：`create.core.kinetic`（动力网络）、`create.core.fluid`（流体）、`create.core.machinery`（机械）、`create.api`（公开 API 边界），预留清晰的 API 边界

### 动力网络设计

- **数据结构**：坐标 Set + BFS 全量重建，每次方块放置/破坏时遍历所有连接节点重新计算网络属性
- **存储策略**：NBT 仅存储 TileEntity 的视觉/配置数据（旋转角度、朝向、红石禁用状态）。转速（speed）、应力容量（stressCapacity）、应力消耗（stressImpact）由网络重建时计算并填充到 transient 字段，不序列化到 NBT
- **网络拓扑**：无向图遍历，节点为实现了 `IKineticTile` 接口的 TileEntity，通过 `BlockFace` 检测相邻方块连接状态
- 参考模型：Create 6.0.8 for 1.20.1 的动力网络算法

### 渲染系统

- **静态/半静态方块**：ISBRH（`ISimpleBlockRenderingHandler`），在 RenderBlocks 完成后通过 Tessellator 手动绘制
- **动态旋转方块**：TESR（`TileEntitySpecialRenderer`），每帧根据转速计算旋转角度并渲染
- 方块模型不存在于 1.7.10 JSON 系统，所有方块实体渲染由代码直接控制 Tessellator 绘制
- 纹理注册通过 `IIcon` + `registerIcons` 机制
- Flywheel 实例化渲染作为后期性能优化迭代目标

### 注册与 ID 管理

- 方块/物品注册使用 Forge 1.7.10 的 `GameRegistry.registerBlock()` / `GameRegistry.registerItem()`
- 数字 ID 由 Forge 自动分配（4096+）
- 注册名使用 `create:<name>` 命名空间格式
- 所有注册调用按固定顺序排列，避免 ID 漂移
- ItemBlock 必须与对应方块注册名一致
- 需要动态状态的方块全部配备 TileEntity（1.7.10 的 metadata 仅 4 bit，不足以表示复杂状态）

### 网络同步

- 自定义 Packet 抽象层（`IPacket` 接口：`encode(ByteBuf)`, `decode(ByteBuf)`, `handle()`）
- 1.7.10 底层使用 `SimpleNetworkWrapper` + `FMLProxyPacket` 实现
- TileEntity 状态同步通过 `getDescriptionPacket()` + `S35PacketUpdateTileEntity`
- 客户端侧通过 `onDataPacket()` 更新 TileEntity 数据

### 配方系统

- 核心配方初始阶段硬编码（Java 代码注册）
- 同时预留 MineTweaker 3 Hook 接口，允许整合包作者通过脚本自定义配方
- 自定义配方类型（粉碎、鼓风、压印等）通过实现 IRecipe 接口处理

### 模块实施顺序

- Phase 0：项目搭建 — GTNH ExampleMod 模板 + Shim 层基础数据结构 + JUnit 冒烟测试（shim 类型基本运算）
- Phase 1：注册系统 — 方块、物品、TileEntity、创造模式标签页、自定义 IPacket 抽象层
- Phase 2：动力网络引擎 — BFS 传播算法、ChunkEvent 处理、动力源、齿轮/传动杆/转轴、JUnit 单元测试
- Phase 3a：简单机械 — 粉碎机、动力钻、鼓风机、ProcessingRecipeRegistry
- Phase 3b：传送带 — 多段传送带、物品实体运输、隧穿/漏斗交互、坡道段
- Phase 3c：机械臂 — 库存目标选择、状态机、姿态动画
- Phase 4：流体系统 — 完整流体能力层、动力泵、管道、储罐
- Phase 5：附魔系统 — 自定义附魔台方块/容器/GUI、液体经验、超平坦附魔
- Phase 6：附属扩展 — Create: Big Cannons、Create: Aeronautics（独立项目，后期）

### Ponder 系统

- 核心系统完成后追加，不在 Phases 0-5 范围内
- 初始可考虑指南书/NEI 提示作为过渡方案

## Testing Decisions

### 测试理念

- 只测试外部行为（输入→输出），不测试实现细节
- 核心算法测试不需要 Minecraft 运行环境，纯 JUnit 单元测试
- 渲染和网络等耦合 Minecraft 的部分用手工测试世界 + 社区内测

### 单元测试范围

- **Phase 0 — Shim 层冒烟测试 (JUnit)**：验证 `MyBlockPos.offset()` 坐标偏移、`MyVec3.normalize()` 单位向量计算、`MyDirection.rotate()` 方向旋转等基本运算正确，shim 编译通过且不抛出异常
- **动力网络 BFS 算法**：给定一组模拟坐标和连接关系，验证网络重建结果的正确性（覆盖所有节点被访问、跨 Chunk 遍历、孤立子网络、环形拓扑）
- **应力计算**：给定模拟网络配置，验证总应力容量/消耗计算正确，过载检测阈值准确
- **转速转换**：给定齿轮比配置，验证转速缩放计算正确（大齿轮→小齿轮加速，小齿轮→大齿轮减速）

### 游戏内测试

- 建立一个包含所有基础机械和边界情况的测试世界
- 测试边界情况：网络断开/重连、Chunk 加载/卸载、红石信号切换、服务器重启后的状态恢复

## Out of Scope

- **铁路系统**：列车、轨道、信号系统、列车组装
- **装饰方块**：Create 的各种建筑装饰方块
- **Flywheel 实例化渲染**：第一阶段不做，后期迭代
- **Data Generation**：1.7.10 不存在此系统，不试图模拟
- **Create: Big Cannons**：后期独立项目
- **Create: Aeronautics**：后期独立项目
- **无线红石系统**：如有则后期处理
- **状态在高版本与 1.7.10 之间的跨版本兼容**

## Further Notes

- 本项目是一个个人爱好项目，无 deadline、无外部交付压力。节奏从容，注重代码质量和长期可维护性
- 参考源码：Create 6.0.8 for Minecraft 1.20.1（MIT License）
- 未来可能考虑与 GTNH 整合包或 TLM 模组的合作/集成
- 所有内部包结构保持清晰分层，API 边界预留，以便未来开放给附属模组开发者
- 项目将从 GTNH ExampleMod 1.7.10 模板初始化，该模板已预置 UniMixins、lwjgl3ify、ForgeGradle 5.x 和 CI 配置
- `.gitignore` 已补充：`.kotlin/`（Kotlin 构建缓存）、`logs/`（Minecraft 运行日志目录）、`*.tmp`/`*.bak`/`*.swp`（编辑器临时文件）、`tmp/`/`bin/`（构建输出）、`local.properties`（Gradle 本地配置）、`*.launch`（Eclipse 启动配置）

## Design Refinements (2026-07-06 Grilling)

以下决策通过与上述 Implementation Decisions 交叉审查，细化或补充了原有设计。

### 参考源码版本

- **固定参考 Create 6.0.8 for 1.20.1**。6.0.8 拥有成熟 API、多年 Bug 修复积累、社区活跃维护。虽然后期版本包含铁路/蒸汽引擎等超出范围的系统，但这些系统仅作算法参考，无需移植。
- 参考方式：以算法逻辑为参考重写，非逐行代码复制。

### Shim 层范围

- **仅封装轻量数据类型**：`MyBlockPos`、`MyVec3`、`MyDirection`、`MyMathHelper`——这些类型在 1.20.1 和 1.7.10 中结构功能基本相同但方法略有不同。
- **不进 Shim**：TileEntity、World、网络、GUI/Container 等复杂 Minecraft 系统全部手写 1.7.10 原生代码。Create 特有的复杂数据结构（动力网络节点、处理配方等）直接在 1.7.10 上设计，不做封装。
- 理由：窄 Shim 投入低、收益高；宽 Shim 会变成 1.20.1 API 模拟器，维护负担不可持续。

### 动力网络——区块加载/卸载

- 在 Phase 2 中订阅 `ChunkEvent.Load` 和 `ChunkEvent.Unload`。
- **卸载时**：将网络标记为"脏"（dirty），不拆分，等待重新验证。
- **加载时**：对处于区块边界的 TileEntity 触发网络重建。
- 不存在跨区块网络的静默分裂或重新连接问题。

### 渲染——ISBRH/TESR 双路径

- **默认路径 ISBRH**：所有动力方块默认使用 ISBRH 渲染静态模型。
- **动态 TESR 路径**：仅当 TileEntity `speed != 0` 时激活 TESR 动态旋转渲染。TESR 检查 `speed == 0` 立即返回（零开销）。
- 避免了"此方块是或不是 TESR 方块"的二元分类问题，装饰性静态齿轮不消耗 TESR 性能。
- 在 1.7.10 中，方块可以同时拥有 ISBRH 渲染类型和 TESR——它们是不同的 Forge API。

### 注册与 ID 管理——枚举化注册

- 采用 **GTNH 标准模式**：每个方块/物品分配命名常量，使用显式枚举（enum）或类似 GT5U `ItemList` 的静态常量类。
- 新方块/物品始终**追加到末尾**，绝不插入中间，避免 ID 漂移导致旧存档损坏。
- 枚举的 ordinal 或常量索引即为模组 ID 范围内的偏移量。
- ID 稳定性在 code review 中可直接验证。

### NBT 持久化——面连接状态

- **持久化到 NBT**：连接面状态（6 个 `ForgeDirection` 布尔值），在 Chunk 加载时通过 `validate()` 或首 tick 恢复网络拓扑。
- **transient（不序列化）**：speed、stressCapacity、stressImpact、networkID——由网络重建时填充。
- 服务器重启后根据存储的面连接信息重建整个网络，不丢失拓扑结构。

### 网络同步——分阶段实现

- **初期（Phase 2）**：使用方案 A——通过 `getDescriptionPacket()` / `onDataPacket()` 对每个 TileEntity 独立同步。每当网络速度变化时，向每个受影响 TE 发送同步数据包。
- **后期优化**：如性能不足，重构为方案 B——每个动力网络发一个数据包（"networkID X: speed=64 RPM, stress ratio=0.7"），客户端 TE 从 NetworkManager 查询本网络值。
- 理由：1.7.10 多人服务器通常 2-10 人，200 个数据包的网络形成峰值是一次性的；稳态变化（增加水车）仅影响少量方块。如瓶颈出现，方案 B 的修改是局部重构。

### 配方系统——独立注册表

- 不使用原版 `CraftingManager`。构建独立的 `ProcessingRecipeRegistry`，按机器类型（粉碎机、鼓风机、压印机、混合器）分类注册。
- `ProcessingRecipe` 存储：速度要求、处理时间、输入物品/流体、输出物品/流体。
- TileEntity 直接查询对应机器类型的注册表。
- NEI 集成需显式编写处理器（不可避免），但 MineTweaker 3 挂钩自然映射（每种机器类型对应的 `addRecipe`/`removeRecipe` 方法）。

### 流体 API——完整能力层

- 构建**完整的流体能力层**（非轻量工具类），在 1.7.10 `IFluidHandler` 之上封装现代风格的 API：
  - `fill(FluidStack, boolean simulate)` → `int amountFilled`
  - `drain(FluidStack, boolean simulate)` → `FluidStack drained`
  - `tryPushToNeighbor(TileEntity, ForgeDirection, FluidStack)` 等便捷方法
- 支撑后续 Basin 的多流体 I/O、Mixer 的流体配方、Pipe 的跨方块传输。

### 附魔系统——自定义方块方案

- Create 的附魔工业本质上属于附加模组，**完全自定义方块**：自定义容器、自定义 GUI、自定义附魔逻辑。
- 不使用 Mixin 修改原版附魔台/铁砧——避免与其他修改附魔的模组（Thaumcraft 等）冲突。
- "超平坦附魔"通过自定义容器的 `canEnchant()` 检查可配置上限，而非突破原版硬编码限制。
- 输出物品带有合法的原版 NBT 附魔标签，可通过 `EnchantmentHelper.getEnchantmentLevel()` 被其他模组正常读取。

### MineTweaker 3 集成

- 每种机器类型对应一个 ZenScript 处理器：`mods.create.Millstone`、`mods.create.Fan`、`mods.create.Press`、`mods.create.Mixer`。
- 每个处理器提供：`addRecipe()`、`removeRecipe()`、`removeAll()`。
- **默认配方**在 `init` 阶段注册（作为合理默认值）。
- **MT3 脚本**推荐在 `postInit` 阶段运行，覆盖或删除默认配方。
- 模式参照 GT5U 的 MT3 处理器实现。

### Public API 策略

- **初期采用显式标记方案**：使用 `@PublicAPI` 注解标记约 5-10 个核心接口：
  - `IKineticTile`（动力方块接口）
  - 动力网络查询方法（根据坐标查询网络速度/应力）
  - `ProcessingRecipeRegistry`（配方注册 API）
  - 流体能力层接口
- 其他所有类和方法保持 `internal` 或标记为 `@Internal`。
- **后期**：待整个模组成熟后评估哪些额外的 API 需要开放给附属开发者。

### 构建系统初始化策略

- **不从 GTNH ExampleMod 模板直接克隆**。而是以模板为参考，选择性复制所需部分：
  - Gradle Wrapper + `build.gradle` 结构（重写以支持 Kotlin/Java 混合编译）
  - UniMixins 配置（`mixin.create.json` 模板、Mixin Plugin 类桩）
  - CI 配置（适配当前项目）
  - Spotless 代码格式化（初期使用宽松配置，后期收紧）
- Kotlin 从 Phase 0 开头就在 Gradle 中配置，模拟 Create 6.0.8 的 Kotlin 代码（Kotlin 部分改动预期少）。
