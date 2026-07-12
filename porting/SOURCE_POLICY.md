# Source and attribution policy

## Authoritative baseline

- Project: Create
- Upstream tag: `mc1.20.1-6.0.8`
- Upstream repository: <https://github.com/Creators-of-Create/Create>
- License: MIT, preserved at `upstream/create-mc1.20.1-6.0.8/LICENSE.md`

## Porting rules

- Start from upstream source and preserve class names, responsibilities, and package placement when practical.
- Change code only for Minecraft 1.7.10, Forge 1.7.10, Java/runtime, registry, networking, rendering, or unavailable dependency constraints.
- Put gameplay behavior in upstream-like packages. Put metadata, old rendering, old networking, and API shims in explicitly named legacy adapters.
- Add a source header to substantially adapted Java files. Record the exact upstream files and compatibility changes in the unit note.
- Do not edit or format the vendored snapshot.
- Do not claim archived prototype code as a completed port.
- Review every embedded addon license before vendoring or adapting its source.

This project is unofficial and is not endorsed by the upstream Create team.
