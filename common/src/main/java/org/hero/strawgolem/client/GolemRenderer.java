package org.hero.strawgolem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.hero.strawgolem.client.particle.SimplerParticleType;
import org.hero.strawgolem.golem.StrawGolem;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.renderer.specialty.DynamicGeoEntityRenderer;
import static org.hero.strawgolem.registry.ParticleRegistry.snow;
import static org.hero.strawgolem.registry.ParticleRegistry.snowfall;

public class GolemRenderer extends DynamicGeoEntityRenderer<StrawGolem> {

    public GolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GolemModel());
        addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, StrawGolem golem) {
                // Retrieve the items in the golem's hands for the relevant bone
                if (bone.getName().equals("item")) {
                    return golem.getItemBySlot(EquipmentSlot.MAINHAND);
                } else {
                    return null;
                }
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, StrawGolem golem, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                if (stack == golem.getItemBySlot(EquipmentSlot.MAINHAND)) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(0f));
                    poseStack.scale(0.5f, 0.5f, 0.5f);
                }
                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
    }

    @Override
    public void preRender(PoseStack poseStack, StrawGolem animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        getGeoModel().getAnimationProcessor().getBone("hat").setHidden(!animatable.hasHat());
        getGeoModel().getAnimationProcessor().getBone("dynamicsnow").setHidden(!animatable.isFestive());
        getGeoModel().getAnimationProcessor().getBone("snow").setHidden(!animatable.isFestive() || !animatable.hasHat());
        getGeoModel().getAnimationProcessor().getBone("barrel").setHidden(!animatable.hasBarrel());

        GeoBone bon = getGeoModel().getAnimationProcessor().getBone("locator");
        if (bon != null) bon.getLocalPosition();
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

    }

    @Override
    public void actuallyRender(PoseStack poseStack, StrawGolem animatable, BakedGeoModel model, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        getGeoModel().getAnimationProcessor().getBone("locator");

        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    @Override
    public void renderFinal(PoseStack poseStack, StrawGolem animatable, BakedGeoModel model, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (animatable.createSnow) {
            animatable.createSnow = false;
            spawnParticleAtLocator(animatable, "locator", (SimplerParticleType) snow.get(), (SimplerParticleType) snowfall.get(), poseStack);
        }
        super.renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, colour);
    }


    public void spawnParticleAtLocator(StrawGolem golem, String locatorName, SimplerParticleType snow, SimplerParticleType snowfall, PoseStack poseStack) {
        if (!golem.level().isClientSide) return;
        GeoBone bon = getGeoModel().getAnimationProcessor().getBone(locatorName);
        if (bon == null) return;
        // Convert to world coordinates
        Vec3 pos = golem.position().add(new Vec3(-bon.getModelPosition().x / 16.0, bon.getModelPosition().y / 16.0, bon.getModelPosition().z / 16.0).yRot((float) Math.toRadians(-golem.getYRot() + 180)));
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;
        double speed = 1.0;
        Vec3 lookAngle = golem.getLookAngle();
        // Just to make the particles have the correct movement
        double velX = lookAngle.x * speed;
        double velZ = lookAngle.z * speed;
        if (golem.level().isClientSide) {
            for (int i = 0; i < 3; i++) {
                // Just a note: 0.4 is spawn diameter
                double offsetX = (golem.level().random.nextDouble() - 0.5) * 0.4;
                double offsetZ = (golem.level().random.nextDouble() - 0.5) * 0.4;
                Minecraft.getInstance().particleEngine.createParticle(snowfall, x + offsetX, y, z + offsetZ, velX * 2, 0, velZ * 2);
            }
            for (int i = 0; i < 100; i++) {
                // Just a note: 0.4 is spawn diameter
                double offsetX = (golem.level().random.nextDouble() - 0.5) * 0.4;
                double offsetZ = (golem.level().random.nextDouble() - 0.5) * 0.4;
                Minecraft.getInstance().particleEngine.createParticle(snow, x + offsetX, y, z + offsetZ, velX, 0, velZ);

            }
        }
    }
}
