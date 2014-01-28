package com.github.blir.enderprospecting;

import static com.github.blir.enderprospecting.EnderProspecting.canTrack;
import static com.github.blir.enderprospecting.EnderProspecting.formatOreName;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemEyeOfProspecting extends Item {

	@SideOnly(Side.CLIENT)
	private Icon[] icons;

	public ItemEyeOfProspecting(int id) {
		super(id);
		setCreativeTab(CreativeTabs.tabMisc);
		this.maxStackSize = 16;
		this.canRepair = false;
		this.hasSubtypes = true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister reg) {
		icons = new Icon[9];
		for (int idx = 0; idx < icons.length; idx++) {
			icons[idx] = reg.registerIcon(EnderProspecting.modid + ":"
					+ getUnlocalizedName().substring(5) + idx);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Icon getIconFromDamage(int dmg) {
		if (dmg == Block.oreCoal.blockID) {
			return icons[2];
		}
		if (dmg == 0) {
			return icons[0];
		}
		String oreName = EnderProspecting.getOreName((short) dmg);
		if (oreName == null) {
			return icons[0];
		}
		if (oreName.equals("oreGold")) {
			return icons[4];
		}
		if (oreName.equals("oreIron")) {
			return icons[1];
		}
		if (oreName.equals("oreLapis")) {
			return icons[7];
		}
		if (oreName.equals("oreDiamond")) {
			return icons[6];
		}
		if (oreName.equals("oreRedstone")) {
			return icons[3];
		}
		if (oreName.equals("oreEmerald")) {
			return icons[5];
		}
		if (oreName.equals("oreQuartz")) {
			return icons[8];
		}
		return icons[0];
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world,
			EntityPlayer player) {

		if (getDamage(stack) == 0 || world.isRemote) {
			return stack;
		}

		MovingObjectPosition mPos = getMovingObjectPositionFromPlayer(world,
				player, false);
		if (mPos != null) {
			if (canTrack(stack, world, mPos.blockX, mPos.blockY, mPos.blockZ)) {
				// return if looking at the block that this Eye tracks
				return stack;
			}
		}

		Vec3 playerPos = world.getWorldVec3Pool().getVecFromPool(player.posX,
				player.posY, player.posZ);

		Vec3 blockPos = null;
		// search for the block that this eye of prospecting tracks
		for (int i1 = -24; i1 <= 24; i1++) {
			for (int i2 = -12; i2 <= 12; i2++) {
				for (int i3 = -24; i3 <= 24; i3++) {
					int x = i1 + (int) player.posX;
					int y = i2 + (int) player.posY;
					int z = i3 + (int) player.posZ;
					if (canTrack(stack, world, x, y, z)) {
						Vec3 prospective = world.getWorldVec3Pool()
								.getVecFromPool(x, y, z);
						if (blockPos == null
								|| playerPos.distanceTo(prospective) < playerPos
										.distanceTo(blockPos)) {
							// no current block or block is closer
							blockPos = prospective;
						}
					}
				}
			}
		}

		if (blockPos != null) {

			EntityEyeOfProspecting entity = new EntityEyeOfProspecting(world,
					player.posX, player.posY + 1.62D - player.yOffset,
					player.posZ, getDamage(stack));
			entity.setDestination(blockPos.xCoord, blockPos.yCoord,
					blockPos.zCoord);
			world.spawnEntityInWorld(entity);

			world.playSoundAtEntity(player, "random.bow", 0.5F,
					0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
			world.playAuxSFXAtEntity((EntityPlayer) null, 1002,
					(int) player.posX, (int) player.posY, (int) player.posZ, 0);

			if (!player.capabilities.isCreativeMode) {
				stack.stackSize--;
			}
		} else {
			player.addChatMessage("The eye remains motionless; there must be no nearby ores.");
		}
		return stack;
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer player, World world,
			int par4, int par5, int par6, int par7, float par8, float par9,
			float par10) {

		onItemRightClick(is, world, player);
		return true;
	}

	@Override
	public void addInformation(ItemStack is, EntityPlayer player, List list,
			boolean b) {
		if (getDamage(is) != 0) {
			list.add("Tuned to " + formatOreName(is));
		} else {
			list.add("Base eye; not tuned");
		}
	}
}
