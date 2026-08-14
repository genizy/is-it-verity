package breadbb.is_it_verity.client.sphere;

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

public record SphereGeometry(String textureSlot, int rings, int segments, float radius) implements UnbakedGeometry {
	private static final float CENTER = 0.5F;

	@Override
	public QuadCollection bake(TextureSlots textures, ModelBaker baker, ModelState settings, ModelDebugName name) {
		Material material = textures.getMaterial(textureSlot);

		Material.Baked sprite = baker.materials().get(material, name);

		MutableMesh mesh = Renderer.get().mutableMesh();
		QuadEmitter emitter = mesh.emitter();
		emitter.pushTransform(ModelStateHelper.asQuadTransform(settings, baker.materials()));

		for (int ring = 0; ring < rings; ring++) {
			float theta0 = (float) (Math.PI * ring / rings);
			float theta1 = (float) (Math.PI * (ring + 1) / rings);

			for (int segment = 0; segment < segments; segment++) {
				float phi0 = (float) (2.0 * Math.PI * segment / segments);
				float phi1 = (float) (2.0 * Math.PI * (segment + 1) / segments);

				vertex(emitter, 0, theta0, phi0);
				vertex(emitter, 1, theta0, phi1);
				vertex(emitter, 2, theta1, phi1);
				vertex(emitter, 3, theta1, phi0);

				emitter.cullFace(null)
						.materialBake(sprite, MutableQuadView.BAKE_NORMALIZED)
						.emit();
			}
		}

		emitter.popTransform();
		return new MeshQuadCollection(mesh.immutableCopy());
	}

	private void vertex(QuadEmitter emitter, int index, float theta, float phi) {
		float sinTheta = (float) Math.sin(theta);
		float normalX = sinTheta * (float) Math.cos(phi);
		float normalY = (float) Math.cos(theta);
		float normalZ = sinTheta * (float) Math.sin(phi);

		emitter.pos(index, CENTER + normalX * radius, CENTER + normalY * radius, CENTER + normalZ * radius)
				.normal(index, normalX, normalY, normalZ)
				.uv(index, 1.0F - phi / (float) (2.0 * Math.PI), theta / (float) Math.PI);
	}
}
