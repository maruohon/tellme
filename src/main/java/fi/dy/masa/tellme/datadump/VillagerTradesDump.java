package fi.dy.masa.tellme.datadump;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.entity.passive.EntityVillager.ITradeList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

public class VillagerTradesDump
{
    public static List<String> getFormattedVillagerTradesDump(DataDump.Format format)
    {
        DataDump villagerProfessionDump = new DataDump(5, format);
        Iterator<Map.Entry<ResourceLocation, VillagerProfession>> iter = ForgeRegistries.VILLAGER_PROFESSIONS.getEntries().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, VillagerProfession> entry = iter.next();
            List<VillagerCareer> careers = VillagerProfessionDump.getCareers(entry.getValue());

            if (careers != null && entry.getKey() != null)
            {
                String regName = entry.getKey().toString();

                for (VillagerCareer career : careers)
                {
                    Random rand = new Random();
                    MerchantRecipeList list = new MerchantRecipeList();

                    for (int i = 0; i < 64; ++i)
                    {
                        List<ITradeList> trades = career.getTrades(i);

                        if (trades == null)
                        {
                            break;
                        }

                        for (ITradeList trade : trades)
                        {
                            try
                            {
                                trade.addMerchantRecipe(null, list, rand);
                            }
                            catch (Exception e)
                            {
                                villagerProfessionDump.addData("", "", "?", "?", "[Exception. Treasure map?]");
                            }
                        }
                    }

                    villagerProfessionDump.addData(regName, career.getName(), "", "", "");

                    for (MerchantRecipe recipe : list)
                    {
                        ItemStack buy1 = recipe.getItemToBuy();
                        ItemStack buy2 = recipe.getSecondItemToBuy();
                        ItemStack sell = recipe.getItemToSell();
                        String strBuy1 = buy1.isEmpty() == false ? buy1.getDisplayName() : "";
                        String strBuy2 = buy2.isEmpty() == false ? buy2.getDisplayName() : "";
                        String strSell = sell.isEmpty() == false ? sell.getDisplayName() : "";
                        villagerProfessionDump.addData("", "", strBuy1, strBuy2, strSell);
                    }
                }
            }
        }

        villagerProfessionDump.addTitle("Registry name", "Career", "Buy 1", "Buy 2", "Sell");
        villagerProfessionDump.setUseColumnSeparator(true);
        villagerProfessionDump.setSort(false);

        return villagerProfessionDump.getLines();
    }
}
