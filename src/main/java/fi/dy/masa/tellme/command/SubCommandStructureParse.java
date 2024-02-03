package fi.dy.masa.tellme.command;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import malilib.util.data.json.JsonUtils;
import malilib.util.game.wrap.NbtWrap;
import malilib.util.position.BlockPos;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.DataDump.Format;

public class SubCommandStructureParse extends SubCommand
{
    public SubCommandStructureParse(CommandTellme baseCommand)
    {
        super(baseCommand);

        this.subSubCommands.add("console");
        this.subSubCommands.add("dump");
    }

    @Override
    public String getName()
    {
        return "structure-data";
    }

    private File getDir()
    {
        return new File(TellMe.configDirPath, "data");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length > 1)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, this.getExistingStructureFileNames(this.getDir()));
        }

        return super.getTabCompletions(server, sender, args, targetPos);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        super.execute(server, sender, args);

        if (args.length >= 2 && (args[0].equals("console") || args[0].equals("dump")))
        {
            String output = args[0];
            args = dropFirstStrings(args, 1);
            File dir = this.getDir();

            for (String fileName : args)
            {
                Map<String, Multimap<String, StructureBoundingBox>> map = this.parseFile(dir, fileName);

                if (map != null)
                {
                    DataDump dump = this.generateDataDump(map);

                    if (dump != null)
                    {
                        if (output.equals("dump"))
                        {
                            File file = DataDump.dumpDataToFile("structure_data", dump.getLines());

                            if (file != null)
                            {
                                SubCommand.sendClickableLinkMessage(sender, "Output written to file %s", file);
                            }
                        }
                        else
                        {
                            DataDump.printDataToLogger(dump.getLines());
                            this.sendMessage(sender, "Command output printed to console");
                        }
                    }

                    this.generateJsonOutput(map, sender);
                }
            }
        }
    }

    private DataDump generateDataDump(Map<String, Multimap<String, StructureBoundingBox>> map)
    {
        DataDump dump = new DataDump(3, Format.ASCII);
        dump.setSort(false);
        dump.setUseColumnSeparator(true);
        dump.addTitle("Category", "Type", "Bounding Box");

        for (String mainId : map.keySet())
        {
            Multimap<String, StructureBoundingBox> mapType = map.get(mainId);

            for (String childId : mapType.keySet())
            {
                for (StructureBoundingBox bb : mapType.get(childId))
                {
                    dump.addData(mainId, childId, String.format("[%d, %d, %d => %d, %d, %d]",
                            bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ));
                }
            }
        }

        return dump;
    }

    private void generateJsonOutput(Map<String, Multimap<String, StructureBoundingBox>> map, ICommandSender sender)
    {
        JsonObject root = new JsonObject();

        for (String mainId : map.keySet())
        {
            JsonObject objCategory = new JsonObject();
            root.add(mainId, objCategory);

            Multimap<String, StructureBoundingBox> mapType = map.get(mainId);

            for (String childId : mapType.keySet())
            {
                JsonObject objType = new JsonObject();
                JsonArray arrBoxes = new JsonArray();

                objCategory.add(childId, objType);
                objType.add("bounding_boxes", arrBoxes);

                for (StructureBoundingBox bb : mapType.get(childId))
                {
                    JsonArray point1 = new JsonArray();
                    JsonArray point2 = new JsonArray();
                    JsonArray point3 = new JsonArray();
                    JsonArray point4 = new JsonArray();

                    point1.add(bb.minX);
                    point1.add(bb.minY);
                    point1.add(bb.minZ);

                    point2.add(bb.maxX);
                    point2.add(bb.minY);
                    point2.add(bb.minZ);

                    point3.add(bb.maxX);
                    point3.add(bb.minY);
                    point3.add(bb.maxZ);

                    point4.add(bb.minX);
                    point4.add(bb.minY);
                    point4.add(bb.maxZ);

                    JsonArray arr = new JsonArray();

                    arr.add(point1);
                    arr.add(point2);
                    arr.add(point3);
                    arr.add(point4);

                    arrBoxes.add(arr);
                }
            }
        }

        File file = DataDump.dumpDataToFile("structure_overlay", ".json", ImmutableList.of(JsonUtils.GSON.toJson(root)));

        if (file != null)
        {
            SubCommand.sendClickableLinkMessage(sender, "Output written to file %s", file);
        }
    }

    @Nullable
    private Map<String, Multimap<String, StructureBoundingBox>> parseFile(File dir, String fileName)
    {
        File file = new File(dir, fileName);

        if (file.exists() && file.isFile() && file.canRead())
        {
            Exception ex = null;

            try
            {
                FileInputStream is = new FileInputStream(file);
                NBTTagCompound nbt = CompressedStreamTools.readCompressed(is);
                is.close();

                if (nbt != null &&
                    NbtWrap.containsCompound(nbt, "data"))
                {
                    NBTTagCompound tagData = NbtWrap.getCompound(nbt, "data");
                    NBTTagCompound tagFeatures = NbtWrap.getCompound(tagData, "Features");
                    Map<String, Multimap<String, StructureBoundingBox>> mapMain = new HashMap<>();

                    for (String key : NbtWrap.getKeys(tagFeatures))
                    {
                        NBTTagCompound tag = NbtWrap.getCompound(tagFeatures, key);

                        if (NbtWrap.containsList(tag, "Children"))
                        {
                            NBTTagList tagList = NbtWrap.getListOfCompounds(tag, "Children");
                            String mainId = NbtWrap.getString(tag, "id");
                            Multimap<String, StructureBoundingBox> mapType;

                            if (mapMain.containsKey(mainId))
                            {
                                mapType = mapMain.get(mainId);
                            }
                            else
                            {
                                mapType = ArrayListMultimap.create();
                                mapMain.put(mainId, mapType);
                            }

                            NBTTagCompound tagChild = NbtWrap.getCompoundAt(tagList, 0);
                            String idChild = NbtWrap.getString(tagChild, "id");

                            // Only include the main BB for structures with many child parts
                            if (NbtWrap.getListSize(tagList) > 1)
                            {
                                int[] bb = NbtWrap.getIntArray(tag, "BB");
                                StructureBoundingBox mainBB = StructureBoundingBox.createProper(bb[0], bb[1], bb[2], bb[3], bb[4], bb[5]);
                                mapType.put(idChild, mainBB);
                            }
                            else
                            {
                                int[] bb = NbtWrap.getIntArray(tagChild, "BB");
                                StructureBoundingBox childBB = StructureBoundingBox.createProper(bb[0], bb[1], bb[2], bb[3], bb[4], bb[5]);
                                mapType.put(idChild, childBB);
                            }
                        }
                    }

                    return mapMain;
                }
            }
            catch (Exception e)
            {
                ex = e;
            }

            TellMe.LOGGER.warn("Failed to read structure data from file '{}'", file.getAbsolutePath(), ex);
        }

        return null;
    }

    private List<String> getExistingStructureFileNames(File dir)
    {
        if (dir.isDirectory())
        {
            String[] names = dir.list(SubCommandBatchRun.FILTER_FILES);
            return Arrays.asList(names);
        }

        return Collections.emptyList();
    }
}
