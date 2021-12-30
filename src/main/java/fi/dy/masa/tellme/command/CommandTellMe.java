package fi.dy.masa.tellme.command;

import java.util.Collections;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import fi.dy.masa.tellme.command.argument.BiomeArgument;
import fi.dy.masa.tellme.command.argument.BlockStateCountGroupingArgument;
import fi.dy.masa.tellme.command.argument.FileArgument;
import fi.dy.masa.tellme.command.argument.GroupingArgument;
import fi.dy.masa.tellme.command.argument.OutputFormatArgument;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.command.argument.StringCollectionArgument;

public class CommandTellMe
{
    public static void registerServerCommand(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        register(dispatcher, "tellme", 4);
    }

    public static void registerClientCommand(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        register(dispatcher, "ctellme", 0);
    }

    public static void registerArgumentTypes()
    {
        ArgumentTypes.register("tellme:biome", BiomeArgument.class, new EmptyArgumentSerializer<>(BiomeArgument::create));
        ArgumentTypes.register("tellme:block_grouping", BlockStateCountGroupingArgument.class, new EmptyArgumentSerializer<>(BlockStateCountGroupingArgument::create));
        ArgumentTypes.register("tellme:file", FileArgument.class, new EmptyArgumentSerializer<>(FileArgument::createEmpty));
        ArgumentTypes.register("tellme:grouping", GroupingArgument.class, new EmptyArgumentSerializer<>(GroupingArgument::create));
        ArgumentTypes.register("tellme:output_format", OutputFormatArgument.class, new EmptyArgumentSerializer<>(OutputFormatArgument::create));
        ArgumentTypes.register("tellme:output_type", OutputTypeArgument.class, new EmptyArgumentSerializer<>(OutputTypeArgument::create));
        ArgumentTypes.register("tellme:string_collection", StringCollectionArgument.class, new EmptyArgumentSerializer<>(() -> StringCollectionArgument.create(() -> Collections.emptyList(), "")));
    }

    protected static void register(CommandDispatcher<CommandSourceStack> dispatcher, String baseCommandName, final int permissionLevel)
    {
        dispatcher.register(
                Commands.literal(baseCommandName)
                    .requires((src) -> src.hasPermission(permissionLevel))
                    .then(SubCommandBatchRun.registerSubCommand(dispatcher))
                    .then(SubCommandBiome.registerSubCommand(dispatcher))
                    .then(SubCommandBiomeLocate.registerSubCommand(dispatcher))
                    .then(SubCommandBiomeStats.registerSubCommand(dispatcher))
                    .then(SubCommandBlockStats.registerSubCommand(dispatcher))
                    .then(SubCommandCopyToClipboard.registerSubCommand(dispatcher))
                    .then(SubCommandDump.registerSubCommand(dispatcher))
                    .then(SubCommandDumpJson.registerSubCommand(dispatcher))
                    .then(SubCommandDumpPackdevUtilsSnippet.registerSubCommand(dispatcher))
                    .then(SubCommandEntityData.registerSubCommand(dispatcher))
                    .then(SubCommandHolding.registerSubCommand(dispatcher))
                    .then(SubCommandLoaded.registerSubCommand(dispatcher))
                    .then(SubCommandLocate.registerSubCommand(dispatcher))
                    .then(SubCommandLookingAt.registerSubCommand(dispatcher))
        );
    }
}
