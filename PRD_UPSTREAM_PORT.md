# PRD: Create 1.7.10 Unofficial — Upstream-First Reverse Port

## Problem Statement

Minecraft 1.7.10 has a large long-lived technology mod ecosystem, but it does not have the modern Create experience: rotational power, stress, kinetic machines, item logistics, fluid logistics, contraptions, trains, teaching scenes, and the broad block/item set that makes Create feel like Create.

The previous project direction treated Create 6.0.8 mostly as an algorithmic reference and implemented a new 1.7.10-native mod under an independent package structure. That is no longer the desired direction. The project should instead be a source-based reverse port: most implementation should be adapted from Create 6.0.8 for Minecraft 1.20.1, with changes made only where Minecraft 1.7.10, Forge 1.7.10, rendering, networking, registries, or runtime constraints make direct reuse impossible.

The current `create.*` implementation is now considered a discarded prototype. It may be archived as an experiment, but it must not be treated as the formal porting baseline, and no existing block should be marked as ported merely because a prototype exists.

## Solution

Rebase the project around an upstream-first porting workflow.

The repository will maintain a read-only vendored snapshot of Create 6.0.8 for Minecraft 1.20.1 as the authoritative porting source. Formal project code will move toward the upstream package structure under `com.simibubi.create.*`. A thin legacy adaptation layer will isolate Minecraft 1.7.10-specific bridges, while core gameplay code remains as close as practical to the upstream Create content, foundation, and infrastructure packages.

Porting will proceed one block or item at a time. Each unit must be traced to upstream source files, adapted into the formal source tree, documented in a port status table, attributed correctly under the MIT license, and validated through a standard completion loop. The first real loop will be Shaft followed by Creative Motor, ending with an in-game Creative Motor -> Shaft kinetic power test that includes player-adjustable Creative Motor speed using a Create-like Value Settings interaction.

Create content is no longer treated as permanently excluded. The long-term goal is to port Create's own content as completely as possible. Priority determines order, not permanent scope. Create: Enchantment Industry is planned as an embedded extension at lower priority once its upstream source is added to the workspace.

## User Stories

1. As a Minecraft 1.7.10 player, I want Create's rotational power system in 1.7.10, so that I can build modern kinetic factories in legacy modpacks.
2. As a player, I want shafts, cogwheels, gearboxes, belts, motors, wheels, and machines to behave like their Create counterparts, so that existing Create knowledge transfers naturally.
3. As a player, I want Creative Motor to use a Create-like value setting interaction, so that speed control feels like the original mod instead of a generic legacy GUI.
4. As a player, I want Shaft and Creative Motor to form the first real power loop, so that the project proves the porting pipeline with actual gameplay.
5. As a player, I want kinetic blocks to render visibly and directionally in 1.7.10, so that placement, axis, and motion can be understood in-game.
6. As a player, I want kinetic state to persist across world reloads, so that machines do not lose speed, axis, configuration, or connection information unexpectedly.
7. As a player, I want stress and overload behavior to match Create's rules as closely as possible, so that factory planning remains familiar.
8. As a player, I want processing machines such as Millstone, Mechanical Press, Mixer, Encased Fan, Mechanical Saw, Drill, and Deployer, so that core Create production chains are available.
9. As a player, I want belts, funnels, chutes, depots, brass tunnels, and mechanical arms, so that item logistics can be built in the Create style.
10. As a player, I want pumps, pipes, tanks, basins, spouts, and fluid-aware processing, so that fluid automation is part of the port.
11. As a player, I want contraptions such as bearings, pistons, pulleys, chassis, glue, moving drills, and moving deployers, so that Create's moving machine identity is preserved.
12. As a player, I want Ponder or a close teaching equivalent, so that complex mechanics can be learned inside the mod.
13. As a player, I want trains and railway systems eventually ported, so that they are not permanently excluded from the project.
14. As a player, I want decorative and building blocks eventually ported, so that the port can approach the full Create content set.
15. As a player, I want Create: Enchantment Industry included as an embedded lower-priority extension, so that liquid experience and Create-style enchanting become part of the final mod.
16. As a modpack author, I want a full port status table generated from upstream registries, so that I can see what is done, next, deferred, or blocked.
17. As a modpack author, I want MineTweaker 3 hooks for recipes and machine behavior where appropriate, so that the port can be integrated into 1.7.10 progression packs.
18. As an addon developer, I want a stable public API boundary after the core porting workflow is proven, so that legacy Create addons can target this port without depending on internals.
19. As a contributor, I want each ported block or item to list its upstream files, adaptation notes, test status, and attribution, so that work is reviewable and repeatable.
20. As a contributor, I want legacy-only compatibility code isolated from Create gameplay code, so that upstream structure remains recognizable.
21. As a contributor, I want the current prototype implementation archived outside the formal compile path, so that old experimental code does not distort future porting decisions.
22. As a maintainer, I want a repeatable first milestone before broad content work, so that Shaft and Creative Motor become the template for every later block or item.
23. As a maintainer, I want the repository to remain buildable after the A0 reorganization, so that structural cleanup does not leave the project in a broken long-term state.
24. As a maintainer, I want source attribution and license discipline for upstream-derived code, so that the project respects Create's MIT license and original authorship.
25. As a maintainer, I want content priority to be explicit, so that "complete final port" remains the ambition without blocking early milestones.

