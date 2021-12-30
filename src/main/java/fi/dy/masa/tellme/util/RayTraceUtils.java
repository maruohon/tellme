package fi.dy.masa.tellme.util;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RayTraceUtils
{
    @Nonnull
    public static HitResult getRayTraceFromEntity(Level worldIn, Entity entityIn, boolean useLiquids)
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
    public static HitResult getRayTraceFromEntity(Level worldIn, Entity entityIn, boolean useLiquids, double range)
    {
        Vec3 eyesVec = new Vec3(entityIn.getX(), entityIn.getY() + entityIn.getEyeHeight(), entityIn.getZ());
        Vec3 rangedLookRot = entityIn.getViewVector(1f).scale(range);
        Vec3 lookVec = eyesVec.add(rangedLookRot);

        ClipContext ctx = new ClipContext(eyesVec, lookVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, entityIn);
        HitResult result = worldIn.clip(ctx);

        if (result == null)
        {
            result = BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO);
        }

        AABB bb = entityIn.getBoundingBox().expandTowards(rangedLookRot.x, rangedLookRot.y, rangedLookRot.z).expandTowards(1d, 1d, 1d);
        List<Entity> list = worldIn.getEntities(entityIn, bb);

        double closest = result.getType() == HitResult.Type.BLOCK ? eyesVec.distanceTo(result.getLocation()) : Double.MAX_VALUE;
        Vec3 entityTraceHitPos = null;
        Entity targetEntity = null;

        for (int i = 0; i < list.size(); i++)
        {
            Entity entity = list.get(i);
            bb = entity.getBoundingBox();
            Optional<Vec3> optional = bb.clip(eyesVec, lookVec);

            if (optional.isPresent())
            {
                Vec3 hitPos = optional.get();
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
            result = new EntityHitResult(targetEntity, entityTraceHitPos);
        }

        return result;
    }
}
