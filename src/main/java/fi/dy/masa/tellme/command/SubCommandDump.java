package fi.dy.masa.tellme.command;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.google.common.collect.Sets;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import fi.dy.masa.tellme.datadump.*;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.datadump.OreDictionaryDump.OreDumpType;

public class SubCommandDump extends SubCommand
{
    public SubCommandDump(CommandTellme baseCommand)
    {
        super(baseCommand);

        this.subSubCommands.add("all");
        this.subSubCommands.add("biomes");
        this.subSubCommands.add("biomes-id-to-name");
        this.subSubCommands.add("blocks");
        this.subSubCommands.add("blocks-id-to-registryname");
        this.subSubCommands.add("blocks-with-nbt");
        this.subSubCommands.add("blockstates-by-block");
        this.subSubCommands.add("blockstates-by-state");
        this.subSubCommands.add("dimensions");
        this.subSubCommands.add("enchantments");
        this.subSubCommands.add("entities");
        this.subSubCommands.add("fluids");
        this.subSubCommands.add("items");
        this.subSubCommands.add("items-with-nbt");
        this.subSubCommands.add("oredictionary-by-key");
        this.subSubCommands.add("oredictionary-by-key-individual");
        this.subSubCommands.add("oredictionary-by-item");
        this.subSubCommands.add("potions");
        this.subSubCommands.add("potiontypes");
        this.subSubCommands.add("soundevents");
        this.subSubCommands.add("spawneggs");
        this.subSubCommands.add("tileentities");
        this.subSubCommands.add("villagerprofessions");
    }

    @Override
    public String getName()
    {
        return "dump";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args)
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

    private void outputData(MinecraftServer server, ICommandSender sender, String arg) throws CommandException
    {
        Format format = this.getName().endsWith("-csv") ? Format.CSV : Format.ASCII;
        List<String> data = this.getData(arg, format);

        if (data.isEmpty())
        {
            throw new WrongUsageException("Unrecognized parameter: '" + arg + "'");
        }

        if (this.getName().startsWith("dump"))
        {
            File file;

            if (format == Format.CSV)
            {
                file = DataDump.dumpDataToFile(arg + "-csv", ".csv", data);
            }
            else
            {
                file = DataDump.dumpDataToFile(arg, data);
            }

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

    private List<String> getData(String type, Format format)
    {
        if (type.equals("biomes"))
        {
            return BiomeDump.getFormattedBiomeDump(format);
        }
        else if (type.equals("biomes-id-to-name"))
        {
            return BiomeDump.getBiomeDumpIdToName(format);
        }
        else if (type.equals("blocks"))
        {
            return BlockDump.getFormattedBlockDump(format, false);
        }
        else if (type.equals("blocks-id-to-registryname"))
        {
            return BlockDump.getBlockDumpIdToRegistryName(format);
        }
        else if (type.equals("blocks-with-nbt"))
        {
            return BlockDump.getFormattedBlockDump(format, true);
        }
        else if (type.equals("blockstates-by-block"))
        {
            return BlockStatesDump.getFormattedBlockStatesDumpByBlock();
        }
        else if (type.equals("blockstates-by-state"))
        {
            return BlockStatesDump.getFormattedBlockStatesDumpByState(format);
        }
        else if (type.equals("dimensions"))
        {
            return DimensionDump.getFormattedDimensionDump(format);
        }
        else if (type.equals("enchantments"))
        {
            return EnchantmentDump.getFormattedEnchantmentDump(format);
        }
        else if (type.equals("entities"))
        {
            return EntityDump.getFormattedEntityDump(format);
        }
        else if (type.equals("fluids"))
        {
            return FluidRegistryDump.getFormattedFluidRegistryDump(format);
        }
        else if (type.equals("items"))
        {
            return ItemDump.getFormattedItemDump(format, false);
        }
        else if (type.equals("items-with-nbt"))
        {
            return ItemDump.getFormattedItemDump(format, true);
        }
        else if (type.equals("oredictionary-by-key"))
        {
            return OreDictionaryDump.getFormattedOreDictionaryDump(format, OreDumpType.BY_ORE_GROUPED);
        }
        else if (type.equals("oredictionary-by-key-individual"))
        {
            return OreDictionaryDump.getFormattedOreDictionaryDump(format, OreDumpType.BY_ORE_INDIVIDUAL);
        }
        else if (type.equals("oredictionary-by-item"))
        {
            return OreDictionaryDump.getFormattedOreDictionaryDump(format, OreDumpType.BY_STACK);
        }
        else if (type.equals("potions"))
        {
            return PotionDump.getFormattedPotionDump(format);
        }
        else if (type.equals("potiontypes"))
        {
            return PotionTypeDump.getFormattedPotionTypeDump(format);
        }
        else if (type.equals("soundevents"))
        {
            return SoundEventDump.getFormattedSoundEventDump(format);
        }
        else if (type.equals("spawneggs"))
        {
            return SpawnEggDump.getFormattedSpawnEggDump(format);
        }
        else if (type.equals("tileentities"))
        {
            return TileEntityDump.getFormattedTileEntityDump(format);
        }
        else if (type.equals("villagerprofessions"))
        {
            return VillagerProfessionDump.getFormattedVillagerProfessionDump(format);
        }

        return Collections.emptyList();
    }
}
