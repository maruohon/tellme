package fi.dy.masa.tellme.datadump;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class FluidRegistryDump extends DataDump
{
    private FluidRegistryDump()
    {
        super(8);
    }

    public static List<String> getFormattedFluidRegistryDump()
    {
        FluidRegistryDump fluidRegistryDump = new FluidRegistryDump();
        Iterator<Map.Entry<String, Fluid>> iter = FluidRegistry.getRegisteredFluids().entrySet().iterator();

        while (iter.hasNext())
        {
            Map.Entry<String, Fluid> entry = iter.next();
            Fluid fluid = entry.getValue();
            String name = entry.getKey();
            String density = String.valueOf(fluid.getDensity());
            String temp = String.valueOf(fluid.getTemperature());
            String viscosity = String.valueOf(fluid.getViscosity());
            String luminosity = String.valueOf(fluid.getLuminosity());
            String isGaseous = String.valueOf(fluid.isGaseous());
            String rarity = fluid.getRarity().toString();
            String block = fluid.getBlock() != null ? fluid.getBlock().getRegistryName().toString() : "-";

            fluidRegistryDump.addData(name, density, temp, viscosity, luminosity, rarity, isGaseous, block);
        }

        fluidRegistryDump.addTitle("Name", "Density", "Temperature", "Viscosity", "Luminosity", "Rarity", "isGaseous", "Block");
        fluidRegistryDump.setColumnAlignment(1, Alignment.RIGHT); // density
        fluidRegistryDump.setColumnAlignment(2, Alignment.RIGHT); // temperature
        fluidRegistryDump.setColumnAlignment(3, Alignment.RIGHT); // viscosity
        fluidRegistryDump.setColumnAlignment(4, Alignment.RIGHT); // luminosity
        fluidRegistryDump.setColumnAlignment(6, Alignment.RIGHT); // isGaseous
        fluidRegistryDump.setUseColumnSeparator(true);

        return fluidRegistryDump.getLines();
    }
}
