package com.futurebackport.entity;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.Brain.Provider;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;

public final class CreakingAi {
   private static final List<SensorType<? extends Sensor<? super Creaking>>> SENSORS = List.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS);
   private static final List<MemoryModuleType<?>> MEMORIES = List.of(
      MemoryModuleType.NEAREST_LIVING_ENTITIES,
      MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
      MemoryModuleType.NEAREST_PLAYERS,
      MemoryModuleType.NEAREST_VISIBLE_PLAYER,
      MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
      MemoryModuleType.LOOK_TARGET,
      MemoryModuleType.WALK_TARGET,
      MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
      MemoryModuleType.PATH,
      MemoryModuleType.ATTACK_TARGET,
      MemoryModuleType.ATTACK_COOLING_DOWN
   );

   private CreakingAi() {
   }

   public static Provider<Creaking> brainProvider() {
      return Brain.provider(MEMORIES, SENSORS);
   }

   public static Brain<?> makeBrain(Creaking creaking, Brain<Creaking> rawBrain) {
      rawBrain.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim(0.8F) {
         protected boolean checkExtraStartConditions(ServerLevel level, Mob mob) {
            return ((Creaking)mob).canMove() && super.checkExtraStartConditions(level, mob);
         }
      }, new LookAtTargetSink(45, 90), new MoveToTargetSink()));
      rawBrain.addActivity(
         Activity.IDLE,
         10,
         ImmutableList.<BehaviorControl<? super Creaking>>of(
            StartAttacking.<Creaking>create(Creaking::isActive, mob -> mob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)),
            SetEntityLookTargetSometimes.create(8.0F, UniformInt.of(30, 60)),
            new RunOne<>(
               ImmutableList.of(
                  Pair.of(RandomStroll.stroll(0.3F), 2), Pair.of(SetWalkTargetFromLookTarget.create(0.3F, 3), 2), Pair.of(new DoNothing(30, 60), 1)
               )
            )
         )
      );
      rawBrain.addActivityWithConditions(
         Activity.FIGHT,
         ImmutableList.of(
            Pair.of(10, SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F)),
            Pair.of(11, BehaviorBuilder.triggerIf(Creaking::canMove, MeleeAttack.create(40))),
            Pair.of(12, StopAttackingIfTargetInvalid.create(target -> !isAttackTargetStillReachable(creaking, target)))
         ),
         ImmutableSet.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT))
      );
      rawBrain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      rawBrain.setDefaultActivity(Activity.IDLE);
      rawBrain.useDefaultActivity();
      return rawBrain;
   }

   private static boolean isAttackTargetStillReachable(Creaking creaking, LivingEntity target) {
      Optional<List<Player>> players = creaking.getBrain().getMemory(MemoryModuleType.NEAREST_PLAYERS);
      return target instanceof Player player
         && players.<Boolean>map(list -> list.contains(player)).orElse(false)
         && Sensor.isEntityAttackable(creaking, player)
         && creaking.hasLineOfSight(player);
   }

   public static void updateActivity(Creaking creaking) {
      if (!creaking.canMove()) {
         creaking.getBrain().useDefaultActivity();
      } else {
         creaking.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
      }
   }
}
