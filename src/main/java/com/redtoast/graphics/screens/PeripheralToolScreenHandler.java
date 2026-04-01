package com.redtoast.graphics.screens;

import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.neet.NeetComputersServer;
import com.redtoast.neet.Networking.PeripheralToolScreenInitPayload;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.VarType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Language;

import java.util.List;
import java.util.UUID;

public class PeripheralToolScreenHandler extends ScreenHandler {
    private PeripheralToolScreenInitPayload data;
    private PeripheralProvider provider = null;
    private String retur = "";
    private String display = "";
    private VarType type = VarType.NULL;
    private boolean isLong = false;

    public PeripheralToolScreenHandler(int syncId, PlayerInventory playerInventory, PeripheralToolScreenInitPayload payload) {
        super(NeetComputersServer.PERIPHERAL_TOOL_SCREEN_HANDLER, syncId);
        display = Language.getInstance().get("menu.neetcomputers.return_field_default");
        data = payload;
    }

    public PeripheralToolScreenHandler(int syncId, PeripheralProvider peripheralProvider) {
        super(NeetComputersServer.PERIPHERAL_TOOL_SCREEN_HANDLER, syncId);
        data = PeripheralToolScreenInitPayload.fromPeripheral(peripheralProvider);
        provider = peripheralProvider;
    }

    public Value<?> call(String function, List<Value<?>> args){
        if (provider!=null){
            try{
                return provider.callFunction(null, function, args.toArray(new Value[]{}));
            }catch (Exception e){
                return Value.asError("Internal error");
            }
        }else{
            return Value.NULL;
        }
    }

    public void setTag(String tag){
        if (provider!=null) provider.setTag(tag);
    }

    public String[] functionNames(){
        return data.listOfFunctions();
    }

    public String getTypeName(){
        return data.modelName();
    }

    public String getTag(){
        return data.tag();
    }

    public UUID getUuid(){
        return data.uuid();
    }

    public void setReturn(String value, VarType type){
        retur = value;
        if ((Language.getInstance().get("menu.neetcomputers.return_field")+retur).length()>24) {
            display = (Language.getInstance().get("menu.neetcomputers.return_field")+retur).substring(0,21)+"...";
            isLong = true;
        } else {
            display = Language.getInstance().get("menu.neetcomputers.return_field")+retur;
            isLong = false;
        }
        this.type = type;
    }

    public VarType getVarType(){return type;}

    public String getMessage(){return retur;}

    public String getDisplayMessage(){return display;}

    public boolean isLong() {return isLong;}

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
