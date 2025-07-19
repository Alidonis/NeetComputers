package com.redtoast;

/**
 * {@link computerSpecs} defines the property's of a {@link Computer}
 * <p>
 * you can use the following methods to configure the instance, its recommended to use the intended functions instead of manually
 * modifying its public variables
 * </p>
 *
 * <ul>
 *     <li>{@link #setMaxCores(int)}</li>
 *     <li>{@link #setCoreUtilizationBonus(int)}</li>
 *     <li>{@link #setMachineName(String)}</li>
 *     <li>{@link #setIPS(int)}</li>
 *     <li>{@link #setBinaryGraphicsSize(int, int)}</li>
 *     <li>{@link #setColorGraphicsSize(int, int)}</li>
 * </ul>
 *
 * @see Computer
 */
public class computerSpecs {
    @Deprecated public int MaxCores = 6;
    @Deprecated public double CoreUtilizationBonus = 0;
    @Deprecated public int BatchSize = 10;
    @Deprecated public int Batches = 58;
    @Deprecated public boolean doesBinaryGraphics = false;
    @Deprecated public boolean doesRBGGraphics = false;
    @Deprecated public int GraphicsSizeX = 0;
    @Deprecated public int GraphicsSizeY = 0;
    @Deprecated public int ColorGraphicsSizeX = 0;
    @Deprecated public int ColorGraphicsSizeY = 0;
    @Deprecated public String MachineName = "";

    /**
     * sets the amount of cores the computer has access to
     * @return this
     */
    public computerSpecs setMaxCores(int cores){
        MaxCores = cores;
        return this;
    }

    /**
     * sets the bonus applied to threads to reward core utilization, the following is the equation used to calculate this bonus
     * <p>
     *     {@code coreUtilizationBonus * (amountOfThreads - 1)}
     * </p>
     * @return this
     */
    public computerSpecs setCoreUtilizationBonus(int percent){
        CoreUtilizationBonus = percent / 100d;
        return this;
    }

    /**
     * sets the name of the <b>MODEL</b> of the machine, note that this is not intended to be personalized
     * @return this
     */
    public computerSpecs setMachineName(String name){
        MachineName=name;
        return this;
    }

    /**
     * sets the amount of instructions the computer can execute per second (assuming the computer is ticked 20 times a second), think of this as the
     * speed of the computer.
     * <p>
     *     a fast computer would run at maybe 100,000 IPS
     * </p>
     * <p>
     *     and a slow one would maybe run around 10,000 IPS
     * </p>
     * @return this
     */
    public computerSpecs setIPS(int IPS){
        int IPT = IPS / 20;
        Batches = IPT / 10;
        BatchSize = 10;
        return this;
    }

    /**
     * sets the size of the visual graphics that are rendered in the gameWorld and NOT the gui, if no size is specified the computer wont supper any binary graphics
     * @return this
     */
    public computerSpecs setBinaryGraphicsSize(int SizeX, int SizeY){
        doesBinaryGraphics = true;
        GraphicsSizeX = SizeX;
        GraphicsSizeY = SizeY;
        return this;
    }

    /**
     * sets the size of the GUI screen
     * @return this
     */
    public computerSpecs setColorGraphicsSize(int SizeX, int SizeY){
        doesRBGGraphics = true;
        ColorGraphicsSizeX = SizeX;
        ColorGraphicsSizeY = SizeY;
        return this;
    }
}