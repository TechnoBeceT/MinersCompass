package com.technobecet.minerscompass;

import com.technobecet.minerscompass.item.ModItems;
import com.technobecet.minerscompass.item.custom.OreCompass;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.item.CompassAnglePredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;

public class MinersCompassModClient implements ClientModInitializer {

    CompassAnglePredicateProvider ANGLE_DELEGATE = new CompassAnglePredicateProvider((world, stack, entity) -> {
        return OreCompass.getTrackedPos(stack.getNbt());
    });

    private static float getSpinningAngle(ClientWorld world) {
        Long t = world.getTime() % 32L;
        return t.floatValue() / 32.0f;
    }

    @Override
    public void onInitializeClient() {
        ModelPredicateProviderRegistry.register(ModItems.ORE_COMPASS, new Identifier("angle"),
                (stack, world, entity, i) -> {
                    var pos = OreCompass.getTrackedPos(stack.getNbt());
                    if (pos == null && world != null) {
                        return getSpinningAngle(world);
                    }

                    return ANGLE_DELEGATE.unclampedCall(stack, world, entity, i);
                });

    }
}
