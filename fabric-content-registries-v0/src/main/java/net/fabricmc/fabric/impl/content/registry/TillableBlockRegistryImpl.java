package net.fabricmc.fabric.impl.content.registry;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.level.BlockEvent;

import net.minecraft.block.Block;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class TillableBlockRegistryImpl {
    private static final Map<Block, Pair<Predicate<ItemUsageContext>, Consumer<ItemUsageContext>>> TILLABLES = new IdentityHashMap<>();
    private static final Function<World, World> READ_ONLY_LEVEL = Util.memoize(ReadOnlyWorld::new);

    public static void register(Block input, Predicate<ItemUsageContext> usagePredicate, Consumer<ItemUsageContext> tillingAction) {
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
            WorldAccess level = event.getLevel();
            BlockPos pos = event.getPos();
            Block block = level.getBlockState(pos).getBlock();
            Pair<Predicate<ItemUsageContext>, Consumer<ItemUsageContext>> pair = TILLABLES.get(block);

            if (pair != null) {
				ItemUsageContext context = event.getContext();
                if (pair.getFirst().test(context)) {
                    if (event.isSimulated()) {
                        World readOnlyView = READ_ONLY_LEVEL.apply(context.getWorld());
						ItemUsageContext readOnlyContext = new ItemUsageContext(readOnlyView, context.getPlayer(), context.getHand(), context.getStack(), context.getHitResult());
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
