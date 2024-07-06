package fi.dy.masa.tellme.command;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import javax.annotation.Nullable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.JsonOps;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.reference.Reference;
import fi.dy.masa.tellme.util.OutputUtils;

public class SubCommandEntityNbt
{
    public static CommandNode<ServerCommandSource> registerSubCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralCommandNode<ServerCommandSource> subCommandRootNode = CommandManager.literal("entity-nbt").executes(c -> printHelp(c.getSource())).build();

        subCommandRootNode.addChild(createNodes());

        return subCommandRootNode;
    }

    private static int printHelp(ServerCommandSource source)
    {
        CommandUtils.sendMessage(source, "Dumps the NBT data of the given entity");
        CommandUtils.sendMessage(source, "Usage: /tellme entity-nbt <entity selector>");

        return 1;
    }

    private static CommandNode<ServerCommandSource> createNodes()
    {
        CommandNode<ServerCommandSource> argEntity = CommandManager.argument("entity", EntityArgumentType.entities())
                                                                             .executes(c -> dumpEntityData(c, EntityArgumentType.getEntities(c, "entity"))).build();
        return argEntity;
    }

    private static int dumpEntityData(CommandContext<ServerCommandSource> context,
                                      Collection<? extends Entity> entities)
    {
        JsonArray arr = new JsonArray();

        if (entities.size() > 0)
        {
            for (Entity entity : entities)
            {
                try
                {
                    NbtCompound tag = new NbtCompound();

                    if (entity.saveSelfNbt(tag) == false)
                    {
                        entity.writeNbt(tag);
                    }

                    arr.add(NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, tag));
                }
                catch (Exception e)
                {
                    TellMe.logger.warn("Failed to serialize entity data as JSON for entity {}", entity, e);
                }
            }
        }
        else
        {
            context.getSource().sendFeedback(() -> Text.literal("No entities provided"), false);
        }

        if (arr.size() > 0)
        {
            String date = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS").format(new Date(System.currentTimeMillis()));
            String fileNameBaseWithDate = "entity-nbt_" + date + ".json";
            Path file = TellMe.dataProvider.getConfigDirectory().toPath().resolve(Reference.MOD_ID).resolve(fileNameBaseWithDate);
            writeJsonToFile(arr, file);

            @Nullable PlayerEntity player = context.getSource().getEntity() instanceof PlayerEntity ? (PlayerEntity) context.getSource().getEntity() : null;

            if (player != null)
            {
                OutputUtils.sendClickableLinkMessage(player, "Output written to file %s", file.toFile());
            }
        }
        else
        {
            context.getSource().sendFeedback(() -> Text.literal("No output generated"), false);
        }

        return 1;
    }

    public static final Gson GSON = new GsonBuilder().create();

    public static boolean writeJsonToFile(JsonElement root, Path file)
    {
        return writeJsonToFile(root, file, GSON);
    }

    public static boolean writeJsonToFile(final JsonElement root, final Path file, final Gson gson)
    {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8))
        {
            writer.write(gson.toJson(root));
            writer.close();
            return true;
        }
        catch (Exception e)
        {
            TellMe.logger.warn("Failed to write to file '{}'", file.toAbsolutePath(), e);
        }

        return false;
    }
}
