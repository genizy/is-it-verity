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
	private final int size;

	public ObesityBlock(Properties settings, int size) {
		super(settings);
		this.size = size;
	}

	public int size() {
		return size;
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

		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				for (int z = 0; z < size; z++) {
					if (x != 0 || y != 0 || z != 0) {
						parts.add(base.offset(x, y, z));
					}
				}
			}
		}

		return parts;
	}
}