## Implementation Decisions

### Source Of Truth

- Create 6.0.8 for Minecraft 1.20.1 is the authoritative source baseline.
- The project will maintain a vendored, read-only upstream snapshot in the repository.
- The upstream snapshot is not compiled, formatted, edited, or treated as project source.
- Formal ported code is adapted from the upstream snapshot into the active source tree.
- The previous implementation under the `create.*` package is discarded as a formal baseline.

### Porting Style

- Most code should be adapted from upstream Create source.
- Self-authored code is reserved for necessary Minecraft 1.7.10 compatibility, missing API replacements, bridges, shims, runtime integration, build integration, and tests.
- Upstream class names, module boundaries, and package organization should be preserved whenever they can survive the 1.7.10 environment.
- When direct adaptation is impossible, the 1.7.10 implementation should keep upstream concepts visible and document the compatibility reason.
- Each file or module obviously derived from Create 6.0.8 must have attribution through file header, module note, status table entry, or source attribution document.

### Package And Module Structure

- Formal source will move toward `com.simibubi.create.*`.
- Core gameplay code belongs in upstream-like content, foundation, and infrastructure areas.
- A thin legacy adaptation layer will contain Minecraft 1.7.10-specific bridges.
- The legacy layer must not become a dumping ground for gameplay logic.
- Examples of legacy-only concerns include Forge 1.7.10 registration, metadata/state conversion, Tessellator rendering bridge, ISBRH/TESR bridge, SimpleNetworkWrapper bridge, legacy GUI bridge, and lightweight world/position adapters.

### Prototype Isolation

- Current `create.*` implementation is a discarded prototype.
- Prototype code may be archived for reference outside the formal compile path.
- Prototype classes, tests, textures, and notes do not count as ported status.
- Formal porting status starts only when a unit has been checked against upstream source, adapted into the new structure, documented, and tested.

### Port Status System

- Port status tables will be generated from upstream registries rather than hand-written from memory.
- Registry sources include upstream block, item, block entity, recipe type, menu, entity, particle, and other registration entry points as needed.
- Every upstream entry should be classified.
- Preferred status vocabulary:
  - `planned-core`
  - `planned-late`
  - `in-progress`
  - `ported`
  - `blocked-technical`
  - `blocked-legal`
  - `needs-decision`
  - `prototype-archived`
- Decorative blocks, trains, and building blocks are not excluded; they are planned late.
- Blocked means "cannot currently proceed for a concrete reason", not "low priority".

### Priority Order

- P0: Current active block or item.
- P1: Kinetic basics, foundational processing, and item logistics loop.
- P2: Fluids, larger processing chains, and automation interactions.
- P3: Contraptions and Ponder or teaching system.
- P4: Embedded Create: Enchantment Industry content after upstream source is added.
- P5: Trains and railway systems.
- P6: Decorative and building blocks.

### Phase A0: Porting Foundation

Phase A0 happens before Shaft.

Required outcomes:

- Archive discarded prototype implementation outside the formal compile path.
- Establish the read-only upstream Create 6.0.8 snapshot.
- Establish porting documentation.
- Establish registry-derived status table skeletons.
- Establish source attribution and porting rules.
- Establish the new `com.simibubi.create.*` minimal source tree.
- Keep the project buildable with a minimal mod entry.
- Record that existing prototype code is not an implementation source of truth.

### Phase A1: Shaft

Shaft is the first formal porting unit.

Shaft must prove:

- Upstream source files can be traced and documented.
- Upstream class/module shape can be adapted into the formal source tree.
- Axis state can be represented in Minecraft 1.7.10 through metadata, TileEntity, or a documented hybrid.
- Registration, placement, breaking, language entry, basic visibility, and persistence can work in the new structure.
- A minimal kinetic connection contract exists for the next unit.

Shaft is not complete merely because an old prototype shaft exists.

### Phase A2: Value Settings UI Foundation

The Creative Motor must use a Create-like value setting interaction, so the Value Settings UI infrastructure is part of the first milestone.

The port should adapt the upstream Value Settings concepts where possible:

- Value setting screen behavior.
- Formatting and range handling.
- Client-side opening flow.
- Server packet submission.
- Block-targeted interaction.
- Legacy GUI/rendering bridge for 1.7.10.

The ideal result is close to the Create 6.0.8 world-space value box experience. A traditional legacy GUI is a fallback only if exact behavior is temporarily impossible and must be documented as such.

### Phase A3: Creative Motor

Creative Motor is the second formal porting unit.

Creative Motor completion requires:

- Upstream source trace and adaptation notes.
- Registration, placement, breaking, language entry, and visible rendering.
- Player-adjustable speed through the Value Settings interaction.
- Speed persistence through NBT.
- Client/server synchronization for changed speed.
- Kinetic source behavior that can drive Shaft.
- Compatibility decisions documented where upstream implementation cannot be reused directly.

