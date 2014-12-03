TellMe
=========================

TellMe is a small informational mod for Minecraft.
It is mainly meant for modpack makers or other users who need some technical type information
about the game or some settings.

Currently implemented commands:
  * /tellme biome [ current | dump | list ]
    - 'current' prints some information about the biome the player is currently in
    - 'dump' prints the complete biome array to a timestamped file in the config/tellme/ directory
    - 'list' prints the same list into the game console
    - An example of the biome information dump: http://pastebin.com/raw.php?i=389akfmm
  * /tellme blockstats [ count | dump | query ]
    - '/tellme blockstats count &lt;playername&gt; &lt;x-distance&gt; &lt;y-distance&gt; &lt;z-distance&gt;' Counts blocks from an area around the given player.
    - '/tellme blockstats count &lt;dimension&gt; &lt;x-min&gt; &lt;y-min&gt; &lt;z-min&gt; &lt;x-max&gt; &lt;y-max&gt; &lt;z-max&gt;' Counts the blocks in an area in the specified dimension.
    - '/tellme blockstats dump' Dumps the block stats from the previous count command into a file in the config/tellme/ directory.
    - '/tellme blockstats dump modid:blockname[:meta] modid:blockname[:meta] ...' Like above, but only dumps the specified blocks.
    - '/tellme blockstats query' Prints the block stats from the previous count command into the server console.
    - '/tellme blockstats query modid:blockname[:meta] modid:blockname[:meta] ...' Like above, but only prints the specified blocks.
    - Supported filter formats for query and dump are: 'blockname' for vanilla stuff only, 'modid:blockname' or 'modid:blockname:meta' for everything.
    - An example of the blockstats dump output:&nbsp; http://pastebin.com/raw.php?i=tptt3pUH


* Entity and Block/TileEntity information:
  - You can get the NBT data of Entities and TileEntities by right clicking on them with a gold nugget. Some very basic information will be printed to the game chat, and the full NBT data for the object in question will be printed to the server console.
  - If you are sneaking while right clicking, then the information will be dumped into a timestamped file in the config/tellme/ directory.
  - An example of the data dump from a Zombie Pigman: http://pastebin.com/5jyZd0Jz
  - An example of the data dump from a custom mod block (TileEntity): http://pastebin.com/s67zt02J

* Item information:
  - You can get similar information about items. Right click with a Blaze Rod to print the information of the item that is to the right of the Blaze Rod in your hotbar.
  - Sneak + right click to dump the information to a timestamp file in the config/tellme/ directory.
  - An example of the data dump from a custom mod item with NBT data: http://pastebin.com/TnDYLHdN

=====================================

Compilation and installation from source:

* git clone https://github.com/maruohon/tellme.git
* cd tellme
* gradlew build

Then copy the tellme-&lt;version&gt;.jar from build/libs/ into your Minecraft mods/ directory.
The mod needs Forge to be installed.
