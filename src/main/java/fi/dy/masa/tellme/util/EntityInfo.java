package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.SubCommand;
import fi.dy.masa.tellme.datadump.DataDump;

public class EntityInfo
{
    private static String getBasicEntityInfo(Entity target)
    {
        ResourceLocation rl = EntityList.getKey(target);
        String regName = rl != null ? rl.toString() : "null";

        return String.format("Entity: %s [registry name: %s] (entityId: %d)", target.getName(), regName, target.getEntityId());
    }

    public static List<String> getFullEntityInfo(Entity target)
    {
        List<String> lines = new ArrayList<>();
        lines.add(getBasicEntityInfo(target));

        NBTTagCompound nbt = new NBTTagCompound();

        if (target.writeToNBTOptional(nbt) == false)
        {
            target.writeToNBT(nbt);
        }

        lines.add("Entity class: " + target.getClass().getName());
        lines.add("");

        if (target instanceof EntityLivingBase)
        {
            lines.addAll(getActivePotionEffectsForEntity((EntityLivingBase) target, DataDump.Format.ASCII));
            lines.add("");
        }

        NBTFormatter.getPrettyFormattedNBT(lines, nbt);

        return lines;
    }

    public static List<String> getActivePotionEffectsForEntity(EntityLivingBase entity, DataDump.Format format)
    {
        Collection<PotionEffect> effects = entity.getActivePotionEffects();

        if (effects.isEmpty() == false)
        {
            DataDump dump = new DataDump(4, format);

            for (PotionEffect effect : effects)
            {
                ResourceLocation rl = effect.getPotion().getRegistryName();

                dump.addData(
                        rl != null ? rl.toString() : effect.getClass().getName(),
                        String.valueOf(effect.getAmplifier()),
                        String.valueOf(effect.getDuration()),
                        String.valueOf(effect.getIsAmbient()));
            }

            dump.addTitle("Effect", "Amplifier", "Duration", "Ambient");
            dump.setColumnProperties(1, DataDump.Alignment.RIGHT, true); // amplifier
            dump.setColumnProperties(2, DataDump.Alignment.RIGHT, true); // duration
            dump.setUseColumnSeparator(true);

            return dump.getLines();
        }

        return Collections.emptyList();
    }

    public static void printBasicEntityInfoToChat(EntityPlayer player, Entity target)
    {
        ResourceLocation rl = EntityList.getKey(target);
        String regName = rl != null ? rl.toString() : "null";
        String textPre = String.format("Entity: %s [registry name: ", target.getName());
        String textPost = String.format("] (entityId: %d)", target.getEntityId());

        player.sendMessage(ChatUtils.getClipboardCopiableMessage(textPre, regName, textPost));
    }

    public static void printFullEntityInfoToConsole(EntityPlayer player, Entity target)
    {
        List<String> lines = getFullEntityInfo(target);

        for (String line : lines)
        {
            TellMe.logger.info(line);
        }
    }

    public static void printEntityInfo(EntityPlayer player, Entity target, boolean dumpToFile)
    {
        EntityInfo.printBasicEntityInfoToChat(player, target);

        if (dumpToFile)
        {
            dumpFullEntityInfoToFile(player, target);
        }
        else
        {
            printFullEntityInfoToConsole(player, target);
        }
    }

    public static void dumpFullEntityInfoToFile(EntityPlayer player, Entity target)
    {
        File file = DataDump.dumpDataToFile("entity_data", getFullEntityInfo(target));
        SubCommand.sendClickableLinkMessage(player, "Output written to file %s", file);
    }

    public static List<String> getPlayerList(DataDump.Format format)
    {
        DataDump dump = new DataDump(6, format);

        for (EntityPlayer player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers())
        {
            String name = player.getName();
            String dim = String.valueOf(player.getEntityWorld().provider.getDimension());
            String health = String.format("%.2f", player.getHealth());
            BlockPos pos = new BlockPos(player);
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            String blockPos = String.format("x: %d, y: %d, z: %d", x, y, z);
            String chunkPos = String.format("cx: %d, cy: %d, cz: %d", x >> 4, y >> 4, z >> 4);
            String regionPos = String.format("r.%d.%d", x >> 9, z >> 9);

            dump.addData(name, health, dim, blockPos, chunkPos, regionPos);
        }

        dump.addTitle("Name", "Health", "Dimension", "Position", "Chunk", "Region");

        dump.setColumnProperties(1, DataDump.Alignment.RIGHT, true); // health
        dump.setColumnProperties(2, DataDump.Alignment.RIGHT, true); // dim
        dump.setColumnProperties(3, DataDump.Alignment.RIGHT, false); // block pos
        dump.setColumnProperties(4, DataDump.Alignment.RIGHT, false); // chunk pos
        dump.setColumnProperties(5, DataDump.Alignment.RIGHT, false); // region pos

        dump.setUseColumnSeparator(true);

        return dump.getLines();
    }

    public static String getEntityNameFromClass(Class<? extends Entity> clazz)
    {
        String name = null;
        ResourceLocation rl = EntityList.getKey(clazz);

        if (rl != null)
        {
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(rl);

            if (entry != null)
            {
                name = entry.getName();
            }
        }

        if (name == null)
        {
            name = clazz.getSimpleName();
        }

        return name;
    }
}
