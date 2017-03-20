package fi.dy.masa.tellme.command;

import java.io.File;
import java.util.Collections;
import java.util.List;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import fi.dy.masa.tellme.datadump.BiomeDump;
import fi.dy.masa.tellme.datadump.BlockDump;
import fi.dy.masa.tellme.datadump.BlockStatesDump;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.datadump.DimensionDump;
import fi.dy.masa.tellme.datadump.EnchantmentDump;
import fi.dy.masa.tellme.datadump.EntityDump;
import fi.dy.masa.tellme.datadump.FluidRegistryDump;
import fi.dy.masa.tellme.datadump.ItemDump;
import fi.dy.masa.tellme.datadump.OreDictionaryDump;
import fi.dy.masa.tellme.datadump.PotionDump;
import fi.dy.masa.tellme.datadump.PotionTypeDump;
import fi.dy.masa.tellme.datadump.SoundEventDump;
import fi.dy.masa.tellme.datadump.SpawnEggDump;
import fi.dy.masa.tellme.datadump.TileEntityDump;
import fi.dy.masa.tellme.datadump.VillagerProfessionDump;

public class SubCommandDump extends SubCommand
{
    public SubCommandDump(CommandTellme baseCommand)
    {
        super(baseCommand);

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
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        super.execute(server, sender, args);

        if (args.length == 1)
        {
            Format format = this.getName().endsWith("-csv") ? Format.CSV : Format.ASCII;
            List<String> data = this.getData(args[0], format);

            if (data.isEmpty())
            {
                throw new WrongUsageException("tellme.command.error.unknown.parameter", args[0]);
            }

            if (this.getName().startsWith("dump"))
            {
                File file = DataDump.dumpDataToFile(args[0], data);

                if (file != null)
                {
                    this.sendMessage(sender, "tellme.info.output.to.file", file.getName());
                }
            }
            else if (this.getName().startsWith("list"))
            {
                DataDump.printDataToLogger(data);
                this.sendMessage(sender, "tellme.info.output.to.console");
            }
        }
    }

    protected List<String> getData(String type, Format format)
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
            return OreDictionaryDump.getFormattedOreDictionaryDump(format, false);
        }
        else if (type.equals("oredictionary-by-item"))
        {
            return OreDictionaryDump.getFormattedOreDictionaryDump(format, true);
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
