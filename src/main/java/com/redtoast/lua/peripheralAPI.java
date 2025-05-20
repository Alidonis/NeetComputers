package com.redtoast.lua;

import com.redtoast.Computer;
import net.minecraft.block.entity.BlockEntity;

public class peripheralAPI extends LuaAPI {
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
