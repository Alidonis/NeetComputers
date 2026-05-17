package com.redtoast.graphics;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

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

    private static List<Sector> scanGraphics(Vector2i size, Function<Vector2i, Integer> getColor, @Nullable List<Integer> colorOutput) {
        ArrayList<Sector> sectors = new ArrayList<>();
        int targetVolume = size.x * size.y;
        int currentVolume = 0;
        boolean[] map = new boolean[size.x*size.y];
        int lastColor = -1;
        Vector2i start = null;
        for (int x = 0; x < size.x; x++){
            for (int y = 0; y < size.y; y++) {
                int color = getColor.apply(new Vector2i(x, y));
                boolean mapped = map[x * size.y + y];
                if (color!=lastColor && !mapped) {
                    if (start!=null) {
                        Vector2i end = new Vector2i(start.x, y-1);
                        for (int c = start.x; c < size.x; c++) {
                            for (int v = start.y; v < y; v++) {
                                if (getColor.apply(new Vector2i(c, v)) != lastColor) {
                                    c = size.x;
                                    v = size.y;
                                }
                            }
                            if (c!=size.x) {
                                end = new Vector2i(c, y - 1);
                                for (int v = start.y; v < y; v++) map[c * size.y + v] = true;
                            }
                        }
                        Sector sector = new Sector(start.x, start.y, end.x, end.y, lastColor);
                        currentVolume += (sector.x2 - sector.x1 + 1) * (sector.y2 - sector.y1 + 1);
                        if (colorOutput!=null && colorOutput.size()<257 && !colorOutput.contains(sector.color)) colorOutput.add(sector.color());
                        sectors.add(sector);
                        if (currentVolume==targetVolume) return sectors;
                    }
                    start = new Vector2i(x, y);
                    lastColor = color;
                }else{
                    if (mapped && start!=null) {
                        Vector2i end = new Vector2i(start.x, y-1);
                        for (int c = start.x; c < size.x; c++) {
                            for (int v = start.y; v < y; v++) {
                                if (getColor.apply(new Vector2i(c, v)) != lastColor) {
                                    c = size.x;
                                    v = size.y;
                                }
                            }
                            if (c!=size.x) {
                                end = new Vector2i(c, y - 1);
                                for (int v = start.y; v < y; v++) map[c * size.y + v] = true;
                            }
                        }
                        Sector sector = new Sector(start.x, start.y, end.x, end.y, lastColor);
                        currentVolume += (sector.x2 - sector.x1 + 1) * (sector.y2 - sector.y1 + 1);
                        if (colorOutput!=null && colorOutput.size()<257 && !colorOutput.contains(sector.color)) colorOutput.add(sector.color());
                        sectors.add(sector);
                        if (currentVolume==targetVolume) return sectors;
                        start = null;
                        lastColor = -1;
                    }else if(!mapped && start==null) {
                        start = new Vector2i(x, y);
                    }else {
                        lastColor = color;
                    }
                }
            }
            if (start!=null) {
                Vector2i end = new Vector2i(start.x, size.y-1);
                for (int c = start.x; c < size.x; c++) {
                    for (int v = start.y; v < size.y; v++) {
                        if (getColor.apply(new Vector2i(c, v)) != lastColor) {
                            c = size.x;
                            v = size.y;
                        }
                    }
                    if (c!=size.x) {
                        end = new Vector2i(c, size.y - 1);
                        for (int v = start.y; v < size.y; v++) map[c * size.y + v] = true;
                    }
                }
                Sector sector = new Sector(start.x, start.y, end.x, end.y, lastColor);
                currentVolume += (sector.x2 - sector.x1 + 1) * (sector.y2 - sector.y1 + 1);
                if (colorOutput!=null && colorOutput.size()<257 && !colorOutput.contains(sector.color)) colorOutput.add(sector.color());
                sectors.add(sector);
                if (currentVolume==targetVolume) return sectors;
            }
            start = null;
            lastColor = -1;

        }
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
        List<Sector> sectors = scanGraphics(size, (pos) -> RGBGraphicsArray.blendPixel(0xFF000000, pixels[pos.y][pos.x] | 0xFF000000), colorBuffer);
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
            return new SectoredGraphics(sectors, size);
        }else if (control==0b1000000 || control==0b1010000) {
            int[] pixelBuffer = new int[size.x * size.y];
            for (int i = 0; i < pixelBuffer.length; i++){
                pixelBuffer[i] = doPalletization ? colorPallet[packet.readByte() & 0xFF] : readColor(packet);
            }
            List<Sector> sectors = scanGraphics(size, (pos) -> pixelBuffer[pos.x * size.y + pos.y], null);
            return new SectoredGraphics(sectors.toArray(new Sector[]{}), size);
        }
        throw new IllegalArgumentException("Illegal control byte");
    }

    public static SectoredGraphics decodeFromGraphics(RGBGraphicsArray graphics) {
        Vector2i size = graphics.getSize();
        List<Sector> sectors = scanGraphics(size, (pos) -> RGBGraphicsArray.blendPixel(0xFF000000, graphics.get(pos.x, pos.y) | 0xFF000000), null);
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
