package com.github.blir.enderprospecting.entity;

import static com.github.blir.enderprospecting.EnderProspecting.*;

import com.github.blir.enderprospecting.EnderProspecting;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityEyeOfProspecting extends Entity implements IEntityAdditionalSpawnData
{
	private String oreName;
    private double targetX, targetY, targetZ;
    private int ticksLived;
    private boolean shouldShatter;

    public EntityEyeOfProspecting(World world)
    {
        super(world);
        setSize(0.25F, 0.25F);
    }
    
    public EntityEyeOfProspecting(World world, double x, double y, double z, String oreName, BlockPos target)
    {
        super(world);
        ticksLived = 0;
        setSize(0.25F, 0.25F);
        setPosition(x, y, z);
        this.oreName = oreName;
        targetX = target.getX() + 0.5;
        targetY = target.getY() + 0.5;
        targetZ = target.getZ() + 0.5;
	    ticksLived = 0;
	    shouldShatter = rand.nextInt(5) == 0;
    }

    public ItemStack createItemEyeOfProspecting() {
    	return createProspectorEyeForOre(oreName);
    }
    
    @Override
    protected void entityInit()
    {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        double d0 = getEntityBoundingBox().getAverageEdgeLength() * 4.0D;
        if (Double.isNaN(d0))
            d0 = 4.0D;
        d0 = d0 * 64.0D;
        return distance < d0 * d0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(double x, double y, double z)
    {
        motionX = x;
        motionY = y;
        motionZ = z;
        if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt(x * x + z * z);
            rotationYaw = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
            rotationPitch = (float)(MathHelper.atan2(y, (double)f) * (180D / Math.PI));
            prevRotationYaw = rotationYaw;
            prevRotationPitch = rotationPitch;
        }
    }

    private boolean tooCloseToSurface;
    private double tcMotX, tcMotY, tcMotZ;
    
    @Override
    public void onUpdate()
    {
        lastTickPosX = posX;
        lastTickPosY = posY;
        lastTickPosZ = posZ;
        super.onUpdate();
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        float f = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
        this.rotationYaw = (float)(MathHelper.atan2(motionX, motionZ) * (180D / Math.PI));

        for (
        		rotationPitch = (float)(MathHelper.atan2(motionY, (double)f) * (180D / Math.PI));
        		rotationPitch - prevRotationPitch < -180.0F;
        		prevRotationPitch -= 360.0F
        				)
        {
            ;
        }
        while (rotationPitch - prevRotationPitch >= 180.0F)
        {
            prevRotationPitch += 360.0F;
        }
        while (rotationYaw - prevRotationYaw < -180.0F)
        {
            prevRotationYaw -= 360.0F;
        }
        while (rotationYaw - prevRotationYaw >= 180.0F)
        {
            prevRotationYaw += 360.0F;
        }

        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;

        if (!world.isRemote)
        {
            double d0 = targetX - posX;
            double d1 = targetZ - posZ;
            float f1 = (float)Math.sqrt(d0 * d0 + d1 * d1);
            float f2 = (float)MathHelper.atan2(d1, d0);
            double d2 = (double)f + (double)(f1 - f) * 0.001D;

            if (f1 < 1.0F)
                d2 *= 0.8D;

            motionX = Math.cos((double)f2) * d2;
            motionZ = Math.sin((double)f2) * d2;

            motionY += ((posY < targetY ? 1.0D : -1.0D) - motionY) * 0.014999999664723873D;
            
            double yDist = Math.abs(targetY - posY);
            if (yDist < 1.5)
                motionY *= 0.2D;
            double nextX = posX + 3 * (tooCloseToSurface ? tcMotX : motionX);
            double nextY = posY + 3 * (tooCloseToSurface ? tcMotY : motionY);
            double nextZ = posZ + 3 * (tooCloseToSurface ? tcMotZ : motionZ);
            IBlockState nextBlock = world.getBlockState(new BlockPos(nextX, nextY, nextZ));
            boolean alreadyTooCloseToSurface = tooCloseToSurface;
            tooCloseToSurface = !nextBlock.getBlock().equals(Blocks.AIR)
            		&& !nextBlock.getBlock().equals(Blocks.WATER)
            		&& !nextBlock.getBlock().equals(Blocks.FLOWING_WATER);
            if (tooCloseToSurface) {
            	if (!alreadyTooCloseToSurface) {
	            	tcMotX = motionX;
	            	tcMotY = motionY;
	            	tcMotZ = motionZ;
            	}
            	motionX = 0;
            	motionY = 0;
            	motionZ = 0;
            }
            
            /*logger.info("tc: " + tooCloseToSurface + 
            		" atc " + alreadyTooCloseToSurface +
            		" mot: " + formatDouble(motionX) + "," + formatDouble(motionY) + "," + formatDouble(motionZ) + 
            		" tcmot: " + formatDouble(tcMotX) + "," + formatDouble(tcMotY) + "," + formatDouble(tcMotZ) + 
            		" pos: " + formatDouble(posX) + "," + formatDouble(posY) + "," + formatDouble(posZ) +
            		" nextPos: " + formatDouble(nextX) + "," + formatDouble(nextY) + "," + formatDouble(nextZ));*/
            
//            logger.info("x: " + motionX);
//            logger.info("y: " + motionY);
//            logger.info("z: " + motionZ);
                
        }

        float f3 = 0.25F;

        if (isInWater())
        {
            for (int i = 0; i < 4; ++i)
            {
                world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * 0.25D, posY - motionY * 0.25D, posZ - motionZ * 0.25D, motionX, motionY, motionZ, new int[0]);
            }
        }
        else
        {
            world.spawnParticle(EnumParticleTypes.PORTAL, posX - motionX * 0.25D + rand.nextDouble() * 0.6D - 0.3D, posY - motionY * 0.25D - 0.5D, posZ - motionZ * 0.25D + rand.nextDouble() * 0.6D - 0.3D, motionX, motionY, motionZ, new int[0]);
        }

        if (!world.isRemote)
        {
            setPosition(posX, posY, posZ);
            ticksLived++;

            if (ticksLived > 150)
            {
                setDead();

                if (shouldShatter)
                    world.playEvent(2003, new BlockPos(this), 0);
                else
                    world.spawnEntity(new EntityItem(world, posX, posY, posZ, createItemEyeOfProspecting()));
            }
        }
    }
    
