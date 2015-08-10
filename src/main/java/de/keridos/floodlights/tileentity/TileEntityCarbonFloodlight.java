package de.keridos.floodlights.tileentity;

import de.keridos.floodlights.handler.ConfigHandler;
import de.keridos.floodlights.handler.lighting.LightHandler;
import de.keridos.floodlights.reference.Names;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import static de.keridos.floodlights.util.GeneralUtil.getBurnTime;
import static de.keridos.floodlights.util.GeneralUtil.safeLocalize;

/**
 * Created by Keridos on 09/10/2014.
 * This Class describes the carbon floodlight TileEntity.
 */
public class TileEntityCarbonFloodlight extends TileEntityMetaFloodlight implements ISidedInventory {
    public int timeRemaining;
    private LightHandler lightHandler = LightHandler.getInstance();
    private ItemStack[] inventory;

    public TileEntityCarbonFloodlight() {
        super();
        inventory = new ItemStack[1];
        timeRemaining = 0;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        if (nbtTagCompound.hasKey(Names.NBT.TIME_REMAINING)) {
            this.timeRemaining = nbtTagCompound.getInteger(Names.NBT.TIME_REMAINING);
        }
        NBTTagList list = nbtTagCompound.getTagList(Names.NBT.ITEMS, 10);
        NBTTagCompound item = list.getCompoundTagAt(0);
        int slot = item.getByte(Names.NBT.ITEMS);
        if (slot >= 0 && slot < getSizeInventory()) {
            setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(item));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setInteger(Names.NBT.TIME_REMAINING, timeRemaining);
        NBTTagList list = new NBTTagList();
        ItemStack itemstack = getStackInSlot(0);
        if (itemstack != null) {
            NBTTagCompound item = new NBTTagCompound();
            item.setByte(Names.NBT.ITEMS, (byte) 0);
            itemstack.writeToNBT(item);
            list.appendTag(item);
        }
        nbtTagCompound.setTag(Names.NBT.ITEMS, list);
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemstack, int j) {
        return (i == 0 && getBurnTime(itemstack) > 0);
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return (getBurnTime(itemstack) > 0);
    }

    @Override
    public void updateEntity() {
        World world = this.getWorldObj();
        if (!world.isRemote) {
            ForgeDirection direction = this.getOrientation();
            if (timeRemaining == 0 && inventory[0] != null) {
                timeRemaining = ConfigHandler.carbonTime * getBurnTime(inventory[0]) / 1600 * (mode == 0 ? 20 : 10);
                decrStackSize(0, 1);
            }
            if (active && timeRemaining > 0) {
                if (!wasActive || world.getTotalWorldTime() % timeout == 0) {
                    if (world.getTotalWorldTime() % timeout == 0) {
                        lightHandler.removeSource(world, this.xCoord, this.yCoord, this.zCoord, direction, mode);
                        lightHandler.addSource(world, this.xCoord, this.yCoord, this.zCoord, direction, mode);
                        world.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, this.getOrientation().ordinal() + 6, 2);
                    } else {
                        lightHandler.addSource(world, this.xCoord, this.yCoord, this.zCoord, direction, mode);
                        world.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, this.getOrientation().ordinal() + 6, 2);
                    }
                }
                timeRemaining--;
                wasActive = true;
            } else {
                if (wasActive) {
                    lightHandler.removeSource(world, this.xCoord, this.yCoord, this.zCoord, direction, mode);
                    world.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, world.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord) - 6, 2);
                }
                wasActive = false;
            }
        }
    }

    public void changeMode(EntityPlayer player) {
        World world = this.getWorldObj();
        if (!world.isRemote) {
            ForgeDirection direction = this.getOrientation();
            lightHandler.removeSource(world, this.xCoord, this.yCoord, this.zCoord, direction, this.mode);
            mode = (mode == 2 ? 0 : mode + 1);
            if (mode == 1) {
                timeRemaining /= 4;
            } else if (mode == 0) {
                timeRemaining *= 4;
            }
            if (active && timeRemaining > 0) {
                lightHandler.addSource(world, this.xCoord, this.yCoord, this.zCoord, direction, this.mode);
            }
            String modeString = (mode == 0 ? Names.Localizations.STRAIGHT : mode == 1 ? Names.Localizations.NARROW_CONE : Names.Localizations.WIDE_CONE);
            player.addChatMessage(new ChatComponentText(safeLocalize(Names.Localizations.MODE) + ": " + safeLocalize(modeString)));
        }
    }
}
