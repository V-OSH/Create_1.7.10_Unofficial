# Porting workflow

Create 6.0.8 is ported one block or item at a time. The active unit is the only P0 unit.

## Unit completion loop

1. Trace the registry entry and all required upstream classes and assets.
2. Mark the unit `in-progress` in `status-overrides.csv`.
3. Adapt source into the matching `com.simibubi.create` package.
4. Keep Minecraft 1.7.10-only mechanics in a narrow `legacy` adapter.
5. Add automated behavior tests where the Forge runtime is not required.
6. Build the mod and complete the unit's manual in-game checklist.
7. Record incompatibilities and deferred upstream behavior in the unit note.
8. Mark the unit `ported` only after both automated and required manual checks pass.

Run `powershell -ExecutionPolicy Bypass -File porting/tools/Generate-PortStatus.ps1` to regenerate the full upstream registry inventory.

The first closure is Shaft, Value Settings, Creative Motor, then Creative Motor -> Shaft propagation.

Historical prototypes must be stored outside this Git repository. The `prototype/` path is ignored intentionally and must not be used as a source, test, or asset directory.
