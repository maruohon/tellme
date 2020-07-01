package fi.dy.masa.tellme.command;

import java.util.Collections;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import fi.dy.masa.tellme.command.argument.BiomeArgument;
import fi.dy.masa.tellme.command.argument.FileArgument;
import fi.dy.masa.tellme.command.argument.GroupingArgument;
import fi.dy.masa.tellme.command.argument.OutputFormatArgument;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.command.argument.StringCollectionArgument;

public class CommandTellMe
{
    public static void registerServerCommand(CommandDispatcher<CommandSource> dispatcher)
    {
        register(dispatcher, "tellme", 4);
    }

    public static void registerClientCommand(CommandDispatcher<CommandSource> dispatcher)
    {
        register(dispatcher, "ctellme", 0);
    }

    public static void registerArgumentTypes()
    {
        ArgumentTypes.register("tellme:biome", BiomeArgument.class, new ArgumentSerializer<>(BiomeArgument::create));
        ArgumentTypes.register("tellme:file", FileArgument.class, new ArgumentSerializer<>(FileArgument::createEmpty));
        ArgumentTypes.register("tellme:grouping", GroupingArgument.class, new ArgumentSerializer<>(GroupingArgument::create));
        ArgumentTypes.register("tellme:output_format", OutputFormatArgument.class, new ArgumentSerializer<>(OutputFormatArgument::create));
        ArgumentTypes.register("tellme:output_type", OutputTypeArgument.class, new ArgumentSerializer<>(OutputTypeArgument::create));
        ArgumentTypes.register("tellme:string_collection", StringCollectionArgument.class, new ArgumentSerializer<>(() -> StringCollectionArgument.create(() -> Collections.emptyList(), "")));
    }

    protected static void register(CommandDispatcher<CommandSource> dispatcher, String baseCommandName, final int permissionLevel)
    {
        dispatcher.register(
                Commands.literal(baseCommandName)
                    .requires((src) -> src.hasPermissionLevel(permissionLevel))
                    .then(SubCommandBatchRun.registerSubCommand(dispatcher))
                    .then(SubCommandBiome.registerSubCommand(dispatcher))
                    .then(SubCommandBiomeLocate.registerSubCommand(dispatcher))
                    .then(SubCommandBiomeStats.registerSubCommand(dispatcher))
                    .then(SubCommandBlockStats.registerSubCommand(dispatcher))
                    .then(SubCommandCopyToClipboard.registerSubCommand(dispatcher))
                    .then(SubCommandDump.registerSubCommand(dispatcher))
                    .then(SubCommandDumpJson.registerSubCommand(dispatcher))
                    .then(SubCommandHolding.registerSubCommand(dispatcher))
                    .then(SubCommandLoaded.registerSubCommand(dispatcher))
                    .then(SubCommandLocate.registerSubCommand(dispatcher))
                    .then(SubCommandLookingAt.registerSubCommand(dispatcher))
        );
    }
}
