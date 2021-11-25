/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.utilities.instances;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;

public class ContainerBuilds extends Container {

    public final IInventory inventory;

    public ContainerBuilds(IInventory inventory, PlayerEntity player) {
        this.inventory = inventory;
        int numRows = inventory.getContainerSize() / 9;
        inventory.openInventory(player);

        for (int i = 0; i < numRows; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9, 8 + j * 18, 18 + i * 18));
            }
        }
    }
    
    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        this.inventory.closeInventory(playerIn);
    }

}
