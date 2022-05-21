package fi.dy.masa.tellme.util;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import fi.dy.masa.malilib.util.ItemUtils;

/**
 * Wrapper class for ItemStack, which implements equals()
 * for the item, damage and NBT, but not stackSize.
 */
public class ItemType
{
    private final ItemStack stack;

    public ItemType(@Nonnull ItemStack stack)
    {
        this.stack = stack.copy();
    }

    public ItemStack getStack()
    {
        return this.stack;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        //result = prime * result + ((stack == null) ? 0 : stack.hashCode());
        result = prime * result + this.stack.getMetadata();
        result = prime * result + this.stack.getItem().hashCode();
        NBTTagCompound tag = ItemUtils.getTag(this.stack);
        result = prime * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;

        ItemType other = (ItemType) obj;
        boolean isEmpty1 = ItemUtils.isEmpty(this.stack);
        boolean isEmpty2 = ItemUtils.isEmpty(other.stack);

        if (isEmpty1 || isEmpty2)
        {
            return isEmpty1 == isEmpty2;
        }
        else
        {
            if (this.stack.getMetadata() != other.stack.getMetadata())
            {
                return false;
            }

            if (this.stack.getItem() != other.stack.getItem())
            {
                return false;
            }

            return ItemStack.areItemStackTagsEqual(this.stack, other.stack);
        }
    }
}
