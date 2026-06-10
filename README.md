# Modern Energy API

Modern Energy is a Fabric 1.21.10 core mod for testing and sharing a small
Mechatronic Energy (ME) API.

The first version is intentionally developer-focused: it provides the energy
storage API, an in-memory debug command set, and unit tests. It does not add
player-facing machines, items, blocks, or an energy network yet.

## VSCode

1. Install the recommended Java extensions when VSCode prompts you.
2. Open this folder in VSCode.
3. Run the task `Modern Energy: prepare VSCode launch` once if launch
   configurations are missing or stale.
4. Open Run and Debug, then start `Minecraft Client`.

The workspace settings point VSCode and Gradle at JDK 21:
`C:\Program Files\Java\jdk-21`.
