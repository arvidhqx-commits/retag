# ReTag

**Nametag prefixes, suffixes and tab sorting for Paper 1.21+ and 26.x — a drop-in successor to NametagEdit.**

---

## Why this plugin exists

[NametagEdit](https://www.spigotmc.org/resources/nametagedit.3836/) is the plugin most servers used for
coloured nametags — 2.2 million downloads. It has had no update since December 2023 and its last tested
platform was Minecraft 1.20.

ReTag picks up exactly where it left off: the same workflow, the same commands, and an automatic import of
your existing data — but built and tested against current Paper.

## Features

- **Group prefixes/suffixes** driven by permissions (`retag.group.vip`, `retag.group.admin`, …)
- **Per-player overrides** that win over the group value
- **Tab-list sorting** by group weight — staff on top, guests at the bottom
- **Automatic NametagEdit import**: if a `plugins/NametagEdit/` folder exists, ReTag reads it on first start
- **`/nte` alias** — your old commands and your muscle memory keep working
- **MiniMessage *and* legacy `&` colours** — `<gradient:#ff0000:#00ff00>` works, `&c` works
- No dependencies, no ProtocolLib, one small jar

## Commands

| Command | What it does |
|---|---|
| `/retag prefix <player> [value]` | Set or clear a player prefix |
| `/retag suffix <player> [value]` | Set or clear a player suffix |
| `/retag clear <player>` | Remove all overrides for a player |
| `/retag group list\|add\|remove` | Manage groups |
| `/retag group prefix\|suffix\|weight <group> <value>` | Configure a group |
| `/retag import` | Re-run the NametagEdit import |
| `/retag reload` | Reload the config |

Permission: `retag.admin` (default: op). Group membership: `retag.group.<name>`.

## Compatibility

Built for the Paper API 1.21 and up. Every release is started on a **live Paper 1.21.11 server and a live
Paper 26.2 server** and the actual behaviour is checked — not just "the plugin loads".

## Updates

This project exists to stay current. When Minecraft and Paper ship a new version, a compatibility check and,
if needed, a new build follow quickly — that is the entire point of ReTag rather than another abandoned
nametag plugin.

## Source & licence

MIT licensed, source on [GitHub](https://github.com/arvidhqx-commits/retag). Issues and pull requests welcome.

## Development note

This project is **AI-assisted**: the code is written with Claude under the direction, testing and release
approval of the maintainer. Every release is run against a live Paper server before it ships.
