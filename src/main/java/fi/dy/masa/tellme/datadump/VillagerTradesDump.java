package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.entity.merchant.villager.VillagerTrades.ITrade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import fi.dy.masa.tellme.util.datadump.DataDump;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class VillagerTradesDump
{
    public static List<String> getFormattedVillagerTradesDump(DataDump.Format format, @Nullable Entity trader)
    {
        DataDump dump = new DataDump(6, format);
        Random rand = new Random();

        ArrayList<VillagerProfession> professions = new ArrayList<>(VillagerTrades.VILLAGER_DEFAULT_TRADES.keySet());
        professions.sort(Comparator.comparing(v -> v.getRegistryName().toString()));

        for (VillagerProfession profession : professions)
        {
            String regName = profession.getRegistryName().toString();

            dump.addData(regName, profession.toString(), "", "", "", "");
            Int2ObjectMap<ITrade[]> map = VillagerTrades.VILLAGER_DEFAULT_TRADES.get(profession);
            ArrayList<Integer> levels = new ArrayList<>(map.keySet());
            Collections.sort(levels);

            for (int level : levels)
            {
                ITrade[] trades = map.get(level);

                if (trades == null)
                {
                    continue;
                }

                dump.addData("", "", String.valueOf(level), "", "", "");

                for (int i = 0; i < trades.length; ++i)
                {
                    try
                    {
                        ITrade trade = trades[i];

                        // Exclude the map trades, they are super slow to fetch
                        if (trade.getClass().getName().equals("net.minecraft.entity.merchant.villager.VillagerTrades$EmeraldForMapTrade"))
                        {
                            dump.addData("", "", "skipping", "map trade", "lvl: " + level, "id: " + i);
                            continue;
                        }

                        MerchantOffer offer = trade.getOffer(trader, rand);

                        if (offer != null)
                        {
                            ItemStack buy1 = offer.getBuyingStackFirst();
                            ItemStack buy2 = offer.getBuyingStackSecond();
                            ItemStack sell = offer.getSellingStack();
                            String strBuy1 = buy1.isEmpty() == false ? buy1.getDisplayName().getString() : "";
                            String strBuy2 = buy2.isEmpty() == false ? buy2.getDisplayName().getString() : "";
                            String strSell = sell.isEmpty() == false ? sell.getDisplayName().getString() : "";

                            dump.addData("", "", "", strBuy1, strBuy2, strSell);
                        }
                    }
                    catch (Exception e)
                    {
                        dump.addData("", "", "EXCEPTION", "@ lvl " + level, "id: " + i, "");
                    }
                }
            }
        }

        dump.addTitle("Registry name", "Profession", "Level", "Buy 1", "Buy 2", "Sell");
        dump.setSort(false);

        return dump.getLines();
    }
}
