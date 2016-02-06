package com.choonster.testmod3.pigspawner;

import com.choonster.testmod3.api.pigspawner.IPigSpawner;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.world.World;

/**
 * Base implementation of {@link IPigSpawner}.
 * <p>
 * {@link #spawnPig} is implemented here instead of as a default method so other implementations can add their own
 * functionality without having to rewrite the pig spawning themselves (you can't call a default method from its override).
 *
 * @author Choonster
 */
public abstract class PigSpawnerBase implements IPigSpawner {
	/**
	 * Can a pig be spawned at the specified position?
	 *
	 * @param world The world
	 * @param x     The x coordinate
	 * @param y     The y coordinate
	 * @param z     The z coordinate
	 * @return Can a pig be spawned?
	 */
	@Override
	public boolean spawnPig(World world, double x, double y, double z) {
		EntityPig pig = new EntityPig(world);
		pig.setPosition(x, y, z);
		return world.spawnEntityInWorld(pig);
	}
}
