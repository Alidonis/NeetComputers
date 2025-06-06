package com.redtoast;

public class ComputerSpecs {
    public int MaxCores = 6;
    public double CoreUtilizationBonus = 0;
    public int BatchSize = 500;
    public int Batches = 58;
    public boolean doesGraphics = false;
    public int GraphicsSizeX = 0;
    public int GraphicsSizeY = 0;
    public int ColorGraphicsSizeX = 0;
    public int ColorGraphicsSizeY = 0;
    public String MachineName = "";
    public ComputerSpecs setMaxCores(int cores){
        MaxCores = cores;
        return this;
    }
    public ComputerSpecs setCoreUtilizationBonus(int percent){
        CoreUtilizationBonus = percent / 100d;
        return this;
    }
    public ComputerSpecs setMachineName(String name){
        MachineName=name;
        return this;
    }
    public ComputerSpecs setIPS(int IPS, int size){
        int IPT = IPS / 20;
        Batches = IPT / BatchSize;
        BatchSize = size;
        return this;
    }
    public ComputerSpecs setGraphics(int SizeX, int SizeY){
        doesGraphics = true;
        GraphicsSizeX = SizeX;
        GraphicsSizeY = SizeY;
        return this;
    }
    public ComputerSpecs setColorGraphics(int SizeX, int SizeY){
        ColorGraphicsSizeX = SizeX;
        ColorGraphicsSizeY = SizeY;
        return this;
    }
}