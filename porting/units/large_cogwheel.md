# Large Cogwheel port unit

Status: `in-progress`

Priority: `P0`

## Upstream trace

- `com/simibubi/create/AllBlocks.java` (`LARGE_COGWHEEL`)
- `com/simibubi/create/AllShapes.java` (`LARGE_GEAR`, `SIX_VOXEL_POLE`)
- `content/kinetics/RotationPropagator.java`
- `content/kinetics/base/KineticBlockEntity.java`
- `content/kinetics/base/KineticBlockEntityVisual.java`
- `content/kinetics/simpleRelays/CogWheelBlock.java`
- `content/kinetics/simpleRelays/CogwheelBlockItem.java`
- `content/kinetics/simpleRelays/ICogWheel.java`
- `content/kinetics/simpleRelays/SimpleKineticBlockEntity.java`
- `assets/create/models/block/large_cogwheel.json`
- `assets/create/textures/block/large_cogwheel.png`
- `assets/create/textures/block/cogwheel_axis.png`
- `assets/create/textures/block/axis_top.png`
- `foundation/data/recipe/CreateStandardRecipeGen.java` (`LARGE_COGWHEEL`, `LARGE_COGWHEEL_FROM_LITTLE`)

Paths are relative to the vendored Create 6.0.8 snapshot.

## 1.7.10 adaptations

- The upstream `CogWheelBlock.small/large` factory distinction is retained in one metadata-axis block class. Block
  identity selects the small or large shape, model, texture, language key, render phase, and kinetic rules.
- Placement axis uses the existing `LegacyAxis` bridge. As upstream does, only a clicked small Cogwheel directly
  supplies its axis. The placement bridge rejects adjacent configurations whose full gear bodies would overlap.
- The unchanged 16-element upstream JSON model and `large_cogwheel.png` are copied byte-for-byte. The shared legacy
  model baker applies the same standard 0-16 UV domain and sprite-dependent `FaceBakery` shrink used by the repaired
  small Cogwheel; it adds no custom texture crop. The oversized tooth geometry is allowed to render outside the
  owning block just as the upstream model does. Its TESR visibility box is inflated by one block to match upstream
  `SimpleKineticBlockEntity#createRenderBoundingBox` and prevent the overhanging teeth from being culled.
- The legacy kinetic network now accepts non-cardinal propagation candidates. Small Cogwheels add the four diagonal
  positions in their rotation plane; Large Cogwheels add all twelve face-diagonal positions, matching upstream
  `KineticBlockEntity` and `SimpleKineticBlockEntity` discovery.
- A Large-to-Small diagonal connection conveys `-2x`; the reciprocal Small-to-Large connection conveys `-0.5x`.
  Perpendicular Large Cogwheels convey `+1x` or `-1x` according to their relative axis signs. Axial Shaft connections
  remain `+1x` and adjacent parallel Small Cogwheels remain `-1x`.
- The upstream visual phase is retained: even parity uses 22.5 degrees, while odd-parity Large Cogwheels use 11.25
  degrees. Shafts and Small Cogwheels keep their existing 22.5/0 parity.
- Encasing, brackets, waterlogging, wrenching, placement assistance, the shifting-gears advancement, Rotation Speed
  Controller integration, stress impact, and contraption behavior remain deferred to their own units.

## Automated checks

- [x] Large Cogwheel reports the upstream large/small `ICogWheel` identity.
- [x] Selection and collision combine the full-block Large Gear body with its axial Shaft.
- [x] The original 16 model elements, 45/22.5-degree rotations, and tooth overhang are preserved.
- [x] The bundled `large_cogwheel.png` SHA-256 matches the vendored Create 6.0.8 texture.
- [x] Large Cogwheel TESR bounds include the complete upstream tooth overhang.
- [x] Small/Large diagonal neighbour discovery matches the upstream candidate sets.
- [x] Large-to-Small and Small-to-Large connections apply reciprocal `-2` and `-0.5` modifiers.
- [x] Perpendicular Large Cogwheels relay at the upstream signed 1:1 ratio.
- [x] A 32 RPM Large Cogwheel drives a diagonal Small Cogwheel and its output Shaft at -64 RPM.
- [x] The upstream 11.25-degree odd-parity Large Cogwheel render offset is preserved.
- [x] Existing Motor, Shaft, Small Cogwheel, source-conflict, and rendering tests remain green.

## Manual game checklist

- [ ] Large Cogwheel appears in the Create creative tab with English and Chinese names.
- [ ] Shaft plus two wooden planks, or Small Cogwheel plus one wooden plank, crafts one Large Cogwheel.
- [ ] Large Cogwheel places on X, Y, and Z axes and rejects overlapping adjacent gear bodies.
- [ ] Original Large Cogwheel model and textures render without gaps or legacy UV cropping in inventory and world.
- [ ] An axial Motor or Shaft drives the Large Cogwheel at the same signed speed.
- [ ] A diagonal Small Cogwheel rotates oppositely at twice the Large Cogwheel speed.
- [ ] Driving from Small to Large halves the conveyed speed.
- [ ] Perpendicular Large Cogwheels mesh and visibly relay their signed speed at 1:1.
- [ ] Breaking either gear clears every disconnected cardinal and diagonal network branch.
- [ ] Axis and speed survive save and reload without client/server errors.

The unit remains `in-progress` until the manual checklist is completed.
