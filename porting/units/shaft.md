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

- Modern `Direction.Axis` block state is encoded as metadata by `LegacyAxis`.
- Modern block entity registration is bridged through `GameRegistry`.
- The modern voxel model is represented by axis-sensitive block bounds and legacy icons.
- Waterlogging, brackets, wrenching, pole placement assistance, encasing, Flywheel visuals, and Ponder are deferred to their own units.
- `KineticBlockEntity` currently persists and synchronizes speed; full upstream network propagation arrives with the Creative Motor closure.

## Automated checks

- [x] Every axis round-trips through metadata.
- [x] Placement face selects the axis.
- [x] Shaft connections are exposed only on the two axis faces.
- [x] Speed survives an NBT round trip through the registered tile entity.

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
