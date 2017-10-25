package fi.dy.masa.tellme.proxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import com.google.common.base.MoreObjects;
import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.ClientCommandTellme;
import fi.dy.masa.tellme.config.Configs;
import fi.dy.masa.tellme.datadump.BlockDump;
import fi.dy.masa.tellme.datadump.ItemDump;

public class ClientProxy extends CommonProxy
{
    @Override
    public String getBiomeName(Biome biome)
    {
        return biome.getBiomeName();
    }

    @Override
    public void getCurrentBiomeInfoClientSide(EntityPlayer player, Biome bgb)
    {
        BlockPos pos = player.getPosition();
        String pre = TextFormatting.GREEN.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

        // These are client-side only:
        int color = bgb.getModdedBiomeGrassColor(bgb.getGrassColorAtPos(pos));
        player.sendMessage(new TextComponentString(String.format("Grass color: %s0x%08X%s (%s%d%s)", pre, color, rst, pre, color, rst)));

        color = bgb.getModdedBiomeFoliageColor(bgb.getFoliageColorAtPos(pos));
        player.sendMessage(new TextComponentString(String.format("Foliage color: %s0x%08X%s (%s%d%s)", pre, color, rst, pre, color, rst)));
    }

    @Override
    public void getDataForBlockSubtypes(Block block, ResourceLocation rl, BlockDump blockDump)
    {
        Item item = Item.getItemFromBlock(block);

        if (item != null)
        {
            NonNullList<ItemStack> stacks = NonNullList.<ItemStack>create();
            CreativeTabs tab = block.getCreativeTabToDisplayOn();
            block.getSubBlocks(tab, stacks);
            boolean subtypes = stacks.size() > 1;

            for (ItemStack stack : stacks)
            {
                blockDump.addData(block, rl, true, subtypes, stack);
            }
        }
        else
        {
            blockDump.addData(block, rl, false, false, ItemStack.EMPTY);
        }
    }

    @Override
    public void getDataForItemSubtypes(Item item, ResourceLocation rl, ItemDump itemDump)
    {
        if (item.getHasSubtypes())
        {
            for (CreativeTabs tab : item.getCreativeTabs())
            {
                NonNullList<ItemStack> stacks = NonNullList.<ItemStack>create();
                item.getSubItems(tab, stacks);

                for (ItemStack stack : stacks)
                {
                    // FIXME: Ignore identical duplicate entries from different tabs...
                    itemDump.addData(item, rl, true, stack);
                }
            }
        }
        else
        {
            itemDump.addData(item, rl, false, new ItemStack(item, 1, 0));
        }
    }

    @Override
    public void getExtendedBlockStateInfo(World world, IBlockState state, BlockPos pos, List<String> lines)
    {
        try
        {
            state = state.getBlock().getExtendedState(state, world, pos);
        }
        catch (Exception e)
        {
            TellMe.logger.error("getFullBlockInfo(): Exception while calling getExtendedState() on the block");
        }

        if (state instanceof IExtendedBlockState)
        {
            IExtendedBlockState extendedState = (IExtendedBlockState) state;

            if (extendedState.getUnlistedProperties().size() > 0)
            {
                lines.add("IExtendedBlockState properties:");

                UnmodifiableIterator<Entry<IUnlistedProperty<?>, Optional<?>>> iterExt = extendedState.getUnlistedProperties().entrySet().iterator();

                while (iterExt.hasNext())
                {
                    Entry<IUnlistedProperty<?>, Optional<?>> entry = iterExt.next();
                    lines.add(MoreObjects.toStringHelper(entry.getKey())
                            .add("name", entry.getKey().getName())
                            .add("clazz", entry.getKey().getType())
                            .add("value", entry.getValue().toString()).toString());
                }
            }
        }
    }

    @Override
    public Collection<Chunk> getLoadedChunks(World world)
    {
        Collection<Chunk> chunksServer = super.getLoadedChunks(world);

        if (chunksServer.isEmpty() == false)
        {
            return chunksServer;
        }

        EntityPlayer player = Minecraft.getMinecraft().player;

        if (player != null)
        {
            BlockPos pos = player.getPosition();
            int cX = pos.getX() >> 4;
            int cZ = pos.getZ() >> 4;
            int radius = Minecraft.getMinecraft().gameSettings.renderDistanceChunks + 1;
            IChunkProvider provider = world.getChunkProvider();
            List<Chunk> chunks = new ArrayList<Chunk>();

            for (int z = cZ - radius; z <= (cZ + radius); z++)
            {
                for (int x = cX - radius; x <= (cX + radius); x++)
                {
                    Chunk chunk = provider.getLoadedChunk(x, z);

                    if (chunk != null)
                    {
                        chunks.add(chunk);
                    }
                }
            }

            return chunks;
        }

        return Collections.emptyList();
    }

    @Override
    public boolean isSinglePlayer()
    {
        return Minecraft.getMinecraft().isSingleplayer();
    }

    @Override
    public void registerClientCommand()
    {
        TellMe.logger.info("Registering the client-side command");
        ClientCommandHandler.instance.registerCommand(new ClientCommandTellme());
    }

    @Override
    public void registerEventHandlers()
    {
        MinecraftForge.EVENT_BUS.register(new Configs());
    }
}
