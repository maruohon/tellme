package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;

import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;

public class FluidRegistryDump
{
    public static List<String> getFormattedFluidRegistryDump(DataDump.Format format)
    {
        DataDump fluidRegistryDump = new DataDump(8, format);

        for (Map.Entry<ResourceKey<Fluid>, Fluid> entry : ForgeRegistries.FLUIDS.getEntries())
        {
            Fluid fluid = entry.getValue();
            String name = entry.getKey().location().toString();

            FluidType attr = fluid.getFluidType();
            String density = String.valueOf(attr.getDensity());
            String temp = String.valueOf(attr.getTemperature());
            String viscosity = String.valueOf(attr.getViscosity());
            String lightLevel = String.valueOf(attr.getLightLevel());
            String isLighterThanAir = String.valueOf(attr.isLighterThanAir());
            String rarity = attr.getRarity().toString();
            BlockState blockState = fluid.defaultFluidState().createLegacyBlock();
            Block block = blockState.getBlock();
            String blockName = block != null && block != Blocks.AIR ? BlockDump.getRegistryName(block) : "-";

            fluidRegistryDump.addData(name, density, temp, viscosity, lightLevel, rarity, isLighterThanAir, blockName);
        }

        fluidRegistryDump.addTitle("Name", "Density", "Temperature", "Viscosity", "Light Level", "Rarity", "Lighter Than Air", "Block");

        fluidRegistryDump.setColumnProperties(1, Alignment.RIGHT, true); // density
        fluidRegistryDump.setColumnProperties(2, Alignment.RIGHT, true); // temperature
        fluidRegistryDump.setColumnProperties(3, Alignment.RIGHT, true); // viscosity
        fluidRegistryDump.setColumnProperties(4, Alignment.RIGHT, true); // luminosity
        fluidRegistryDump.setColumnAlignment(6, Alignment.RIGHT); // isGaseous

        return fluidRegistryDump.getLines();
    }
}
