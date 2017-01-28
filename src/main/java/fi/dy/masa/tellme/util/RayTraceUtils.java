package fi.dy.masa.tellme.util;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RayTraceUtils
{
    public static RayTraceResult rayTraceFromPlayer(World worldIn, EntityPlayer playerIn, boolean useLiquids)
    {
        double reach = 5.0d;

        if (playerIn instanceof EntityPlayerMP)
        {
            reach = ((EntityPlayerMP) playerIn).interactionManager.getBlockReachDistance();
        }

        return rayTraceFromPlayer(worldIn, playerIn, useLiquids, reach);
    }

    public static RayTraceResult rayTraceFromPlayer(World worldIn, EntityPlayer playerIn, boolean useLiquids, double reach)
    {
        float f = playerIn.prevRotationPitch + (playerIn.rotationPitch - playerIn.prevRotationPitch);
        float f1 = playerIn.prevRotationYaw + (playerIn.rotationYaw - playerIn.prevRotationYaw);
        double d0 = playerIn.prevPosX + (playerIn.posX - playerIn.prevPosX);
        double d1 = playerIn.prevPosY + (playerIn.posY - playerIn.prevPosY) + (double)(worldIn.isRemote ? playerIn.getEyeHeight() - playerIn.getDefaultEyeHeight() : playerIn.getEyeHeight()); // isRemote check to revert changes to ray trace position due to adding the eye height clientside and player yOffset differences
        double d2 = playerIn.prevPosZ + (playerIn.posZ - playerIn.prevPosZ);
        Vec3d eyesVec = new Vec3d(d0, d1, d2);
        float f2 = MathHelper.cos(-f1 * 0.017453292F - (float)Math.PI);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - (float)Math.PI);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        Vec3d lookVec = eyesVec.addVector((double)f6 * reach, (double)f5 * reach, (double)f7 * reach);

        RayTraceResult result = worldIn.rayTraceBlocks(eyesVec, lookVec, useLiquids, !useLiquids, false);

        Entity targetEntity = null;
        RayTraceResult entityTrace = null;
        AxisAlignedBB bb = new AxisAlignedBB(eyesVec.xCoord, eyesVec.yCoord, eyesVec.zCoord, eyesVec.xCoord, eyesVec.yCoord, eyesVec.zCoord);
        List<Entity> list = worldIn.getEntitiesWithinAABBExcludingEntity(playerIn, bb.expand(reach, reach, reach));
        double closest = 0.0D;

        for (int i = 0; i < list.size(); ++i)
        {
            Entity entity = list.get(i);
            bb = entity.getEntityBoundingBox();
            RayTraceResult traceTmp = bb.calculateIntercept(lookVec, eyesVec);

            if (traceTmp != null)
            {
                double tmp = eyesVec.distanceTo(traceTmp.hitVec);

                if (tmp < closest || closest == 0.0D)
                {
                    targetEntity = entity;
                    entityTrace = traceTmp;
                    closest = tmp;
                }
            }
        }

        if (targetEntity != null)
        {
            result = new RayTraceResult(targetEntity, entityTrace.hitVec);
        }

        return result;
    }
}
