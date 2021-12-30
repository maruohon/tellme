package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;

public class TileEntityDump
{
    public static List<String> getFormattedTileEntityDump(DataDump.Format format)
    {
        DataDump tileEntityDump = new DataDump(3, format);

        try
        {
            for (Map.Entry<ResourceKey<BlockEntityType<?>>, BlockEntityType<?>> entry : ForgeRegistries.TILE_ENTITIES.getEntries())
            {
                BlockEntityType<?> type = entry.getValue();
                String id = type.getRegistryName().toString();
                BlockEntity te = type.create();
                Class <? extends BlockEntity> clazz = te.getClass();
                tileEntityDump.addData(id, clazz.getName(), TickableBlockEntity.class.isAssignableFrom(clazz) ? "yes" : "-");
            }

            tileEntityDump.addTitle("Registry name", "Class", "Ticking?");
            tileEntityDump.setColumnAlignment(2, Alignment.RIGHT);
        }
        catch (Exception e)
        {
            TellMe.logger.warn("Failed to dump the TileEntity map");
        }

        return tileEntityDump.getLines();
    }
}
