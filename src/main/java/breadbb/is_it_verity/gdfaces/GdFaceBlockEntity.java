package breadbb.is_it_verity.gdfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GdFaceBlockEntity extends BlockEntity {
	private long lookStart;
	private long lookEnd;

	public GdFaceBlockEntity(BlockPos pos, BlockState state) {
		super(GdFaceBlocks.BLOCK_ENTITY, pos, state);
	}

	public long lookEnd() {
		return lookEnd;
	}

	@Override
	public boolean triggerEvent(int id, int param) {
		if (id != GdFaceBlock.LOOK_EVENT) {
			return super.triggerEvent(id, param);
		}

		Level level = getLevel();
		lookStart = level == null ? 0L : level.getGameTime();
		lookEnd = lookStart + param + GdFaceBlock.RETURN_TICKS;
		return true;
	}

	public float weight(long time, float partialTick) {
		if (lookEnd <= lookStart) {
			return 0.0F;
		}

		float elapsed = (float) (time - lookStart) + partialTick;
		float span = (float) (lookEnd - lookStart);

		if (elapsed <= 0.0F || elapsed >= span) {
			return 0.0F;
		}

		float rise = Math.min(1.0F, elapsed / GdFaceBlock.TURN_TICKS);
		float fall = Math.min(1.0F, (span - elapsed) / GdFaceBlock.RETURN_TICKS);
		float blend = Math.min(rise, fall);

		return blend * blend * (3.0F - 2.0F * blend);
	}
}
