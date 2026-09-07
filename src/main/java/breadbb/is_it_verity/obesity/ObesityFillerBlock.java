package breadbb.is_it_verity.obesity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class ObesityFillerBlock extends Block {
	public static final IntegerProperty OFFSET_X = IntegerProperty.create("offset_x", 0, 4);
	public static final IntegerProperty OFFSET_Y = IntegerProperty.create("offset_y", 0, 4);
	public static final IntegerProperty OFFSET_Z = IntegerProperty.create("offset_z", 0, 4);

	public ObesityFillerBlock(Properties settings) {
		super(settings);
		registerDefaultState(stateDefinition.any()
				.setValue(OFFSET_X, 0)
				.setValue(OFFSET_Y, 0)
				.setValue(OFFSET_Z, 0));
	}

	public static BlockPos base(BlockState state, BlockPos pos) {
		return pos.offset(-state.getValue(OFFSET_X), -state.getValue(OFFSET_Y), -state.getValue(OFFSET_Z));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(OFFSET_X, OFFSET_Y, OFFSET_Z);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && player.isCreative()) {
			BlockPos base = base(state, pos);

			if (level.getBlockState(base).getBlock() instanceof ObesityBlock) {
				level.removeBlock(base, false);
			}
		}

		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
		BlockPos base = base(state, pos);

		if (level.getBlockState(base).getBlock() instanceof ObesityBlock) {
			level.destroyBlock(base, true, null, 512);
		}
	}
}
