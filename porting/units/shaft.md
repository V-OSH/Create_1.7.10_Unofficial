# Shaft port unit

Status: `in-progress`

Priority: `P0`

## Upstream trace

- `com/simibubi/create/AllBlocks.java` (`SHAFT`)
- `com/simibubi/create/AllBlockEntityTypes.java` (`BRACKETED_KINETIC`)
- `content/kinetics/base/IRotate.java`
- `content/kinetics/base/KineticBlock.java`
- `content/kinetics/base/KineticBlockEntity.java`
- `content/kinetics/simpleRelays/AbstractShaftBlock.java`
- `content/kinetics/simpleRelays/AbstractSimpleShaftBlock.java`
- `content/kinetics/simpleRelays/ShaftBlock.java`
- `assets/create/models/block/shaft.json`
- `assets/create/textures/block/axis.png`
- `assets/create/textures/block/axis_top.png`

All paths are relative to `upstream/create-mc1.20.1-6.0.8/src/main/`.

## 1.7.10 adaptations

- Modern `Direction.Axis` block state is encoded as metadata by `LegacyAxis`; values `0`, `4`, and `8` deliberately
  reuse Minecraft 1.7.10's log-renderer axis bits so horizontal side UVs rotate correctly.
  Earlier development metadata `1` and `2` remains readable as X and Z, but those already-placed shafts must be
  replaced once to rewrite the UV rotation bits.
- `ShaftItemBlock` adapts `RotatedPillarKineticBlock` placement before the block enters the world: connected kinetic
  neighbours provide the preferred axis, otherwise the player's nearest look axis is used, while sneaking overrides
  a neighbour preference with the clicked-face axis.
- Modern block entity registration is bridged through `GameRegistry`.
- The four-voxel visual model uses axis-sensitive render bounds and the vanilla log UV bridge. Collision and selection
  separately use upstream's six-voxel pole shape.
- Waterlogging, brackets, wrenching, pole placement assistance, encasing, Flywheel visuals, and Ponder are deferred to their own units.
- `KineticBlockEntity` currently persists and synchronizes speed; full upstream network propagation arrives with the Creative Motor closure.

## Automated checks

- [x] Every axis round-trips through metadata.
- [x] Axis metadata drives the legacy log renderer's UV rotation.
- [x] Placement follows upstream look-axis, connected-neighbour, and sneaking precedence.
- [x] Shaft connections are exposed only on the two axis faces.
- [x] Visual and interaction bounds match upstream's four- and six-voxel shapes.
- [x] Hardness and pickaxe harvesting match upstream's andesite-derived properties.
- [x] Speed survives an NBT round trip reconstructed through the registered tile entity ID.

## Manual game checklist

- [ ] Mod loads with only the formal source tree.
- [ ] Shaft appears in the Create creative tab with English and Chinese names.
- [ ] Shaft can be placed on all six faces and keeps the expected axis.
- [ ] Shaft uses side and end textures on the correct faces.
- [ ] Selection and collision bounds follow the shaft axis.
- [ ] Breaking a Shaft drops a Shaft.
- [ ] Axis survives save and reload.
- [ ] No client/server registration or synchronization errors occur.

The unit remains `in-progress` until this manual checklist is completed.
