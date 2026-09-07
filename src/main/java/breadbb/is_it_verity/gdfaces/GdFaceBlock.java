package breadbb.is_it_verity.gdfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import breadbb.is_it_verity.sphere.VerityBlock;

public class GdFaceBlock extends VerityBlock implements EntityBlock {
	public static final BooleanProperty LOOKING = BooleanProperty.create("looking");

	public static final int LOOK_EVENT = 1;
	public static final int TURN_TICKS = 5;
	public static final int RETURN_TICKS = 8;

	private final SoundEvent sound;
	private final int soundTicks;

	public GdFaceBlock(Properties settings, SoundEvent sound, int soundTicks) {
		super(settings);
		this.sound = sound;
		this.soundTicks = soundTicks;
		registerDefaultState(getStateDefinition().any().setValue(LOOKING, Boolean.FALSE));
	}

	public SoundEvent sound() {
		return sound;
	}

	public int soundTicks() {
		return soundTicks;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LOOKING);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new GdFaceBlockEntity(pos, state);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return state.getValue(LOOKING) ? RenderShape.INVISIBLE : RenderShape.MODEL;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
		level.blockEvent(pos, this, LOOK_EVENT, soundTicks);

		if (!state.getValue(LOOKING)) {
			level.setBlock(pos, state.setValue(LOOKING, Boolean.TRUE), Block.UPDATE_CLIENTS);
		}

		level.scheduleTick(pos, this, soundTicks + RETURN_TICKS + 1);
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (level.getBlockEntity(pos) instanceof GdFaceBlockEntity face
				&& level.getGameTime() < face.lookEnd()) {
			return;
		}

		if (state.getValue(LOOKING)) {
			level.setBlock(pos, state.setValue(LOOKING, Boolean.FALSE), Block.UPDATE_CLIENTS);
		}
	}

	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);

		BlockEntity blockEntity = level.getBlockEntity(pos);
		return blockEntity != null && blockEntity.triggerEvent(id, param);
	}
}
