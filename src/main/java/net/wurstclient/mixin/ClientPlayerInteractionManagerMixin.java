/*
 * Copyright (C) 2014 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.packet.PlayerActionC2SPacket;
import net.minecraft.server.network.packet.PlayerActionC2SPacket.Action;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.wurstclient.WurstClient;
import net.wurstclient.events.BlockBreakingProgressListener.BlockBreakingProgressEvent;
import net.wurstclient.mixinterface.IClientPlayerInteractionManager;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin
	implements IClientPlayerInteractionManager
{
	@Shadow
	private MinecraftClient client;
	@Shadow
	private float currentBreakingProgress;
	@Shadow
	private boolean breakingBlock;
	
	/**
	 * blockHitDelay
	 */
	@Shadow
	private int field_3716;
	
	private boolean overrideReach;
	
	@Inject(at = {@At(value = "INVOKE",
		target = "Lnet/minecraft/client/network/ClientPlayerEntity;getEntityId()I",
		ordinal = 0)},
		method = {
			"method_2902(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z"})
	private void onPlayerDamageBlock(BlockPos blockPos_1, Direction direction_1,
		CallbackInfoReturnable<Boolean> cir)
	{
		BlockBreakingProgressEvent event =
			new BlockBreakingProgressEvent(blockPos_1, direction_1);
		WurstClient.INSTANCE.getEventManager().fire(event);
	}
	
	@Inject(at = {@At("HEAD")},
		method = {"method_21765()F"},
		cancellable = true)
	private void onGetReachDistance(CallbackInfoReturnable<Float> ci)
	{
		if(overrideReach)
			ci.setReturnValue(10F);
	}
	
	@Override
	public float getCurrentBreakingProgress()
	{
		return currentBreakingProgress;
	}
	
	@Override
	public void setBreakingBlock(boolean breakingBlock)
	{
		this.breakingBlock = breakingBlock;
	}
	
	@Override
	public ItemStack windowClick_PICKUP(int slot)
	{
		return method_2906(0, slot, 0, SlotActionType.PICKUP, client.player);
	}
	
	@Override
	public ItemStack windowClick_QUICK_MOVE(int slot)
	{
		return method_2906(0, slot, 0, SlotActionType.QUICK_MOVE,
			client.player);
	}
	
	@Override
	public ItemStack windowClick_THROW(int slot)
	{
		return method_2906(0, slot, 1, SlotActionType.THROW, client.player);
	}
	
	@Override
	public void rightClickItem()
	{
		interactItem(client.player, client.world, Hand.MAIN_HAND);
	}
	
	@Override
	public void rightClickBlock(BlockPos pos, Direction side, Vec3d hitVec)
	{
		interactBlock(client.player, client.world, Hand.MAIN_HAND,
			new BlockHitResult(hitVec, side, pos, false));
	}
	
	@Override
	public void sendPlayerActionC2SPacket(Action action, BlockPos blockPos,
		Direction direction)
	{
		method_21706(action, blockPos, direction);
	}
	
	@Override
	public void setOverrideReach(boolean overrideReach)
	{
		this.overrideReach = overrideReach;
	}
	
	@Shadow
	private void method_21706(
		PlayerActionC2SPacket.Action playerActionC2SPacket$Action_1,
		BlockPos blockPos_1, Direction direction_1)
	{
		
	}
	
	@Override
	public void setBlockHitDelay(int delay)
	{
		field_3716 = delay;
	}
	
	@Shadow
	public abstract ActionResult interactBlock(
		ClientPlayerEntity clientPlayerEntity_1, ClientWorld clientWorld_1,
		Hand hand_1, BlockHitResult blockHitResult_1);
	
	@Shadow
	public abstract ActionResult interactItem(PlayerEntity playerEntity_1,
		World world_1, Hand hand_1);
	
	@Shadow
	public abstract ItemStack method_2906(int int_1, int int_2, int int_3,
		SlotActionType slotActionType_1, PlayerEntity playerEntity_1);
}
