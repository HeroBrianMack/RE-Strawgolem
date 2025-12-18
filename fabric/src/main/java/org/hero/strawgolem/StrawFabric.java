package org.hero.strawgolem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.hero.strawgolem.registry.EntityRegistry;
import org.hero.strawgolem.registry.ParticleRegistry;

public class StrawFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.
        CommonClass.init();
        EntityRegistry.registerEntityAttributes(FabricDefaultAttributeRegistry::register);
        // Use Fabric to bootstrap the Common mod.
        Constants.LOG.info("Hello Fabric world!");

    }
}
