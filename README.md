[![downloads](https://img.shields.io/badge/-⬇%20releases-brightgreen)](https://github.com/makamys/DMod/releases)
[![CurseForge](https://shields.io/badge/CurseForge-555555?logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/dmod)

# D-Mod

![](https://raw.githubusercontent.com/makamys/DMod/master/docs/dmod_banner.png)

D-Mod is a mod that backports some features from later versions of Minecraft to 1.7.10. You could consider it an add-on to [Et Futurum Requiem](https://www.curseforge.com/minecraft/mc-mods/et-futurum-requiem) (though it's not a hard dependency).

Currently implemented:

* Foxes
* Bundles

## Foxes

Foxes have some (optional) extra features implemented. Untamed, they behave the same as they do in new vanilla versions. But as they defeat enemies, they gain EXP and unlock various new abilities and AI improvements.

The major ones are getting healed when fed food, following the owner after being fed a sweet berry, and possessing Looting I intrinsically (does not stack with swords that have Looting). Foxes pass down their EXP to their children.

More info [in the wiki](https://github.com/makamys/DMod/wiki/Fox).

# Dependencies

* Item icons in bundle tooltips will only be drawn if [NEI](https://www.curseforge.com/minecraft/mc-mods/notenoughitems) is installed (I recommend the [GTNH fork](https://www.curseforge.com/minecraft/mc-mods/notenoughitems-gtnh)).
* [Et Futurum Requiem](https://www.curseforge.com/minecraft/mc-mods/et-futurum-requiem) is highly recommended as it backports sweet berries (useful for foxes) and rabbits (useful for bundles).

# Incompatibilities

* [Hodgepodge](https://github.com/GTNewHorizons/Hodgepodge): set `preventPickupLoot=false` or else foxes won't be able to pick up items
* [BugTorch](https://github.com/jss2a98aj/BugTorch): crashes in GTNH if certain mixins to the Block class are enabled while D-Mod's `lootingFoxFix` is enabled. (The exact list is unknown. The culprit seems to be Witching Gadgets.)
* [Backlytra](https://github.com/unascribed/Backlytra): crashes if D-Mod's `lootingFoxFix` is enabled. Fixed by [Mixingasm](https://github.com/makamys/Mixingasm).

# License

This mod is licensed under [the Unlicense](https://github.com/makamys/DMod/blob/master/LICENSE). It largely consists of ported Mojang code though, so keep that in mind.

# Contributing

When running in an IDE, add these program arguments
```
--tweakClass org.spongepowered.asm.launch.MixinTweaker --mixin dmod.mixin.json
```
