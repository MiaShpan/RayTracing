package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.*;

import static java.lang.Double.NaN;

public class Sphere extends Shape {
	private Point center;
	private double radius;

	public Sphere(Point center, double radius) {
		this.center = center;
		this.radius = radius;
	}

	public Sphere() {
		this(new Point(0, -0.5, -6), 0.5);
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Sphere:" + endl + "Center: " + center + endl + "Radius: " + radius + endl;
	}

	public Sphere initCenter(Point center) {
		this.center = center;
		return this;
	}

	public Sphere initRadius(double radius) {
		this.radius = radius;
		return this;
	}

	@Override
	public Hit intersect(Ray ray) {
		Vec v = ray.direction();
		Point p0 = ray.source();
		double b = 2*v.x * (p0.x - center.x) + 2 * v.y * (p0.y - center.y) + 2 * v.z * (p0.z - center.z);
		double c = Math.pow(p0.x - center.x, 2) + Math.pow(p0.y - center.y, 2)
				+ Math.pow(p0.z - center.z, 2) - Math.pow(radius, 2);

		double sqrt = Math.sqrt(Math.pow(b,2) - 4 * c);
		if (Double.isNaN(sqrt)) {
			return null;
		} else {
			double t1 = (-b + sqrt) / 2.0 ;
			if ((sqrt == 0)) {
				if (t1 < Ops.epsilon){
					return null;
				} else {
					Vec normalizedToSurface = center.sub(ray.source().add(ray.direction().mult(t1))).normalize();
					return new Hit(t1, normalizedToSurface);
				}
			} else {
				double t2 = (-b - sqrt) / 2.0;

				if (t1 > Ops.epsilon && t2 > Ops.epsilon) {
					double min = Math.min(t1, t2);
					Vec normalizedToSurface = center.sub(ray.source().add(ray.direction().mult(min))).normalize();
					return new Hit(min, normalizedToSurface);
				} else if ((t1 < Ops.epsilon) && (t2 > Ops.epsilon)){
					Vec normalizedToSurface = center.sub(ray.source().add(ray.direction().mult(t2))).normalize().mult(-1);
					return new Hit(t2, normalizedToSurface).setIsWithin(true);
				} else if ((t2 < Ops.epsilon) && (t1 > Ops.epsilon)){
					Vec normalizedToSurface = center.sub(ray.source().add(ray.direction().mult(t1))).normalize().mult(-1);
					return new Hit(t1, normalizedToSurface).setIsWithin(true);
				} else {
					return null;
				}
			}
		}
	}
}
