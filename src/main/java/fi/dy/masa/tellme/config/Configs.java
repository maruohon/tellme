package fi.dy.masa.tellme.config;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.reference.Reference;

public class Configs
{
    public static File configurationFile;
    public static Configuration config;
    
    public static final String CATEGORY_GENERIC = "Generic";

    public static boolean enableDebugItemForBlockAndEntities;
    public static boolean enableDebugItemForItems;

    private static String debugItemNameBlocks;
    private static String debugItemNameItems;

    public static ItemStack debugItemBlocks;
    public static ItemStack debugItemItems;

    @SubscribeEvent
    public void onConfigChangedEvent(OnConfigChangedEvent event)
    {
        if (Reference.MOD_ID.equals(event.getModID()))
        {
            loadConfigs(config);
        }
    }

    public static void loadConfigsFromFile(File configFile)
    {
        configurationFile = configFile;
        config = new Configuration(configFile, null, true);
        config.load();

        loadConfigs(config);
    }

    public static void loadConfigs(Configuration conf)
    {
        Property prop;

        prop = conf.get(CATEGORY_GENERIC, "enableDebugItemForBlocksAndEntities", true).setRequiresMcRestart(false);
        prop.setComment("Enables the debug item to right click on blocks or entities");
        enableDebugItemForBlockAndEntities = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "enableDebugItemForItems", true).setRequiresMcRestart(false);
        prop.setComment("Enables the debug item to right with to dump data from the item next to the right of it on the hotbar");
        enableDebugItemForItems = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "debugItemNameBlocks", "minecraft:gold_nugget").setRequiresMcRestart(false);
        prop.setComment("The debug item to use for right clicking on blocks and entities. Examples: minecraft:gold_nugget or minecraft:coal@1 for Charcoal (metadata 1)");
        debugItemNameBlocks = prop.getString();
        debugItemBlocks = getDebugItem(debugItemNameBlocks);

        prop = conf.get(CATEGORY_GENERIC, "debugItemNameItems", "minecraft:blaze_rod").setRequiresMcRestart(false);
        prop.setComment("The debug item to use for right clicking with to dump item NBT");
        debugItemNameItems = prop.getString();
        debugItemItems = getDebugItem(debugItemNameItems);

        if (conf.hasChanged() == true)
        {
            conf.save();
        }
    }

    private static ItemStack getDebugItem(String nameIn)
    {
        String name = nameIn;
        int meta = 0;

        try
        {
            Pattern pattern = Pattern.compile("([a-zA-Z0-9_\\.:-]+)@([0-9]+)");
            Matcher matcher = pattern.matcher(nameIn);

        if (matcher.matches())
        {
            name = matcher.group(1);
            meta = Integer.parseInt(matcher.group(2));
        }
        }
        catch (PatternSyntaxException | NumberFormatException e)
        {
            TellMe.logger.warn("Failed to parse debug item name '{}'", nameIn, e);
        }

        Item item = Item.REGISTRY.getObject(new ResourceLocation(name));

        return item != null ? new ItemStack(item, 1, meta) : null;
    }
}
