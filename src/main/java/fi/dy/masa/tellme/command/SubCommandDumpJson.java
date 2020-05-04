package fi.dy.masa.tellme.command;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.command.argument.StringCollectionArgument;
import fi.dy.masa.tellme.datadump.BlockDump;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.ItemDump;
import fi.dy.masa.tellme.util.OutputUtils;

public class SubCommandDumpJson
{
    public static CommandNode<CommandSource> registerSubCommand(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralCommandNode<CommandSource> subCommandRootNode = Commands.literal("dump-json").build();

        @SuppressWarnings("unchecked")
        ArgumentCommandNode<CommandSource, List<String>> dumpTypesNode =
                Commands.argument("dump_types",
                        StringCollectionArgument.create(() -> ImmutableList.of("blocks", "items-with-props"), "No dump types given"))
                .executes(c -> execute(c, (List<String>) c.getArgument("dump_types", List.class))).build();

        subCommandRootNode.addChild(dumpTypesNode);

        return subCommandRootNode;
    }

    private static int execute(CommandContext<CommandSource> ctx, List<String> types) throws CommandSyntaxException
    {
        @Nullable Entity entity = ctx.getSource().getEntity();

        for (String type : types)
        {
            String lines = getData(type, entity);

            if (lines == null)
            {
                CommandUtils.throwException("Unrecognized type: '" + type + "'");;
            }

            OutputUtils.printOutput(Arrays.asList(lines), OutputType.FILE, DataDump.Format.ASCII, type, entity);
        }

        return 1;
    }

    @Nullable
    private static String getData(String type, @Nullable Entity entity)
    {
        switch (type)
        {
            case "blocks":              return BlockDump.getJsonBlockDump();
            case "items-with-props":    if (entity instanceof PlayerEntity) { return ItemDump.getJsonItemsWithPropsDump((PlayerEntity) entity); } break;
        }

        return null;
    }
}
