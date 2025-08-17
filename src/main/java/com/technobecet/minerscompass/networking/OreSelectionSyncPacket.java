package com.technobecet.minerscompass.networking;

import com.technobecet.minerscompass.MinersCompassMod;
import com.technobecet.minerscompass.util.DynamicOreType;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public record OreSelectionSyncPacket(Set<String> selectedOreTypeIds) implements FabricPacket {
    public static final PacketType<OreSelectionSyncPacket> TYPE = PacketType.create(
        new Identifier(MinersCompassMod.MOD_ID, "ore_selection_sync"), 
        OreSelectionSyncPacket::new
    );

    public OreSelectionSyncPacket(PacketByteBuf buf) {
        this(readOreTypeIds(buf));
    }

    private static Set<String> readOreTypeIds(PacketByteBuf buf) {
        int size = buf.readVarInt();
        Set<String> oreTypeIds = new HashSet<>();
        for (int i = 0; i < size; i++) {
            oreTypeIds.add(buf.readString());
        }
        return oreTypeIds;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(selectedOreTypeIds.size());
        for (String oreTypeId : selectedOreTypeIds) {
            buf.writeString(oreTypeId);
        }
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public static OreSelectionSyncPacket create(Set<DynamicOreType> selectedOreTypes) {
        Set<String> oreTypeIds = new HashSet<>();
        for (DynamicOreType oreType : selectedOreTypes) {
            oreTypeIds.add(oreType.getId());
        }
        return new OreSelectionSyncPacket(oreTypeIds);
    }
}