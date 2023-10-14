package fi.dy.masa.tellme.datadump;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import fi.dy.masa.tellme.util.datadump.DataDump;

public class FluidRegistryDump
{
    public static List<String> getFormattedFluidRegistryDump(DataDump.Format format)
    {
        DataDump fluidRegistryDump = new DataDump(2, format);

        for (Identifier id : Registries.FLUID.getIds())
        {
            Fluid fluid = Registries.FLUID.get(id);
            BlockState blockState = fluid.getDefaultState().getBlockState();
            Block block = blockState.getBlock();
            String blockName = block != null && block != Blocks.AIR ? Registries.BLOCK.getId(block).toString() : "-";

            fluidRegistryDump.addData(id.toString(), blockName);
        }

        fluidRegistryDump.addTitle("Name", "Block");

        return fluidRegistryDump.getLines();
    }
}
