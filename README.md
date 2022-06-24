# Open Parties and Claims

This Minecraft mod adds the ability to claim and to forceload world chunks, as well as create and manage player parties. It also gives server owners powerful controls over their players' usage of the mod's features.

The mod's API allows other mods or plugins to easily interact with the parties and the chunk claims, both on the server and the client side.

The mod is currently in beta. Only Forge 1.18.2 is supported as of writing this. Fabric/Quilt and Minecraft 1.19 support are coming soon.

# User Guide

## Playing

Official downloads of the mod as well as map integration for the mod in Xaero's Minimap and World Map mods are coming soon.

## Developing

_The license that this mod is released under, as I understand it, explicitly allows you have this mod as a dependency for your own mod and use the API through Java/JVM mechanics, 
under some conditions in some cases, mostly when embedding the mod in yours. That is why I chose it. It is very similar to what Minecraft Forge is currently released under. This paragraph is not a legal statement or legal advice. Make sure to get familiar with the actual license terms on your own._

Add this project as a dependency to your build.gradle, ~~for example with [CurseMaven](https://www.cursemaven.com/) or manually 
download a jar file from CurseForge and~~ use a flatDir repository. 

Build the mod jar from the source yourself until official downloads are available.

I strongly recommend that you use the [javadoc](https://thexaero.github.io/open-parties-and-claims/javadoc) for reference when working with the API. 
Locate either or both of the following 2 main API classes in the javadoc and you should be good to go from there:

`xaero.pac.client.api.OpenPACClientAPI`

`xaero.pac.common.server.api.OpenPACServerAPI`

# Contributions

I do not accept pull-requests at the moment, in case I decide to release this mod under a different license in the near future. Contributions from other people might complicate things too much.

# MultiLoader Template

This project uses a multi-loader template from [jaredlll08/MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template)

Fabric support is still a work-in-progress though.