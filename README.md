# Open Parties and Claims

This Minecraft mod adds the ability to claim and to forceload world chunks, as well as create and manage player parties. It also gives server 
owners powerful controls over their players' usage of the mod's features.

The mod's API allows other mods or plugins to easily interact with the parties and the chunk claims, both on the server and the client side.

The mod is currently in beta. 

# Dependencies

When using Fabric or Quilt, this mod depends on the [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) (or the [Quilted Fabric API](https://www.curseforge.com/minecraft/mc-mods/qsl)) and the [Forge Config API Port](https://www.curseforge.com/minecraft/mc-mods/forge-config-api-port-fabric).
You also need the Forge Config API Port mod to run Forge versions of the mod starting at Minecraft version 1.20.4.

# User Guide

## Playing or running a server

Get this mod from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/open-parties-and-claims) or [Modrinth](https://modrinth.com/mod/open-parties-and-claims). 
It is also recommended to get the Xaero's [Minimap](https://www.curseforge.com/minecraft/mc-mods/xaeros-minimap) and 
the [World Map](https://www.curseforge.com/minecraft/mc-mods/xaeros-world-map) mods, which implement this mod's API. 
Install this mod how you normally would a Forge/Fabric mod. There are plenty of tutorials online, if you're new.

Please read through the mod's [description on Modrinth](https://modrinth.com/mod/open-parties-and-claims) for the general info on how to use the mod and concrete answers to frequently asked questions that cover the most common use cases for the mod.

## Developing

_The license that this mod is released under, as I understand it, explicitly allows you to have this mod as a dependency for your own mod and 
use the API through Java/JVM mechanics, under some conditions in some cases, mostly when embedding the mod in yours. That is why I chose it. 
It is very similar to what Minecraft Forge is currently released under. This paragraph is not a legal statement or legal advice. Make sure to 
get familiar with the actual license terms on your own._

Add this project as a dependency to your build.gradle, for example using my Maven repository (https://chocolateminecraft.com/maven), as explained further below, or with [CurseMaven](https://www.cursemaven.com/) or manually 
download a jar file from CurseForge and use a flatDir repository. 
Build the mod jar from the source code yourself if you so prefer.

I strongly recommend that you use the [javadoc](https://thexaero.github.io/open-parties-and-claims/javadoc) for reference when working with the API. 
Locate either or both of the following 2 main API classes in the javadoc and you should be good to go from there:

`xaero.pac.client.api.OpenPACClientAPI`

`xaero.pac.common.server.api.OpenPACServerAPI`

### Using my Maven repository

The recommended way to start developing addons/mods that implement this mod's API is by using my Maven repository to fetch OPAC as a dependency.
Add the following to your build script to use the repository:
```
repositories {
    maven {
        url "https://chocolateminecraft.com/maven"
        name "Xaero's Maven"
    }
}
```

If you're on Forge, make sure the MixinGradle plugin is applied in your project (org.spongepowered.mixin), unless you're on Minecraft 26.1 or newer.

Finally, add one or more of the following dependency declarations, replacing `<minecraft version>` and `<mod version>` with actual values you can see at [chocolateminecraft.com/maven/xaero/pac](https://chocolateminecraft.com/maven/xaero/pac).

Forge with official Mojang mappings before 1.21.1:

```
implementation "xaero.pac:open-parties-and-claims-forge-<minecraft version>:<mod version>:dev"//purposely no deobfuscation!
```

Forge with official Mojang mappings on 1.21.1 or after:

```
implementation "xaero.pac:open-parties-and-claims-forge-<minecraft version>:<mod version>"
```

Forge with other mappings:

```
implementation fg.deobf("xaero.pac:open-parties-and-claims-forge-<minecraft version>:<mod version>")
```

Fabric:

```
modImplementation "xaero.pac:open-parties-and-claims-fabric-<minecraft version>:<mod version>"
```

NeoForge:

```
implementation "xaero.pac:open-parties-and-claims-neoforge-<minecraft version>:<mod version>"
```

In "common" projects in multi-loader setups:

```
compileOnly "xaero.pac:open-parties-and-claims-common-<minecraft version>:<mod version>"
```

# Contributions

I do not accept pull-requests at the moment, in case I decide to release this mod under a different license in the near future. 
Contributions from other people might cause complications.

# MultiLoader Template

This project uses a multi-loader template from [jaredlll08/MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template)
