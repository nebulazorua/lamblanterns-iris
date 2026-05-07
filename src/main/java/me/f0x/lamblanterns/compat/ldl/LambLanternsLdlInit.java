package me.f0x.lamblanterns.compat.ldl;

import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public class LambLanternsLdlInit implements DynamicLightsInitializer {
    private static final ResourceLocation TYPE_ID =
            ResourceLocation.fromNamespaceAndPath("lamblanterns", "curios_lantern");
    private static final CuriosBeltLuminance INSTANCE = new CuriosBeltLuminance();
    private static final EntityLuminance.Type TYPE =
            EntityLuminance.Type.registerSimple(TYPE_ID, INSTANCE);

    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {
        context.entityLightSourceManager().onRegisterEvent().register(
                ctx -> ctx.register(EntityType.PLAYER, INSTANCE)
        );
    }

    @Override
    public void onInitializeDynamicLights(ItemLightSourceManager itemLightSourceManager) {
        // No item light sources to register.
    }

    private static final class CuriosBeltLuminance implements EntityLuminance {
        @Override
        public Type type() {
            return TYPE;
        }

        @Override
        public int getLuminance(ItemLightSourceManager manager, Entity entity) {
            if (!(entity instanceof LivingEntity living)) return 0;
            var inv = CuriosApi.getCuriosInventory(living).orElse(null);
            if (inv == null) return 0;
            ICurioStacksHandler handler = inv.getCurios().get("lantern");
            if (handler == null) return 0;

            boolean wet = entity.isUnderWater();
            int max = 0;
            var stacks = handler.getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                ItemStack stack = stacks.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                int lum = manager.getLuminance(stack, wet);
                if (lum > max) max = lum;
            }
            return max;
        }
    }
}
