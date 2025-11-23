package org.hero.strawgolem.mixin;

import net.minecraft.world.entity.player.Player;
import org.hero.strawgolem.Constants;
import net.minecraft.client.Minecraft;
import org.hero.strawgolem.golem.StrawGolem;
import org.hero.strawgolem.mixinInterfaces.GolemOrderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class MixinPlayer implements GolemOrderer {
    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(CallbackInfo info) {
        Constants.LOG.info("This line is printed by the Straw Golem common mixin!");
        Constants.LOG.info("MC Version: {}", Minecraft.getInstance().getVersionType());
    }
    @Unique // Optional, but good practice to ensure the name is unique and won't conflict
    private StrawGolem strawgolemRewrite$strawGolem; // The new field being added

    @Override
    public StrawGolem strawgolemRewrite$getGolem() {
        return strawgolemRewrite$strawGolem;
    }

    @Override
    public void strawgolemRewrite$setGolem(StrawGolem golem) {
        strawgolemRewrite$strawGolem = golem;
    }
}