package fi.dy.masa.tellme.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.command.argument.StringCollectionArgument;
import fi.dy.masa.tellme.datadump.DumpUtils;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class SubCommandDumpPackdevUtilsSnippet
{
    public static CommandNode<CommandSourceStack> registerSubCommand(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralCommandNode<CommandSourceStack> subCommandRootNode = Commands.literal("dump-packdevutils-snippet").build();

        @SuppressWarnings("unchecked")
        ArgumentCommandNode<CommandSourceStack, List<String>> dumpTypesNode =
                Commands.argument("dump_types",
                                  StringCollectionArgument.create(() -> ImmutableList.of("blocks", "items", "entities", "biomes", "enchantments", "potions"), "No dump types given"))
                        .executes(c -> execute(c, (List<String>) c.getArgument("dump_types", List.class))).build();

        subCommandRootNode.addChild(dumpTypesNode);

        return subCommandRootNode;
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, List<String> types) throws CommandSyntaxException
    {
        @Nullable Entity entity = ctx.getSource().getEntity();

        for (String type : types)
        {
            String lines = getData(type, entity);

            if (lines == null)
            {
                CommandUtils.throwException("Unrecognized type: '" + type + "'");;
            }

            OutputUtils.printOutput(Collections.singletonList(lines), CommandUtils.OutputType.FILE, DataDump.Format.ASCII, type, entity);
        }

        return 1;
    }

    @Nullable
    private static String getData(String type, @Nullable Entity entity)
    {
        switch (type)
        {
            case "blocks":          return DumpUtils.getPackDevUtilsSnippetData(ForgeRegistries.BLOCKS, "blocks");
            case "items":           return DumpUtils.getPackDevUtilsSnippetData(ForgeRegistries.ITEMS, "items");
            case "entities":        return DumpUtils.getPackDevUtilsSnippetData(ForgeRegistries.ENTITY_TYPES, "entities");
            case "biomes":          return DumpUtils.getPackDevUtilsSnippetData(ForgeRegistries.BIOMES, "biomes");
            case "enchantments":    return DumpUtils.getPackDevUtilsSnippetData(ForgeRegistries.ENCHANTMENTS, "enchant");
            case "potions":         return DumpUtils.getPackDevUtilsSnippetData(ForgeRegistries.POTIONS, "potions");
        }

        return null;
    }
}
