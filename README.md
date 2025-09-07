# ClevelandCustomBlocks
A simple, lightweight, clean and robust custom block plugin
<img width="854" height="480" alt="2025-09-07_13 50 23" src="https://github.com/user-attachments/assets/2ade2300-2624-48f6-b1e7-e89f27d41bed" />

Source code: AGPL-3.0-or-later (see LICENSE).
Artwork & media: © 2025 M0ch0. All rights reserved. Not covered by the AGPL.

## For Normal Users

### Introduction
- Compatible with Paper 1.21.4 and later.
- Built with Kotlin/JDK 21

### How to Install
1. Download / build the plugin JAR (named `ClevelandCustomBlocks-<version>.jar`).
2. Drop it into your server’s plugins/ folder.
3. Start the server once. The plugin will create its data folder and copy a default define.yml into:
   `plugins/ClevelandCustomBlocks/define.yml`
4. (Optional but recommended) Prepare a resource pack (see "How to build resourcepack" below) so your custom blocks render as intended.
5. Make sure your server is running Paper 1.21.4+ and clients use the same resource pack. (or just use force server-resourcepack)

### How to Configure
All custom blocks live in `plugins/ClevelandCustomBlocks/define.yml`

#### Minimal example (define.yml)
```yaml
packName: ccbs
blocks:
  steel_block:
    displayName: "Steel Block" #require, string
    originalBlock: STONE #require, Bukkit.Material
    orientation: NONE #optional, [NONE|FACE|STAIRS_LIKE]

  steel_stairs:
    displayName: "Steel Stairs"
    originalBlock: STONE_STAIRS
    orientation: STAIRS_LIKE

  cardboard_box:
    displayName: "Cardboard Box"
    originalBlock: STONE
    orientation: FACE
    action: #optional
      - as: player #require (If you set an action), [PLAYER|SERVER]
        run: "/me $clicker right-clicked a CustomBlock!" #require (If you set an action), runnable command
      - as: server
        run: "/execute at $clicker run playsound minecraft:entity.cat.ambient master @a ~ ~ ~ 20"

  # advanced_workbench:
  #   displayName: "Advanced Workbench"
  #   originalBlock: STRIPPED_OAK_LOG
  #   orientation: FACE # Orientation is Optional
  #   action
  #     - as: server
  #       run: "/exactcraft forceopen $clicker"
  #
```
#### Fields
- `packName` — your logical namespace for item models (e.g., `ccbs`).
- `blocks.<key>` — the short id within the pack (full id becomes `packName:key`, e.g., `ccbs:steel_block`).

    - `displayName` — the in-hand/in-inventory name users see.
    - `originalBlock` — the vanilla **block** id to base on (e.g., `STONE`, `STRIPPED_OAK_LOG`).
      The original block can be anything **(as long as it’s a block!)**.
    - `action` (optional) — list of click actions:

        - `as: player | server`
        - `run: "/your command here"`
          You can use `$clicker` to substitute the clicking player’s name.
> [!TIP]
> On reload, invalid entries are reported as warnings in console and summarized to the command sender.

### How to build resourcepack

You’ll use **string-based** `custom_model_data` selectors introduced with 1.21 to map your logical ids (like `ccbs:steel`) to item models.

Create one model file per block in your own namespace, then a selector under `minecraft` that routes based on `custom_model_data`.

```
assets/{yourpackname}/models/item/{name}.json
```

```json
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "{yourpackname}:block/{source}"
  }
}
```
> [!NOTE]
> The model above is a simple “blocky cube” that reuses a single texture (`{yourpackname}:block/{source}`), which you should also include in your pack under `assets/{yourpackname}/textures/block/{source}.png`.

Now add a **selector** for the original base block the plugin uses (so Minecraft knows when to swap the model).

```
assets/minecraft/items/{original_block}.json
```

```json
{
  "model": {
    "type": "select",
    "property": "custom_model_data",
    "fallback": {
      "type": "model",
      "model": "block/{original_block}"
    },
    "cases": [
      {
        "when": "{packname}:{blockname}",
        "model": {
          "type": "model",
          "model": "{packname}:item/{blockname}"
        }
      },
      {
        "when": "{packname}:{blockname2}",
        "model": {
          "type": "model",
          "model": "{packname}:item/{blockname2}"
        }
      }
    ]
  }
}
```

- Replace:
    - `{packname}` with your `packName` from `define.yml` (e.g., `ccbs`)
    - `{blockname}` with your block key (e.g., `steel`)
    - `{blockname2}` for more cases
    - `{original_block}` with the vanilla block you set in `originalBlock` (e.g., `stone` **lowercase** in model paths)
    - `{source}` with your texture name (e.g., `steel`)

> [!NOTE]
> You can repeat the `cases` entry for as many custom blocks as you want that share the same `originalBlock`.

