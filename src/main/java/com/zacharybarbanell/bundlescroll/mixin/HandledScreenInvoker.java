package com.zacharybarbanell.bundlescroll.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HandledScreen.class)
public interface HandledScreenInvoker {
	@Invoker("getSlotAt")
	public Slot invokeGetSlotAt(double x, double y);
}
