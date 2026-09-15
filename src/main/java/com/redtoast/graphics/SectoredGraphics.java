package com.redtoast.graphics;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.IntBinaryOperator;
import java.util.stream.IntStream;

public record SectoredGraphics(Sector[] sectors, Vector2i size) implements Iterable<SectoredGraphics.Sector> {
    public record Sector(int x1, int y1, int x2, int y2, int color) {
        @Override
        public String toString(){
            return x1+"-"+y1+':'+x2+'-'+y2+":"+ RGBGraphicsArray.decimalToRgb(color);
        }

        public void writeToPacket(PacketByteBuf packet) {
            packet.writeInt(x1);
            packet.writeInt(y1);
            packet.writeInt(x2);
            packet.writeInt(y2);
            writeColor(packet, color);
        }

        public void writeToPacket(PacketByteBuf packet, List<Integer> colorPallet) {
            packet.writeInt(x1);
            packet.writeInt(y1);
            packet.writeInt(x2);
            packet.writeInt(y2);
            packet.writeByte(colorPallet.indexOf(color));
        }

        public static Sector readFromPacket(PacketByteBuf packet) {
            return new Sector(packet.readInt(), packet.readInt(), packet.readInt(), packet.readInt(), readColor(packet));
        }

        public static Sector readFromPacket(PacketByteBuf packet, int[] colorPallet) {
            return new Sector(packet.readInt(), packet.readInt(), packet.readInt(), packet.readInt(), colorPallet[packet.readByte() & 0xFF]);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Sector other) {
                return  other.x1() == x1() &&
                        other.y1() == y1() &&
                        other.x2() == x2() &&
                        other.y2() == y2() &&
                        other.color() == color();
            }
            return false;
        }
    }

    @NotNull
    @Override
    public Iterator<Sector> iterator() {
        return Arrays.stream(sectors).iterator();
    }

    //internal class for scanGraphics that allows sectors to be dynamically adjusted before being locked in
    private static class LiquidSector {
        public final int x;
        public final int y;
        public int width = 1;
        public int height = 1;
        public final int color;

        public LiquidSector(int x, int y, int color){
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public Sector solidify(){
            return new Sector(x, y, x + width - 1, y + height - 1, color);
        }
    }

    private static List<Sector> scanGraphics(Vector2i size, IntBinaryOperator getColor, @Nullable List<Integer> colorOutput) {
        //current position on the frame
        int x = 0, y = 0;
        //volume of the frame
        final int volume = size.x * size.y;
        //stores the pushed sectors, should only be appended
        final LinkedList<Sector> sectors = new LinkedList<>();
        //marks the pixels that have already been counted, so they won't be counted again
        boolean[] marks = new boolean[volume];
        //the current sector being worked on, if one is being worked on
        @Nullable LiquidSector currentSector = null;

        //start searching the frame left to right, top to bottom
        while (true) {
            int i = y * size.x + x;
            if (i==volume) break;
            boolean marked = marks[i];
            boolean overbounds = x == size.x;
            if (currentSector==null && !overbounds && !marked) {
                currentSector = new LiquidSector(x, y, getColor.applyAsInt(x, y));
                x++;
            }else{
                if (!overbounds && !marked) {
                    //test if the current pixel matches the sector and extend the sector if so
                    int color = getColor.applyAsInt(x, y);
                    if (color == currentSector.color) {
                        currentSector.width++;
                        x++;
                        continue;//move to next pixel
                    }else
                        x--;
                    //if the colors don't match, move to scan and push the sector
                }
                //scan and push the current sector to the stack
                if (currentSector==null) {
                    if (overbounds) {//current coordinates if over bounds
                        x = 0;
                        y++;
                    }else
                        x++;
                    continue;
                }

                //if there's a sector present, push it to the stack
                int subY = y+1;
                final int farX = currentSector.x + currentSector.width - 1;
                while (subY<size.y) {
                    //scan under the sector and fail if not all the pixels under it match the color
                    boolean kill = false;
                    for (int c = currentSector.x; c <= farX; c++) {
                        if (getColor.applyAsInt(c, subY) != currentSector.color) {
                            kill = true;
                            break;
                        }
                    }
                    if (kill) break;

                    //if nothing failed, mark the spaces and move down
                    for (int c = subY * size.x + currentSector.x; c <= (subY * size.x + farX); c++)
                        marks[c] = true;
                    currentSector.height++;
                    subY++;
                }

                if (overbounds) {//current coordinates if over bounds
                    x = 0;
                    y++;
                }else if (!marked)
                    x++;

                if (colorOutput!=null && colorOutput.size()<257 && !colorOutput.contains(currentSector.color)) colorOutput.add(currentSector.color);
                sectors.add(currentSector.solidify());
                currentSector=null;
            }
        }
        //push the current sector
        if (currentSector!=null)
            sectors.add(currentSector.solidify());
        return sectors;
    }

    public record IntegrabilityScan(Vector2i size, int numberOfSectors, int productPixelsOfSectors, int pixelsNotCovered, int numberOfPixelOverlaps, int numberOfOverlappingSectors, int numberOfDuplicateSectors, int invalidSectors, boolean overscan) {
        private static Logger logger = LoggerFactory.getLogger("Sectored Graphics");
        public boolean isAbnormal() {
            return  pixelsNotCovered != 0 ||
                    numberOfPixelOverlaps != 0 ||
                    invalidSectors != 0 ||
                    overscan;
        }

        public void print(boolean onlyPrintErrors, Object id) {
            if (isAbnormal()) {
                logger.warn("""
                        Error printout for graphics Object #{}:
                            Size: {}x{}
                            Volume of frame: {}
                            Sector count: {}
                            Sum of pixels in sectors: {}
                            Blank/missing pixels: {}
                            Overlapping pixel count: {}
                            Overlapping sector count: {}
                            Duplicate sector count: {}
                            Invalid sector count: {}
                            Some sectors out of bounds: {}""",
                        id != null ? id.toString() : "unknown", size.x, size.y, size.x * size.y, numberOfSectors, productPixelsOfSectors, pixelsNotCovered, numberOfPixelOverlaps, numberOfOverlappingSectors, numberOfDuplicateSectors, invalidSectors, overscan);
            } else if (!onlyPrintErrors) {
                logger.info("""
                        Graphics Object #{} has no abnormalities:
                            Size: {}x{}
                            Volume of frame: {}
                            Sector count: {}""",
                        id != null ? id.toString() : "unknown", size.x, size.y, size.x * size.y, numberOfSectors);
            }
        }

        public void print(boolean onlyPrintErrors) {print(onlyPrintErrors, null);}
        public void print(int id) {print(false, id);}
        public void print(String id) {print(false, id);}
        public void print() {print(false, null);}
    }

    public IntegrabilityScan scanIntegrability() {
        boolean[] coverageMap = new boolean[size.x * size.y];
        boolean overscan = false;
        int productOfSectors = 0;
        int numOverlaps = 0;
        int numOverlapping = 0;
        int numDuplicates = 0;
        int invalidSectors = 0;
        int missingPixels = size.x * size.y;
        ArrayList<Sector> checkList = new ArrayList<>();
        for (Sector sector : sectors) {
            if (checkList.contains(sector)) {
                numDuplicates++;
                numOverlapping++;
            }else{
                checkList.add(sector);
            }
        }
        checkList.clear();
        for (Sector sector : sectors) {
            if (sector.x1>sector.x2 || sector.y1>sector.y2) {
                invalidSectors++;
                continue;
            }
            if (sector.x1<0 || sector.x2>=size.x || sector.y1<0 || sector.y2>=size.y) {
                invalidSectors++;
                overscan = true;
            }
            productOfSectors += (sector.x2 - sector.x1 + 1) * (sector.y2 - sector.y1 + 1);
            for (int x = Math.max(sector.x1, 0); x <= Math.min(sector.x2, size.x-1); x++) {
                for (int y = Math.max(sector.y1, 0); y <= Math.min(sector.y2, size.y-1); y++) {
                    if (coverageMap[x * size.y + y]) {
                        if (!checkList.contains(sector)) numOverlapping++;
                        checkList.add(sector);
                        numOverlaps++;
                    }else{
                        missingPixels--;
                    }
                    coverageMap[x * size.y + y] = true;
                }
            }
        }
        return new IntegrabilityScan(size, sectors.length, productOfSectors, missingPixels, numOverlaps, numOverlapping, numDuplicates, invalidSectors, overscan);
    }

    private static void writeColor(PacketByteBuf packet, int color) {
        packet.writeByte((color & 0xFF0000) >> 16);
        packet.writeByte((color & 0xFF00) >> 8);
        packet.writeByte(color & 0xFF);
    }

    private static int readColor(PacketByteBuf packet) {
        int color = 0xFF;
        for (int i = 0; i < 3; i++) {
            color <<= 8;
            color |= packet.readByte() & 0xFF;
        }
        return color;
    }

    public static PacketByteBuf encodeFromGraphicsArray(RGBGraphicsArray graphics) {
        PacketByteBuf packet = PacketByteBufs.create();
        encodeFromGraphicsArray(packet, graphics);
        return packet;
    }

    public static void encodeFromGraphicsArray(PacketByteBuf packet, RGBGraphicsArray graphics) {
        Vector2i size = graphics.getSize();
        int[][] pixels = graphics.pixels;
        LinkedList<Integer> colorBuffer = new LinkedList<>();
        List<Sector> sectors = scanGraphics(size, (x, y) -> RGBGraphicsArray.blendPixel(0xFF000000, pixels[y][x] | 0xFF000000), colorBuffer);
        if (sectors.size()==1){
            packet.writeByte(0b0100000);
            packet.writeInt(size.x);
            packet.writeInt(size.y);
            writeColor(packet, sectors.getFirst().color);
        }else{
            if ((size.x * size.y * 3) >= (sectors.size() * (Integer.BYTES * 4 + 3) + 4)) {
                boolean doPalletization = colorBuffer.size()<=256 && sectors.size() * 3 > colorBuffer.size() * 3 + 1 + sectors.size();
                packet.writeByte(doPalletization ? 0b0010000 : 0b0000000);
                packet.writeInt(size.x);
                packet.writeInt(size.y);
                if (doPalletization) {
                    packet.writeByte(colorBuffer.size()-1);
                    for (int color : colorBuffer) writeColor(packet, color);
                }
                packet.writeInt(sectors.size());
                for (Sector sector : sectors) {
                    if (doPalletization) {
                        sector.writeToPacket(packet, colorBuffer);
                    }else{
                        sector.writeToPacket(packet);
                    }
                }
            }else{
                boolean doPalletization = colorBuffer.size()<=256 && (size.x * size.y * 3) > colorBuffer.size() * 3 + 1 + (size.x * size.y);
                packet.writeByte(doPalletization ? 0b1010000 : 0b1000000);
                packet.writeInt(size.x);
                packet.writeInt(size.y);
                if (doPalletization) {
                    packet.writeByte(colorBuffer.size()-1);
                    for (int color : colorBuffer) writeColor(packet, color);
                }
                for (int x = 0; x < size.x; x++) {
                    for (int y = 0; y < size.y; y++) {
                        if (doPalletization) {
                            packet.writeByte(colorBuffer.indexOf(RGBGraphicsArray.blendPixel(0xFF000000, graphics.get(x, y) | 0xFF000000)));
                        }else{
                            writeColor(packet, RGBGraphicsArray.blendPixel(0xFF000000, graphics.get(x, y)));
                        }
                    }
                }
            }
        }
    }

    public static SectoredGraphics decodeFromPacket(PacketByteBuf packet) {
        byte control = packet.readByte();
        Vector2i size = new Vector2i(packet.readInt(), packet.readInt());
        boolean doPalletization = (control & 0b0010000) != 0;
        int[] colorPallet = new int[doPalletization ? (packet.readByte() & 0xFF) + 1 : 0];
        for (int i = 0; i < colorPallet.length; i++) {
            colorPallet[i] = readColor(packet);
        }
        if (control==0b0100000){
            return new SectoredGraphics(new Sector[]{new Sector(0, 0, size.x-1, size.y-1, readColor(packet))}, size);
        }else if (control==0b0000000 || control==0b0010000) {
            Sector[] sectors = new Sector[packet.readInt()];
            for (int i = 0; i < sectors.length; i++) {
                sectors[i] = doPalletization ? Sector.readFromPacket(packet, colorPallet) : Sector.readFromPacket(packet);
            }
            SectoredGraphics sectors1 = new SectoredGraphics(sectors, size);
            return sectors1;
        }else if (control==0b1000000 || control==0b1010000) {
            int[] pixelBuffer = new int[size.x * size.y];
            for (int i = 0; i < pixelBuffer.length; i++){
                pixelBuffer[i] = doPalletization ? colorPallet[packet.readByte() & 0xFF] : readColor(packet);
            }
            List<Sector> sectors = scanGraphics(size, (x, y) -> pixelBuffer[x * size.y + y], null);
            SectoredGraphics sectors1 = new SectoredGraphics(sectors.toArray(new Sector[]{}), size);
            return sectors1;
        }
        throw new IllegalArgumentException("Illegal control byte");
    }

    public static SectoredGraphics decodeFromGraphics(RGBGraphicsArray graphics) {
        Vector2i size = graphics.getSize();
        List<Sector> sectors = scanGraphics(size, (x, y) -> RGBGraphicsArray.blendPixel(0xFF000000, graphics.get(x, y) | 0xFF000000), null);
        return new SectoredGraphics(sectors.toArray(new Sector[]{}), size);
    }

    public RGBGraphicsArray encodeToGraphicsArray() {
        RGBGraphicsArray graphics = new RGBGraphicsArray(size.x, size.y);
        for (Sector sector : sectors) {
            for (int x = sector.x1; x <= sector.x2; x++) {
                for (int y = sector.y1; y <= sector.y2; y++) {
                    graphics.set(x, y, sector.color);
                }
            }
        }
        return graphics;
    }
}
