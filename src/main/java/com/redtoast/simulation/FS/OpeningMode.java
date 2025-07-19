package com.redtoast.simulation.FS;

public record OpeningMode(boolean canRead, boolean canWrite, boolean truncate, boolean binary, boolean create, boolean invalid) {
}
