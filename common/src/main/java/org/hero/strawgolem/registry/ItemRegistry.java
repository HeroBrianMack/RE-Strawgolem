package org.hero.strawgolem.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.platform.Services;

import java.util.function.Supplier;

// ToDo:
// Add Straw Hat (and functionality)
// Possible routes:
// Make other hats for other "roles"
// ^^ Addition: Remove default golem farming, and require straw hat to farm
public class ItemRegistry {
    public static void init() {
        Services.PLATFORM.registerCreativeModeTab("strawgolem_tab", () ->
                Services.PLATFORM.newCreativeTabBuilder()
                        .title(Component.translatable("itemGroup.strawgolem.tab"))
                        .icon(() -> new ItemStack(ItemRegistry.STRAW_HAT.get()))
                        .displayItems((parameters, output) ->
                                output.accept(ItemRegistry.STRAW_HAT.get()))
                        .build()
        );
    }
    public static final Supplier<Item> STRAW_HAT = registerItem("straw_hat", () -> new Item(new Item.Properties()
            .stacksTo(1)));
    private static <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item) {
        return Constants.COMMON_PLATFORM.registerItem(name, item);
    }

}
