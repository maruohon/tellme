package fi.dy.masa.tellme.event.datalogging;

import java.util.EnumMap;

import fi.dy.masa.tellme.event.datalogging.DataLogger.DataType;

public class EventManager
{
    private static final EnumMap<DataType, Boolean> REGISTERED_HANDLERS = new EnumMap<DataType, Boolean>(DataType.class);

    public static boolean isLoggingEnabled(DataType type)
    {
        return REGISTERED_HANDLERS.get(type) != null;
    }

    public static void registerHandler(DataType type)
    {
        if (REGISTERED_HANDLERS.get(type) == null)
        {
            REGISTERED_HANDLERS.put(type, Boolean.TRUE);
        }
    }

    public static void unregisterHandler(DataType type)
    {
        if (REGISTERED_HANDLERS.get(type) != null)
        {
            REGISTERED_HANDLERS.remove(type);
        }
    }
}
