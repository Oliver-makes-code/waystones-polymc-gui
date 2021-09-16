package olivermakesco.de.waypoint.gui.mixin;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import olivermakesco.de.waypoint.gui.WaystoneSGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import wraith.waystones.Waystones;
import wraith.waystones.block.WaystoneBlock;
import wraith.waystones.block.WaystoneBlockEntity;
import wraith.waystones.interfaces.PlayerEntityMixinAccess;
import wraith.waystones.item.LocalVoid;
import wraith.waystones.item.WaystoneScroll;
import wraith.waystones.registries.ItemRegistry;
import wraith.waystones.util.Config;

import java.util.HashSet;
import java.util.OptionalInt;

@Mixin(WaystoneBlock.class)
public abstract class WaystoneGuiMixin {
	/**
	 * @author null
	 */
	@Overwrite()
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient) {
			Item heldItem = player.getMainHandStack().getItem();
			BlockPos openPos = pos;
			if (state.get(WaystoneBlock.HALF) == DoubleBlockHalf.UPPER) {
				openPos = pos.down();
			}

			BlockState topState = world.getBlockState(openPos.up());
			BlockState bottomState = world.getBlockState(openPos);
			if (heldItem == Items.VINE) {
				if (!(Boolean)topState.get(WaystoneBlock.MOSSY)) {
					world.setBlockState(openPos.up(), topState.with(WaystoneBlock.MOSSY, true));
					world.setBlockState(openPos, bottomState.with(WaystoneBlock.MOSSY, true));
					player.getMainHandStack().decrement(1);
				}

				return ActionResult.PASS;
			}

			if (heldItem == Items.SHEARS) {
				if (topState.get(WaystoneBlock.MOSSY)) {
					world.setBlockState(openPos.up(), topState.with(WaystoneBlock.MOSSY, false));
					world.setBlockState(openPos, bottomState.with(WaystoneBlock.MOSSY, false));
					openPos = openPos.up(2);
					ItemScatterer.spawn(world, openPos.getX(), openPos.getY(), openPos.getZ(), new ItemStack(Items.VINE));
				}

				return ActionResult.PASS;
			}

			if (heldItem instanceof WaystoneScroll || heldItem instanceof LocalVoid) {
				return ActionResult.PASS;
			}

			HashSet<String> discovered = ((PlayerEntityMixinAccess)player).getDiscoveredWaystones();
			WaystoneBlockEntity blockEntity = (WaystoneBlockEntity)world.getBlockEntity(openPos);
			if (blockEntity == null) {
				return ActionResult.FAIL;
			}

			if (player.isSneaking() && player.hasPermissionLevel(2) || Config.getInstance().canOwnersRedeemPayments() && blockEntity.getOwner().equals(player.getUuid())) {
				if (blockEntity.hasStorage()) {
					ItemScatterer.spawn(world, openPos.up(2), blockEntity.getInventory());
					blockEntity.setInventory(DefaultedList.ofSize(0, ItemStack.EMPTY));
				}
			} else {
				if (blockEntity.getOwner() == null) {
					blockEntity.setOwner(player);
				}

				if (!Waystones.WAYSTONE_STORAGE.containsHash(blockEntity.getHash())) {
					Waystones.WAYSTONE_STORAGE.addWaystone(blockEntity);
				}

				if (!discovered.contains(blockEntity.getHash())) {
					if (!blockEntity.isGlobal()) {
						player.sendMessage((new LiteralText(blockEntity.getWaystoneName() + " ")).append(new TranslatableText("waystones.discover_waystone")).formatted(Formatting.AQUA), false);
					}

					((PlayerEntityMixinAccess)player).discoverWaystone(blockEntity);
				}

				new WaystoneSGui((ServerPlayerEntity) player, blockEntity);
			}

			blockEntity.markDirty();
		}

		return ActionResult.success(world.isClient());
	}
}
