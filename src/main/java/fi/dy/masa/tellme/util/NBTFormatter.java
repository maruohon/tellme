package fi.dy.masa.tellme.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.ReflectionHelper;
import fi.dy.masa.tellme.TellMe;

public class NBTFormatter
{
    @SuppressWarnings("unchecked")
    public static void addFormattedLinePretty(ArrayList<String> lines, NBTBase nbt, String name, int depth)
    {
        String line;
        String pre = "";
        String pre2 = "";
        try
        {
            String fmt = String.format("%%-%ds", (depth * 4));
            if (depth > 0) { pre  = String.format(fmt, ""); }
            fmt = String.format("%%%ds", (depth * 4 + 3));
            pre2 = String.format(fmt, "");
        }
        catch(Exception e)
        {
        }

        switch(nbt.getId())
        {
            case Constants.NBT.TAG_END:
                lines.add(pre + "}");
                break;

            case Constants.NBT.TAG_BYTE:
                lines.add(pre + "TAG_Byte (" + nbt.getId() + ") ('" + name + "'): " + ((NBTTagByte)nbt).func_150287_d());
                break;

            case Constants.NBT.TAG_SHORT:
                lines.add(pre + "TAG_Short (" + nbt.getId() + ") ('" + name + "'): " + ((NBTTagShort)nbt).func_150287_d());
                break;

            case Constants.NBT.TAG_INT:
                lines.add(pre + "TAG_Int (" + nbt.getId() + ") ('" + name + "'): " + ((NBTTagInt)nbt).func_150287_d());
                break;

            case Constants.NBT.TAG_LONG:
                lines.add(pre + "TAG_Long (" + nbt.getId() + ") ('" + name + "'): " + ((NBTTagLong)nbt).func_150291_c());
                break;

            case Constants.NBT.TAG_FLOAT:
                lines.add(pre + "TAG_Float (" + nbt.getId() + ") ('" + name + "'): " + ((NBTTagFloat)nbt).func_150288_h());
                break;

            case Constants.NBT.TAG_DOUBLE:
                lines.add(pre + "TAG_Double (" + nbt.getId() + ") ('" + name + "'): " + ((NBTTagDouble)nbt).func_150286_g());
                break;

            case Constants.NBT.TAG_BYTE_ARRAY:
                lines.add(pre + "TAG_Int_Array (" + nbt.getId() + ") ('" + name + "')");
                byte[] arrByte = ((NBTTagByteArray)nbt).func_150292_c();

                for (int pos = 0; pos < arrByte.length; ++pos)
                {
                    line = pre2;
                    for (int i = 0; i < 30 && pos < arrByte.length; ++i, ++pos)
                    {
                        line.concat(String.format(" %02X", arrByte[pos]));
                    }
                    lines.add(line);
                }
                break;

            case Constants.NBT.TAG_STRING:
                lines.add(pre + "TAG_String (" + nbt.getId() + ") ('" + name + "'): " + ((NBTTagString)nbt).toString());
                break;

            case Constants.NBT.TAG_LIST:
                lines.add(pre + "TAG_List (" + nbt.getId() + ") ('" + name + "')");
                lines.add(pre + "{");

                NBTTagList list = (NBTTagList)nbt;
                NBTBase base;
                int size = list.tagCount();

                // Damn crappy NBTTagList doesn't have a generic get method ;_;
                Field tagList = ReflectionHelper.findField(NBTTagList.class, "tagList", "field_74747_a");
                try
                {
                    for (int i = 0; i < size; ++i)
                    {
                        base = ((ArrayList<NBTBase>)tagList.get((NBTTagList)nbt)).get(i);
                        addFormattedLinePretty(lines, base, "", depth + 1);
                    }
                }
                catch (IllegalAccessException e)
                {
                    TellMe.logger.error("Error while trying to read TagList");
                    e.printStackTrace();
                }
                //NBTFormatterPretty(lines, nbt, name, depth + 1);
                //lines.add(pre + "TODO");

                lines.add(pre + "}");
                break;

            case Constants.NBT.TAG_COMPOUND:
                lines.add(pre + "TAG_Compound (" + nbt.getId() + ") ('" + name + "')");
                lines.add(pre + "{");

                NBTTagCompound tag = (NBTTagCompound)nbt;
                @SuppressWarnings("rawtypes")
                Iterator iterator = tag.func_150296_c().iterator();

                while (iterator.hasNext() == true)
                {
                    String key = (String)iterator.next();
                    addFormattedLinePretty(lines, tag.getTag(key), key, depth + 1);
                }

                lines.add(pre + "}");
                break;

            case Constants.NBT.TAG_INT_ARRAY:
                lines.add(pre + "TAG_Int_Array (" + nbt.getId() + ") ('" + name + "')");
                int[] arrInt = ((NBTTagIntArray)nbt).func_150302_c();

                for (int pos = 0; pos < arrInt.length; ++pos)
                {
                    line = pre2;
                    for (int i = 0; i < 10 && pos < arrInt.length; ++i, ++pos)
                    {
                        line.concat(String.format(" %08X", arrInt[pos]));
                    }
                    lines.add(line);
                }
                break;

            default:
        }
    }

    public static void NBTFormatterPretty(ArrayList<String> lines, NBTTagCompound nbt)
    {
        if (nbt == null)
        {
            return;
        }

        addFormattedLinePretty(lines, nbt, "", 0);
    }
}
