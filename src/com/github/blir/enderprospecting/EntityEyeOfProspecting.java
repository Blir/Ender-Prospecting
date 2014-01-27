package com.github.blir.enderprospecting;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityEyeOfProspecting extends Entity {

	private double targetX, targetY, targetZ;
	private int ticksLived;
	private boolean shatter;
	private int id;

	public EntityEyeOfProspecting(World world) {
		super(world);
		setSize(0.25F, 0.25F);
	}

	@Override
	protected void entityInit() {
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double par1) {
		double dist = boundingBox.getAverageEdgeLength() * 256.0;
		return par1 < dist * dist;
	}

	public EntityEyeOfProspecting(World world, double x, double y, double z,
			int id) {
		super(world);
		this.setSize(0.25F, 0.25F);
		this.setPosition(x, y, z);
		this.yOffset = 0.0F;
		this.id = id;

		shatter = rand.nextInt(100) > 59;
		// flat 60% rate for now
		// shatterOrDrop();
	}

	public void setDestination(double x, double y, double z) {
		targetX = x;
		targetY = y;
		targetZ = z;
	}

	private void shatterOrDrop() {

		// determine chance of shattering based on what you're tracking

		if (id == Block.oreCoal.blockID) {
			// 20%
			shatter = this.rand.nextInt(100) > 19;
		} else if (id == Block.oreDiamond.blockID) {
			// 65%
			shatter = this.rand.nextInt(100) > 64;
		} else if (id == Block.oreEmerald.blockID) {
			// 50%
			shatter = this.rand.nextInt(100) > 49;
		} else if (id == Block.oreGold.blockID) {
			// 50%
			shatter = this.rand.nextInt(100) > 49;
		} else if (id == Block.oreIron.blockID) {
			// 35%
			shatter = this.rand.nextInt(100) > 34;
		} else if (id == Block.oreLapis.blockID) {
			// 45%
			shatter = this.rand.nextInt(100) > 44;
		} else if (id == Block.oreNetherQuartz.blockID) {
			// 55%
			shatter = this.rand.nextInt(100) > 54;
		} else if (id == Block.oreRedstone.blockID
				|| id == Block.oreRedstoneGlowing.blockID) {
			// 30%
			shatter = this.rand.nextInt(100) > 29;
		}

	}

	@SideOnly(Side.CLIENT)
	@Override
	public void setVelocity(double x, double y, double z) {
		motionX = x;
		motionY = y;
		motionZ = z;

		if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F) {
			prevRotationYaw = rotationYaw = (float) (Math.atan2(x, z) * 180.0D / Math.PI);
			prevRotationPitch = rotationPitch = (float) (Math.atan2(y,
					Math.sqrt(x * x + z * z)) * 180.0D / Math.PI);
		}
	}

	@Override
	public void onUpdate() {
		lastTickPosX = posX;
		lastTickPosY = posY;
		lastTickPosZ = posZ;
		super.onUpdate();
		posX += motionX;
		posY += motionY;
		posZ += motionZ;
		double distanceMoved = Math.sqrt(motionX * motionX + motionZ * motionZ);
		rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);

		// normalize the angles

		for (rotationPitch = (float) (Math.atan2(motionY,
				(double) distanceMoved) * 180.0D / Math.PI); rotationPitch
				- prevRotationPitch < -180.0F; prevRotationPitch -= 360.0F) {
			;
		}

		while (rotationPitch - prevRotationPitch >= 180.0F) {
			prevRotationPitch += 360.0F;
		}

		while (rotationYaw - prevRotationYaw < -180.0F) {
			prevRotationYaw -= 360.0F;
		}

		while (rotationYaw - prevRotationYaw >= 180.0F) {
			prevRotationYaw += 360.0F;
		}

		rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch)
				* 0.2F;
		rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;

		if (!worldObj.isRemote) {
			double dx = targetX - posX;
			double dz = targetZ - posZ;
			double distAway = Math.sqrt(dx * dx + dz * dz);
			double f2 = Math.atan2(dz, dx);
			double d2 = distanceMoved + (distAway - distanceMoved) * 0.0025D;

			if (distAway < 1.0F) {
				d2 *= 0.8D;
				motionY *= 0.8D;
			}

			motionX = Math.cos(f2) * d2;
			motionZ = Math.sin(f2) * d2;

			if (posY < targetY) {
				motionY += (1.0 - motionY) * 0.014999999664723873;
			} else {
				motionY += (-1.0 - motionY) * 0.014999999664723873;
			}
		}

		double f3 = 0.25F;

		if (isInWater()) {
			for (int i = 0; i < 4; i++) {
				worldObj.spawnParticle("bubble", posX - motionX * f3, posY
						- motionY * f3, posZ - motionZ * f3, motionX, motionY,
						motionZ);
			}
		} else {
			worldObj.spawnParticle("portal",
					posX - motionX * f3 + rand.nextDouble() * 0.6 - 0.3, posY
							- motionY * f3 - 0.5,
					posZ - motionZ * f3 + rand.nextDouble() * 0.6 - 0.3,
					motionX, motionY, motionZ);
		}

		if (!worldObj.isRemote) {
			setPosition(posX, posY, posZ);
			ticksLived++;

			if (ticksLived > 80) {
				setDead();

				if (shatter) {
					worldObj.spawnEntityInWorld(new EntityItem(worldObj, posX,
							posY, posZ, new ItemStack(
									EnderProspecting.prospector, 1, id)));
				} else {
					worldObj.playAuxSFX(2003, (int) Math.round(posX),
							(int) Math.round(posY), (int) Math.round(posZ), 0);
				}
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getShadowSize() {
		return 0.0F;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getBrightness(float par1) {
		// how bright is 1?
		return 1.0F;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getBrightnessForRender(float par1) {
		// who determines this silly number?
		return 15728880;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean canAttackWithItem() {
		return false;
	}
}