//    static String formatDouble(double d) {
//    	String s = Double.toString(d);
//    	return s.length() > 6 ? s.substring(0, 5) : s;
//    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
    	nbt.setString("ore", oreName);
    	nbt.setInteger("ticksLived", ticksLived);
    	nbt.setBoolean("shouldShatter", shouldShatter);
    	nbt.setDouble("targetX", targetX);
    	nbt.setDouble("targetY", targetY);
    	nbt.setDouble("targetZ", targetZ);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
    	oreName = nbt.getString("ore");
    	ticksLived = nbt.getInteger("ticksLived");
    	shouldShatter = nbt.getBoolean("shouldShatter");
    	targetX = nbt.getDouble("targetX");
    	targetY = nbt.getDouble("targetY");
    	targetZ = nbt.getDouble("targetZ");
    }

    @Override
    public float getBrightness()
    {
        return 1.0F;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender()
    {
        return 15728880;
    }

    @Override
    public boolean canBeAttackedWithItem()
    {
        return false;
    }

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		// in SP oreName will be null if the player exits/enters the game while the eye is in the air
		ByteBufUtils.writeUTF8String(buffer, oreName);
		ByteBufUtils.writeUTF8String(buffer, Boolean.toString(shouldShatter));
		ByteBufUtils.writeUTF8String(buffer, Integer.toString(ticksLived));
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		oreName = ByteBufUtils.readUTF8String(buffer);
		shouldShatter = Boolean.parseBoolean(ByteBufUtils.readUTF8String(buffer));
		ticksLived = Integer.parseInt(ByteBufUtils.readUTF8String(buffer));
	}
}