# Creative Motor port unit

Status: `in-progress`

Priority: `P0`

## Upstream trace

- `com/simibubi/create/AllBlocks.java` (`CREATIVE_MOTOR`)
- `com/simibubi/create/AllBlockEntityTypes.java` (`MOTOR`)
- `com/simibubi/create/AllPackets.java`
- `content/kinetics/base/DirectionalKineticBlock.java`
- `content/kinetics/base/GeneratingKineticBlockEntity.java`
- `content/kinetics/base/KineticBlockEntity.java`
- `content/kinetics/motor/CreativeMotorBlock.java`
- `content/kinetics/motor/CreativeMotorBlockEntity.java`
- `content/kinetics/motor/KineticScrollValueBehaviour.java`
- `foundation/blockEntity/behaviour/ValueSettingsBehaviour.java`
- `foundation/blockEntity/behaviour/ValueSettingsPacket.java`
- `foundation/blockEntity/behaviour/ValueSettingsScreen.java`
- `assets/create/models/block/creative_motor/block.json`
- `assets/create/models/block/creative_motor/block_vertical.json`
- `assets/create/textures/block/creative_casing.png`
- `assets/create/textures/block/creative_motor.png`

All paths are relative to `upstream/create-mc1.20.1-6.0.8/src/main/` unless generated-resource paths are stated.

## 1.7.10 adaptations

- Six-way facing is stored directly as `ForgeDirection` metadata.
- Legacy adapter `foundation/utility/legacy/block/CreativeMotorItemBlock` computes the final state before placement.
  It prefers a connected kinetic neighbour and
  otherwise follows upstream's player-look and sneaking rules.
- The modern multipart model is temporarily represented by the upstream directional motor shape, original textures,
  and a legacy axis-aware cuboid. A dedicated ISBRH/TESR model remains a visual follow-up.
- The world-space Value Settings board is temporarily represented by a non-pausing legacy screen with the same two
  direction rows and `1..256 RPM` range. This is the documented PRD fallback, not the final UI target.
- Legacy `MotorSpeedPacket` queues work onto the server tick before validating distance, block identity, and tile
  entity type.
- `LegacyKineticNetwork` provides the first minimal propagation seam, while `LegacyKineticWorldAdapter` isolates 1.7.10
  world access. It covers aligned shaft networks, source speed changes, disconnect clearing, faster-source arbitration,
  and equal-opposite shutdown. A4 guarantees only the single-Motor-to-Shaft closure: unlike upstream Create, the
  temporary multi-source path does not yet break a conflicting source block. Gears, ratios, stress, overload, and
  upstream conflict destruction remain later work.

## Automated checks

- [x] Generated speed defaults to 16 RPM and persists through NBT.
- [x] Value settings map the two direction rows and clamp magnitude to `1..256`.
- [x] Placement follows upstream look, neighbour, and sneaking precedence.
- [x] Only the facing side exposes a shaft connection.
- [x] Directional bounds match upstream's motor shape.
- [x] The client packet round-trips its target and applies the value setting.
- [x] A Motor powers an aligned Shaft chain and removing the Motor clears downstream speed.

## Manual game checklist

- [ ] Creative Motor appears in the Create creative tab with English and Chinese names.
- [ ] Original motor and casing textures render without transparent gaps on all six facings.
- [ ] Placement follows player look and aligns to an adjacent Shaft unless the player sneaks.
- [ ] Right-click opens the legacy Value Settings fallback without pausing the world.
- [ ] Both rotation directions and every speed from 1 through 256 reach the server.
- [ ] Speed and facing survive world save and reload.
- [ ] A connected Shaft receives the configured signed speed.
- [ ] Changing Motor speed updates the connected Shaft immediately.
- [ ] Breaking either Motor or Shaft clears the disconnected Shaft network.
- [ ] Multiplayer interaction produces no registration, packet-thread, or synchronization errors.

The unit remains `in-progress` until this manual checklist is completed and the world-space Value Settings replacement
is either implemented or explicitly accepted as a milestone exception.
