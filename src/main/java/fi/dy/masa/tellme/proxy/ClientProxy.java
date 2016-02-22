package fi.dy.masa.tellme.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.biome.BiomeGenBase;

public class ClientProxy extends CommonProxy
{
    @Override
    public void getCurrentBiomeInfoClientSide(EntityPlayer player, BiomeGenBase bgb)
    {
        BlockPos pos = player.getPosition();
        String pre = EnumChatFormatting.YELLOW.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();

        // These are client-side only:
        player.addChatMessage(new ChatComponentText(String.format("%sGrass color%s: 0x%08X (%d)",
                pre, rst, bgb.getModdedBiomeGrassColor(bgb.getGrassColorAtPos(pos)), bgb.getModdedBiomeGrassColor(bgb.getGrassColorAtPos(pos)))));
        player.addChatMessage(new ChatComponentText(String.format("%sFoliage color%s: 0x%08X (%d)",
                pre, rst, bgb.getModdedBiomeFoliageColor(bgb.getFoliageColorAtPos(pos)), bgb.getModdedBiomeFoliageColor(bgb.getFoliageColorAtPos(pos)))));
    }
}
