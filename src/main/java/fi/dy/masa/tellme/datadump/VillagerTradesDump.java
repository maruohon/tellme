package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import fi.dy.masa.tellme.util.datadump.DataDump;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class VillagerTradesDump
{
    public static List<String> getFormattedVillagerTradesDump(DataDump.Format format, @Nullable Entity trader)
    {
        DataDump dump = new DataDump(6, format);
        Random rand = new Random();

        ArrayList<VillagerProfession> professions = new ArrayList<>(VillagerTrades.TRADES.keySet());
        professions.sort(Comparator.comparing(v -> v.getRegistryName().toString()));

        for (VillagerProfession profession : professions)
        {
            String regName = profession.getRegistryName().toString();

            dump.addData(regName, profession.toString(), "", "", "", "");
            Int2ObjectMap<ItemListing[]> map = VillagerTrades.TRADES.get(profession);
            ArrayList<Integer> levels = new ArrayList<>(map.keySet());
            Collections.sort(levels);

            for (int level : levels)
            {
                ItemListing[] trades = map.get(level);

                if (trades == null)
                {
                    continue;
                }

                dump.addData("", "", String.valueOf(level), "", "", "");

                for (int i = 0; i < trades.length; ++i)
                {
                    try
                    {
                        ItemListing trade = trades[i];

                        // Exclude the map trades, they are super slow to fetch
                        if (trade.getClass().getName().equals("net.minecraft.entity.merchant.villager.VillagerTrades$EmeraldForMapTrade"))
                        {
                            dump.addData("", "", "skipping", "map trade", "lvl: " + level, "id: " + i);
                            continue;
                        }

                        MerchantOffer offer = trade.getOffer(trader, rand);

                        if (offer != null)
                        {
                            ItemStack buy1 = offer.getBaseCostA();
                            ItemStack buy2 = offer.getCostB();
                            ItemStack sell = offer.getResult();
                            String strBuy1 = buy1.isEmpty() == false ? buy1.getHoverName().getString() : "";
                            String strBuy2 = buy2.isEmpty() == false ? buy2.getHoverName().getString() : "";
                            String strSell = sell.isEmpty() == false ? sell.getHoverName().getString() : "";

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
