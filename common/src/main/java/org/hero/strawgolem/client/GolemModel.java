package org.hero.strawgolem.client;

import net.minecraft.resources.ResourceLocation;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;
import software.bernie.geckolib.model.GeoModel;

public class GolemModel extends GeoModel<StrawGolem> {
    private static final ResourceLocation model = ResourceLocation.tryBuild(Constants.MODID, "geo/strawgolem.geo.json");
    private static final ResourceLocation animation = ResourceLocation.tryBuild(Constants.MODID, "animations/strawgolem.animation.json");
    private static final ResourceLocation[] textures = {
            ResourceLocation.tryBuild(Constants.MODID, "textures/straw_golem.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/straw_golem_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/straw_golem_dying.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_dying.png")
    };


    @Override
    public ResourceLocation getModelResource(StrawGolem strawGolem) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(StrawGolem strawGolem) {
        return textures[strawGolem.healthStatus() + (strawGolem.isFestive() ? 3 : 0)];
    }

    @Override
    public ResourceLocation getAnimationResource(StrawGolem strawGolem) {
        return animation;
    }
}
