package org.hero.strawgolem.registry;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class EntityRegistry {
    public static void init() {}

    public static final Supplier<EntityType<StrawGolem>> STRAWGOLEM = registerEntity("strawgolem", StrawGolem::new, 0.6f, 0.9f);

    public static void registerEntityAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier> registrar) {
        registrar.accept(EntityRegistry.STRAWGOLEM.get(), StrawGolem.createAttributes().build());
    }
    private static <T extends Mob> Supplier<EntityType<T>> registerEntity(String name, EntityType.EntityFactory<T> entity, float width, float height) {
        return Constants.COMMON_PLATFORM.registerEntity(name, () -> EntityType.Builder.of(entity, MobCategory.CREATURE).sized(width, height).build(name));
    }
}
