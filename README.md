TellMe
=========================

TellMe is a small informational mod for Minecraft.
It is mainly meant for modpack makers or other users who need some technical type information
about the game or some settings.

Currently there is one command:
  * /tellme biome [ current | dump | list ]
    - 'current' prints some information about the biome the player is currently in
    - 'dump' prints the complete biome array to a timestamped file in the config/tellme/ directory
    - 'list' prints the same list into the game console

=====================================

Compilation and installation from source:

* git clone https://github.com/maruohon/tellme.git
* cd tellme
* gradlew build

Then copy the tellme-&lt;version&gt;.jar from build/libs/ into your Minecraft mods/ directory.
The mod needs Forge to be installed.
