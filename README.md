# Lamb Lanterns

A NeoForge companion mod for [LambDynamicLights](https://lambdaurora.dev/projects/lambdynamiclights),
Minecraft **1.21.1**. Wear a vanilla lantern (or soul lantern) in a Curios
dedicated **lantern** slot — it hangs from the right hip, swings on a damped 2D
pendulum, and emits real dynamic light through the LDL API.

## Requirements

| What                | Version            |
| ------------------- | ------------------ |
| Minecraft           | 1.21.1             |
| NeoForge            | 21.1.228+          |
| Curios              | 9.0.0+             |
| LambDynamicLights   | 4.x — optional, client-only (the whole point) |

The mod must be installed on **both server and client**:

- **Server** registers the belt slot and accepts lanterns into it (Curios
  inventory is server-authoritative).
- **Client** renders the lantern on the body and handles the optional
  LambDynamicLights light emission.

## Features

- Adds a Curios `lantern` slot attached to the player, accepting any item
  in the `#curios:lantern` tag (vanilla lantern + soul lantern out of the box).
  The slot has its own faint lantern silhouette icon.
- Renders the lantern hanging from the right hip with chain attachment.
- 2D damped-spring pendulum: swings forward/back when you walk and
  side-to-side when you strafe; gets a kick when you start sneaking.
- LambDynamicLights integration via the LDL API and the Yumi entrypoint
  system — the worn lantern emits dynamic light as if held. The class is
  loaded lazily, so the mod runs fine without LDL.

## Installation

Drop `lamblanterns-<version>.jar` into the `mods/` folder of both your
server and your client. Make sure Curios is also installed.

## Building from source

Requires JDK 21.

```sh
./gradlew build
```

The output jar lands in `build/libs/lamblanterns-<version>.jar`.

## Tagging more items as wearable belt items

You can extend `#curios:lantern` from another datapack to make any item
belt-eligible. The lantern renderer only renders vanilla lanterns; other
items will sit invisibly in the slot unless they ship their own
`ICurioRenderer`.

## Credits

The body-part anchored rendering technique and the pendulum physics
shape are adapted from
[ImmersiveLanterns](https://github.com/TonimatasDEV/ImmersiveLanterns)
by Toni — go check it out, it's a much fuller implementation.

## License

[MIT](LICENSE)
