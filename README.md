[![downloads](https://img.shields.io/badge/-⬇%20releases-brightgreen)](https://github.com/makamys/DMod/releases)
[![CurseForge](https://shields.io/badge/CurseForge-555555?logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/dmod)

# D-Mod

![](https://raw.githubusercontent.com/makamys/DMod/master/docs/dmod_banner.png)

D-Mod is a mod that backports some features from later versions of Minecraft to 1.7.10. You could consider it an add-on to [Et Futurum Requiem](https://www.curseforge.com/minecraft/mc-mods/et-futurum-requiem) (though it's not a hard dependency).

Currently implemented:

* Foxes
* Bundles

## Foxes

Foxes also have some extra features implemented. Untamed, they behave the same as they do in new vanilla versions. But as they defeat enemies, they gain EXP and unlock various new abilities and AI improvements. The major ones being: getting healed when fed food, following the owner after being fed a sweet berry, and possessing Looting I intrinsically (does not stack with swords that have Looting). Foxes pass down their EXP to their children.

Full documentation coming soon : ) For the time being you can look [here](https://github.com/makamys/DMod/blob/21d63d86e357626f3d212447e34f2f1fd4495cf6/src/main/java/makamys/dmod/entity/EntityFox.java#L1957-L1987) to get a general picture.

This system can be disabled, or configured to unlock all abilities from the start.

# Dependencies

* Item icons in bundle tooltips will only be drawn if [NEI](https://www.curseforge.com/minecraft/mc-mods/notenoughitems) is installed (I recommend the [GTNH fork](https://www.curseforge.com/minecraft/mc-mods/notenoughitems-gtnh)).
* [Et Futurum Requiem](https://www.curseforge.com/minecraft/mc-mods/et-futurum-requiem) is highly recommended as it backports sweet berries (useful for foxes) and rabbits (useful for bundles).

# Incompatibilities

* [Hodgepodge](https://github.com/GTNewHorizons/Hodgepodge): set `preventPickupLoot=false` or else foxes won't be able to pick up items
* [BugTorch](https://github.com/jss2a98aj/BugTorch): crashes in GTNH if certain mixins to the Block class are enabled while D-Mod's `lootingFoxFix` is enabled. (The exact list is unknown. The culprit seems to be Witching Gadgets.)
* [Backlytra](https://github.com/unascribed/Backlytra): crashes if D-Mod's `lootingFoxFix` is enabled. This option will automatically get disabled if Backlytra is present since there's no other workaround for this.

# License

This mod is licensed under [the Unlicense](https://github.com/makamys/DMod/blob/master/LICENSE). It largely consists of ported Mojang code though, so keep that in mind.
