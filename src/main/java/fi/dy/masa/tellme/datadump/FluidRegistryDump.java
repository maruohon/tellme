package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class FluidRegistryDump
{
    public static List<String> getFormattedFluidRegistryDump(DataDump.Format format)
    {
        DataDump fluidRegistryDump = new DataDump(2, format);

        for (Identifier id : Registry.FLUID.getIds())
        {
            Fluid fluid = Registry.FLUID.get(id);
            BlockState blockState = fluid.getDefaultState().getBlockState();
            Block block = blockState.getBlock();
            String blockName = block != null && block != Blocks.AIR ? Registry.BLOCK.getId(block).toString() : "-";

            fluidRegistryDump.addData(id.toString(), blockName);
        }

        fluidRegistryDump.addTitle("Name", "Block");

        return fluidRegistryDump.getLines();
    }
}
