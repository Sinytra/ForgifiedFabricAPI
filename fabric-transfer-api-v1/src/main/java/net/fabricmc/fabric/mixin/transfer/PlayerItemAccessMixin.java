package net.fabricmc.fabric.mixin.transfer;

import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.fabricmc.fabric.impl.transfer.compat.FabricPlayerItemAccess;

@Mixin(targets = "net.neoforged.neoforge.transfer.access.PlayerItemAccess")
public class PlayerItemAccessMixin implements FabricPlayerItemAccess {
	@Shadow
	@Final
	private PlayerInventoryWrapper inventoryWrapper;
	
	@Override
	public PlayerInventoryWrapper getInventoryWrapper() {
		return this.inventoryWrapper;
	}
}
