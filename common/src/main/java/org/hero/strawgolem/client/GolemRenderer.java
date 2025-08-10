package org.hero.strawgolem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.hero.strawgolem.golem.StrawGolem;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.renderer.specialty.DynamicGeoEntityRenderer;

public class GolemRenderer extends DynamicGeoEntityRenderer<StrawGolem> {

    public GolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GolemModel());
        System.out.println("HERORENDER");
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
        getGeoModel().getAnimationProcessor().getBone("barrel").setHidden(!animatable.hasBarrel());
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    @Override
    public void actuallyRender(PoseStack poseStack, StrawGolem animatable, BakedGeoModel model, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {

        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
}
