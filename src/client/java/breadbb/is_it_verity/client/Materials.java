package breadbb.is_it_verity.client;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;

public final class Materials {
	private static Method cached;

	private Materials() {
	}

	public static Material.Baked bake(ModelBaker baker, Material material, ModelDebugName name) {
		Object materials = baker.materials();

		try {
			Method method = cached;

			if (method == null || !method.getDeclaringClass().isInstance(materials)) {
				method = resolve(materials.getClass());
				cached = method;
			}

			return (Material.Baked) method.invoke(materials, material, name);
		} catch (ReflectiveOperationException | RuntimeException failure) {
			throw new IllegalStateException("Could not bake " + material + " through "
					+ materials.getClass().getName(), failure);
		}
	}

	private static Method resolve(Class<?> owner) throws NoSuchMethodException {
		for (Class<?> type = owner; type != null; type = type.getSuperclass()) {
			if (Modifier.isPublic(type.getModifiers())) {
				Method found = lookup(type);

				if (found != null) {
					return found;
				}
			}

			for (Class<?> face : type.getInterfaces()) {
				Method found = lookup(face);

				if (found != null) {
					return found;
				}
			}
		}

		Method fallback = lookup(owner);

		if (fallback == null) {
			throw new NoSuchMethodException("No get(Material, ModelDebugName) on " + owner.getName());
		}

		fallback.setAccessible(true);
		return fallback;
	}

	private static Method lookup(Class<?> type) {
		try {
			return type.getMethod("get", Material.class, ModelDebugName.class);
		} catch (NoSuchMethodException missing) {
			for (Method method : type.getMethods()) {
				if (method.getName().equals("get") && method.getParameterCount() == 2
						&& method.getParameterTypes()[0].isAssignableFrom(Material.class)) {
					return method;
				}
			}

			return null;
		}
	}
}
