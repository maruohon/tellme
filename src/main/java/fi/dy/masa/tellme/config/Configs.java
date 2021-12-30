package fi.dy.masa.tellme.config;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.reference.Reference;

public class Configs
{
    public static File dumpOutputDir = new File("config/tellme/");
    public static File configurationFile;

    public static final String CATEGORY_GENERIC = "Generic";

    public static ItemStack debugItemBlocks = new ItemStack(Items.GOLD_NUGGET);
    public static ItemStack debugItemItems = new ItemStack(Items.BLAZE_ROD);

    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec COMMON_CONFIG;

    private static File configFileGlobal;
    private static Path lastLoadedConfig;

    public static class Generic
    {
        public static boolean enableDebugItemForBlocksAndEntities;
        public static boolean enableDebugItemForItems;

        private static String debugItemNameBlocks;
        private static String debugItemNameItems;
    }

    static
    {
        setupConfigs();
    }

    private static void setupConfigs()
    {
        addCategoryGeneric();

        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    private static void addCategoryGeneric()
    {
        COMMON_BUILDER.push(CATEGORY_GENERIC);

        COMMON_BUILDER.comment(" Enables the debug item to right click on blocks or entities to get data about them")
                      .define("enableDebugItemForBlocksAndEntities", true);

        COMMON_BUILDER.comment(" Enables the debug item to right click with to dump data\n" +
                               " from the item next to the right of it on the hotbar")
                      .define("enableDebugItemForItems", true);

        COMMON_BUILDER.comment(" The debug item to right click on blocks and entities with.")
                      .define("debugItemNameBlocks", "minecraft:gold_nugget");

        COMMON_BUILDER.comment(" The debug item to right click with to dump item NBT from the item to the right of it on the hotbar")
                      .define("debugItemNameItems", "minecraft:blaze_rod");

        COMMON_BUILDER.pop();
    }

    private static void setConfigValues(ForgeConfigSpec spec)
    {
        setValuesInClass(Generic.class, spec);

        debugItemBlocks = getDebugItem(Generic.debugItemNameBlocks);
        debugItemItems = getDebugItem(Generic.debugItemNameItems);
    }

    private static void setValuesInClass(Class<?> clazz, ForgeConfigSpec spec)
    {
        for (Field field : clazz.getDeclaredFields())
        {
            String category = clazz.getSimpleName();
            String name = field.getName();

            try
            {
                Class<?> type = field.getType();
                field.setAccessible(true);

                if (type == boolean.class)
                {
                    field.set(null, spec.getValues().<ForgeConfigSpec.BooleanValue>get(category + "." + name).get().booleanValue());
                }
                else if (type == double.class)
                {
                    field.set(null, spec.getValues().<ForgeConfigSpec.DoubleValue>get(category + "." + name).get().doubleValue());
                }
                else if (type == String.class)
                {
                    field.set(null, spec.getValues().<ForgeConfigSpec.ConfigValue<String>>get(category + "." + name).get());
                }
                else if (List.class.isAssignableFrom(type))
                {
                    field.set(null, spec.getValues().<ForgeConfigSpec.ConfigValue<List<String>>>get(category + "." + name).get());
                }
            }
            catch (Exception e)
            {
                TellMe.logger.error("Failed to set config value for config '{}.{}'", category, name, e);
            }
        }
    }

    @SubscribeEvent
    public static void onConfigLoad(final ModConfigEvent.Loading event)
    {
        //System.out.printf("*** ModConfig.Loading\n");
        setConfigValues(COMMON_CONFIG);
    }

    @SubscribeEvent
    public static void onConfigReload(final ModConfigEvent.Reloading event)
    {
        //System.out.printf("*** ModConfig.ConfigReloading\n");
        setConfigValues(COMMON_CONFIG);
    }

    public static void loadConfig(Path path)
    {
        TellMe.logger.info("Reloading the configs from file '{}'", path.toAbsolutePath().toString());

        ForgeConfigSpec spec = COMMON_CONFIG;
        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);

        setConfigValues(spec);

        lastLoadedConfig = path;
    }

    public static void setGlobalConfigDirAndLoadConfigs(File configDirCommon)
    {
        dumpOutputDir = new File(configDirCommon, Reference.MOD_ID);
        File configFile = new File(configDirCommon, Reference.MOD_ID + ".toml");
        configFileGlobal = configFile;

        loadConfigsFromGlobalConfigFile();
    }

    public static void loadConfigsFromGlobalConfigFile()
    {
        loadConfig(configFileGlobal.toPath());
    }

    public static boolean reloadConfig()
    {
        if (lastLoadedConfig != null)
        {
            loadConfig(lastLoadedConfig);
            return true;
        }

        return false;
    }

    private static ItemStack getDebugItem(String nameIn)
    {
        try
        {
            ResourceLocation id = new ResourceLocation(nameIn);
            Item item = ForgeRegistries.ITEMS.getValue(id);

            if (item != null && item != Items.AIR)
            {
                return new ItemStack(item);
            }
        }
        catch (Exception e)
        {
            TellMe.logger.warn("Failed to parse debug item name '{}'", nameIn, e);
        }

        return ItemStack.EMPTY;
    }
}
