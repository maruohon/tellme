package fi.dy.masa.tellme.util;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class RayTraceUtils
{
    @Nonnull
    public static RayTraceResult getRayTraceFromEntity(World worldIn, Entity entityIn, boolean useLiquids)
    {
        double reach = 6.0d;

        /*
        if (entityIn instanceof PlayerEntity)
        {
            reach = ((PlayerEntity) entityIn).getAttribute(PlayerEntity.REACH_DISTANCE).getValue();
        }
        */

        return getRayTraceFromEntity(worldIn, entityIn, useLiquids, reach);
    }

    @Nonnull
    public static RayTraceResult getRayTraceFromEntity(World worldIn, Entity entityIn, boolean useLiquids, double range)
    {
        Vector3d eyesVec = new Vector3d(entityIn.getX(), entityIn.getY() + entityIn.getEyeHeight(), entityIn.getZ());
        Vector3d rangedLookRot = entityIn.getViewVector(1f).scale(range);
        Vector3d lookVec = eyesVec.add(rangedLookRot);

        RayTraceContext ctx = new RayTraceContext(eyesVec, lookVec, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, entityIn);
        RayTraceResult result = worldIn.clip(ctx);

        if (result == null)
        {
            result = BlockRayTraceResult.miss(Vector3d.ZERO, Direction.UP, BlockPos.ZERO);
        }

        AxisAlignedBB bb = entityIn.getBoundingBox().expandTowards(rangedLookRot.x, rangedLookRot.y, rangedLookRot.z).expandTowards(1d, 1d, 1d);
        List<Entity> list = worldIn.getEntities(entityIn, bb);

        double closest = result.getType() == RayTraceResult.Type.BLOCK ? eyesVec.distanceTo(result.getLocation()) : Double.MAX_VALUE;
        Vector3d entityTraceHitPos = null;
        Entity targetEntity = null;

        for (int i = 0; i < list.size(); i++)
        {
            Entity entity = list.get(i);
            bb = entity.getBoundingBox();
            Optional<Vector3d> optional = bb.clip(eyesVec, lookVec);

            if (optional.isPresent())
            {
                Vector3d hitPos = optional.get();
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
