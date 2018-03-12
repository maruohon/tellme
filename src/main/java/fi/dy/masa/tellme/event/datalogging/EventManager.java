package fi.dy.masa.tellme.event.datalogging;

import java.util.EnumMap;
import javax.annotation.Nullable;
import net.minecraftforge.common.MinecraftForge;
import fi.dy.masa.tellme.event.datalogging.DataLogger.DataType;

public class EventManager
{
    private static final EnumMap<DataType, Boolean> REGISTERED_HANDLERS = new EnumMap<DataType, Boolean>(DataType.class);
    private static final EnumMap<DataType, Object> HANDLER_INSTANCES = new EnumMap<DataType, Object>(DataType.class);

    public static void registerHandler(DataType type)
    {
        if (REGISTERED_HANDLERS.get(type) == null)
        {
            Object handler = HANDLER_INSTANCES.get(type);

            if (handler == null)
            {
                handler = createHandler(type);

                if (handler == null)
                {
                    return;
                }

                HANDLER_INSTANCES.put(type, handler);
            }

            MinecraftForge.EVENT_BUS.register(handler);
            REGISTERED_HANDLERS.put(type, Boolean.TRUE);
        }
    }

    public static void unregisterHandler(DataType type)
    {
        if (REGISTERED_HANDLERS.get(type) != null)
        {
            Object handler = HANDLER_INSTANCES.get(type);

            if (handler != null)
            {
                MinecraftForge.EVENT_BUS.unregister(handler);
                REGISTERED_HANDLERS.remove(type);
            }
        }
    }

    @Nullable
    private static Object createHandler(DataType type)
    {
        switch (type)
        {
            case CHUNK_LOAD:        return new EventHandlerChunkLoad();
            case CHUNK_UNLOAD:      return new EventHandlerChunkUnload();
            case ENTITY_JOIN_WORLD: return new EventHandlerEntityJoinWorld();
            default:                return null;
        }
    }
}
