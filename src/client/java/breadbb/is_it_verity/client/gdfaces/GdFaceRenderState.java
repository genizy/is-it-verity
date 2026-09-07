package breadbb.is_it_verity.client.gdfaces;

import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class GdFaceRenderState extends BlockEntityRenderState {
	public final MovingBlockRenderState block = new MovingBlockRenderState();
	public boolean visible;
	public float yaw;
	public float pitch;
}
