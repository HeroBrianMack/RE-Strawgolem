package org.hero.strawgolem.registry;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.constant.dataticket.DataTicket;

public class DataTicketsRegistry {
    public static void init() {}
    // Boolean statuses of Straw Golem
    public static final DataTicket<Boolean> FESTIVE = DataTicket.create("golemIsFestive", Boolean.class);
    public static final DataTicket<Boolean> SHIVER = DataTicket.create("golemShiver", Boolean.class);
    public static final DataTicket<Boolean> HAT = DataTicket.create("golemHat", Boolean.class);
    public static final DataTicket<Boolean> BARREL = DataTicket.create("golemBarrel", Boolean.class);

    // Integer Health Status of Straw Golem
    public static final DataTicket<Integer> HEALTH = DataTicket.create("golemHealthStatus", Integer.class);
    // Double YRot of Straw Golem
    public static final DataTicket<Float> YROT = DataTicket.create("golemYRot", Float.class);


    // Vec3 of Straw Golem
    public static final DataTicket<Vec3> POSITION = DataTicket.create("golemPosition", Vec3.class);
    public static final DataTicket<Vec3> ANGLE = DataTicket.create("golemLookAngle", Vec3.class);


    // A special section
    public static final DataTicket<ItemStack> ITEM = DataTicket.create("golemItem", ItemStack.class);

}
