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
        this.subSubCommands.add("blocks");
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
            List<String> data = this.getData(args[0]);

            if (data.isEmpty())
            {
                throw new WrongUsageException("tellme.command.error.unknown.parameter", args[0]);
            }

            if (this.getName().equals("dump"))
            {
                File file = DataDump.dumpDataToFile(args[0], data);

                if (file != null)
                {
                    this.sendMessage(sender, "tellme.info.output.to.file", file.getName());
                }
            }
            else if (this.getName().equals("list"))
            {
                DataDump.printDataToLogger(data);
                this.sendMessage(sender, "tellme.info.output.to.console");
            }
        }
    }

    protected List<String> getData(String type)
    {
        if (type.equals("biomes"))
        {
            return BiomeDump.getFormattedBiomeDump();
        }
        else if (type.equals("blocks"))
        {
            return BlockDump.getFormattedBlockDump(false);
        }
        else if (type.equals("blocks-with-nbt"))
        {
            return BlockDump.getFormattedBlockDump(true);
        }
        else if (type.equals("blockstates-by-block"))
        {
            return BlockStatesDump.getFormattedBlockStatesDumpByBlock();
        }
        else if (type.equals("blockstates-by-state"))
        {
            return BlockStatesDump.getFormattedBlockStatesDumpByState();
        }
        else if (type.equals("dimensions"))
        {
            return DimensionDump.getFormattedDimensionDump();
        }
        else if (type.equals("enchantments"))
        {
            return EnchantmentDump.getFormattedEnchantmentDump();
        }
        else if (type.equals("entities"))
        {
            return EntityDump.getFormattedEntityDump();
        }
        else if (type.equals("fluids"))
        {
            return FluidRegistryDump.getFormattedFluidRegistryDump();
        }
        else if (type.equals("items"))
        {
            return ItemDump.getFormattedItemDump(false);
        }
        else if (type.equals("items-with-nbt"))
        {
            return ItemDump.getFormattedItemDump(true);
        }
        else if (type.equals("oredictionary-by-key"))
        {
            return OreDictionaryDump.getFormattedOreDictionaryDump(false);
        }
        else if (type.equals("oredictionary-by-item"))
        {
            return OreDictionaryDump.getFormattedOreDictionaryDump(true);
        }
        else if (type.equals("potions"))
        {
            return PotionDump.getFormattedPotionDump();
        }
        else if (type.equals("potiontypes"))
        {
            return PotionTypeDump.getFormattedPotionTypeDump();
        }
        else if (type.equals("soundevents"))
        {
            return SoundEventDump.getFormattedSoundEventDump();
        }
        else if (type.equals("spawneggs"))
        {
            return SpawnEggDump.getFormattedSpawnEggDump();
        }
        else if (type.equals("tileentities"))
        {
            return TileEntityDump.getFormattedTileEntityDump();
        }
        else if (type.equals("villagerprofessions"))
        {
            return VillagerProfessionDump.getFormattedVillagerProfessionDump();
        }

        return Collections.emptyList();
    }
}