Finally, ship/distribute the pack to clients (via server resource pack or manually).


### How to Use


#### Commands
- **Permissions**
    - `clevelandcustomblocks.use`: All Commands (default: OP)

- **Give Items**
    ```
    /ccbs give <player|@selector> <pack:id> [amount]
    ```
  
    Examples:

    ```
    /ccbs give Steve ccbs:steel
    /ccbs give @a ccbs:cardboard 64
    ```
- **Admin utilities**
    ```
    /ccbs reload
    ```

    Reloads `define.yml` and reports changes/warnings.

    ```
    /ccbs chunk GET
    /ccbs chunk CLEANUP
    ```

    Summarize or forcibly clean all registered custom blocks in the current chunk.

#### Play

* **Place**
  Just place it like a normal block. The plugin:

    1. Replaces the target block with an invisible **collision** block (`BARRIER`).
    2. Spawns an `ItemDisplay` at the block center showing your model.

* **Interact**

    * **Right-click** the block to run configured **actions** (as player/server).
    * **Left-click** the block to **remove** it.

        * In **Creative**, it removes without dropping.
        * In **Survival**, it drops the corresponding item.


## For Advanced User

### How does this work?

* **Placement**

    * Saves a **chunk-local index** of custom block positions in the chunk’s PDC (compact 3-byte encoding via `PackedRelativePos`).
    * Sets the real block to a collision **BARRIER** (domain `CollisionBlock`) and spawns a persistent `ItemDisplay` with your item model (amount set to 1).
    * Stores a **two-way link** in the `ItemDisplay`’s PDC:

        * `link-world-uuid`
        * `link-block-xyz` (int\[3])
* **Lookup & cleanup**

    * On **EntityRemove** and **Chunk/EntitiesLoad**, the plugin validates links and fixes or cleans orphans:

        * If a registered position lacks a display → remove collision block + unregister.
        * If a display exists but the block isn’t collision anymore → remove display + unregister.
* **Actions** (`action:` in YAML)

    * Executed on **right-click**. Run **as player** or **as server**.
    * `$clicker` is substituted with the player name. (If a command dispatch fails, the player is notified; OPs also get a hint to check console.)
* **String-based `custom_model_data` (1.21+)**

    * Items created by `/ccbs give` embed a **string** id (`packName:blockKey`) so your **selector** model can route to the right item model without magic numbers.
* **Public API**

    * A `ClevelandCustomBlocksService` is registered with Bukkit’s `ServicesManager` at plugin startup.
    * Other plugins can:

        * create items from ids
        * place/remove
        * query links/index

**Kotlin example (from another plugin):**

```kotlin
val service = server.servicesManager.load(io.github.m0ch0.clevelandCustomBlocks.api.service.ClevelandCustomBlocksService::class.java)
val item = service?.createItem("ccbs:steel", 8)
if (item != null) player.inventory.addItem(item)
```

---

### Replace or add `messages_**.properties` in the JAR

The plugin ships with English (US) and Japanese bundles:

```
io/github/m0ch0/clevelandCustomBlocks/i18n/messages_en_US.properties
io/github/m0ch0/clevelandCustomBlocks/i18n/messages_ja_JP.properties
```

The Adventure translator loads resource bundles from the **plugin classpath**. To customize or add locales:

#### Option A — Build from source (recommended)

1. Edit or add files under
   `src/main/resources/io/github/m0ch0/clevelandCustomBlocks/i18n/messages_<locale>.properties`
2. Build:

   ```
   ./gradlew clean build
   ```
3. Use the produced JAR in `build/libs/`.

#### Option B — Patch the jar (quick hack)

1. Open the plugin JAR with a zip tool.
2. Navigate to
   `io/github/m0ch0/clevelandCustomBlocks/i18n/`
3. Replace `messages_en_US.properties` / `messages_ja_JP.properties`, or add `messages_<your_LOCALE>.properties`.
4. Save the JAR and restart the server.

> [!IMPORTANT]
> **Keys** are stable and live in code under `MsgKey`. Keep placeholders (`<arg:0>`, etc.) intact.


## Development

* **JDK**: 21
* **Gradle**: Kotlin DSL + Shadow (relocations for ACF/McCoroutine/Coroutines)
* **Lint**: Detekt (`./gradlew detekt`)
* **Run Dev Server**:

  ```
  ./gradlew runServer
  ```

  (Configured for Minecraft 1.21 in `build.gradle.kts`)


## Troubleshooting

* **Placed block shows as barrier / no model**
  Ensure the client has your resource pack and the **selector model** exists under the correct path for your `originalBlock`.
* **Right-click does nothing**
  Make sure the block definition has an `action:` section and your commands are valid for your server/plugins. `$clicker` becomes the player name.
* **Items don’t stack as expected**
  The plugin respects the base item’s `maxStackSize` and splits large grants into multiple stacks.
