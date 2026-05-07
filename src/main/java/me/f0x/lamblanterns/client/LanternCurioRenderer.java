package me.f0x.lamblanterns.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LanternCurioRenderer implements ICurioRenderer {
    private final Block block;

    public LanternCurioRenderer(Block block) {
        this.block = block;
    }

    // Vanilla HumanoidModel body cube: addBox(-4, 0, -2, 8, 12, 4).
    // (Slim/Alex skins use the same body; only arms differ.)
    private static final float BODY_MIN_X = -4f, BODY_MAX_X =  4f;
    private static final float BODY_MIN_Y =  0f, BODY_MAX_Y = 12f;
    private static final float BODY_MIN_Z = -2f, BODY_MAX_Z =  2f;

    // The chain attachment point inside the (hanging) lantern model, normalized 0..1.
    private static final float LANTERN_TOP = 11f / 16f;

    private static final Map<UUID, Pendulum> STATES = new ConcurrentHashMap<>();

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack, SlotContext slotContext, PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent, MultiBufferSource bufferSource,
            int packedLight, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch
    ) {
        if (!(slotContext.entity() instanceof Player player)) return;
        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?> humanoid)) return;

        Pendulum p = STATES.computeIfAbsent(player.getUUID(), u -> new Pendulum());

        poseStack.pushPose();

        // Anchor at the right-front-bottom of the body cube (the right hip / belt area).
        // Coordinate convention used: model +X is the entity's left, +Z is the entity's
        // back, +Y is down. Normalized percent: 0 = center, +1 = AABB min, -1 = AABB max,
        // values outside [-1,1] extrapolate beyond the body.
        transformToBody(poseStack, humanoid.body, /*xPct*/ 2.0f, /*yPct*/ -1.55f, /*zPct*/ -1.0f);

        // Rotate around the chain attachment of the lantern.
        poseStack.translate(0.5f, LANTERN_TOP, 0.5f);

        // Compute the chain attachment's world position so the pendulum sees real motion.
        Vec3 hipWorld = playerHipPosition(player, partialTicks);
        Vec3 swing = p.update(player, hipWorld, partialTicks);

        // Body pitch leak so the lantern stays vertical when the body tilts (sneaking).
        float xRot = (float) swing.z - humanoid.body.xRot;
        float zRot = (float) swing.x;
        poseStack.mulPose(new Quaternionf().rotationZYX(zRot, 0f, xRot));
        poseStack.translate(-0.5f, -LANTERN_TOP, -0.5f);

        BlockState bs = block.defaultBlockState().setValue(LanternBlock.HANGING, Boolean.TRUE);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                bs, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }

    private static void transformToBody(PoseStack ps, ModelPart body, float xPct, float yPct, float zPct) {
        body.translateAndRotate(ps);
        // Switch to model-pixel scale so we can address points inside the cube AABB.
        ps.scale(1f / 16f, 1f / 16f, 1f / 16f);
        ps.translate(
                Mth.lerp((-xPct + 1f) / 2f, BODY_MIN_X, BODY_MAX_X),
                Mth.lerp((-yPct + 1f) / 2f, BODY_MIN_Y, BODY_MAX_Y),
                Mth.lerp((-zPct + 1f) / 2f, BODY_MIN_Z, BODY_MAX_Z)
        );
        // Scale back up so subsequent translations are in half-block units, and flip
        // around X to undo the entity-level Y-flip so the block renders right-side-up.
        ps.scale(8f, 8f, 8f);
        ps.mulPose(Axis.XP.rotationDegrees(180f));
    }

    private static Vec3 playerHipPosition(Player player, float partialTicks) {
        // Approximate hip world position: body center, ~half player height up.
        double x = Mth.lerp(partialTicks, player.xo, player.getX());
        double y = Mth.lerp(partialTicks, player.yo, player.getY()) + player.getBbHeight() * 0.5;
        double z = Mth.lerp(partialTicks, player.zo, player.getZ());
        return new Vec3(x, y, z);
    }

    /** 2D damped pendulum (forward/back as zAngle, left/right as xAngle), driven by player motion. */
    private static final class Pendulum {
        float xAngle, xVel;
        float zAngle, zVel;
        Vec3 lastHip;
        boolean wasCrouching;

        // Tunables
        private static final float G = 9.81f;
        private static final float DAMPING = 0.98f;       // per-tick velocity decay
        private static final float HORIZONTAL_DRIVE = 50f;
        private static final float VERTICAL_DRIVE = 20f;
        private static final float CLAMP_ANGLE = Constants.PI / 3f; // 60 degrees
        private static final float CLAMP_VEL = 3f;

        Vec3 update(Player player, Vec3 newHipPos, float dt) {
            boolean crouching = player.isCrouching();
            // Kick the pendulum when the player starts sneaking, like the lantern is yanked.
            if (crouching && !wasCrouching) {
                xAngle = Constants.PI / 8f;
                zAngle = Constants.PI / 10f;
            }
            wasCrouching = crouching;

            Vec3 oldHip = lastHip != null ? lastHip : newHipPos;
            lastHip = newHipPos;

            Vec3 deltaWorld = newHipPos.subtract(oldHip);
            Vec3 deltaLocal = toPlayerLocal(deltaWorld, player.getForward());

            // Forward/back swing (around X axis from the body's POV)
            float zForce = G * (float) Math.sin(zAngle)
                    + (float) deltaLocal.z * HORIZONTAL_DRIVE * -1f
                    + (float) deltaLocal.y * VERTICAL_DRIVE   * -1f;
            zVel += -zForce * (dt / 20f);
            zVel *= DAMPING;
            zAngle += zVel * (dt / 20f);
            zAngle = Mth.clamp(zAngle, -CLAMP_ANGLE, CLAMP_ANGLE);
            zVel = Mth.clamp(zVel, -CLAMP_VEL, CLAMP_VEL);

            // Side-to-side swing (around Z axis from the body's POV)
            float xForce = G * (float) Math.sin(xAngle)
                    + (float) deltaLocal.x * HORIZONTAL_DRIVE * -1f
                    + (float) deltaLocal.y * VERTICAL_DRIVE   * -1f;
            xVel += -xForce * (dt / 20f);
            xVel *= DAMPING;
            xAngle += xVel * (dt / 20f);
            xAngle = Mth.clamp(xAngle, -CLAMP_ANGLE, CLAMP_ANGLE);
            xVel = Mth.clamp(xVel, -CLAMP_VEL, CLAMP_VEL);

            return new Vec3(
                    Math.abs(xAngle) < 0.01f ? 0f : xAngle,
                    0f,
                    Math.abs(zAngle) < 0.01f ? 0f : zAngle
            );
        }

        private static Vec3 toPlayerLocal(Vec3 worldDelta, Vec3 forward) {
            Vec3 f = forward.normalize();
            Vec3 up = new Vec3(0, 1, 0);
            Vec3 right = f.cross(up).normalize();
            up = right.cross(f).normalize();
            return new Vec3(worldDelta.dot(right), worldDelta.dot(up), worldDelta.dot(f));
        }
    }
}
