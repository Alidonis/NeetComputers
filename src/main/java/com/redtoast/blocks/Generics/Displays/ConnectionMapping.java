package com.redtoast.blocks.Generics.Displays;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.joml.Math;
import org.joml.Vector2i;

import java.util.LinkedList;
import java.util.function.Function;

public class ConnectionMapping {
    private final Direction direction;
    private final BlockPos referencePoint;
    private final World world;
    private final Function<BlockPos, Boolean> validityFunction;
    private final LinkedList<Vector2i> filter = new LinkedList<>();
    private final LinkedList<Vector2i> queue = new LinkedList<>();
    private final LinkedList<Vector2i> validPositions = new LinkedList<>();
    private final LinkedList<Selection> selections = new LinkedList<>();
    private final Vector2i sizeCap;
    private int minX = 0;
    private int minY = 0;
    private int maxX = 0;
    private int maxY = 0;

    public ConnectionMapping(World world, BlockPos startingPos, Direction direction, int maxWidth, int maxHeight, Function<BlockPos, Boolean> validityFunction) {
        this.direction = direction;
        this.referencePoint = startingPos;
        this.world = world;
        this.validityFunction = validityFunction;
        this.sizeCap = new Vector2i(maxWidth, maxHeight);
        queue.add(new Vector2i(0, 0));
    }

    private void scanCell(Vector2i position) {
        if (filter.contains(position)) return;
        filter.add(position);
        if (validityFunction.apply(deLocalize(position))){
            queue.add(new Vector2i(position.x-1, position.y));
            queue.add(new Vector2i(position.x, position.y-1));
            queue.add(new Vector2i(position.x+1, position.y));
            queue.add(new Vector2i(position.x, position.y+1));
            validPositions.add(position);
            if (position.x < minX) minX = position.x;
            if (position.y < minY) minY = position.y;
            if (position.x > maxX) maxX = position.x;
            if (position.y > maxY) maxY = position.y;
        }
    }

    private boolean pointValid(Vector2i position) {
        return validPositions.contains(position) && !filter.contains(position);
    }

    private record Selection(Vector2i topPoint, Vector2i masterPoint, LinkedList<Vector2i> points, Vector2i size){}

    private void findMesh(Vector2i topPoint, int YFloor) {
        int sizeX = 0;
        LinkedList<Vector2i> rectangle = new LinkedList<>();
        for (int x = topPoint.x; x <= maxX; x++) {
            LinkedList<Vector2i> thisPass = new LinkedList<>();
            for (int y = topPoint.y; y >= YFloor; y--) {
                if (!pointValid(new Vector2i(x, y))){
                    selections.add(new Selection(topPoint, new Vector2i(topPoint.x(), YFloor), rectangle, new Vector2i(sizeX, Math.abs(topPoint.y-YFloor)+1)));
                    return;
                }
                thisPass.add(new Vector2i(x, y));
            }
            sizeX++;
            rectangle.addAll(thisPass);
            filter.addAll(thisPass);
        }
        selections.add(new Selection(topPoint, new Vector2i(topPoint.x(), YFloor), rectangle, new Vector2i(sizeX, Math.abs(topPoint.y-YFloor)+1)));
    }

    public void start() {
        while (!queue.isEmpty()) {
            scanCell(queue.removeFirst());
        }
        filter.clear();
        boolean lastPositive = false;
        Vector2i startingPoint = null;
        Vector2i lastPoint = null;
        for (int x = minX; x < maxX+1; x++){
            for (int y = maxY; y >= minY; y--){
                if (pointValid(new Vector2i(x, y))) {
                    if (!lastPositive){
                        startingPoint = new Vector2i(x, y);
                    }
                    lastPoint = new Vector2i(x, y);
                    lastPositive = true;
                }else{
                    if (lastPositive){
                        findMesh(startingPoint, lastPoint.y);
                    }
                    lastPositive = false;
                }
            }
            if (lastPositive){
                findMesh(startingPoint, lastPoint.y);
            }
            lastPositive = false;
        }
        for (Selection selection : selections) {
            BlockPos masterPos = deLocalize(selection.masterPoint);
            for (Vector2i point : selection.points()) {
                BlockPos pos = deLocalize(point);
                if (world.getBlockEntity(pos) instanceof ConnectionMappingAccess access) {
                    if (pos.equals(masterPos)){
                        access.setMaster(selection.size);
                    }else{
                        access.setSlave(masterPos);
                    }
                    access.calculateModel(selection.size, point.sub(selection.topPoint()).absolute());
                }
            }
        }
    }

    private BlockPos deLocalize(Vector2i pos){
        return deLocalize(pos.x(), pos.y());
    }

    private BlockPos deLocalize(int x, int y){
        return referencePoint.add(switch (direction) {
            case DOWN, UP -> null;
            case NORTH -> new Vec3i(-x, y, 0);
            case SOUTH -> new Vec3i(x, y, 0);
            case WEST -> new Vec3i(0, y, x);
            case EAST -> new Vec3i(0, y, -x);
        });
    }

    private Vector2i localize(BlockPos pos){
        BlockPos relativePos = pos.subtract(new Vec3i(referencePoint.getX(), referencePoint.getY(), referencePoint.getZ()));
        return switch (direction) {
            case DOWN, UP -> null;
            case NORTH -> new Vector2i(-relativePos.getX(), relativePos.getY());
            case SOUTH -> new Vector2i(relativePos.getX(), relativePos.getY());
            case WEST -> new Vector2i(relativePos.getZ(), relativePos.getY());
            case EAST -> new Vector2i(-relativePos.getZ(), relativePos.getY());
        };
    }
}
