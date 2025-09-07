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

See [wikipage: How-to-Configure-Resourcepack-and-define.)](https://github.com/M0ch0/ClevelandCustomBlocks/wiki/How-to-Configure-Resourcepack-and-define)

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
    /ccbs give Steve ccbs:steel_block
    /ccbs give @a ccbs:cardboard_box 64
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

See [wikipage: For Advanced User Tips)]([https://github.com/M0ch0/ClevelandCustomBlocks/wiki/How-to-Configure-Resourcepack-and-define](https://github.com/M0ch0/ClevelandCustomBlocks/wiki/For-Advanced-User-Tips)


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
