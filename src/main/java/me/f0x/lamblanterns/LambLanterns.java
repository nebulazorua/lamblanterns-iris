package me.f0x.lamblanterns;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(LambLanterns.MOD_ID)
public class LambLanterns {
    public static final String MOD_ID = "lamblanterns";
    public static final Logger LOGGER = LoggerFactory.getLogger("LambLanterns");

    public LambLanterns(IEventBus modBus, ModContainer container) {
        String version = container.getModInfo().getVersion().toString();
        boolean ldl = ModList.get().isLoaded("lambdynlights");

        LOGGER.info("Lamb Lanterns v{} loaded", version);
        LOGGER.info("  lantern slot attached to player; lanterns are tagged #curios:lantern");
        LOGGER.info("  LambDynamicLights: {}", ldl
                ? "detected - worn lanterns will emit dynamic light"
                : "not installed - lanterns wearable but won't light up");
    }
}
