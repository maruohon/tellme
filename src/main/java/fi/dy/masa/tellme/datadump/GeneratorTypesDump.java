package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.client.world.GeneratorType;
import fi.dy.masa.tellme.mixin.IMixinGeneratorType;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class GeneratorTypesDump
{
    public static List<String> getFormattedDump(DataDump.Format format)
    {
        DataDump dump = new DataDump(1, format);

        for (GeneratorType type : IMixinGeneratorType.tellme_getValues())
        {
            dump.addData(type.getDisplayName().getString());
        }

        dump.addTitle("Name");

        return dump.getLines();
    }
}
