package fi.dy.masa.tellme.util.nbt;

import javax.annotation.Nullable;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;

public abstract class NbtStringifierBase
{
    @Nullable protected final String baseColor;
    protected final boolean colored;
    protected final boolean useNumberSuffix;

    protected String tagNameQuote = "\"";
    protected String keyColor = TextFormatting.YELLOW.toString();
    protected String numberColor = TextFormatting.GOLD.toString();
    protected String numberTypeColor = TextFormatting.RED.toString();
    protected String stringColor = TextFormatting.GREEN.toString();

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
    protected String getPrimitiveValue(INBT tag)
    {
        switch (tag.getId())
        {
            case Constants.NBT.TAG_BYTE:    return String.valueOf(((ByteNBT) tag).getAsByte());
            case Constants.NBT.TAG_SHORT:   return String.valueOf(((ShortNBT) tag).getAsShort());
            case Constants.NBT.TAG_INT:     return String.valueOf(((IntNBT) tag).getAsInt());
            case Constants.NBT.TAG_LONG:    return String.valueOf(((LongNBT) tag).getAsLong());
            case Constants.NBT.TAG_FLOAT:   return String.valueOf(((FloatNBT) tag).getAsFloat());
            case Constants.NBT.TAG_DOUBLE:  return String.valueOf(((DoubleNBT) tag).getAsDouble());
            case Constants.NBT.TAG_STRING:  return ((StringNBT) tag).getAsString();
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

    protected String getFormattedPrimitiveString(INBT tag)
    {
        String valueStr = this.getPrimitiveValue(tag);
        String valueColorStr = this.colored ? this.getPrimitiveColorCode(tag.getId()) : null;
        String numberSuffixStr = this.useNumberSuffix ? this.getNumberSuffix(tag.getId()) : null;
        boolean useQuotes = tag.getId() == Constants.NBT.TAG_STRING;

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

    protected void appendTag(String tagName, INBT tag)
    {
        switch (tag.getId())
        {
            case Constants.NBT.TAG_COMPOUND:
                this.appendCompound(tagName, (CompoundNBT) tag);
                break;

            case Constants.NBT.TAG_LIST:
                this.appendList(tagName, (ListNBT) tag);
                break;

            case Constants.NBT.TAG_BYTE_ARRAY:
                this.appendByteArray(tagName, ((ByteArrayNBT) tag).getAsByteArray());
                break;

            case Constants.NBT.TAG_INT_ARRAY:
                this.appendIntArray(tagName, ((IntArrayNBT) tag).getAsIntArray());
                break;

            case Constants.NBT.TAG_LONG_ARRAY:
                this.appendLongArray(tagName, ((LongArrayNBT) tag).getAsLongArray());
                break;

            default:
                this.appendPrimitive(tagName, tag);
        }
    }

    protected abstract void appendPrimitive(String tagName, INBT tag);
    protected abstract void appendCompound(String tagName, CompoundNBT tag);
    protected abstract void appendList(String tagName, ListNBT list);
    protected abstract void appendByteArray(String tagName, byte[] arr);
    protected abstract void appendIntArray(String tagName, int[] arr);
    protected abstract void appendLongArray(String tagName, long[] arr);
}
