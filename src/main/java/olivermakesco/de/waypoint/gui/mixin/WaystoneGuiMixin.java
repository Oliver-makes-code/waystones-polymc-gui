package olivermakesco.de.waypoint.gui.mixin;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import olivermakesco.de.waypoint.gui.WaystoneSGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import wraith.waystones.Waystones;
import wraith.waystones.block.WaystoneBlock;
import wraith.waystones.block.WaystoneBlockEntity;
import wraith.waystones.interfaces.PlayerEntityMixinAccess;
import wraith.waystones.registries.ItemRegistry;

import java.util.HashSet;
import java.util.OptionalInt;

@Mixin(WaystoneBlock.class)
public class WaystoneGuiMixin {
	@Redirect(at=@At(
			value = "INVOKE",
			target= "Lnet/minecraft/entity/player/PlayerEntity;openHandledScreen(Lnet/minecraft/screen/NamedScreenHandlerFactory;)Ljava/util/OptionalInt;"
	), method = "onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"
	)
	public OptionalInt redirectToChestGUI(PlayerEntity playerEntity, NamedScreenHandlerFactory factory) {
		if (playerEntity instanceof ServerPlayerEntity player)
			new WaystoneSGui(player);

		return OptionalInt.empty();
	}
}
