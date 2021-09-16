package olivermakesco.de.waypoint.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.BlockPos;
import wraith.waystones.Waystones;
import wraith.waystones.block.WaystoneBlock;
import wraith.waystones.block.WaystoneBlockEntity;
import wraith.waystones.interfaces.PlayerEntityMixinAccess;
import wraith.waystones.item.AbyssWatcherItem;
import wraith.waystones.item.WaystoneItem;
import wraith.waystones.registries.ItemRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WaystoneSGui extends SimpleGui {
    public WaystoneBlockEntity[] waystoneArr;
    public WaystoneBlockEntity[] globalWaystones;
    public WaystoneBlockEntity[] localWaystones;
    public int maxScreens;
    public boolean isGlobal = false;
    public Text ownerName;
    public UUID ownerUuid;
    public String playerName;
    public UUID playerUuid;
    public WaystoneBlockEntity blockEntity;

    public WaystoneSGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X4, player, false);
        ownerName = Text.of("Owner: " + player.getName().getString());
        playerName = player.getName().getString();
        ownerUuid = player.getUuid();
        playerUuid = ownerUuid;
        this.setTitle(Text.of("Waystones"));
        this.fillArr();
        this.setLocal();
        this.open();
    }
    public WaystoneSGui(ServerPlayerEntity player, WaystoneBlockEntity entity) {
        super(ScreenHandlerType.GENERIC_9X4, player, false);
        blockEntity = entity;
        ownerName = Text.of("Owner: " + entity.getOwnerName());
        playerName = entity.getOwnerName();
        ownerUuid = entity.getOwner();
        playerUuid = player.getUuid();
        updateTitle();
        this.fillArr();
        this.setLocal();
        this.open();
    }

    public void playClickSound() {
        player.playSound(SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 2.0f, 1.0f);
    }

    public void teleport(WaystoneBlockEntity entity, boolean abyss) {
        close();
        var exp = player.experienceLevel;
        if (--exp < 0 && !player.isCreative()) {
            Text text = Text.of("Not enough levels.").shallowCopy().setStyle(Style.EMPTY.withColor(0xff0000));
            player.sendMessage(text,false);
            return;
        }
        player.setExperienceLevel(exp);
        entity.teleportPlayer(player, abyss);
        player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 2.0f, 1.0f);
    }

    public void fillArr() {
        var waystones = ((PlayerEntityMixinAccess)player).getDiscoveredWaystones().toArray(String[]::new);
        List<WaystoneBlockEntity> waystoneList = new ArrayList<>();

        for (String value : waystones) {
            WaystoneBlockEntity waystone = Waystones.WAYSTONE_STORAGE.getWaystone(value);
            if (waystone != null) waystoneList.add(waystone);
        }
        globalWaystones = waystoneList.toArray(WaystoneBlockEntity[]::new);

        waystoneList = new ArrayList<>();
        for (WaystoneBlockEntity e : globalWaystones) {
            if (e.getOwner().equals(playerUuid))
                waystoneList.add(e);
        }
        localWaystones = waystoneList.toArray(WaystoneBlockEntity[]::new);
    }

    public void setGlobal() {
        waystoneArr = globalWaystones;
        maxScreens = (int) Math.ceil((globalWaystones.length - 1)/18);
        isGlobal = true;
        setScreen(0);
    }
    public void setLocal() {
        waystoneArr = localWaystones;
        maxScreens = (int) Math.ceil((localWaystones.length - 1)/18);
        isGlobal = false;
        setScreen(0);
    }

    public void setScreen(int idx) {
        if (idx > maxScreens) idx = maxScreens;
        if (idx < 0) idx = 0;

        //Clear slots
        for (int i = 0; i < 36; i++)
            this.clearSlot(i);

        //Fill screen
        int offset = idx * 18;

        for (int i = offset; i < offset + 18 && i < waystoneArr.length; i++) {
            WaystoneBlockEntity waystone = waystoneArr[i];
            BlockPos waypos = waystone.getPos();
            String pos = waypos.getX() + ", " + waypos.getY() + ", " + waypos.getZ();
            ItemStack stack = new ItemStack(waystone.getWorld().getBlockState(waystone.getPos()).getBlock()).setCustomName(Text.of(waystone.getWaystoneName() + " (" + pos + ")"));
            boolean watcher = player.getStackInHand(player.getActiveHand()).getItem() instanceof AbyssWatcherItem;
            this.addSlot(stack, (id,type,act,gui) -> {
                teleport(waystone, watcher);
            });
        }

        //Add navigation
        int screen = idx;
        if (maxScreens > idx) {
            this.setSlot(35, Items.DIAMOND.getDefaultStack().setCustomName(Text.of("Next")), (id, type, act, gui) -> {
                setScreen(screen + 1);
                playClickSound();
            });
        } else this.setSlot(35, Items.BARRIER.getDefaultStack().setCustomName(Text.of("Next")));

        this.setSlot(29, Items.PLAYER_HEAD.getDefaultStack().setCustomName(ownerName));

        this.setSlot(28, Items.NAME_TAG.getDefaultStack().setCustomName(Text.of("Rename")), (id, type, act, gui) -> {
            enterRename(screen);
            playClickSound();
        });
        this.setSlot(34, Items.SKELETON_SKULL.getDefaultStack().setCustomName(Text.of("Abandon Waystone")), (id, type, act, gui) -> {
            if (blockEntity == null) return;
            Text text = Text.of("Abandoned waystone " + blockEntity.getWaystoneName()).shallowCopy().setStyle(Style.EMPTY.withColor(0x0088ff));
            player.sendMessage(text,false);
            close();
            playClickSound();
            ((PlayerEntityMixinAccess)player).forgetWaystone(blockEntity.getHash());
            if (!playerUuid.equals(ownerUuid)) return;
            blockEntity.setOwner(null);
        });

        String experienceString = player.experienceLevel + " Levels";
        this.setSlot(30, Items.EXPERIENCE_BOTTLE.getDefaultStack().setCustomName(Text.of(experienceString)));

        String totalStones = "Total waystones in list: " + waystoneArr.length;
        this.setSlot(33, ItemRegistry.ITEMS.get("waystone").getDefaultStack().setCustomName(Text.of(totalStones)));

        String globalString = isGlobal? " (on)": " (off)";
        this.setSlot(32, ItemRegistry.ITEMS.get("local_void").getDefaultStack().setCustomName(Text.of("View Global Waystones"+globalString)), (id, type, act, gui) -> {
            if (isGlobal) setLocal();
            else setGlobal();
            playClickSound();
        });

        this.setSlot(31, Items.STRUCTURE_VOID.getDefaultStack().setCustomName(Text.of("Close menu")), (id, type, act, gui) -> {
            this.close();
            playClickSound();
        });

        if (idx == 0) this.setSlot(27,Items.BARRIER.getDefaultStack().setCustomName(Text.of("Previous")));
        else {
            this.setSlot(27, Items.DIAMOND.getDefaultStack().setCustomName(Text.of("Previous")), (id, type, act, gui) -> {
                setScreen(screen - 1);
                playClickSound();
            });
        }
    }

    public void enterRename(int idx) {
        if (idx > maxScreens) idx = maxScreens;
        if (idx < 0) idx = 0;

        //Clear slots
        for (int i = 0; i < 36; i++)
            this.clearSlot(i);

        int offset = idx * 18;
        int screen = idx;

        for (int i = offset; i < offset + 18 && i < waystoneArr.length; i++) {
            WaystoneBlockEntity waystone = waystoneArr[i];
            ItemStack stack = new ItemStack(waystone.getWorld().getBlockState(waystone.getPos()).getBlock()).setCustomName(Text.of(waystone.getWaystoneName()));
            this.addSlot(stack, (id,type,act,gui) -> {
                new RenameGui(player, screen, (WaystoneSGui) gui, waystone);
                playClickSound();
            });
        }

        //Add navigation
        if (maxScreens > idx) {
            this.setSlot(35, Items.DIAMOND.getDefaultStack().setCustomName(Text.of("Next Page")), (id, type, act, gui) -> {
                enterRename(screen + 1);
                playClickSound();
            });
        } else this.setSlot(35, Items.BARRIER.getDefaultStack().setCustomName(Text.of("Next Page")));
        this.setSlot(31, Items.STRUCTURE_VOID.getDefaultStack().setCustomName(Text.of("Cancel rename")),  (id, type, act, gui) -> {
            setScreen(screen);
            playClickSound();
        });
        if (idx == 0) this.setSlot(27,Items.BARRIER.getDefaultStack().setCustomName(Text.of("Prev Page")));
        else {
            this.setSlot(27, Items.DIAMOND.getDefaultStack().setCustomName(Text.of("Next Page")), (id, type, act, gui) -> {
                setLocal();
                enterRename(screen - 1);
                playClickSound();
            });
        }
    }

    public void updateTitle() {
        if (blockEntity == null) return;
        Text text = Text.of("Waystone: " + blockEntity.getWaystoneName());
        setTitle(text);
    }
}
