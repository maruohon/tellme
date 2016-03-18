package fi.dy.masa.tellme.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.BiomeGenBase;

public class ClientProxy extends CommonProxy
{
    @Override
    public void getCurrentBiomeInfoClientSide(EntityPlayer player, BiomeGenBase bgb)
    {
        BlockPos pos = player.getPosition();
        String pre = TextFormatting.YELLOW.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

        // These are client-side only:
        player.addChatMessage(new TextComponentString(String.format("%sGrass color%s: 0x%08X (%d)",
                pre, rst, bgb.getModdedBiomeGrassColor(bgb.getGrassColorAtPos(pos)), bgb.getModdedBiomeGrassColor(bgb.getGrassColorAtPos(pos)))));
        player.addChatMessage(new TextComponentString(String.format("%sFoliage color%s: 0x%08X (%d)",
                pre, rst, bgb.getModdedBiomeFoliageColor(bgb.getFoliageColorAtPos(pos)), bgb.getModdedBiomeFoliageColor(bgb.getFoliageColorAtPos(pos)))));
    }
}
