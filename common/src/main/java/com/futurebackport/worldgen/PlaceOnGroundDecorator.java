package com.futurebackport.worldgen;

import com.futurebackport.registry.ModWorldgen;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator.Context;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class PlaceOnGroundDecorator extends TreeDecorator {
   public static final MapCodec<PlaceOnGroundDecorator> CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("tries", 128).forGetter(d -> d.tries),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("radius", 2).forGetter(d -> d.radius),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("height", 1).forGetter(d -> d.height),
            BlockStateProvider.CODEC.fieldOf("block_state_provider").forGetter(d -> d.blockStateProvider)
         )
         .apply(i, PlaceOnGroundDecorator::new)
   );
   private final int tries;
   private final int radius;
   private final int height;
   private final BlockStateProvider blockStateProvider;

   public PlaceOnGroundDecorator(int tries, int radius, int height, BlockStateProvider blockStateProvider) {
      this.tries = tries;
      this.radius = radius;
      this.height = height;
      this.blockStateProvider = blockStateProvider;
   }

   protected TreeDecoratorType<?> type() {
      return (TreeDecoratorType<?>)ModWorldgen.PLACE_ON_GROUND.get();
   }

   private static List<BlockPos> lowestTrunkOrRoots(Context context) {
      List<BlockPos> blocks = new ArrayList<>();
      List<BlockPos> roots = context.roots();
      List<BlockPos> logs = context.logs();
      if (roots.isEmpty()) {
         blocks.addAll(logs);
      } else if (!logs.isEmpty() && roots.get(0).getY() == logs.get(0).getY()) {
         blocks.addAll(logs);
         blocks.addAll(roots);
      } else {
         blocks.addAll(roots);
      }

      return blocks;
   }

   public void place(Context context) {
      List<BlockPos> positions = lowestTrunkOrRoots(context);
      if (!positions.isEmpty()) {
         BlockPos origin = positions.get(0);
         int minY = origin.getY();
         int minX = origin.getX();
         int maxX = origin.getX();
         int minZ = origin.getZ();
         int maxZ = origin.getZ();

         for (BlockPos pos : positions) {
            if (pos.getY() == minY) {
               minX = Math.min(minX, pos.getX());
               maxX = Math.max(maxX, pos.getX());
               minZ = Math.min(minZ, pos.getZ());
               maxZ = Math.max(maxZ, pos.getZ());
            }
         }

         RandomSource random = context.random();
         BoundingBox box = new BoundingBox(minX, minY, minZ, maxX, minY, maxZ).inflatedBy(this.radius, this.height, this.radius);
         MutableBlockPos posx = new MutableBlockPos();

         for (int i = 0; i < this.tries; i++) {
            posx.set(
               random.nextIntBetweenInclusive(box.minX(), box.maxX()),
               random.nextIntBetweenInclusive(box.minY(), box.maxY()),
               random.nextIntBetweenInclusive(box.minZ(), box.maxZ())
            );
            BlockPos above = posx.above();
            if (context.level().isStateAtPosition(above, state -> state.isAir() || state.is(Blocks.VINE))
               && context.level().isStateAtPosition(posx, state -> state.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO))
               && context.level().getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, posx).getY() <= above.getY()) {
               context.setBlock(above, this.blockStateProvider.getState(random, above));
            }
         }
      }
   }
}
