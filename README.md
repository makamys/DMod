# D-Mod

![](https://raw.githubusercontent.com/makamys/DMod/master/docs/dmod_banner.png)

D-Mod is a mod that backports some features from later versions of Minecraft to 1.7.10. You could consider it an add-on to [Et Futurum Requiem](https://www.curseforge.com/minecraft/mc-mods/et-futurum-requiem) (though it's not a hard dependency).

Currently implemented:

* Foxes
* Bundles

## Foxes

Foxes also have some extra features implemented. Untamed, they behave the same as they do in new vanilla versions. But as they defeat enemies, they gain EXP and unlock various new abilities and AI improvements. The major ones being: getting healed when fed food, following the owner after being fed a sweet berry, and possessing Looting I intrinsically (does not stack with swords that have Looting).

Full documentation coming soon : ) For the time being you can look [here](https://github.com/makamys/DMod/blob/21d63d86e357626f3d212447e34f2f1fd4495cf6/src/main/java/makamys/dmod/entity/EntityFox.java#L1957-L1987) to get a general picture.

This system can be disabled, or configured to unlock all abilities from the start.

# Dependencies

* Item icons in bundle tooltips will only be drawn if [NEI](https://www.curseforge.com/minecraft/mc-mods/notenoughitems) is installed (I recommend the [GTNH fork](https://www.curseforge.com/minecraft/mc-mods/notenoughitems-gtnh)).
* [Et Futurum Requiem](https://www.curseforge.com/minecraft/mc-mods/et-futurum-requiem) is highly recommended as it backports sweet berries (useful for foxes) and rabbits (useful for bundles).

# License

This mod is licensed under [the Unlicense](https://github.com/makamys/DMod/blob/master/LICENSE). It largely consists of ported Mojang code though, so keep that in mind.
