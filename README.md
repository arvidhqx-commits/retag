# ReTag

Modern nametag prefixes & suffixes for Paper 1.21+. A drop-in successor to NametagEdit.

## Why
NametagEdit (2.2M downloads) has been unmaintained since December 2023 and was last tested on 1.20.
ReTag picks up where it left off: same workflow, automatic data import, built for current Paper.

## Features
- Prefix/suffix per **group** (permission-based, e.g. `retag.group.vip`) and per **player**
- **Tab-list sorting** by group weight
- **Auto-import** from an existing `plugins/NametagEdit/` folder on first start
- `/nte` alias — existing NametagEdit commands keep working
- Legacy `&` color codes **and** MiniMessage (`<gradient:...>`) supported
- Zero dependencies, one small jar

## Commands
```
/retag prefix <player> [value]     set/clear a player prefix
/retag suffix <player> [value]     set/clear a player suffix
/retag clear <player>              remove player overrides
/retag group list|add|remove|prefix|suffix|weight
/retag import                      re-run NametagEdit import
/retag reload
```
Permission: `retag.admin` (default: op)

## Build
JDK 21, `gradle shadowJar` → `build/libs/retag-x.y.z-all.jar`

## Tested on
Paper 1.21.11 and Paper 26.2 (runtime-tested, not just "it loads").

## License
MIT — see [LICENSE](LICENSE).

## Development note
This project is **AI-assisted**: the code is written with Claude under the direction, testing and
release approval of the maintainer. Every release is run against a live Paper server before it ships.
