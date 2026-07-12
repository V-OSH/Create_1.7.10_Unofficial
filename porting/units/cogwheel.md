# Cogwheel port unit

Status: `in-progress`

Priority: `P0`

## Upstream trace

- `com/simibubi/create/AllBlocks.java` (`COGWHEEL`)
- `com/simibubi/create/AllShapes.java` (`SMALL_GEAR`, `SIX_VOXEL_POLE`)
- `content/kinetics/RotationPropagator.java`
- `content/kinetics/base/KineticBlockEntity.java`
- `content/kinetics/base/KineticBlockEntityVisual.java`
- `content/kinetics/base/RotatedPillarKineticBlock.java`
- `content/kinetics/simpleRelays/AbstractShaftBlock.java`
- `content/kinetics/simpleRelays/AbstractSimpleShaftBlock.java`
- `content/kinetics/simpleRelays/CogWheelBlock.java`
- `content/kinetics/simpleRelays/CogwheelBlockItem.java`
- `content/kinetics/simpleRelays/ICogWheel.java`
- `assets/create/models/block/cogwheel.json`
- `assets/create/textures/block/cogwheel.png`
- `assets/create/textures/block/cogwheel_axis.png`
- `assets/create/textures/block/axis_top.png`
- `data/create/recipes/crafting/kinetics/cogwheel.json`

Paths are relative to the vendored Create 6.0.8 snapshot; generated recipe paths retain their generated-resource root.

## 1.7.10 adaptations

- Axis state uses the existing `LegacyAxis` metadata bridge. Placement keeps upstream Cogwheel precedence: sneaking
  selects the clicked-face axis, otherwise a clicked Cogwheel axis wins, followed by a connected Shaft preference and
  finally the clicked-face axis.
- `CogWheelItemBlock` resolves placement before the block enters the world and triggers the legacy kinetic rebuild.
  Upstream placement helpers depend on modern ray/context APIs, so the 1.7.10 adapter currently preserves direct
  clicked-Cogwheel alignment while its multi-placement assistance remains deferred.
- The unchanged upstream seven-element JSON model is parsed once. Although Blockbench writes
  `"texture_size": [32, 32]`, vanilla's `BlockModel` ignores that metadata: all face UVs remain in the standard 0-16
  domain, including the 32x32 `cogwheel.png`. The legacy TESR also mirrors `FaceBakery`'s per-face UV shrink
  (`4 / sprite pixel width`) before atlas interpolation to prevent the narrow tooth faces from sampling over their
  borders. It then applies per-face UV rotations, element rotations, and the upstream position-dependent 22.5-degree
  half-tooth phase.
- Collision uses the upstream union of a six-voxel pole and the 12x4x12 small-gear body. Minecraft 1.7.10 selection
  outlines cannot express the union, so the selected outline is its axis-oriented enclosing box.
- `LegacyKineticNetwork` now assigns a speed multiplier to every edge. Shaft connections use `+1`; adjacent small
  Cogwheels with equal, parallel axes mesh on a perpendicular side with `-1`.
- `ICogWheel` is retained as the shared upstream seam for later Large and encased variants.
- Large Cogwheel ratios, diagonal small/large engagement, encasing, brackets, waterlogging, wrenching, contraption
  behavior, stress impact, and the shifting-gears advancement remain in their own later units.

## Automated checks

- [x] Placement follows clicked Cogwheel, neighbouring Shaft, and sneaking precedence.
- [x] Shaft connectivity exists only along the Cogwheel axis.
- [x] Selection covers both the gear body and its full axial Shaft.
- [x] Adjacent parallel small Cogwheels reverse signed speed without changing its magnitude.
- [x] Shaft transmission after a meshed Cogwheel retains the reversed speed.
- [x] Original model element count, tooth overhang, vanilla UV domain/shrink, and +/-45-degree teeth are preserved.
- [x] Adjacent Cogwheels receive upstream's alternating 22.5-degree tooth phase.
- [x] Existing Motor-to-Shaft propagation and source-conflict tests remain green.

## Manual game checklist

- [ ] Cogwheel appears in the Create creative tab with English and Chinese names.
- [ ] Shaft plus any wooden planks crafts one Cogwheel.
- [ ] Cogwheel places on X, Y, and Z axes with the expected upstream precedence.
- [ ] Original Cogwheel model and textures render without gaps in the inventory and world.
- [ ] A Motor or axial Shaft drives the Cogwheel at the same signed speed.
- [ ] Two side-by-side Cogwheels with parallel axes mesh, visibly interlock, and rotate in opposite directions.
- [ ] Breaking either meshed Cogwheel clears the disconnected network.
- [ ] Axis and speed survive save and reload without client/server errors.

The unit remains `in-progress` until the manual checklist is completed. Large Cogwheel interaction is explicitly not
part of this small-Cogwheel unit.
