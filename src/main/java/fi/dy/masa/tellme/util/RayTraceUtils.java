package fi.dy.masa.tellme.util;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RayTraceUtils
{
    @Nonnull
    public static RayTraceResult getRayTraceFromEntity(World worldIn, Entity entityIn, boolean useLiquids)
    {
        double reach = 5.0d;

        if (entityIn instanceof PlayerEntity)
        {
            reach = ((PlayerEntity) entityIn).getAttribute(PlayerEntity.REACH_DISTANCE).getValue();
        }

        return getRayTraceFromEntity(worldIn, entityIn, useLiquids, reach);
    }

    @Nonnull
    public static RayTraceResult getRayTraceFromEntity(World worldIn, Entity entityIn, boolean useLiquids, double range)
    {
        Vec3d eyesVec = new Vec3d(entityIn.posX, entityIn.posY + entityIn.getEyeHeight(), entityIn.posZ);
        Vec3d rangedLookRot = entityIn.getLook(1f).scale(range);
        Vec3d lookVec = eyesVec.add(rangedLookRot);

        RayTraceContext ctx = new RayTraceContext(eyesVec, lookVec, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, entityIn);
        RayTraceResult result = worldIn.rayTraceBlocks(ctx);

        if (result == null)
        {
            result = BlockRayTraceResult.createMiss(Vec3d.ZERO, Direction.UP, BlockPos.ZERO);
        }

        AxisAlignedBB bb = entityIn.getBoundingBox().expand(rangedLookRot.x, rangedLookRot.y, rangedLookRot.z).expand(1d, 1d, 1d);
        List<Entity> list = worldIn.getEntitiesWithinAABBExcludingEntity(entityIn, bb);

        double closest = result.getType() == RayTraceResult.Type.BLOCK ? eyesVec.distanceTo(result.getHitVec()) : Double.MAX_VALUE;
        Vec3d entityTraceHitPos = null;
        Entity targetEntity = null;

        for (int i = 0; i < list.size(); i++)
        {
            Entity entity = list.get(i);
            bb = entity.getBoundingBox();
            Optional<Vec3d> optional = bb.rayTrace(eyesVec, lookVec);

            if (optional.isPresent())
            {
                Vec3d hitPos = optional.get();
                double distance = eyesVec.distanceTo(hitPos);

                if (distance <= closest)
                {
                    targetEntity = entity;
                    entityTraceHitPos = hitPos;
                    closest = distance;
                }
            }
        }

        if (targetEntity != null)
        {
            result = new EntityRayTraceResult(targetEntity, entityTraceHitPos);
        }

        return result;
    }
}
