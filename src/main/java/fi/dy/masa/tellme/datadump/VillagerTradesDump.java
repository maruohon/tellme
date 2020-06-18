package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import fi.dy.masa.tellme.util.datadump.DataDump;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class VillagerTradesDump
{
    public static List<String> getFormattedVillagerTradesDump(DataDump.Format format, @Nullable Entity trader)
    {
        DataDump dump = new DataDump(6, format);
        Random rand = new Random();

        ArrayList<VillagerProfession> professions = new ArrayList<>(TradeOffers.PROFESSION_TO_LEVELED_TRADE.keySet());
        professions.sort(Comparator.comparing(v -> Registry.VILLAGER_PROFESSION.getId(v).toString()));

        for (VillagerProfession profession : professions)
        {
            String regName = Registry.VILLAGER_PROFESSION.getId(profession).toString();

            dump.addData(regName, profession.toString(), "", "", "", "");
            Int2ObjectMap<TradeOffers.Factory[]> map = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(profession);
            ArrayList<Integer> levels = new ArrayList<>(map.keySet());
            Collections.sort(levels);

            for (int level : levels)
            {
                TradeOffers.Factory[] trades = map.get(level);

                if (trades == null)
                {
                    continue;
                }

                dump.addData("", "", String.valueOf(level), "", "", "");

                for (int i = 0; i < trades.length; ++i)
                {
                    try
                    {
                        TradeOffers.Factory trade = trades[i];

                        // Exclude the map trades, they are super slow to fetch
                        String name = trade.getClass().getName();

                        if (name.equals("net.minecraft.village.TradeOffers$SellMapFactory") ||
                            name.equals("net.minecraft.class_3853$class_1654"))
                        {
                            dump.addData("", "", "skipping", "map trade", "lvl: " + level, "id: " + i);
                            continue;
                        }

                        TradeOffer offer = trade.create(trader, rand);

                        if (offer != null)
                        {
                            ItemStack buy1 = offer.getOriginalFirstBuyItem();
                            ItemStack buy2 = offer.getSecondBuyItem();
                            ItemStack sell = offer.getSellItem();
                            String strBuy1 = buy1.isEmpty() == false ? buy1.getName().getString() : "";
                            String strBuy2 = buy2.isEmpty() == false ? buy2.getName().getString() : "";
                            String strSell = sell.isEmpty() == false ? sell.getName().getString() : "";

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
