package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.entity.merchant.villager.VillagerTrades.ITrade;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class VillagerTradesDump
{
    public static List<String> getFormattedVillagerTradesDump(DataDump.Format format)
    {
        DataDump dump = new DataDump(6, format);
        Random rand = new Random();
        PlayerEntity player = null;

        for (Map.Entry<VillagerProfession, Int2ObjectMap<ITrade[]>> entry : VillagerTrades.VILLAGER_DEFAULT_TRADES.entrySet())
        {
            VillagerProfession profession = entry.getKey();
            String regName = profession.getRegistryName().toString();

            dump.addData(regName, profession.toString(), "", "", "", "");

            for (int level = 0; level < 64; ++level)
            {
                ITrade[] trades = entry.getValue().get(level);

                if (trades == null)
                {
                    break;
                }

                dump.addData("", "", String.valueOf(level), "", "", "");

                for (int i = 0; i < trades.length; ++i)
                {
                    MerchantOffer offer = trades[i].getOffer(player, rand);

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
            }
        }

        dump.addTitle("Registry name", "Profession", "Level", "Buy 1", "Buy 2", "Sell");
        dump.setSort(false);

        return dump.getLines();
    }
}
