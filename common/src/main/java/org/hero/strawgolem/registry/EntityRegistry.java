package org.hero.strawgolem.registry;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.BreederGolem;
import org.hero.strawgolem.golem.BeekeeperGolem;
import org.hero.strawgolem.golem.FisherGolem;
import org.hero.strawgolem.golem.LumberjackGolem;
import org.hero.strawgolem.golem.CookGolem;
import org.hero.strawgolem.golem.ExcavatorGolem;
import org.hero.strawgolem.golem.SmelterGolem;
import org.hero.strawgolem.golem.MinerGolem;
import org.hero.strawgolem.golem.StockGolem;
import org.hero.strawgolem.golem.StrawGolem;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class EntityRegistry {
    public static void init() {}

    public static final Supplier<EntityType<StrawGolem>> STRAWGOLEM = registerEntity("strawgolem", StrawGolem::new, 0.6f, 0.9f);
    public static final Supplier<EntityType<BreederGolem>> BREEDERGOLEM = registerEntity("breeder_golem", BreederGolem::new, 0.6f, 0.9f);
    public static final Supplier<EntityType<StockGolem>> STOCKGOLEM = registerEntity("stock_golem", StockGolem::new, 0.6f, 0.9f);
    public static final Supplier<EntityType<MinerGolem>> MINERGOLEM = registerEntity("miner_golem", MinerGolem::new, 0.6f, 0.9f);
    public static final Supplier<EntityType<BeekeeperGolem>> BEEKEEPERGOLEM = registerEntity("beekeeper_golem", BeekeeperGolem::new, 0.6f, 0.9f);
    public static final Supplier<EntityType<FisherGolem>> FISHERGOLEM = registerEntity("fisher_golem", FisherGolem::new, 0.6f, 0.9f);
    public static final Supplier<EntityType<LumberjackGolem>> LUMBERJACKGOLEM = registerEntity("lumberjack_golem", LumberjackGolem::new, 0.6f, 0.9f);
    public static final Supplier<EntityType<SmelterGolem>> SMELTERGOLEM = registerEntity("smelter_golem", SmelterGolem::new, 0.6f, 0.9f);
    public static final Supplier<EntityType<ExcavatorGolem>> EXCAVATORGOLEM = registerEntity("excavator_golem", ExcavatorGolem::new, 0.6f, 0.9f);
    public static final Supplier<EntityType<CookGolem>> COOKGOLEM = registerEntity("cook_golem", CookGolem::new, 0.6f, 0.9f);

    public static void registerEntityAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier> registrar) {
        registrar.accept(EntityRegistry.STRAWGOLEM.get(), StrawGolem.createAttributes().build());
        registrar.accept(EntityRegistry.BREEDERGOLEM.get(), StrawGolem.createAttributes().build());
        registrar.accept(EntityRegistry.STOCKGOLEM.get(), StrawGolem.createAttributes().build());
        registrar.accept(EntityRegistry.MINERGOLEM.get(), StrawGolem.createAttributes().build());
        registrar.accept(EntityRegistry.BEEKEEPERGOLEM.get(), StrawGolem.createAttributes().build());
        registrar.accept(EntityRegistry.FISHERGOLEM.get(), StrawGolem.createAttributes().build());
        registrar.accept(EntityRegistry.LUMBERJACKGOLEM.get(), StrawGolem.createAttributes().build());
        registrar.accept(EntityRegistry.SMELTERGOLEM.get(), StrawGolem.createAttributes().build());
        registrar.accept(EntityRegistry.EXCAVATORGOLEM.get(), StrawGolem.createAttributes().build());
        registrar.accept(EntityRegistry.COOKGOLEM.get(), StrawGolem.createAttributes().build());
    }
    private static <T extends Mob> Supplier<EntityType<T>> registerEntity(String name, EntityType.EntityFactory<T> entity, float width, float height) {
        return Constants.COMMON_PLATFORM.registerEntity(name, () -> EntityType.Builder.of(entity, MobCategory.CREATURE).sized(width, height).build(name));
    }
}
