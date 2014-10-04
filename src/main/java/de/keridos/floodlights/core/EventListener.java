package de.keridos.floodlights.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import de.keridos.floodlights.util.DiskIO;
import net.minecraftforge.event.world.WorldEvent;

/**
 * Created by Nico on 28.02.14.
 */
public class EventListener {
    private static EventListener instance = null;

    private EventListener() {
    }

    public static EventListener getInstance() {
        if (instance == null) {
            instance = new EventListener();
        }
        return instance;
    }


    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event)
    {
        DiskIO.saveToDisk(LightHandler.getInstance());
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        LightHandler.getInstance();
    }

}