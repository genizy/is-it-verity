package breadbb.is_it_verity.client.gdfaces;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import breadbb.is_it_verity.gdfaces.GdFaceBlock;
import breadbb.is_it_verity.gdfaces.GdFaceBlockEntity;

public class GdFaceRenderer implements BlockEntityRenderer<GdFaceBlockEntity, GdFaceRenderState> {
	private static final float PIVOT = 0.5F;

	public GdFaceRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public GdFaceRenderState createRenderState() {
		return new GdFaceRenderState();
	}

	@Override
	public void extractRenderState(GdFaceBlockEntity face, GdFaceRenderState state, float partialTick,
			Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(face, state, partialTick, cameraPos, crumbling);

		state.visible = false;
		state.yaw = 0.0F;
		state.pitch = 0.0F;

		BlockState blockState = face.getBlockState();
		Level level = face.getLevel();

		if (!(level instanceof ClientLevel client)
				|| !blockState.hasProperty(GdFaceBlock.LOOKING)
				|| !blockState.getValue(GdFaceBlock.LOOKING)) {
			return;
		}

		BlockPos pos = face.getBlockPos();

		state.block.randomSeedPos = pos;
		state.block.blockPos = pos;
		state.block.blockState = blockState.setValue(GdFaceBlock.LOOKING, Boolean.FALSE);
		state.block.biome = client.getBiome(pos);
		state.block.cardinalLighting = client.cardinalLighting();
		state.block.lightEngine = client.getLightEngine();
		state.visible = true;

		float weight = face.weight(client.getGameTime(), partialTick);

		if (weight <= 0.0F) {
			return;
		}

		Entity viewer = Minecraft.getInstance().getCameraEntity();

		if (viewer == null) {
			viewer = Minecraft.getInstance().player;
		}

		if (viewer == null) {
			return;
		}

		Vec3 eye = viewer.getEyePosition(partialTick);
		double toX = eye.x - (pos.getX() + 0.5);
		double toY = eye.y - (pos.getY() + 0.5);
		double toZ = eye.z - (pos.getZ() + 0.5);
		double flat = Math.sqrt(toX * toX + toZ * toZ);

		if (flat < 1.0E-4 && Math.abs(toY) < 1.0E-4) {
			return;
		}

		state.yaw = (float) Math.atan2(toZ, -toX) * weight;
		state.pitch = (float) -Math.atan2(toY, flat) * weight;
	}

	@Override
	public void submit(GdFaceRenderState state, PoseStack pose, SubmitNodeCollector collector,
			CameraRenderState camera) {
		if (!state.visible) {
			return;
		}

		if (state.yaw == 0.0F && state.pitch == 0.0F) {
			collector.submitMovingBlock(pose, state.block);
			return;
		}

		Quaternionf spin = Axis.YP.rotation(state.yaw);
		spin.mul(Axis.ZP.rotation(state.pitch));

		pose.pushPose();
		pose.rotateAround(spin, PIVOT, PIVOT, PIVOT);
		collector.submitMovingBlock(pose, state.block);
		pose.popPose();
	}
}
