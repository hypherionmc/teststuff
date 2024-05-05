package com.hypherionmc.craterlib;

import com.hypherionmc.craterlib.api.events.client.LateInitEvent;
import com.hypherionmc.craterlib.client.gui.config.CraterConfigScreen;
import com.hypherionmc.craterlib.common.NeoForgeServerEvents;
import com.hypherionmc.craterlib.compat.Vanish;
import com.hypherionmc.craterlib.core.config.ConfigController;
import com.hypherionmc.craterlib.core.config.ModuleConfig;
import com.hypherionmc.craterlib.core.config.annotations.NoConfigScreen;
import com.hypherionmc.craterlib.core.event.CraterEventBus;
import com.hypherionmc.craterlib.core.networking.CraterPacketNetwork;
import com.hypherionmc.craterlib.core.networking.PacketRegistry;
import com.hypherionmc.craterlib.core.networking.data.PacketSide;
import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.craterlib.network.CraterNeoForgeNetworkHandler;
import com.hypherionmc.craterlib.nojang.client.BridgedMinecraft;
import com.hypherionmc.craterlib.nojang.client.BridgedOptions;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Optional;

@Mod(CraterConstants.MOD_ID)
public class CraterLib {

    private final PacketRegistry handler;

    public CraterLib(IEventBus eventBus) {
        NeoForge.EVENT_BUS.register(new NeoForgeServerEvents());
        eventBus.addListener(this::commonSetup);
        handler = new CraterNeoForgeNetworkHandler(FMLLoader.getDist().isClient() ? PacketSide.CLIENT : PacketSide.SERVER);

        if (ModloaderEnvironment.INSTANCE.isModLoaded("vmod")) {
            eventBus.register(new Vanish());
        }
    }

    public void commonSetup(FMLCommonSetupEvent evt) {
        new CraterPacketNetwork(handler);
        if (FMLEnvironment.dist.isClient()) {
            LateInitEvent event = new LateInitEvent(new BridgedMinecraft(), BridgedOptions.of(Minecraft.getInstance().options));
            CraterEventBus.INSTANCE.postEvent(event);

            ConfigController.getMonitoredConfigs().forEach((conf, watcher) -> {
                if (!conf.getClass().isAnnotationPresent(NoConfigScreen.class)) {
                    ModuleConfig config = (ModuleConfig) conf;
                    ModList.get().getModContainerById(config.getModId()).ifPresent(c -> c.registerExtensionPoint(IConfigScreenFactory.class, ((minecraft, screen) -> new CraterConfigScreen(config, screen))));
                }
            });
        }
    }
}
