package dev.huskcasaca.effortless.buildmode.oneclick;

import dev.huskcasaca.effortless.building.BuildOp;
import dev.huskcasaca.effortless.buildmode.OneClickBuildable;
import dev.huskcasaca.effortless.utils.InventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Single extends OneClickBuildable {
    @Override
    public void initialize(Player player) { }

    public BuildOp operationOnUse(Player player) {
        return InventoryHelper.holdingBucket(player, true) ? BuildOp.DRENCH : BuildOp.PLACE;
    }
    public BuildOp operationOnAttack(Player player) {return BuildOp.BREAK; }

    @Override
    public boolean isInProgress(Player player) { return false; }

    @Override
    public boolean onUse(Player player, BlockPos blockPos, boolean skipRaytrace, BuildOp operation) {
        return !findCoordinates(player, blockPos, false).isEmpty();
    }

    @Override
    public List<BlockPos> findCoordinates(Player player, BlockPos blockPos, boolean skipRaytrace) {
        var lookVec = player.getLookAngle();
        // If looking straight down, unconditionally return block beneath player's feet.
        // Thus, construction can be started in thin air.
        if (Math.abs(lookVec.x) < 0.0001 && Math.abs(lookVec.z) < 0.0001 && lookVec.y < -0.9999) {
            blockPos = player.blockPosition().below();
        }
        if (blockPos == null) return Collections.emptyList();
        return getFinalBlocks(player, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public List<BlockPos> getFinalBlocks(Player player, int x1, int y1, int z1) {
        List<BlockPos> list = new ArrayList<>();
        list.add(new BlockPos(x1, y1, z1));
        return list;
    }
}
