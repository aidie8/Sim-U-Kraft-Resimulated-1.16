package com.Resimulators.simukraft.common.block;

import com.Resimulators.simukraft.SimuKraft;
import com.Resimulators.simukraft.common.tileentity.TileConstructor;
import com.Resimulators.simukraft.common.world.Faction;
import com.Resimulators.simukraft.common.world.SavedWorldData;
import com.Resimulators.simukraft.handlers.SimUKraftPacketHandler;
import com.Resimulators.simukraft.packets.OpenJobGuiPacket;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;

import java.util.ArrayList;

public class BlockConstructor extends BlockBase {
    public BlockConstructor(final Properties properties, String name) {
        super(properties, name);
    }

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace) {
        if (!world.isRemote) {
            SimuKraft.LOGGER().debug("Tile Entity At Pos = " + world.getTileEntity(pos));
            Faction faction = SavedWorldData.get(world).getFactionWithPlayer(player.getUniqueID());
            ArrayList<Integer> simids = faction.getSimIds((ServerWorld) world);
            SimUKraftPacketHandler.INSTANCE.sendTo(new OpenJobGuiPacket(simids, pos), ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        }
        return ActionResultType.SUCCESS;
    }


    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileConstructor();
    }


}
