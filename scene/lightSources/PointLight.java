package edu.cg.scene.lightSources;

import edu.cg.algebra.*;
import edu.cg.scene.objects.Surface;

public class PointLight extends Light {
	protected Point position;
	
	//Decay factors:
	protected double kq = 0.01;
	protected double kl = 0.1;
	protected double kc = 1;
	
	protected String description() {
		String endl = System.lineSeparator();
		return "Intensity: " + intensity + endl +
				"Position: " + position + endl +
				"Decay factors: kq = " + kq + ", kl = " + kl + ", kc = " + kc + endl;
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Point Light:" + endl + description();
	}
	
	@Override
	public PointLight initIntensity(Vec intensity) {
		return (PointLight)super.initIntensity(intensity);
	}

	@Override
	public Ray rayToLight(Point fromPoint) {
		Vec direction = Ops.sub(position, fromPoint).normalize();

		return new Ray(fromPoint, direction);
	}

	@Override
	public boolean isOccludedBy(Surface surface, Ray rayToLight) {
		Hit hit = surface.intersect(rayToLight);
		if(hit != null){
			Point intersectionPoint = rayToLight.getHittingPoint(hit);
			Point pointOnSurface = rayToLight.source();

			double distanceToIntersection = pointOnSurface.dist(intersectionPoint);
			double distanceToLightSource = pointOnSurface.dist(position);

			return distanceToIntersection < distanceToLightSource;
		}

		return false;
	}

	@Override
	public Vec intensity(Point hittingPoint, Ray rayToLight) {
		double distance = hittingPoint.dist(position);
		double denominator = this.kc + this.kl * distance + this.kq * Math.pow(distance ,2);

		return intensity.mult(1.0 / denominator);
	}

	public PointLight initPosition(Point position) {
		this.position = position;
		return this;
	}
	
	public PointLight initDecayFactors(double kq, double kl, double kc) {
		this.kq = kq;
		this.kl = kl;
		this.kc = kc;
		return this;
	}

	//TODO: add some methods
}
