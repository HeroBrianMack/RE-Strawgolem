package org.hero.strawgolem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import java.util.List;

import static org.hero.strawgolem.registry.DataTicketsRegistry.*;

public class GolemRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<StrawGolem, R> {

    public GolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GolemModel());

        addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            // Name of the item bone that displays Straw Golem's held item.
            private final String itemBone = "item";

            @Override
            protected List<BlockAndItemGeoLayer.RenderData<R>> getRelevantBones(R renderState, BakedGeoModel model) {
                return List.of(renderDataForGolem(this.itemBone));
            }

            private static <R extends GeoRenderState> BlockAndItemGeoLayer.RenderData<R> renderDataForGolem(String boneName) {
                ItemDisplayContext context = ItemDisplayContext.NONE;
                return new BlockAndItemGeoLayer.RenderData<>(boneName, context, (bone, renderState2) ->
                        Either.left(renderState2.getGeckolibData(ITEM)));
            }

            @Override
            public void addRenderData(StrawGolem animatable, Void relatedObject, R renderState) {
                var equipment = animatable.getMainHandItem();
                renderState.addGeckolibData(ITEM, equipment);
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, ItemDisplayContext displayContext, R renderState, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
                // This is essentially undoing
                if (stack == renderState.getGeckolibData(ITEM)) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(0f));
                    poseStack.scale(0.5f, 0.5f, 0.5f);
                }
                // Should be this in super:
                // Minecraft.getInstance().getItemRenderer().renderStatic(stack, displayContext, packedLight, packedOverlay, poseStack,
                // bufferSource, ClientUtil.getLevel(),
                // ((Long)renderState.getGeckolibData(DataTickets.ANIMATABLE_INSTANCE_ID)).intValue());
                super.renderStackForBone(poseStack, bone, stack, displayContext, renderState, bufferSource, packedLight, packedOverlay);
            }
        });
    }

    @Override
    public void addRenderData(StrawGolem animatable, Void relatedObject, R renderState) {
        // Adding relevant data for rendering here.
        renderState.addGeckolibData(FESTIVE, animatable.isFestive());
        renderState.addGeckolibData(SHIVER, animatable.shouldShiver());
        renderState.addGeckolibData(HAT, animatable.hasHat());
        renderState.addGeckolibData(BARREL, animatable.hasBarrel());

        renderState.addGeckolibData(HEALTH, animatable.healthStatus());
        renderState.addGeckolibData(YROT, animatable.getYRot());

        renderState.addGeckolibData(POSITION, animatable.position());
        renderState.addGeckolibData(ANGLE, animatable.getLookAngle());
        renderState.addGeckolibData(ITEM, animatable.getMainHandItem());
    }

    @Override
    public void preRender(R renderState, PoseStack poseStack, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, int packedLight, int packedOverlay, int renderColor) {
        // I'm going to not check for null here, it should be not possible for nulls to occur.
        final var hat = renderState.getGeckolibData(HAT);
        final var festive = renderState.getGeckolibData(FESTIVE);
        final var barrel = renderState.getGeckolibData(BARREL);
        getGeoModel().getAnimationProcessor().getBone("hat").setHidden(!hat);
        getGeoModel().getAnimationProcessor().getBone("dynamicsnow").setHidden(!festive);
        getGeoModel().getAnimationProcessor().getBone("snow").setHidden(!festive || !hat);
        getGeoModel().getAnimationProcessor().getBone("barrel").setHidden(!barrel);

        GeoBone bon = getGeoModel().getAnimationProcessor().getBone("locator");
        if (bon != null) bon.getLocalPosition();

        super.preRender(renderState, poseStack, model, bufferSource, buffer, isReRender, packedLight, packedOverlay, renderColor);
    }

    @Override
    public void actuallyRender(R renderState, PoseStack poseStack, BakedGeoModel model, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, int packedLight, int packedOverlay, int renderColor) {
        getGeoModel().getAnimationProcessor().getBone("locator");
        if (Constants.Golem.shiver && renderState.getGeckolibData(SHIVER)) {
            // Trying out just using Math.random here.
            var random = RandomSource.create();
            double deltaX = random.nextDouble() * 0.02;
            double deltaZ = random.nextDouble()  * 0.02;
            poseStack.translate(deltaX, 0, deltaZ);
        }
        super.actuallyRender(renderState, poseStack, model, renderType, bufferSource, buffer, isReRender, packedLight, packedOverlay, renderColor);
    }

    @Override
    public void renderFinal(R renderState, PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, int packedOverlay, int renderColor) {
        super.renderFinal(renderState, poseStack, model, bufferSource, buffer, packedLight, packedOverlay, renderColor);
    }
}
