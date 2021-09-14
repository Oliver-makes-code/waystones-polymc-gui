package olivermakesco.de.waypoint.gui.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import olivermakesco.de.waypoint.gui.WaystoneSGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.waystones.item.PocketWormholeItem;

@Mixin(PocketWormholeItem.class)
public class PocketWormholeMixin {
    @Inject(at=@At("HEAD"),method="use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",cancellable=true)
    private void changeScreen(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (user instanceof ServerPlayerEntity player) new WaystoneSGui(player);
        cir.setReturnValue(TypedActionResult.success(user.getStackInHand(hand), world.isClient()));
    }
}
