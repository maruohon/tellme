package fi.dy.masa.tellme.proxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import com.google.common.base.MoreObjects;
import com.google.common.collect.UnmodifiableIterator;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancements.Advancement;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.ICommandSender;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.client.FMLClientHandler;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.ClientCommandTellme;
import fi.dy.masa.tellme.config.Configs;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.ItemDump;

public class ClientProxy extends CommonProxy
{
    @Override
    @Nullable
    public Iterable<Advancement> getAdvacements(ICommandSender sender)
    {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.isSingleplayer() && mc.player != null)
        {
            World world = DimensionManager.getWorld(mc.player.getEntityWorld().provider.getDimension());

            if (world instanceof WorldServer)
            {
                return ((WorldServer) world).getAdvancementManager().getAdvancements();
            }
        }
        else
        {
            INetHandler nh = FMLClientHandler.instance().getClientPlayHandler();

            if (nh instanceof NetHandlerPlayClient)
            {
                return ((NetHandlerPlayClient) nh).getAdvancementManager().getAdvancementList().getAdvancements();
            }
        }

        return null;
    }

    @Override
    public String getBiomeName(Biome biome)
    {
        return biome.getBiomeName();
    }

    @Override
    public void getCurrentBiomeInfoClientSide(EntityPlayer player, Biome biome)
    {
        BlockPos pos = player.getPosition();
        String pre = TextFormatting.GREEN.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

        // These are client-side only:
        int color = biome.getGrassColorAtPos(pos);
        player.sendMessage(new TextComponentString(String.format("Grass color: %s0x%08X%s (%s%d%s)", pre, color, rst, pre, color, rst)));

        color = biome.getFoliageColorAtPos(pos);
        player.sendMessage(new TextComponentString(String.format("Foliage color: %s0x%08X%s (%s%d%s)", pre, color, rst, pre, color, rst)));
    }

    @Override
    public Pair<Integer, Integer> getBiomeGrassAndFoliageColors(Biome biome)
    {
        return Pair.of(this.getGrassColor(biome), this.getFoliageColor(biome));
    }

    private int getGrassColor(Biome biome)
    {
        double temperature = MathHelper.clamp(biome.getDefaultTemperature(), 0.0F, 1.0F);
        double humidity = MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
        return biome.getModdedBiomeGrassColor(ColorizerGrass.getGrassColor(temperature, humidity));
    }

    private int getFoliageColor(Biome biome)
    {
        double temperature = MathHelper.clamp(biome.getDefaultTemperature(), 0.0F, 1.0F);
        double humidity = MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
        return biome.getModdedBiomeFoliageColor(ColorizerFoliage.getFoliageColor(temperature, humidity));
    }

    @Override
    public void addCreativeTabData(DataDump dump)
    {
        for (int i = 0; i < CreativeTabs.CREATIVE_TAB_ARRAY.length; i++)
        {
            CreativeTabs tab = CreativeTabs.CREATIVE_TAB_ARRAY[i];

            if (tab != null)
            {
                String index = String.valueOf(i);
                String name = tab.getTabLabel();
                String key = tab.getTranslationKey();
                ItemStack stack = tab.createIcon();

                if (key == null)
                {
                    TellMe.logger.warn("null name for tab at index {} (name: '{}')", tab.getIndex(), name);
                    continue;
                }

                if (name == null)
                {
                    TellMe.logger.warn("null name for tab at index {} (translation key: '{}')", tab.getIndex(), key);
                    continue;
                }

                if (stack == null)
                {
                    TellMe.logger.warn("null icon item for tab at index {} (name: '{}', translation key: '{}')", tab.getIndex(), name, key);
                    continue;
                }

                String translatedName = I18n.format(key);
                String iconItem = ItemDump.getStackInfoBasic(stack);

                dump.addData(index, name, translatedName, iconItem);
            }
        }
    }

    @Override
    public void addCreativeTabNames(JsonObject obj, Item item)
    {
        CreativeTabs[] tabs = item.getCreativeTabs();
        String[] ctNames = new String[tabs.length];
        int i = 0;
        int count = 0;

        for (CreativeTabs tab : tabs)
        {
            if (tab != null)
            {
                ctNames[i++] = I18n.format(tab.getTranslationKey());
                count++;
            }
        }

        ctNames = Arrays.copyOf(ctNames, count);
        obj.add("CreativeTabs", new JsonPrimitive(String.join(",", ctNames)));
    }

    @Override
    public void addMusicTypeData(DataDump dump)
    {
        for (MusicType music : MusicType.values())
        {
            SoundEvent sound = music.getMusicLocation();
            String minDelay = String.valueOf(music.getMinDelay());
            String maxDelay = String.valueOf(music.getMaxDelay());
            ResourceLocation regName = SoundEvent.REGISTRY.getNameForObject(sound);

            dump.addData(music.name().toLowerCase(), regName != null ? regName.toString() : "<null>", minDelay, maxDelay);
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
    public boolean isClient()
    {
        return true;
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
