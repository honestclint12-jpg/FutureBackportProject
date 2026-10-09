package com.futurebackport.gametest;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;

/** Test helpers 1.20.1's GameTestHelper lacks (it only has a creative mock player and no per-arena entity lookup). */
final class TestPlayers {

   private TestPlayers() {
   }

   /** Like 1.21's makeMockPlayer(GameType): a player that is not added to the level. */
   static Player mock(GameTestHelper helper, GameType gameType) {
      Player player = new Player(helper.getLevel(), BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "test-mock-player")) {
         @Override
         public boolean isSpectator() {
            return gameType == GameType.SPECTATOR;
         }

         @Override
         public boolean isCreative() {
            return gameType == GameType.CREATIVE;
         }
      };
      gameType.updatePlayerAbilities(player.getAbilities());
      return player;
   }

   /**
    * Like 1.21's makeMockServerPlayerInLevel: the connection gets an EmbeddedChannel, as in 1.21. 1.20.1's own mock has
    * none, which crashes Forge's networking when the player logs in.
    */
   static ServerPlayer mockServerPlayer(GameTestHelper helper) {
      ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(UUID.randomUUID(), "test-mock-player")) {
         @Override
         public boolean isSpectator() {
            return false;
         }

         @Override
         public boolean isCreative() {
            return true;
         }
      };
      Connection connection = new Connection(PacketFlow.SERVERBOUND);
      new EmbeddedChannel(connection);
      helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player);
      return player;
   }

   /** Entities of a type inside this test's arena only (tests run side by side). */
   static <E extends Entity> List<E> entities(GameTestHelper helper, EntityType<E> type) {
      BlockPos origin = helper.absolutePos(BlockPos.ZERO);
      AABB arena = new AABB(origin, origin.offset(16, 16, 16));
      return helper.getLevel().getEntities(type, arena, Entity::isAlive);
   }
}
