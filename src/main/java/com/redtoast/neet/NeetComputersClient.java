package com.redtoast.neet;

import com.redtoast.blocks.LargeBlockComputer;
import com.redtoast.blocks.LargeEntityComputer;
import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.graphics.GraphicsScreen;
import com.redtoast.graphics.GraphicsScreenHandler;
import com.redtoast.graphics.RGBGraphicsArray;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.util.math.BlockPos;

public class NeetComputersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		HandledScreens.register(NeetComputers.GRAPHICS_SCREEN_HANDLER, GraphicsScreen::new);
        assert NeetComputers.SCREEN_PACKET_ID != null;
        ClientPlayNetworking.registerGlobalReceiver(NeetComputers.SCREEN_PACKET_ID, (client, handler, buf, responseSender) -> {
            assert client.player != null;
            if (client.player.currentScreenHandler instanceof GraphicsScreenHandler) {
				((GraphicsScreenHandler) client.player.currentScreenHandler).updateGraphics(RGBGraphicsArray.fromPacket(buf));
			}
			client.execute(() -> {
				// Everything in this lambda is run on the render thread
			});
		});
        assert NeetComputers.BINARY_SCREEN_PACKET != null;
        ClientPlayNetworking.registerGlobalReceiver(NeetComputers.BINARY_SCREEN_PACKET, (client, handler, buf, responseSender) -> {
			assert client.player != null;
			BlockPos pos = buf.readBlockPos();
			BinaryGraphicsArray graphics = BinaryGraphicsArray.fromPacket(buf);
			client.execute(() -> {
				if (client.world == null) return;
				if (!(client.world.getBlockEntity(pos) instanceof LargeEntityComputer)) return;
				BlockEntity be = client.world.getBlockEntity(pos);
				if (be instanceof LargeEntityComputer computer) {
					computer.computer.setBinaryGraphics(graphics);
					BlockState state = client.world.getBlockState(pos);
					client.world.setBlockState(pos, state.with(LargeBlockComputer.ON, computer.computer.IsOn()), Block.NOTIFY_ALL);
				}
			});
		});
	}
}