package fi.dy.masa.tellme.command;

import java.util.Collections;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
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
        ArgumentTypeRegistry.registerArgumentType(new Identifier("tellme:biome"), BiomeArgument.class, ConstantArgumentSerializer.of(BiomeArgument::create));
        ArgumentTypeRegistry.registerArgumentType(new Identifier("tellme:block_grouping"), BlockStateCountGroupingArgument.class, ConstantArgumentSerializer.of(BlockStateCountGroupingArgument::create));
        ArgumentTypeRegistry.registerArgumentType(new Identifier("tellme:file"), FileArgument.class, ConstantArgumentSerializer.of(FileArgument::createEmpty));
        ArgumentTypeRegistry.registerArgumentType(new Identifier("tellme:grouping"), GroupingArgument.class, ConstantArgumentSerializer.of(GroupingArgument::create));
        ArgumentTypeRegistry.registerArgumentType(new Identifier("tellme:output_format"), OutputFormatArgument.class, ConstantArgumentSerializer.of(OutputFormatArgument::create));
        ArgumentTypeRegistry.registerArgumentType(new Identifier("tellme:output_type"), OutputTypeArgument.class, ConstantArgumentSerializer.of(OutputTypeArgument::create));
        ArgumentTypeRegistry.registerArgumentType(new Identifier("tellme:string_collection"), StringCollectionArgument.class, ConstantArgumentSerializer.of(() -> StringCollectionArgument.create(Collections::emptyList, "")));
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
