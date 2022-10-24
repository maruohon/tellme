package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;

import fi.dy.masa.tellme.LiteModTellMe;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.mixin.IMixinTileEntity;

public class TileEntityDump
{
    public static List<String> getFormattedTileEntityDump(Format format)
    {
        DataDump tileEntityDump = new DataDump(3, format);

        try
        {
            RegistryNamespaced <ResourceLocation, Class <? extends TileEntity>> registry = getTileEntityRegistry();
            Set<ResourceLocation> keys = registry.getKeys();

            for (ResourceLocation key : keys)
            {
                Class <? extends TileEntity> clazz = registry.getObject(key);
                tileEntityDump.addData(clazz.getName(), key.toString(), ITickable.class.isAssignableFrom(clazz) ? "yes" : "-");
            }

            tileEntityDump.addTitle("Class", "Registry name", "Ticking?");
            tileEntityDump.setColumnAlignment(2, Alignment.RIGHT);
            tileEntityDump.setUseColumnSeparator(true);
        }
        catch (Exception e)
        {
            LiteModTellMe.logger.warn("Failed to dump the TileEntity map");
        }

        return tileEntityDump.getLines();
    }

    @Nullable
    public static RegistryNamespaced <ResourceLocation, Class <? extends TileEntity>> getTileEntityRegistry()
    {
        return IMixinTileEntity.getRegistry();
    }
}
