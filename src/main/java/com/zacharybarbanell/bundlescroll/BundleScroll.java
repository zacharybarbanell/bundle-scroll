package com.zacharybarbanell.bundlescroll;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class BundleScroll implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("bundlescroll");
    public static final Identifier SCROLL_PACKET_ID = new Identifier("bundlescroll", "scrollbundle");

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(SCROLL_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            int syncId = buf.readInt();
		    int revision = buf.readInt();
		    int i = buf.readInt();
            int amt = buf.readInt();
            
            server.execute( () -> {
                player.updateLastActionTime();
                ScreenHandler screenHandler = player.currentScreenHandler;

                if (screenHandler.syncId != syncId) {
                    return;
                }
                if (player.isSpectator()) {
                    screenHandler.syncState();
                    return;
                }
                if (!screenHandler.canUse(player)) {
                    LOGGER.debug("Player {} interacted with invalid menu {}", player, screenHandler);
                    return;
                }
                if (!screenHandler.isValid(i)) {
                    LOGGER.debug("Player {} clicked invalid slot index: {}, available slots: {}", player.getName(), i, screenHandler.slots.size());
                    return;
                }
                boolean flag = revision == player.currentScreenHandler.getRevision();
                screenHandler.disableSyncing();
                Slot slot = screenHandler.getSlot(i);
                ItemStack stack = slot.getStack();
                if ((stack.getItem() instanceof BundleItem)) {
			        shiftBundle(stack, amt);
		        }
                screenHandler.enableSyncing();
                if (flag) {
                    screenHandler.updateToClient();
                } else {
                    screenHandler.sendContentUpdates();
                }
            });
        });
    }

    public static void shiftBundle(ItemStack bundle, int shift) {
		NbtCompound nbtCompound = bundle.getOrCreateNbt();
		if (!nbtCompound.contains("Items")) {
            nbtCompound.put("Items", new NbtList());
        }
		NbtList nbtList = nbtCompound.getList("Items", NbtElement.COMPOUND_TYPE);
		int len = nbtList.size();
		if (len == 0) {
			return;
		}
		shift = Math.floorMod(shift, len);
		for (int i = 0; i < shift; i++) {
			nbtList.add(nbtList.remove(0));
		}
	}
}
