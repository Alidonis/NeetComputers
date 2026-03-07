package com.redtoast.items.generics;

import com.redtoast.Connections.PipeType;
import com.redtoast.Connections.CableManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.Hashtable;
import java.util.LinkedList;

public class ConnectorItem extends Item {
    private final PipeType pipeType;
    private final Hashtable<PlayerEntity, Long> timers = new Hashtable<>();
    private final Hashtable<PlayerEntity, BlockPos> lastPlaced = new Hashtable<>();
    private PipeInteraction lastInteraction = null;
    public ConnectorItem(Settings settings, PipeType pipeType) {
        super(settings);
        this.pipeType = pipeType;
    }

    public PipeType getType() {
        return pipeType;
    }

    private boolean heldDownPlace(PlayerEntity player) {
        if (!timers.containsKey(player)) {
            timers.put(player, System.currentTimeMillis());
            return false;
        }else{
            int delay = (int) (System.currentTimeMillis() - timers.get(player));
            timers.put(player, System.currentTimeMillis());
            return Math.abs(delay-200)<15;
        }
    }

    @FunctionalInterface
    private interface PipeInteraction {
        boolean apply(World world, BlockPos pos, PlayerEntity player);
    }

    private void pathfind(World world, BlockPos start, BlockPos end, PlayerEntity player, PipeInteraction interactionMethod) {
        if (!start.isWithinDistance(end, 5)) return;
        if (start.isWithinDistance(end, 1)) return;
        LinkedList<BlockPos> que = new LinkedList<>();
        LinkedList<BlockPos> blackList = new LinkedList<>();
        Hashtable<BlockPos, BlockPos> parentalTable = new Hashtable<>();
        que.add(start);

        int minX = Math.min(start.getX(), end.getX());
        int maxX = Math.max(start.getX(), end.getX());
        int minY = Math.min(start.getY(), end.getY());
        int maxY = Math.max(start.getY(), end.getY());
        int minZ = Math.min(start.getZ(), end.getZ());
        int maxZ = Math.max(start.getZ(), end.getZ());

        while (!que.isEmpty()) {
            BlockPos current = que.getFirst();
            que.remove();
            for (Direction direction : Direction.values()){
                BlockPos offset = current.offset(direction);
                if (
                        offset.getX() < minX || offset.getX() > maxX ||
                        offset.getY() < minY || offset.getY() > maxY ||
                        offset.getZ() < minZ || offset.getZ() > maxZ
                ) continue;
                if (blackList.contains(offset)) continue;
                if (world.getBlockState(offset).isAir()) continue;
                if (offset.asLong()==end.asLong()){
                    BlockPos backTrace = current;
                    LinkedList<BlockPos> que2 = new LinkedList<>();
                    while (parentalTable.containsKey(backTrace)) {
                        que2.add(backTrace);
                        backTrace = parentalTable.get(backTrace);
                    }
                    for (int i = que2.size() - 1; i >= 0; i--) {
                        interactionMethod.apply(world, que2.get(i), player);
                    }
                    return;
                }
                parentalTable.put(offset, current);
                que.add(offset);
            }
            blackList.add(current);
        }
    }

    public boolean placePipe(World world, BlockPos pos, PlayerEntity player){
        boolean mark = !CableManager.getInstance().pipeExists(world.getDimension(), pos, pipeType);
        if (mark) {
            CableManager.getInstance().createPipe(world.getDimension(), pos, pipeType);
        }
        lastPlaced.put(player, pos);
        return mark;
    }

    public boolean removePipe(World world, BlockPos pos, PlayerEntity player){
        boolean mark = CableManager.getInstance().pipeExists(world.getDimension(), pos, pipeType);
        if (mark) {
            CableManager.getInstance().removePipe(world.getDimension(), pos, pipeType);
        }
        lastPlaced.put(player, pos);
        return mark;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand){
        if (!world.isClient()){
            BlockHitResult blockHitResult = raycast(world, player, RaycastContext.FluidHandling.NONE);
            if (blockHitResult.getType() == HitResult.Type.BLOCK) {
                PipeInteraction interaction;
                if (heldDownPlace(player) && lastPlaced.containsKey(player)) {
                    interaction = lastInteraction==null ? this::placePipe : lastInteraction;
                    pathfind(world, blockHitResult.getBlockPos(), lastPlaced.get(player), player, interaction);
                }else{
                    interaction = CableManager.getInstance().pipeExists(world.getDimension(), blockHitResult.getBlockPos(), getType()) ? this::removePipe : this::placePipe;
                    lastInteraction = interaction;
                }
                interaction.apply(world, blockHitResult.getBlockPos(), player);
            }
        }
        return TypedActionResult.success(player.getStackInHand(hand));
    }
}
