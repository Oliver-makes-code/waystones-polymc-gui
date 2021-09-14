package olivermakesco.de.waypoint.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import wraith.waystones.Waystones;
import wraith.waystones.block.WaystoneBlockEntity;
import wraith.waystones.interfaces.PlayerEntityMixinAccess;
import wraith.waystones.item.AbyssWatcherItem;

public class WaystoneSGui extends SimpleGui {
    public WaystoneBlockEntity[] waystoneArr;
    public int maxScreens;

    public WaystoneSGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X4, player, false);
        this.setTitle(Text.of("Waystones"));
        this.fillArr();
        this.setScreen(0);
        this.open();
    }

    public void fillArr() {
        var waystones = ((PlayerEntityMixinAccess)player).getDiscoveredWaystones().toArray(String[]::new);
        waystoneArr = new WaystoneBlockEntity[waystones.length];

        for (int i = 0; i < waystones.length; i++) {
            WaystoneBlockEntity waystone = Waystones.WAYSTONE_STORAGE.getWaystone(waystones[i]);
            waystoneArr[i] = waystone;
        }
        maxScreens = (int) Math.ceil((waystones.length - 1)/18);
    }

    public void setScreen(int idx) {
        if (idx > maxScreens) idx = maxScreens;
        if (idx < 0) idx = 0;

        //Clear slots
        for (int i = 0; i < 36; i++)
            this.clearSlot(i);

        int offset = idx * 18;

        for (int i = offset; i < offset + 18 && i < waystoneArr.length; i++) {
            WaystoneBlockEntity waystone = waystoneArr[i];
            ItemStack stack = new ItemStack(waystone.getWorld().getBlockState(waystone.getPos()).getBlock()).setCustomName(Text.of(waystone.getWaystoneName()));
            boolean watcher = player.getStackInHand(player.getActiveHand()).getItem() instanceof AbyssWatcherItem;
            this.addSlot(stack, (id,type,act,gui) -> {
                waystone.teleportPlayer(player, watcher);
                gui.close();
            });
        }

        //Add navigation
        int screen = idx;
        if (maxScreens > idx) {
            this.setSlot(35, Items.DIAMOND.getDefaultStack().setCustomName(Text.of("Next Page")), (id, type, act, gui) -> {
                setScreen(screen + 1);
            });
        } else this.setSlot(35, Items.BARRIER.getDefaultStack().setCustomName(Text.of("Next Page")));
        if (idx == 0) this.setSlot(27,Items.BARRIER.getDefaultStack().setCustomName(Text.of("Prev Page")));
        else {
            this.setSlot(27, Items.DIAMOND.getDefaultStack().setCustomName(Text.of("Next Page")), (id, type, act, gui) -> {
                setScreen(screen - 1);
            });
        }
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {

        return true;
    }
}
