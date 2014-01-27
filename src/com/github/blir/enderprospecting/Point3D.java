package com.github.blir.enderprospecting;

import net.minecraft.util.Vec3;

public class Point3D {

	public int x, y, z;

	public Point3D(double x, double y, double z) {
		this.x = (int) x;
		this.y = (int) y;
		this.z = (int) z;
	}

	public Point3D(Vec3 vec) {
		this.x = (int) vec.xCoord;
		this.y = (int) vec.yCoord;
		this.z = (int) vec.zCoord;
	}

	public boolean isEquivalentTo(Vec3 vec) {
		return x == (int) vec.xCoord && x == (int) vec.yCoord
				&& z == (int) vec.zCoord;
	}
}
