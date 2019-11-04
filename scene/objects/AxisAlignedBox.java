package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Ops;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;


public class AxisAlignedBox extends Shape {
	private Point minPoint;
	private Point maxPoint;
	private String name = "";
	static private int CURR_IDX;
	private double tNear;
	private double tFar;

	/**
	 * Creates an axis aligned box with a specified minPoint and maxPoint.
	 */
	public AxisAlignedBox(Point minPoint, Point maxPoint) {
		this.minPoint = minPoint;
		this.maxPoint = maxPoint;
		name = new String("Box " + CURR_IDX);
		CURR_IDX += 1;
		fixBoundryPoints();
	}

	/**
	 * Creates a default axis aligned box with a specified minPoint and maxPoint.
	 */
	public AxisAlignedBox() {
		minPoint = new Point(-1.0, -1.0, -1.0);
		maxPoint = new Point(1.0, 1.0, 1.0);
	}

	/**
	 * This methods fixes the boundary points minPoint and maxPoint so that the values are consistent.
	 */
	private void fixBoundryPoints() {
		double min_x = Math.min(minPoint.x, maxPoint.x), max_x = Math.max(minPoint.x, maxPoint.x),
				min_y = Math.min(minPoint.y, maxPoint.y), max_y = Math.max(minPoint.y, maxPoint.y),
				min_z = Math.min(minPoint.z, maxPoint.z), max_z = Math.max(minPoint.z, maxPoint.z);
		minPoint = new Point(min_x, min_y, min_z);
		maxPoint = new Point(max_x, max_y, max_z);
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return name + endl + "Min Point: " + minPoint + endl + "Max Point: " + maxPoint + endl;
	}

	//Initializers
	public AxisAlignedBox initMinPoint(Point minPoint) {
		this.minPoint = minPoint;
		fixBoundryPoints();
		return this;
	}

	public AxisAlignedBox initMaxPoint(Point maxPoint) {
		this.maxPoint = maxPoint;
		fixBoundryPoints();
		return this;
	}

	@Override
	public Hit intersect(Ray ray) {
		double tnear = Ops.infinity*(-1);
		double tfar = Ops.infinity;

		if(isOutOfBox(ray)) {
			return null;
		}

		// x
		double t1 = (minPoint.x - ray.source().x) / ray.direction().x;
		double t2 = (maxPoint.x - ray.source().x) / ray.direction().x;
		double tmin = Math.min(t1, t2);
		t2 = (t1 == tmin) ? t2 : t1;
		t1 = tmin;

		tnear = Math.max(t1, tnear);
		tfar = Math.min(t2, tfar);

		// y
		t1 = (minPoint.y - ray.source().y) / ray.direction().y;
		t2 = (maxPoint.y - ray.source().y) / ray.direction().y;
		tmin = Math.min(t1, t2);
		t2 = (t1 == tmin) ? t2 : t1;
		t1 = tmin;

		tnear = Math.max(t1, tnear);
		tfar = Math.min(t2, tfar);

		// z
		t1 = (minPoint.z - ray.source().z) / ray.direction().z;
		t2 = (maxPoint.z - ray.source().z) / ray.direction().z;
		tmin = Math.min(t1, t2);
		t2 = (t1 == tmin) ? t2 : t1;
		t1 = tmin;

		tnear = Math.max(t1, tnear);
		tfar = Math.min(t2, tfar);

		if(notPositiveInterval(tnear, tfar)){
			return null;
		}

		if (tnear < Ops.epsilon){
			return new Hit(tfar, this.findNormal(ray.add(tfar)).mult(-1)).setIsWithin(true);
		}
		return new Hit(tnear, this.findNormal(ray.add(tnear)));
	}

	private boolean notPositiveInterval(double tnear, double tfar){
		return (tnear > tfar || tfar < Ops.epsilon);
	}

	private boolean isOutOfBox(Ray ray){
		return ((Math.abs(ray.direction().x) <= Ops.epsilon)
				&& (ray.source().x < this.minPoint.x || ray.source().x > this.maxPoint.x))
				|| ((Math.abs(ray.direction().y) <= Ops.epsilon)
				&& (ray.source().y < this.minPoint.y || ray.source().y > this.maxPoint.y))
				|| ((Math.abs(ray.direction().z) <= Ops.epsilon)
				&& (ray.source().z < this.minPoint.z || ray.source().z > this.maxPoint.z));
	}

	private Vec findNormal(Point interPoint) {
		double distanceToMaxX = Math.abs(interPoint.x - maxPoint.x);
		double distanceToMinX = Math.abs(interPoint.x - minPoint.x);
		double distanceToMaxY = Math.abs(interPoint.y - maxPoint.y);
		double distanceToMinY = Math.abs(interPoint.y - minPoint.y);
		double distanceToMaxZ = Math.abs(interPoint.z - maxPoint.z);
		double distanceToMinZ = Math.abs(interPoint.z - minPoint.z);

		if (distanceToMinZ <= Ops.epsilon) {
			return new Vec(0.0, 0.0, -1.0);
		}
		if (distanceToMaxZ <= Ops.epsilon) {
			return new Vec(0.0, 0.0, 1.0);
		}
		if (distanceToMinY <= Ops.epsilon) {
			return new Vec(0.0, -1.0, 0.0);
		}
		if (distanceToMaxY <= Ops.epsilon) {
			return new Vec(0.0, 1.0, 0.0);
		}
		if (distanceToMinX <= Ops.epsilon) {
			return new Vec(-1.0, 0.0, 0.0);
		}
		if (distanceToMaxX <= Ops.epsilon) {
			return new Vec(1.0, 0.0, 0.0);
		}
		return null;
	}
}