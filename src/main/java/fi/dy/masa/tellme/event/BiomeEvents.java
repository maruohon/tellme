package fi.dy.masa.tellme.event;

import net.minecraftforge.event.terraingen.WorldTypeEvent.BiomeSize;
import net.minecraftforge.event.terraingen.WorldTypeEvent.InitBiomeGens;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.tellme.TellMe;

public class BiomeEvents
{
    @SubscribeEvent
    public void onInitBiomeGens(InitBiomeGens event)
    {
        TellMe.logger.info("InitBiomeGens: seed: " + event.getSeed());

        if (event.getWorldType() != null)
        {
            TellMe.logger.info("InitBiomeGens: worldType: " + event.getWorldType().toString());
            TellMe.logger.info(String.format("worldTypeID: %d; worldTypeName: %s", event.getWorldType().getWorldTypeID(), event.getWorldType().getWorldTypeName()));
        }
        else
        {
            TellMe.logger.info("InitBiomeGens: worldType: null");
        }

        if (event.getOriginalBiomeGens() != null)
        {
            TellMe.logger.info("InitBiomeGens: event.originalBiomeGens.length: " + event.getOriginalBiomeGens().length);
        }
        else
        {
            TellMe.logger.info("InitBiomeGens: event.originalBiomeGens: null");
        }
    }

    @SubscribeEvent
    public void onBiomeSize(BiomeSize event)
    {
        TellMe.logger.info("BiomeSize: size: " + event.getOriginalSize());
    }
}
