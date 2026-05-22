package me.f0x.lamblanterns.mixins;

import me.f0x.lamblanterns.LambLanterns;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

@Mixin(targets = "net.irisshaders.iris.uniforms.IdMapUniforms$HeldItemSupplier")
public class IrisMixin {
    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack lamblanterns$fakeOffhandForLighting(LocalPlayer instance, InteractionHand hand){
        if(hand == InteractionHand.OFF_HAND) {
            var inv = CuriosApi.getCuriosInventory(instance).orElse(null);
            LambLanterns.LOGGER.info("fuck {}", inv);
            if (inv == null) return instance.getItemInHand(hand);
            ICurioStacksHandler handler = inv.getCurios().get("lantern");
            if (handler == null) return instance.getItemInHand(hand);

            var stacks = handler.getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                ItemStack stack = stacks.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                return stack;
            }
        }
        return instance.getItemInHand(hand);
    }

}
