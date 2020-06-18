package fi.dy.masa.tellme;

import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.serialize.ConstantArgumentSerializer;
import fi.dy.masa.tellme.command.argument.BiomeArgument;
import fi.dy.masa.tellme.command.argument.FileArgument;
import fi.dy.masa.tellme.command.argument.GroupingArgument;
import fi.dy.masa.tellme.command.argument.OutputFormatArgument;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.command.argument.StringCollectionArgument;
import fi.dy.masa.tellme.datadump.DataProviderBase;
import fi.dy.masa.tellme.datadump.DataProviderClient;
import fi.dy.masa.tellme.reference.Reference;
import net.fabricmc.api.ModInitializer;

public class TellMe implements ModInitializer
{
    public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);
    public static DataProviderBase dataProvider;
    private static boolean isClient;

    public TellMe()
    {
        dataProvider = new DataProviderBase();

        ArgumentTypes.register("tellme:biome", BiomeArgument.class, new ConstantArgumentSerializer<>(BiomeArgument::create));
        ArgumentTypes.register("tellme:file", FileArgument.class, new ConstantArgumentSerializer<>(FileArgument::createEmpty));
        ArgumentTypes.register("tellme:grouping", GroupingArgument.class, new ConstantArgumentSerializer<>(GroupingArgument::create));
        ArgumentTypes.register("tellme:output_format", OutputFormatArgument.class, new ConstantArgumentSerializer<>(OutputFormatArgument::create));
        ArgumentTypes.register("tellme:output_type", OutputTypeArgument.class, new ConstantArgumentSerializer<>(OutputTypeArgument::create));
        ArgumentTypes.register("tellme:string_collection", StringCollectionArgument.class, new ConstantArgumentSerializer<>(() -> StringCollectionArgument.create(() -> Collections.emptyList(), "")));
    }

    @Override
    public void onInitialize()
    {
    }

    public static void setIsClient()
    {
        isClient = true;
        dataProvider = new DataProviderClient();
    }

    public static boolean isClient()
    {
        return isClient;
    }
}
