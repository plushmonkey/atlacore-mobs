# atlacore-mobs
This is a simple implementation of bending mobs using atlacore. It was designed to work with Spigot 1.12.2, but it might work with older versions. Only firebending and airbending is implemented currently.  

The mob ai is extremely rudimentary. Most abilities are activated randomly, except in the case of spawned airbending mobs, which try to use mobility abilities to chase players. The plugin could serve as a decent base for building proper AI on top of it.  

This plugin was designed for a specific server, so a lot of it isn't configurable. It mostly exists as an example of how to extend atlacore, not as a plugin that's ready for general use.  

## Requirements
It requires atlacore to be installed on the server, but it doesn't require that players use it.  
Give players `-atla.bending` permission node if this is being used with ProjectKorra bending. That will disable atlacore for players.  

## Commands
`/acmobs` - Shows the available subcommands.  
`/acmobs reload` - Reloads the config file.  
`/acmobs spawn` - Spawns in a bending mob of a specific element and entity type.  
`/acmobs clearspawns` - Clears all existing bending mobs.  
`/acmobs forceraid` - Forces a fire raider event on a specific Towny town.  

## Permissions
`acmobs.command.[commandName]`  

## MobArena
Takes over a MobArena arena by turning all of the spawned entities into bending mobs.  
It's currently hardcoded to only work in the arena named `forest`. Setting it up to be configurable requires a small amount of development work.  

## Raid
Creates firebending raider mobs surrounding a random Towny town as a random event. They stay outside of the town so it isn't destructive.  
Rewards players for killing the raiders.  

## Examples
[Basic bending mobs](https://gfycat.com/NeighboringScratchyBluetonguelizard)  
[Using sneak-activated abilities](https://gfycat.com/ObedientRemoteHapuku)  
[Town raid](https://gfycat.com/VigorousOpulentGharial)  
