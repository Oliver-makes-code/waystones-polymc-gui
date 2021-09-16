package olivermakesco.de.waypoint.gui;

import eu.pb4.sgui.api.gui.AnvilInputGui;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import wraith.waystones.block.WaystoneBlockEntity;

public class RenameGui extends AnvilInputGui {
    public WaystoneSGui gui;
    public WaystoneBlockEntity entity;
    public int idx;

    public RenameGui(ServerPlayerEntity player, int idx, WaystoneSGui gui, WaystoneBlockEntity entity) {
        super(player, false);
        this.gui = gui;
        this.entity = entity;
        this.idx = idx;
        this.setDefaultInputValue(entity.getWaystoneName());
        onInput(entity.getWaystoneName());
        gui.close();
        open();
    }

    @Override
    public void onInput(String input) {
        setSlot(2, Items.PAPER.getDefaultStack().setCustomName(Text.of(input)), (id,type,act,gui) -> {
            ret(input);
        });
    }

    public void ret(String newName) {
        player.playSound(SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
        entity.setName(newName);
        gui.open();
        gui.updateTitle();
        gui.setScreen(idx);
    }
}
