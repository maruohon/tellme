package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.nbt.NbtStringifierPretty;

public class EntityInfo
{
    private static String getBasicEntityInfo(Entity target)
    {
        String regName = getEntityNameFor(target.getType());
        return String.format("Entity: %s [registry name: %s] (entityId: %d)", target.getName().getString(), regName, target.getId());
    }

    public static List<String> getFullEntityInfo(@Nullable Entity target, boolean targetIsChat)
    {
        if (target == null)
        {
            return Collections.emptyList();
        }

        List<String> lines = new ArrayList<>();
        lines.add(getBasicEntityInfo(target));

        CompoundTag nbt = new CompoundTag();

        if (target.save(nbt) == false)
        {
            target.saveWithoutId(nbt);
        }

        lines.add("Entity class: " + target.getClass().getName());
        lines.add("");

        if (target instanceof LivingEntity)
        {
            lines.addAll(getActivePotionEffectsForEntity((LivingEntity) target, DataDump.Format.ASCII));
            lines.add("");
        }

        lines.addAll((new NbtStringifierPretty(targetIsChat ? ChatFormatting.GRAY.toString() : null)).getNbtLines(nbt));

        return lines;
    }

    public static List<String> getActivePotionEffectsForEntity(LivingEntity entity, DataDump.Format format)
    {
        Collection<MobEffectInstance> effects = entity.getActiveEffects();

        if (effects.isEmpty() == false)
        {
            DataDump dump = new DataDump(4, format);

            for (MobEffectInstance effect : effects)
            {
                ResourceLocation rl = ForgeRegistries.MOB_EFFECTS.getKey(effect.getEffect());

                dump.addData(
                        rl != null ? rl.toString() : effect.getClass().getName(),
                        String.valueOf(effect.getAmplifier()),
                        String.valueOf(effect.getDuration()),
                        String.valueOf(effect.isAmbient()));
            }

            dump.addTitle("Effect", "Amplifier", "Duration", "Ambient");
            dump.setColumnProperties(1, DataDump.Alignment.RIGHT, true); // amplifier
            dump.setColumnProperties(2, DataDump.Alignment.RIGHT, true); // duration

            return dump.getLines();
        }

        return Collections.emptyList();
    }

    public static void printBasicEntityInfoToChat(Player player, Entity target)
    {
        String regName = getEntityNameFor(target.getType());
        String textPre = String.format("Entity: %s [registry name: ", target.getName().getString());
        String textPost = String.format("] (entityId: %d)", target.getId());

        player.displayClientMessage(OutputUtils.getClipboardCopiableMessage(textPre, regName, textPost), false);
    }

    public static void printFullEntityInfoToConsole(Player player, Entity target)
    {
        printBasicEntityInfoToChat(player, target);
        OutputUtils.printOutputToConsole(getFullEntityInfo(target, false));
    }

    public static void dumpFullEntityInfoToFile(Player player, Entity target)
    {
        printBasicEntityInfoToChat(player, target);
        File file = DataDump.dumpDataToFile("entity_data", getFullEntityInfo(target, false));
        OutputUtils.sendClickableLinkMessage(player, "Output written to file %s", file);
    }

    public static List<String> getPlayerList(DataDump.Format format, @Nullable MinecraftServer server)
    {
        DataDump dump = new DataDump(6, format);

        if (server != null)
        {
            for (Player player : server.getPlayerList().getPlayers())
            {
                String name = player.getName().getString();
                String dim = WorldUtils.getDimensionId(player.getCommandSenderWorld());
                String health = String.format("%.2f", player.getHealth());
                BlockPos pos = player.blockPosition();
                int x = pos.getX();
                int y = pos.getY();
                int z = pos.getZ();
                String blockPos = String.format("x: %d, y: %d, z: %d", x, y, z);
                String chunkPos = String.format("cx: %d, cy: %d, cz: %d", x >> 4, y >> 4, z >> 4);
                String regionPos = String.format("r.%d.%d", x >> 9, z >> 9);

                dump.addData(name, health, dim, blockPos, chunkPos, regionPos);
            }
        }

        dump.addTitle("Name", "Health", "Dimension", "Position", "Chunk", "Region");

        dump.setColumnProperties(1, DataDump.Alignment.RIGHT, true); // health
        dump.setColumnProperties(2, DataDump.Alignment.RIGHT, true); // dim
        dump.setColumnProperties(3, DataDump.Alignment.RIGHT, false); // block pos
        dump.setColumnProperties(4, DataDump.Alignment.RIGHT, false); // chunk pos
        dump.setColumnProperties(5, DataDump.Alignment.RIGHT, false); // region pos

        return dump.getLines();
    }

    public static String getEntityNameFor(EntityType<?> type)
    {
        ResourceLocation id = EntityType.getKey(type);
        return id != null ? id.toString() : "<null>";
    }
}
