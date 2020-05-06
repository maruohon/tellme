package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidRegistryDump
{
    public static List<String> getFormattedFluidRegistryDump(DataDump.Format format)
    {
        DataDump fluidRegistryDump = new DataDump(8, format);

        for (Map.Entry<ResourceLocation, Fluid> entry : ForgeRegistries.FLUIDS.getEntries())
        {
            Fluid fluid = entry.getValue();
            String name = entry.getKey().toString();

            FluidAttributes attr = fluid.getAttributes();
            String density = String.valueOf(attr.getDensity());
            String temp = String.valueOf(attr.getTemperature());
            String viscosity = String.valueOf(attr.getViscosity());
            String luminosity = String.valueOf(attr.getLuminosity());
            String isGaseous = String.valueOf(attr.isGaseous());
            String rarity = attr.getRarity().toString();
            BlockState blockState = fluid.getDefaultState().getBlockState();
            Block block = blockState.getBlock();
            String blockName = block != null && block != Blocks.AIR && block.getRegistryName() != null ? block.getRegistryName().toString() : "-";

            fluidRegistryDump.addData(name, density, temp, viscosity, luminosity, rarity, isGaseous, blockName);
        }

        fluidRegistryDump.addTitle("Name", "Density", "Temperature", "Viscosity", "Luminosity", "Rarity", "isGaseous", "Block");

        fluidRegistryDump.setColumnProperties(1, Alignment.RIGHT, true); // density
        fluidRegistryDump.setColumnProperties(2, Alignment.RIGHT, true); // temperature
        fluidRegistryDump.setColumnProperties(3, Alignment.RIGHT, true); // viscosity
        fluidRegistryDump.setColumnProperties(4, Alignment.RIGHT, true); // luminosity
        fluidRegistryDump.setColumnAlignment(6, Alignment.RIGHT); // isGaseous

        return fluidRegistryDump.getLines();
    }
}
