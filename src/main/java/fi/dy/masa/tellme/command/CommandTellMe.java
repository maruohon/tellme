package fi.dy.masa.tellme.command;

import java.util.Collections;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import fi.dy.masa.tellme.command.argument.BiomeArgument;
import fi.dy.masa.tellme.command.argument.BlockStateCountGroupingArgument;
import fi.dy.masa.tellme.command.argument.FileArgument;
import fi.dy.masa.tellme.command.argument.GroupingArgument;
import fi.dy.masa.tellme.command.argument.OutputFormatArgument;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.command.argument.StringCollectionArgument;

public class CommandTellMe
{
    public static void registerServerCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        register(dispatcher, "tellme", 2);
    }

    public static void registerClientCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        register(dispatcher, "ctellme", 0);
    }

    public static void registerArgumentTypes()
    {
        ArgumentTypes.register("tellme:biome", BiomeArgument.class, new ConstantArgumentSerializer<>(BiomeArgument::create));
        ArgumentTypes.register("tellme:block_grouping", BlockStateCountGroupingArgument.class, new ConstantArgumentSerializer<>(BlockStateCountGroupingArgument::create));
        ArgumentTypes.register("tellme:file", FileArgument.class, new ConstantArgumentSerializer<>(FileArgument::createEmpty));
        ArgumentTypes.register("tellme:grouping", GroupingArgument.class, new ConstantArgumentSerializer<>(GroupingArgument::create));
        ArgumentTypes.register("tellme:output_format", OutputFormatArgument.class, new ConstantArgumentSerializer<>(OutputFormatArgument::create));
        ArgumentTypes.register("tellme:output_type", OutputTypeArgument.class, new ConstantArgumentSerializer<>(OutputTypeArgument::create));
        ArgumentTypes.register("tellme:string_collection", StringCollectionArgument.class, new ConstantArgumentSerializer<>(() -> StringCollectionArgument.create(() -> Collections.emptyList(), "")));
    }

    protected static void register(CommandDispatcher<ServerCommandSource> dispatcher, String baseCommandName, final int permissionLevel)
    {
        dispatcher.register(
                CommandManager.literal(baseCommandName)
                    .requires((src) -> src.hasPermissionLevel(permissionLevel))
                    .then(SubCommandBatchRun.registerSubCommand(dispatcher))
                    .then(SubCommandBiome.registerSubCommand(dispatcher))
                    .then(SubCommandBiomeLocate.registerSubCommand(dispatcher))
                    .then(SubCommandBiomeStats.registerSubCommand(dispatcher))
                    .then(SubCommandBlockStats.registerSubCommand(dispatcher))
                    .then(SubCommandCopyToClipboard.registerSubCommand(dispatcher))
                    .then(SubCommandDump.registerSubCommand(dispatcher))
                    .then(SubCommandDumpJson.registerSubCommand(dispatcher))
                    .then(SubCommandEntityData.registerSubCommand(dispatcher))
                    .then(SubCommandHolding.registerSubCommand(dispatcher))
                    .then(SubCommandLoaded.registerSubCommand(dispatcher))
                    .then(SubCommandLocate.registerSubCommand(dispatcher))
                    .then(SubCommandLookingAt.registerSubCommand(dispatcher))
        );
    }
}
