package me.f0x.lamblanterns.client;

import me.f0x.lamblanterns.LambLanterns;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@EventBusSubscriber(modid = LambLanterns.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class LambLanternsClient {
    private LambLanternsClient() {}

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CuriosRendererRegistry.register(Items.LANTERN,      () -> new LanternCurioRenderer(Blocks.LANTERN));
            CuriosRendererRegistry.register(Items.SOUL_LANTERN, () -> new LanternCurioRenderer(Blocks.SOUL_LANTERN));
        });
    }
}
