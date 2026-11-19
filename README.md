# Everything Sell

**Developer:** Ahmed
**Java:** 21 (builds and runs on Java 21 and Java 25)
**Platform:** Spigot / Paper (automatic runtime registry detection)
**Dependencies:** Vault + an economy provider (LuckPerms optional)

---

## Features

- **Automatic material detection** – scans the running server's registry on startup and splits valid materials into `prices/blocks.yml` and `prices/items.yml`.
- **Safe price synchronization** – new materials are added without overwriting prices you already configured.
- **No permission needed for players** – everyone can use `/sell` and sell items; LuckPerms is not required.
- **Configurable command** – change the main command and aliases in `commands.yml`.
- **Sell GUI + optional confirmation GUI** – with protection against item duplication and item loss.
- **Sell result title/subtitle** – small-caps text, configurable icon (`✓`, `◆`, `★`, `$`) and colors.
- **Configurable, validated sounds** for GUI open, confirmation, confirm, cancel, success and error.

## Configuration files

```text
plugins/EverythingSell/
├── config.yml
├── messages.yml
├── commands.yml
├── gui.yml
└── prices/
    ├── blocks.yml
    └── items.yml
```

## Permissions

| Permission              | Description                                     | Default      |
| :---------------------- | :---------------------------------------------- | :----------- |
| *(none)*                | Use `/sell`, open the GUI and sell items        | **Everyone** |
| `everythingsell.reload` | Reload configuration and price tables           | `op`         |
| `everythingsell.admin`  | Full administrative access                      | `op`         |

## Commands

| Command          | Description                          |
| :--------------- | :----------------------------------- |
| `/sell`          | Open the Everything Sell GUI         |
| `/sell reload`   | Reload configs and prices (admin)    |

Aliases: `/esell`, `/everythingsell`.

## Building from source

Requires JDK 21 or newer.

```bash
# If you have Gradle installed
gradle clean build

# Or, after generating the wrapper once (needs Gradle installed)
gradle wrapper --gradle-version 8.14
./gradlew clean build
```

The compiled plugin is created at `build/libs/EverythingSell.jar`.

## GitHub Actions

Push the project to GitHub. The workflow in `.github/workflows/build.yml` builds with Java 21 and 25 and uploads `EverythingSell.jar` as an artifact.

## License

MIT – see `LICENSE`.
