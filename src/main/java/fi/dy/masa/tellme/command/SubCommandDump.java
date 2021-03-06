package fi.dy.masa.tellme.command;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import com.google.common.collect.Sets;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import fi.dy.masa.tellme.datadump.*;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.datadump.OreDictionaryDump.OreDumpType;
import fi.dy.masa.tellme.util.EntityInfo;

public class SubCommandDump extends SubCommand
{
    public SubCommandDump(CommandTellme baseCommand)
    {
        super(baseCommand);

        this.addSubSubCommands();
    }

    @Override
    public String getName()
    {
        return "dump";
    }

    protected void addSubSubCommands()
    {
        this.subSubCommands.add("all");
        this.subSubCommands.add("advancements-simple");
        this.subSubCommands.add("biomes");
        this.subSubCommands.add("biomes-with-colors");
        this.subSubCommands.add("biomes-with-mob-spawns");
        this.subSubCommands.add("biomes-id-to-name");
        this.subSubCommands.add("block-props");
        this.subSubCommands.add("blocks");
        this.subSubCommands.add("blocks-id-to-registryname");
        this.subSubCommands.add("blocks-with-nbt");
        this.subSubCommands.add("blockstates-by-block");
        this.subSubCommands.add("blockstates-by-state");
        this.subSubCommands.add("commands");
        this.subSubCommands.add("creativetabs");
        this.subSubCommands.add("dimensions");
        this.subSubCommands.add("enchantments");
        this.subSubCommands.add("entities");
        this.subSubCommands.add("entities-with-class");
        this.subSubCommands.add("fluids");
        this.subSubCommands.add("food-items");
        this.subSubCommands.add("items");
        this.subSubCommands.add("items-craftable");
        this.subSubCommands.add("items-plantable");
        this.subSubCommands.add("items-with-nbt");
        this.subSubCommands.add("items-with-tool-classes");
        this.subSubCommands.add("mod-list");
        this.subSubCommands.add("musictypes");
        this.subSubCommands.add("oredictionary-by-key");
        this.subSubCommands.add("oredictionary-by-key-individual");
        this.subSubCommands.add("oredictionary-by-item");
        this.subSubCommands.add("players");
        this.subSubCommands.add("player-nbt");
        this.subSubCommands.add("potions");
        this.subSubCommands.add("potiontypes");
        this.subSubCommands.add("soundevents");
        this.subSubCommands.add("spawneggs");
        this.subSubCommands.add("tileentities");
        this.subSubCommands.add("villagerprofessions");
        this.subSubCommands.add("villagertrades");
        this.subSubCommands.add("worldtypes");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length >= 1)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, this.getSubCommands());
        }

        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        super.execute(server, sender, args);

        if (args.length >= 1)
        {
            Set<String> types = Sets.newHashSet(args);

            // Don't bother outputting anything else a second time, if outputting everything once anyway
            if (types.contains("all"))
            {
                for (String arg : this.subSubCommands)
                {
                    if (arg.equals("all") == false && arg.equals("help") == false)
                    {
                        this.outputData(server, sender, arg);
                    }
                }
            }
            else
            {
                for (String arg : types)
                {
                    this.outputData(server, sender, arg);
                }
            }
        }
    }

    protected void outputData(MinecraftServer server, ICommandSender sender, String arg) throws CommandException
    {
        Format format = this.getName().endsWith("-csv") ? Format.CSV : Format.ASCII;
        List<String> data = this.getData(arg, format, sender);

        if (data == null)
        {
            this.sendMessage(sender, "Non-existing dump type: '" + arg + "'");
            return;
        }

        if (data.isEmpty())
        {
            this.sendMessage(sender, "No data available for dump: '" + arg + "'");
            return;
        }

        if (this.getName().startsWith("dump"))
        {
            File file = DataDump.dumpDataToFile(arg, data, format);

            if (file != null)
            {
                sendClickableLinkMessage(sender, "Output written to file %s", file);
            }
        }
        else if (this.getName().startsWith("list"))
        {
            DataDump.printDataToLogger(data);
            this.sendMessage(sender, "Command output printed to console");
        }
    }

    @Nullable
    private List<String> getData(String type, Format format, ICommandSender sender)
    {
        switch (type)
        {
            case "advancements-simple":             return AdvancementDump.getFormattedAdvancementDumpSimple(format, sender);
            case "biomes":                          return BiomeDump.getFormattedBiomeDump(format, false);
            case "biomes-with-colors":              return BiomeDump.getFormattedBiomeDump(format, true);
            case "biomes-with-mob-spawns":          return BiomeDump.getFormattedBiomeDumpWithMobSpawns(format);
            case "biomes-id-to-name":               return BiomeDump.getBiomeDumpIdToName(format);
            case "block-props":                     return BlockDump.getFormattedBlockPropertiesDump(format);
            case "blocks":                          return BlockDump.getFormattedBlockDump(format, false);
            case "blocks-id-to-registryname":       return BlockDump.getBlockDumpIdToRegistryName(format);
            case "blocks-with-nbt":                 return BlockDump.getFormattedBlockDump(format, true);
            case "blockstates-by-block":            return BlockStatesDump.getFormattedBlockStatesDumpByBlock();
            case "blockstates-by-state":            return BlockStatesDump.getFormattedBlockStatesDumpByState(format);
            case "commands":                        return CommandDump.getFormattedCommandDump(format, sender);
            case "creativetabs":                    return CreativetabDump.getFormattedCreativetabDump(format);
            case "dimensions":                      return DimensionDump.getFormattedDimensionDump(format);
            case "enchantments":                    return EnchantmentDump.getFormattedEnchantmentDump(format);
            case "entities":                        return EntityDump.getFormattedEntityDump(format, false);
            case "entities-with-class":             return EntityDump.getFormattedEntityDump(format, true);
            case "fluids":                          return FluidRegistryDump.getFormattedFluidRegistryDump(format);
            case "food-items":                      return FoodItemDump.getFormattedFoodItemDump(format);
            case "items":                           return ItemDump.getFormattedItemDump(format, ItemDump.INFO_BASIC);
            case "items-craftable":                 return ItemDump.getFormattedCraftableItemsDump(format);
            case "items-plantable":                 return ItemDump.getFormattedItemDump(format, ItemDump.INFO_PLANTABLES);
            case "items-with-nbt":                  return ItemDump.getFormattedItemDump(format, ItemDump.INFO_NBT);
            case "items-with-tool-classes":         return ItemDump.getFormattedItemDump(format, ItemDump.INFO_TOOL_CLASS);
            case "mod-list":                        return ModListDump.getFormattedModListDump(format);
            case "musictypes":                      return SoundEventDump.getFormattedMusicTypeDump(format);
            case "oredictionary-by-key":            return OreDictionaryDump.getFormattedOreDictionaryDump(format, OreDumpType.BY_ORE_GROUPED);
            case "oredictionary-by-key-individual": return OreDictionaryDump.getFormattedOreDictionaryDump(format, OreDumpType.BY_ORE_INDIVIDUAL);
            case "oredictionary-by-item":           return OreDictionaryDump.getFormattedOreDictionaryDump(format, OreDumpType.BY_STACK);
            case "players":                         return EntityInfo.getPlayerList(format);
            case "player-nbt":                      return (sender instanceof EntityPlayer) ? EntityInfo.getFullEntityInfo((EntityPlayer) sender) : Collections.emptyList();
            case "potions":                         return PotionDump.getFormattedPotionDump(format);
            case "potiontypes":                     return PotionTypeDump.getFormattedPotionTypeDump(format);
            case "soundevents":                     return SoundEventDump.getFormattedSoundEventDump(format);
            case "spawneggs":                       return SpawnEggDump.getFormattedSpawnEggDump(format);
            case "tileentities":                    return TileEntityDump.getFormattedTileEntityDump(format);
            case "villagerprofessions":             return VillagerProfessionDump.getFormattedVillagerProfessionDump(format);
            case "villagertrades":                  return VillagerTradesDump.getFormattedVillagerTradesDump(format);
            case "worldtypes":                      return WorldTypeDump.getFormattedWorldTypeDump(format);
            default:                                return null;
        }
    }
}
