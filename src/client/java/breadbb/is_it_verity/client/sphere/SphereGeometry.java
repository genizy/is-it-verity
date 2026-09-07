package breadbb.is_it_verity.client.sphere;

import java.util.List;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.MeshQuadCollection;
import net.fabricmc.fabric.api.client.renderer.v1.model.ModelStateHelper;

public record SphereGeometry(String textureSlot, String hornSlot, int rings, int segments, float radius,
		float outline, List<SphereGeometry.Horn> horns) implements UnbakedGeometry {
	public record Horn(float around, float tilt, float aim, float length, float width) {
	}

	private static final float CENTER = 0.5F;
	private static final int OUTLINE_COLOR = 0xFF000000;
	private static final int HORN_COLOR = 0xFFFFFFFF;
	private static final int HORN_SIDES = 10;

	@Override
	public QuadCollection bake(TextureSlots textures, ModelBaker baker, ModelState settings, ModelDebugName name) {
		Material material = textures.getMaterial(textureSlot);

		Material.Baked sprite = baker.materials().get(material, name);

		Material hornMaterial = textures.getMaterial(hornSlot);
		Material.Baked hornSprite = hornMaterial == null ? sprite : baker.materials().get(hornMaterial, name);

		MutableMesh mesh = Renderer.get().mutableMesh();
		QuadEmitter emitter = mesh.emitter();
		emitter.pushTransform(ModelStateHelper.asQuadTransform(settings, baker.materials()));

		float shell = outline > 0.0F ? Math.max(0.05F, radius - outline) : radius;

		build(emitter, sprite, shell, false);

		for (Horn horn : horns) {
			spike(emitter, hornSprite, horn, shell, 0.0F, HORN_COLOR, false);
		}

		if (outline > 0.0F) {
			build(emitter, sprite, radius, true);

			for (Horn horn : horns) {
				spike(emitter, hornSprite, horn, shell, outline, OUTLINE_COLOR, true);
			}
		}

		emitter.popTransform();
		return new MeshQuadCollection(mesh.immutableCopy());
	}

	private void build(QuadEmitter emitter, Material.Baked sprite, float scale, boolean inward) {
		for (int ring = 0; ring < rings; ring++) {
			float theta0 = (float) (Math.PI * ring / rings);
			float theta1 = (float) (Math.PI * (ring + 1) / rings);

			for (int segment = 0; segment < segments; segment++) {
				float phi0 = (float) (2.0 * Math.PI * segment / segments);
				float phi1 = (float) (2.0 * Math.PI * (segment + 1) / segments);

				if (inward) {
					vertex(emitter, 0, theta0, phi0, scale, true);
					vertex(emitter, 1, theta1, phi0, scale, true);
					vertex(emitter, 2, theta1, phi1, scale, true);
					vertex(emitter, 3, theta0, phi1, scale, true);
					emitter.color(OUTLINE_COLOR, OUTLINE_COLOR, OUTLINE_COLOR, OUTLINE_COLOR);
				} else {
					vertex(emitter, 0, theta0, phi0, scale, false);
					vertex(emitter, 1, theta0, phi1, scale, false);
					vertex(emitter, 2, theta1, phi1, scale, false);
					vertex(emitter, 3, theta1, phi0, scale, false);
				}

				emitter.cullFace(null)
						.materialBake(sprite, MutableQuadView.BAKE_NORMALIZED)
						.emit();
			}
		}
	}

	private void vertex(QuadEmitter emitter, int index, float theta, float phi, float scale, boolean inward) {
		float sinTheta = (float) Math.sin(theta);
		float normalX = sinTheta * (float) Math.cos(phi);
		float normalY = (float) Math.cos(theta);
		float normalZ = sinTheta * (float) Math.sin(phi);
		float facing = inward ? -1.0F : 1.0F;

		emitter.pos(index, CENTER + normalX * scale, CENTER + normalY * scale, CENTER + normalZ * scale)
				.normal(index, normalX * facing, normalY * facing, normalZ * facing)
				.uv(index, 1.0F - phi / (float) (2.0 * Math.PI), theta / (float) Math.PI);
	}

	private void spike(QuadEmitter emitter, Material.Baked sprite, Horn horn, float shell, float grow,
			int color, boolean inward) {
		double around = Math.toRadians(horn.around());
		double tilt = Math.toRadians(horn.tilt());
		double aim = Math.toRadians(horn.aim());

		float seatX = (float) -Math.sin(tilt);
		float seatY = (float) (Math.cos(tilt) * Math.sin(around));
		float seatZ = (float) (-Math.cos(tilt) * Math.cos(around));

		float axisX = (float) -Math.sin(aim);
		float axisY = (float) (Math.cos(aim) * Math.sin(around));
		float axisZ = (float) (-Math.cos(aim) * Math.cos(around));

		float sideX = 0.0F;
		float sideY = axisZ;
		float sideZ = -axisY;
		float sideLength = (float) Math.sqrt(sideY * sideY + sideZ * sideZ);

		if (sideLength < 0.001F) {
			sideX = 0.0F;
			sideY = 1.0F;
			sideZ = 0.0F;
		} else {
			sideY /= sideLength;
			sideZ /= sideLength;
		}

		float upX = sideY * axisZ - sideZ * axisY;
		float upY = sideZ * axisX - sideX * axisZ;
		float upZ = sideX * axisY - sideY * axisX;

		float width = horn.width() + grow;
		float root = shell * 0.85F;
		float stretch = horn.length() + grow;

		float baseX = CENTER + seatX * root;
		float baseY = CENTER + seatY * root;
		float baseZ = CENTER + seatZ * root;

		float tipX = CENTER + seatX * shell + axisX * stretch;
		float tipY = CENTER + seatY * shell + axisY * stretch;
		float tipZ = CENTER + seatZ * shell + axisZ * stretch;

		float height = (float) Math.sqrt((tipX - baseX) * (tipX - baseX)
				+ (tipY - baseY) * (tipY - baseY)
				+ (tipZ - baseZ) * (tipZ - baseZ));

		for (int side = 0; side < HORN_SIDES; side++) {
			float first = (float) (2.0 * Math.PI * side / HORN_SIDES);
			float second = (float) (2.0 * Math.PI * (side + 1) / HORN_SIDES);

			emitter.pos(0, tipX, tipY, tipZ);
			emitter.pos(1, tipX, tipY, tipZ);

			float facing = inward ? -1.0F : 1.0F;

			if (inward) {
				ring(emitter, 2, first, baseX, baseY, baseZ, width, axisX, axisY, axisZ, sideX, sideY, sideZ, upX, upY, upZ, height, facing);
				ring(emitter, 3, second, baseX, baseY, baseZ, width, axisX, axisY, axisZ, sideX, sideY, sideZ, upX, upY, upZ, height, facing);
			} else {
				ring(emitter, 2, second, baseX, baseY, baseZ, width, axisX, axisY, axisZ, sideX, sideY, sideZ, upX, upY, upZ, height, facing);
				ring(emitter, 3, first, baseX, baseY, baseZ, width, axisX, axisY, axisZ, sideX, sideY, sideZ, upX, upY, upZ, height, facing);
			}

			emitter.normal(0, axisX * facing, axisY * facing, axisZ * facing);
			emitter.normal(1, axisX * facing, axisY * facing, axisZ * facing);
			emitter.uv(0, 0.5F, 0.0F);
			emitter.uv(1, 0.5F, 0.0F);
			emitter.color(color, color, color, color);

			emitter.cullFace(null)
					.materialBake(sprite, MutableQuadView.BAKE_NORMALIZED)
					.emit();
		}
	}

	private void ring(QuadEmitter emitter, int index, float angle, float baseX, float baseY, float baseZ,
			float width, float axisX, float axisY, float axisZ, float sideX, float sideY, float sideZ,
			float upX, float upY, float upZ, float height, float facing) {
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);

		float outX = sideX * cos + upX * sin;
		float outY = sideY * cos + upY * sin;
		float outZ = sideZ * cos + upZ * sin;

		float normalX = outX * height + axisX * width;
		float normalY = outY * height + axisY * width;
		float normalZ = outZ * height + axisZ * width;
		float length = (float) Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);

		if (length > 0.0001F) {
			normalX /= length;
			normalY /= length;
			normalZ /= length;
		}

		emitter.pos(index, baseX + outX * width, baseY + outY * width, baseZ + outZ * width)
				.normal(index, normalX * facing, normalY * facing, normalZ * facing)
				.uv(index, angle / (float) (2.0 * Math.PI), 1.0F);
	}
}