### Phase A4: First Gameplay Closure

The first formal gameplay closure is Creative Motor -> Shaft.

The closure must prove:

- Creative Motor can provide speed.
- Shaft can receive and expose propagated speed.
- Disconnecting the Shaft or Motor clears the downstream speed.
- Axis and connection rules behave consistently.
- State survives a world save and reload where applicable.
- The test procedure is documented so later blocks can reuse the same style of validation.

### Kinetic System Direction

- The kinetic network should remain as close as practical to upstream Create semantics.
- Minecraft 1.7.10-specific data representation is allowed, but it must be documented as adaptation.
- Stress, overload, speed propagation, axis behavior, and source/consumer behavior are core compatibility concerns.
- Kinetic internals should not be designed around the discarded prototype if upstream design provides a better source.

### Rendering Direction

- 1.7.10 has no modern JSON blockstate/model pipeline.
- The port may use ISBRH, TESR, Tessellator, texture icons, and other legacy rendering systems.
- Rendering bridges belong in the legacy layer, while content-level rendering decisions should keep upstream intent visible.
- Flywheel is not an early requirement, but Create's visual identity remains important.

### Contraptions

- Contraptions are in scope but deferred.
- Do not force every early block to declare full contraption movement behavior yet.
- When contraptions are reached, introduce the necessary movement, snapshot, collision, entity, NBT, and rendering contracts as a focused phase.

### Embedded Enchantment Industry

- Create: Enchantment Industry is planned as built-in P4 content.
- It will use a separate upstream snapshot and separate status table once source is added to the workspace.
- Until the source is present, it remains a planned embedded extension rather than an implementation dependency.

### Public API

- The project should eventually expose a stable public API for addon authors.
- API boundaries should be introduced after the upstream-first structure is established and the first kinetic closure is proven.
- Internal code should not be prematurely frozen while the upstream package alignment is still changing.

### License And Attribution

- Create 6.0.8 is MIT licensed; source adaptation is allowed, but attribution must be clear.
- The mod must remain clearly labeled as an unofficial reverse port.
- The README, metadata, and attribution documents should identify upstream source, tag, authorship, and license.
- Future embedded extensions require their own license review before source is vendored or adapted.

## Testing Decisions

### Testing Philosophy

- Test external behavior rather than implementation details.
- A block or item is not complete until its player-visible behavior, persistence, registration, and integration seam are verified.
- Prototype tests are not formal proof for ported status unless they are rewritten against the new source tree and upstream-derived behavior.
- Each ported unit should have a documented manual test checklist even when automated Minecraft runtime tests are impractical.

### Primary Test Seam

The highest-value test seam is the ported block/item completion loop:

- Upstream trace exists.
- Active source exists under the formal package structure.
- Registration succeeds.
- In-game placement and breaking work.
- Essential state persists.
- Essential interaction works.
- Integration with adjacent already-ported units works.
- Port status is updated.

This seam should be reused for every block and item.

### First Automated/Manual Closure

The first closure test is Creative Motor -> Shaft.

It should verify:

- Motor speed can be changed by the player.
- The changed speed reaches the server.
- The motor stores the speed.
- A connected shaft receives speed.
- A disconnected shaft loses speed.
- World reload does not corrupt axis or speed configuration.

### Unit Tests

Unit tests are useful for:

- Lightweight adapters.
- Value formatting.
- Metadata/state conversion.
- Kinetic propagation rules that can be isolated from Minecraft runtime.
- Registry/status parsing tools.

Unit tests should avoid testing Minecraft internals directly.

### Manual Game Tests

Manual game tests are required for:

- Rendering.
- Block targeting and Value Settings interaction.
- TESR/ISBRH behavior.
- Client/server packet behavior.
- Real Forge 1.7.10 placement, breaking, and save/load behavior.

Manual test results should be recorded in the relevant status table or phase checklist.

## Out of Scope

Nothing from Create's own content is permanently out of scope by category at this PRD level.

The following are out of scope for the first milestone only:

- Full train system.
- Decorative and building block completion.
- Full contraption engine.
- Full Ponder system.
- Full fluid network.
- Full item logistics network.
- Full Create: Enchantment Industry integration.
- Full public addon API stability.
- Flywheel-equivalent renderer.
- Compatibility with high-version Create save data.

Create: Big Cannons, Create: Aeronautics, and other non-selected addon projects are not part of the immediate embedded plan unless later accepted explicitly.

## Further Notes

- The project remains a personal long-term port with no hard deadline.
- Phase labels are the project's milestone system; no separate 0.x/1.0 release policy is required right now.
- A public release should aim for a broadly complete Create port, not a tiny preview that permanently narrows scope.
- The first development priority is Phase A0, then Shaft, then Value Settings UI, then Creative Motor, then the Creative Motor -> Shaft closure.
- The current repository has uncommitted prototype changes. They should be preserved or archived carefully, not treated as authoritative source.
- If upstream source cannot be downloaded by the agent due to network restrictions, the user may add the upstream snapshot manually.
- The PRD replaces the previous "reference and rewrite" direction with an "adapt upstream source first" direction.
