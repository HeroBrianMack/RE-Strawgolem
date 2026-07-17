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

    private static final ResourceLocation[] breederTextures = {
            ResourceLocation.tryBuild(Constants.MODID, "textures/breeder_golem.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/breeder_golem_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/breeder_golem_dying.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_dying.png")
    };

    private static final ResourceLocation[] stockTextures = {
            ResourceLocation.tryBuild(Constants.MODID, "textures/stock_golem.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/stock_golem_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/stock_golem_dying.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_dying.png")
    };

    private static final ResourceLocation[] minerTextures = {
            ResourceLocation.tryBuild(Constants.MODID, "textures/miner_golem.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/miner_golem_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/miner_golem_dying.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_dying.png")
    };

    private static final ResourceLocation[] excavatorTextures = {
            ResourceLocation.tryBuild(Constants.MODID, "textures/excavator_golem.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/excavator_golem_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/excavator_golem_dying.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_dying.png")
    };

    private static final ResourceLocation[] cookTextures = {
            ResourceLocation.tryBuild(Constants.MODID, "textures/cook_golem.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/cook_golem_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/cook_golem_dying.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_dying.png")
    };

    private static final ResourceLocation[] smelterTextures = {
            ResourceLocation.tryBuild(Constants.MODID, "textures/smelter_golem.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/smelter_golem_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/smelter_golem_dying.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_dying.png")
    };

    private static final ResourceLocation[] lumberjackTextures = {
            ResourceLocation.tryBuild(Constants.MODID, "textures/lumberjack_golem.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/lumberjack_golem_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/lumberjack_golem_dying.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_dying.png")
    };

    private static final ResourceLocation[] fisherTextures = {
            ResourceLocation.tryBuild(Constants.MODID, "textures/fisher_golem.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/fisher_golem_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/fisher_golem_dying.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_dying.png")
    };

    private static final ResourceLocation[] beekeeperTextures = {
            ResourceLocation.tryBuild(Constants.MODID, "textures/beekeeper_golem.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/beekeeper_golem_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/beekeeper_golem_dying.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_old.png"),
            ResourceLocation.tryBuild(Constants.MODID, "textures/snow_dying.png")
    };

    @Override
    public ResourceLocation getTextureResource(StrawGolem strawGolem) {
        ResourceLocation[] set = strawGolem instanceof org.hero.strawgolem.golem.BreederGolem ? breederTextures
                : strawGolem instanceof org.hero.strawgolem.golem.ExcavatorGolem ? excavatorTextures
                : strawGolem instanceof org.hero.strawgolem.golem.MinerGolem ? minerTextures
                : strawGolem instanceof org.hero.strawgolem.golem.CookGolem ? cookTextures
                : strawGolem instanceof org.hero.strawgolem.golem.BeekeeperGolem ? beekeeperTextures
                : strawGolem instanceof org.hero.strawgolem.golem.FisherGolem ? fisherTextures
                : strawGolem instanceof org.hero.strawgolem.golem.LumberjackGolem ? lumberjackTextures
                : strawGolem instanceof org.hero.strawgolem.golem.SmelterGolem ? smelterTextures
                : strawGolem instanceof org.hero.strawgolem.golem.StockGolem ? stockTextures : textures;
        return set[strawGolem.healthStatus() + (strawGolem.isFestive() ? 3 : 0)];
    }

    @Override
    public ResourceLocation getAnimationResource(StrawGolem strawGolem) {
        return animation;
    }
}
