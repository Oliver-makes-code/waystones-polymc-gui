package olivermakesco.de.waypoint.gui.polymc;

import io.github.theepicblock.polymc.api.PolyMcEntrypoint;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.block.BlockStateManager;
import io.github.theepicblock.polymc.api.block.BlockStateProfile;
import io.github.theepicblock.polymc.impl.poly.block.PropertyRetainingReplacementPoly;
import io.github.theepicblock.polymc.impl.poly.block.SimpleReplacementPoly;
import io.github.theepicblock.polymc.impl.poly.block.SingleUnusedBlockStatePoly;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoubleBlockProperties;
import wraith.waystones.registries.BlockRegistry;

public class RegisterPoly implements PolyMcEntrypoint {
    @Override
    public void registerPolys(PolyRegistry registry) {
        try {
            registry.registerBlockPoly(BlockRegistry.DESERT_WAYSTONE, new SingleUnusedBlockStatePoly(registry, BlockStateProfile.NOTE_BLOCK_PROFILE));
            registry.registerBlockPoly(BlockRegistry.STONE_BRICK_WAYSTONE, new SingleUnusedBlockStatePoly(registry, BlockStateProfile.NOTE_BLOCK_PROFILE));
            registry.registerBlockPoly(BlockRegistry.WAYSTONE, new SingleUnusedBlockStatePoly(registry, BlockStateProfile.NOTE_BLOCK_PROFILE));
        } catch (Exception ignored) {}
    }
}
