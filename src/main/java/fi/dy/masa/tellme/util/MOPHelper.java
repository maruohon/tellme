package fi.dy.masa.tellme.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class MOPHelper
{
    public static  MovingObjectPosition getMovingObjectPositionFromPlayer(World worldIn, EntityPlayer playerIn, boolean useLiquids)
    {
        float f = playerIn.prevRotationPitch + (playerIn.rotationPitch - playerIn.prevRotationPitch);
        float f1 = playerIn.prevRotationYaw + (playerIn.rotationYaw - playerIn.prevRotationYaw);
        double d0 = playerIn.prevPosX + (playerIn.posX - playerIn.prevPosX);
        double d1 = playerIn.prevPosY + (playerIn.posY - playerIn.prevPosY) + (double)(worldIn.isRemote ? playerIn.getEyeHeight() - playerIn.getDefaultEyeHeight() : playerIn.getEyeHeight()); // isRemote check to revert changes to ray trace position due to adding the eye height clientside and player yOffset differences
        double d2 = playerIn.prevPosZ + (playerIn.posZ - playerIn.prevPosZ);
        Vec3 vec3 = new Vec3(d0, d1, d2);
        float f2 = MathHelper.cos(-f1 * 0.017453292F - (float)Math.PI);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - (float)Math.PI);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d3 = 5.0D;

        if (playerIn instanceof EntityPlayerMP)
        {
            d3 = ((EntityPlayerMP)playerIn).theItemInWorldManager.getBlockReachDistance();
        }

        Vec3 vec31 = vec3.addVector((double)f6 * d3, (double)f5 * d3, (double)f7 * d3);

        return worldIn.rayTraceBlocks(vec3, vec31, useLiquids, !useLiquids, false);
    }
}
