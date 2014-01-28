package com.github.blir.enderprospecting;

import static com.github.blir.enderprospecting.EnderProspecting.canTrack;
import static com.github.blir.enderprospecting.EnderProspecting.formatOreName;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class ItemTalismanOfProspecting extends Item {

	@SideOnly(Side.CLIENT)
	private Icon[] icons;

	public ItemTalismanOfProspecting(int par1) {
		super(par1);
		setCreativeTab(CreativeTabs.tabMisc);
		this.canRepair = false;
		this.maxStackSize = 1;
		this.hasSubtypes = true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister reg) {
		icons = new Icon[10];
		for (int idx = 0; idx < icons.length; idx++) {
			icons[idx] = reg.registerIcon(EnderProspecting.modid + ":"
					+ getUnlocalizedName().substring(5) + idx);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Icon getIconFromDamage(int dmg) {
		if (dmg == Block.oreCoal.blockID) {
			return icons[9];
		}
		if (dmg == 0) {
			return icons[0];
		}
		String oreName = EnderProspecting.getOreName((short) dmg);
		if (oreName == null) {
			return icons[1];
		}
		if (oreName.equals("oreGold")) {
			return icons[4];
		}
		if (oreName.equals("oreIron")) {
			return icons[2];
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
		return icons[1];
	}

	@Override
	public ItemStack onItemRightClick(ItemStack is, World world,
			EntityPlayer player) {

		if (getDamage(is) == 0 || !world.isRemote) {
			return is;
		}

		Vec3 playerPos = player.getPosition(1.0F);

		Vec3 blockPos = null;
		// search for the block that this talisman tracks
		for (int i1 = -8; i1 <= 8; i1++) {
			for (int i2 = -4; i2 <= 4; i2++) {
				for (int i3 = -8; i3 <= 8; i3++) {
					int x = i1 + (int) player.posX;
					int y = i2 + (int) player.posY;
					int z = i3 + (int) player.posZ;
					if (canTrack(is, world, x, y, z)) {
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
			double xDif = playerPos.xCoord - blockPos.xCoord;
			double yDif = playerPos.yCoord - blockPos.yCoord;
			double zDif = playerPos.zCoord - blockPos.zCoord;

			double max = Math.max(Math.max(Math.abs(xDif), Math.abs(yDif)),
					Math.abs(zDif));

			if (max == Math.abs(xDif)) {
				if (xDif > 0) {
					player.addChatMessage("The talisman pulls to the West...");
				} else {
					player.addChatMessage("The talisman pulls to the East...");
				}
			} else if (max == Math.abs(yDif)) {
				if (yDif > 0) {
					player.addChatMessage("The talisman pulls downward...");
				} else {
					player.addChatMessage("The talisman pulls upward...");
				}
			} else if (max == Math.abs(zDif)) {
				if (zDif > 0) {
					player.addChatMessage("The talisman pulls to the North...");
				} else {
					player.addChatMessage("The talisman pulls to the South...");
				}
			}
		} else {
			player.addChatMessage("The talisman remains motionless; there must be no nearby ores.");
		}

		return is;
	}

	@Override
	public void addInformation(ItemStack is, EntityPlayer player, List list,
			boolean b) {
		if (getDamage(is) != 0) {
			list.add("Tuned to " + formatOreName(is));
		} else {
			list.add("Base talisman; not tuned");
		}
	}
}
