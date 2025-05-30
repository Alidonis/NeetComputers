package com.redtoast.simulation.peripheral;

import com.redtoast.Computer;
import com.redtoast.simulation.LangAPI.LangAPI;
import net.minecraft.block.entity.BlockEntity;

public class peripheralAPI extends LangAPI {
    private BlockEntity parent;
    private Computer computer;
    public <BlockEntityClass extends BlockEntity> peripheralAPI(String name, BlockEntityClass Parent, Computer host) {
        super(name);
        parent = Parent;
        computer = host;
    }
    public Computer getComputer(){
        return computer;
    }
    public BlockEntity getParent(){
        return parent;
    }
}
