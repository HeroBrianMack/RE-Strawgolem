package org.hero.strawgolem;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.hero.strawgolem.registry.EntityRegistry;
import org.hero.strawgolem.registry.ParticleRegistry;

@Mod(Constants.MODID)
public final class StrawNeo {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Constants.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Constants.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, Constants.MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, Constants.MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MODID);
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Constants.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Constants.MODID);

    public StrawNeo(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ENTITIES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        ARMOR_MATERIALS.register(modEventBus);
        ITEMS.register(modEventBus);
        // Probably doing this right?
        PARTICLES.register(modEventBus);
        modEventBus.<EntityAttributeCreationEvent>addListener(event -> EntityRegistry.registerEntityAttributes(event::put));
//        modEventBus.<RegisterParticleProvidersEvent>addListener(event -> ParticleRegistry.registerParticleProv(event::put));

        CommonClass.init();
    }
}