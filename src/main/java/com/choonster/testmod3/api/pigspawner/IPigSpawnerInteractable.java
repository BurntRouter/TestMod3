package com.choonster.testmod3.api.pigspawner;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Something that can interacted with by an {@link IPigSpawner}.
 *
 * @author Choonster
 */
public interface IPigSpawnerInteractable {

	/**
	 * Interact with the {@link IPigSpawner}. Only called on the server.
	 *
	 * @param pigSpawner     The IPigSpawner
	 * @param world          The World
	 * @param pos            The position of this object
	 * @param iCommandSender The ICommandSender that caused the interaction, if any
	 * @return {@code true} to prevent the default action of the IPigSpawner
	 */
	boolean interact(IPigSpawner pigSpawner, World world, BlockPos pos, Optional<ICommandSender> iCommandSender);
}
