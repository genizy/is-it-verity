package breadbb.is_it_verity.obesity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import breadbb.is_it_verity.sphere.VerityBlocks;

public class ObesityBlock extends Block {
	private final int width;
	private final int height;
	private final int depth;

	public ObesityBlock(Properties settings, int size) {
		this(settings, size, size, size);
	}

	public ObesityBlock(Properties settings, int width, int height, int depth) {
		super(settings);
		this.width = width;
		this.height = height;
		this.depth = depth;
	}

	public int size() {
		return Math.max(width, Math.max(height, depth));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Level level = context.getLevel();

		for (BlockPos part : parts(context.getClickedPos())) {
			if (!level.getBlockState(part).canBeReplaced(context)) {
				return null;
			}
		}

		return defaultBlockState();
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (level.isClientSide()) {
			return;
		}

		for (BlockPos part : parts(pos)) {
			level.setBlock(part, VerityBlocks.OBESITY_FILLER.defaultBlockState()
					.setValue(ObesityFillerBlock.OFFSET_X, part.getX() - pos.getX())
					.setValue(ObesityFillerBlock.OFFSET_Y, part.getY() - pos.getY())
					.setValue(ObesityFillerBlock.OFFSET_Z, part.getZ() - pos.getZ()), 3);
		}
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
		for (BlockPos part : parts(pos)) {
			BlockState found = level.getBlockState(part);

			if (found.getBlock() instanceof ObesityFillerBlock && ObesityFillerBlock.base(found, part).equals(pos)) {
				level.removeBlock(part, false);
			}
		}
	}

	private List<BlockPos> parts(BlockPos base) {
		List<BlockPos> parts = new ArrayList<>();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < depth; z++) {
					if (x != 0 || y != 0 || z != 0) {
						parts.add(base.offset(x, y, z));
					}
				}
			}
		}

		return parts;
	}
}
