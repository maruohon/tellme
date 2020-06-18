package fi.dy.masa.tellme.util.nbt;

import javax.annotation.Nullable;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Formatting;
import fi.dy.masa.tellme.util.Constants;

public abstract class NbtStringifierBase
{
    @Nullable protected final String baseColor;
    protected final boolean colored;
    protected final boolean useNumberSuffix;

    protected String tagNameQuote = "\"";
    protected String keyColor = Formatting.YELLOW.toString();
    protected String numberColor = Formatting.GOLD.toString();
    protected String numberTypeColor = Formatting.RED.toString();
    protected String stringColor = Formatting.GREEN.toString();

    public NbtStringifierBase(boolean useNumberSuffix)
    {
        this(useNumberSuffix, null);
    }

    public NbtStringifierBase(boolean useNumberSuffix, @Nullable String baseColor)
    {
        this.colored = baseColor != null;
        this.useNumberSuffix = useNumberSuffix;
        this.baseColor = baseColor;
    }

    protected String getFormattedTagName(String name)
    {
        if (name.length() == 0)
        {
            return name;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(this.tagNameQuote);

        if (this.colored)
        {
            sb.append(this.keyColor);
            sb.append(name);
            sb.append(this.baseColor);
        }
        else
        {
            sb.append(name);
        }

        sb.append(this.tagNameQuote);

        return sb.toString();
    }

    @Nullable
    protected String getPrimitiveValue(Tag tag)
    {
        switch (tag.getType())
        {
            case Constants.NBT.TAG_BYTE:    return String.valueOf(((ByteTag) tag).getByte());
            case Constants.NBT.TAG_SHORT:   return String.valueOf(((ShortTag) tag).getShort());
            case Constants.NBT.TAG_INT:     return String.valueOf(((IntTag) tag).getInt());
            case Constants.NBT.TAG_LONG:    return String.valueOf(((LongTag) tag).getLong());
            case Constants.NBT.TAG_FLOAT:   return String.valueOf(((FloatTag) tag).getFloat());
            case Constants.NBT.TAG_DOUBLE:  return String.valueOf(((DoubleTag) tag).getDouble());
            case Constants.NBT.TAG_STRING:  return ((StringTag) tag).asString();
        }

        return null;
    }

    @Nullable
    protected String getNumberSuffix(int tagId)
    {
        switch (tagId)
        {
            case Constants.NBT.TAG_BYTE:    return "b";
            case Constants.NBT.TAG_SHORT:   return "s";
            case Constants.NBT.TAG_LONG:    return "L";
            case Constants.NBT.TAG_FLOAT:   return "f";
            case Constants.NBT.TAG_DOUBLE:  return "d";
        }

        return null;
    }

    @Nullable
    protected String getPrimitiveColorCode(int tagId)
    {
        switch (tagId)
        {
            case Constants.NBT.TAG_BYTE:
            case Constants.NBT.TAG_SHORT:
            case Constants.NBT.TAG_INT:
            case Constants.NBT.TAG_LONG:
            case Constants.NBT.TAG_FLOAT:
            case Constants.NBT.TAG_DOUBLE:
                return this.numberColor;

            case Constants.NBT.TAG_STRING:
                return this.stringColor;
        }

        return null;
    }

    protected String getFormattedPrimitiveString(Tag tag)
    {
        String valueStr = this.getPrimitiveValue(tag);
        String valueColorStr = this.colored ? this.getPrimitiveColorCode(tag.getType()) : null;
        String numberSuffixStr = this.useNumberSuffix ? this.getNumberSuffix(tag.getType()) : null;
        boolean useQuotes = tag.getType() == Constants.NBT.TAG_STRING;

        return this.getFormattedPrimitiveString(valueStr, useQuotes, valueColorStr, numberSuffixStr);
    }

    protected String getFormattedPrimitiveString(String valueStr, boolean useQuotes, @Nullable String valueColorStr, @Nullable String numberSuffixStr)
    {
        StringBuilder sb = new StringBuilder();

        if (valueStr == null)
        {
            return "";
        }

        if (useQuotes)
        {
            sb.append('"');
        }

        if (valueColorStr != null)
        {
            sb.append(valueColorStr);
        }

        sb.append(valueStr);

        if (numberSuffixStr != null)
        {
            if (this.colored)
            {
                sb.append(this.numberTypeColor);
            }

            sb.append(numberSuffixStr);
        }

        if (this.colored)
        {
            sb.append(this.baseColor);
        }

        if (useQuotes)
        {
            sb.append('"');
        }

        return sb.toString();
    }

    protected void appendTag(String tagName, Tag tag)
    {
        switch (tag.getType())
        {
            case Constants.NBT.TAG_COMPOUND:
                this.appendCompound(tagName, (CompoundTag) tag);
                break;

            case Constants.NBT.TAG_LIST:
                this.appendList(tagName, (ListTag) tag);
                break;

            case Constants.NBT.TAG_BYTE_ARRAY:
                this.appendByteArray(tagName, ((ByteArrayTag) tag).getByteArray());
                break;

            case Constants.NBT.TAG_INT_ARRAY:
                this.appendIntArray(tagName, ((IntArrayTag) tag).getIntArray());
                break;

            case Constants.NBT.TAG_LONG_ARRAY:
                this.appendLongArray(tagName, ((LongArrayTag) tag).getLongArray());
                break;

            default:
                this.appendPrimitive(tagName, tag);
        }
    }

    protected abstract void appendPrimitive(String tagName, Tag tag);
    protected abstract void appendCompound(String tagName, CompoundTag tag);
    protected abstract void appendList(String tagName, ListTag list);
    protected abstract void appendByteArray(String tagName, byte[] arr);
    protected abstract void appendIntArray(String tagName, int[] arr);
    protected abstract void appendLongArray(String tagName, long[] arr);
}
