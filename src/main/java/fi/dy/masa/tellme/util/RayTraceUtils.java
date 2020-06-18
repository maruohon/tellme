package fi.dy.masa.tellme.util;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;

public class RayTraceUtils
{
    @Nonnull
    public static HitResult getRayTraceFromEntity(World worldIn, Entity entityIn, boolean useLiquids)
    {
        double reach = 5.0d;
        return getRayTraceFromEntity(worldIn, entityIn, useLiquids, reach);
    }

    @Nonnull
    public static HitResult getRayTraceFromEntity(World worldIn, Entity entityIn, boolean useLiquids, double range)
    {
        Vec3d eyesVec = entityIn.getCameraPosVec(1f);
        Vec3d rangedLookRot = entityIn.getRotationVec(1f).multiply(range);
        Vec3d lookVec = eyesVec.add(rangedLookRot);

        RayTraceContext ctx = new RayTraceContext(eyesVec, lookVec, RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.ANY, entityIn);
        HitResult result = worldIn.rayTrace(ctx);

        if (result == null)
        {
            result = BlockHitResult.createMissed(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN);
        }

        Box bb = entityIn.getBoundingBox().expand(rangedLookRot.x, rangedLookRot.y, rangedLookRot.z).expand(1d, 1d, 1d);
        List<Entity> list = worldIn.getEntities(entityIn, bb);

        double closest = result.getType() == HitResult.Type.BLOCK ? eyesVec.distanceTo(result.getPos()) : Double.MAX_VALUE;
        Vec3d entityTraceHitPos = null;
        Entity targetEntity = null;

        for (Entity entity : list)
        {
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
            result = new EntityHitResult(targetEntity, entityTraceHitPos);
        }

        return result;
    }
}
