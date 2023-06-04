package net.fabricmc.fabric.impl.content.registry;

import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.level.BlockEvent;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class TillableBlockRegistryImpl {
    private static final Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> TILLABLES = new IdentityHashMap<>();
    private static final Function<Level, Level> READ_ONLY_LEVEL = Util.memoize(ReadOnlyLevel::new);

    public static void register(Block input, Predicate<UseOnContext> usagePredicate, Consumer<UseOnContext> tillingAction) {
        TILLABLES.put(input, Pair.of(usagePredicate, tillingAction));
    }

    /*
     * Implementing this is a bit tricky given Forge completely rewamps the tool tilling system and uses
     * events instead of the general map in HoeItem.TILLABLES. Moreso it also adds a `simulate` flag which
     * we can't propagate to fabric consumers. Instead, we pull a hack and give them a NO-OP level
     * implementation to prevent modification of the real world.
     */
    public static void onBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getToolAction() == ToolActions.HOE_TILL) {
            LevelAccessor level = event.getLevel();
            BlockPos pos = event.getPos();
            Block block = level.getBlockState(pos).getBlock();
            Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair = TILLABLES.get(block);

            if (pair != null) {
                UseOnContext context = event.getContext();
                if (pair.getFirst().test(context)) {
                    if (event.isSimulated()) {
                        Level readOnlyView = READ_ONLY_LEVEL.apply(context.getLevel());
                        UseOnContext readOnlyContext = new UseOnContext(readOnlyView, context.getPlayer(), context.getHand(), context.getItemInHand(), context.getHitResult());
                        pair.getSecond().accept(readOnlyContext);
                    } else {
                        pair.getSecond().accept(context);
                        event.setFinalState(level.getBlockState(pos));
                    }
                }
            }
        }
    }
}
