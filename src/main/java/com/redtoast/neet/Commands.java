package com.redtoast.neet;

import com.mojang.brigadier.context.CommandContext;
import com.redtoast.ComputerState;
import com.redtoast.blocks.Generics.ComputerBlockEntity;
import com.redtoast.simulation.RuntimeThread;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class Commands {
    public static int pauseAll(CommandContext<ServerCommandSource> context) {
        int size = RuntimeThread.size();
        RuntimeThread.forEach(thread -> thread.getComputer().pause());
        context.getSource().sendFeedback(() -> Text.of("Paused " + size + " computers"), false);
        return 1;
    }

    public static int stopAll(CommandContext<ServerCommandSource> context) {
        int size = RuntimeThread.size();
        RuntimeThread.forEach(thread -> thread.getComputer().stop());
        context.getSource().sendFeedback(() -> Text.of("Stopped " + size + " computers"), false);
        return 1;
    }

    public static int stopBlock(CommandContext<ServerCommandSource> context) {
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "position");
        BlockEntity block = context.getSource().getWorld().getBlockEntity(pos);
        if (block instanceof ComputerBlockEntity computerBlockEntity) {
            ComputerState state = computerBlockEntity.getComputer().getStatus();
            if (state == ComputerState.ON || state == ComputerState.PAUSED) {
                computerBlockEntity.getComputer().stop();
                context.getSource().sendFeedback(() -> Text.of("Computer stopped"), false);
                return 1;
            }
        }
        context.getSource().sendError(Text.of("Valid target not found"));
        return 1;
    }

    public static int pauseBlock(CommandContext<ServerCommandSource> context) {
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "position");
        BlockEntity block = context.getSource().getWorld().getBlockEntity(pos);
        if (block instanceof ComputerBlockEntity computerBlockEntity) {
            ComputerState state = computerBlockEntity.getComputer().getStatus();
            if (state == ComputerState.ON) {
                computerBlockEntity.getComputer().pause();
                context.getSource().sendFeedback(() -> Text.of("Computer paused"), false);
                return 1;
            }
        }
        context.getSource().sendError(Text.of("Valid target not found"));
        return 1;
    }
}
