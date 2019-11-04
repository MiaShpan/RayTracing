package edu.cg.scene;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.cg.Logger;
import edu.cg.algebra.*;
import edu.cg.scene.camera.PinholeCamera;
import edu.cg.scene.lightSources.Light;
import edu.cg.scene.objects.Surface;

public class Scene {
	private String name = "scene";
	private int maxRecursionLevel = 1;
	private int antiAliasingFactor = 1; //gets the values of 1, 2 and 3
	private boolean renderRefractions = false;
	private boolean renderReflections = false;

	private PinholeCamera camera;
	private Vec ambient = new Vec(1, 1, 1); //white
	private Vec backgroundColor = new Vec(0, 0.5, 1); //blue sky
	private List<Light> lightSources = new LinkedList<>();
	private List<Surface> surfaces = new LinkedList<>();



	//MARK: initializers
	public Scene initCamera(Point eyePoistion, Vec towardsVec, Vec upVec,  double distanceToPlain) {
		this.camera = new PinholeCamera(eyePoistion, towardsVec, upVec,  distanceToPlain);
		return this;
	}

	public Scene initAmbient(Vec ambient) {
		this.ambient = ambient;
		return this;
	}

	public Scene initBackgroundColor(Vec backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public Scene addLightSource(Light lightSource) {
		lightSources.add(lightSource);
		return this;
	}

	public Scene addSurface(Surface surface) {
		surfaces.add(surface);
		return this;
	}

	public Scene initMaxRecursionLevel(int maxRecursionLevel) {
		this.maxRecursionLevel = maxRecursionLevel;
		return this;
	}

	public Scene initAntiAliasingFactor(int antiAliasingFactor) {
		this.antiAliasingFactor = antiAliasingFactor;
		return this;
	}

	public Scene initName(String name) {
		this.name = name;
		return this;
	}

	public Scene initRenderRefractions(boolean renderRefarctions) {
		this.renderRefractions = renderRefarctions;
		return this;
	}

	public Scene initRenderReflections(boolean renderReflections) {
		this.renderReflections = renderReflections;
		return this;
	}

	//MARK: getters
	public String getName() {
		return name;
	}

	public int getFactor() {
		return antiAliasingFactor;
	}

	public int getMaxRecursionLevel() {
		return maxRecursionLevel;
	}

	public boolean getRenderRefractions() {
		return renderRefractions;
	}

	public boolean getRenderReflections() {
		return renderReflections;
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Camera: " + camera + endl +
				"Ambient: " + ambient + endl +
				"Background Color: " + backgroundColor + endl +
				"Max recursion level: " + maxRecursionLevel + endl +
				"Anti aliasing factor: " + antiAliasingFactor + endl +
				"Light sources:" + endl + lightSources + endl +
				"Surfaces:" + endl + surfaces;
	}

	private transient ExecutorService executor = null;
	private transient Logger logger = null;
	private transient int imgWidth;
	private transient int imgHeight;

	private void initSomeFields(int imgWidth, int imgHeight, Logger logger) {
		this.logger = logger;
		this.imgWidth = imgWidth;
		this.imgHeight = imgHeight;
		//TODO: initialize your additional field here.
		//      You can also change the method signature if needed.
	}


	public BufferedImage render(int imgWidth, int imgHeight, double viewPlainWidth,Logger logger)
			throws InterruptedException, ExecutionException {
		// TODO: Please notice the following comment.
		// This method is invoked each time Render Scene button is invoked.
		// Use it to initialize additional fields you need.
		initSomeFields(imgWidth, imgHeight, logger);

		BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		camera.initResolution(imgHeight, imgWidth, viewPlainWidth);
		int nThreads = Runtime.getRuntime().availableProcessors();
		nThreads = nThreads < 2 ? 2 : nThreads;
		this.logger.log("Intitialize executor. Using " + nThreads + " threads to render " + name);
		executor = Executors.newFixedThreadPool(nThreads);

		@SuppressWarnings("unchecked")
		Future<Color>[][] futures = (Future<Color>[][])(new Future[imgHeight][imgWidth]);

		this.logger.log("Starting to shoot " +
				(imgHeight*imgWidth*antiAliasingFactor*antiAliasingFactor) +
				" rays over " + name);

		for(int y = 0; y < imgHeight; ++y)
			for(int x = 0; x < imgWidth; ++x)
				futures[y][x] = calcColor(x, y);

		this.logger.log("Done shooting rays.");
		this.logger.log("Wating for results...");

		for(int y = 0; y < imgHeight; ++y)
			for(int x = 0; x < imgWidth; ++x) {
				Color color = futures[y][x].get();
				img.setRGB(x, y, color.getRGB());
			}

		executor.shutdown();

		this.logger.log("Ray tracing of " + name + " has been completed.");

		executor = null;
		this.logger = null;

		return img;
	}

	private Future<Color> calcColor(int x, int y) {
		return executor.submit(() -> {
			Point centerPoint = camera.transform(x, y);
			Ray ray = new Ray(camera.getCameraPosition(), centerPoint);
			Vec color = calcColor(ray, 0);
			return color.toColor();
		});
	}

	private Vec calcColor(Ray ray, int recusionLevel) {
		if(recusionLevel == maxRecursionLevel){
			return new Vec(0);
		}

		recusionLevel += 1;
		Vec color = new Vec(0);
		Hit minHit = findMinHit(ray);
		if (minHit != null){
			Vec amb;
			Surface hitSurface = minHit.getSurface();
			Point intersectionPoint = ray.getHittingPoint(minHit);

			for (Light lightSource : lightSources) {
				Vec def, spec;
				Ray rayToLight = lightSource.rayToLight(intersectionPoint);
				if (isShadowed(lightSource, hitSurface, rayToLight)) {
					continue;
				}
				def = calcDiffuseColorVec(minHit, ray, lightSource, intersectionPoint);
				spec = calcSpecularColorVec(minHit, ray, lightSource, intersectionPoint);
				color = color.add(spec.add(def));
			}

			color = color.add(calcAmbientColorVec(hitSurface));

			if(renderReflections){
				Vec reflectedDirection = Ops.reflect(ray.direction(), minHit.getNormalToSurface()).normalize();
				Ray reflectedRay = new Ray(intersectionPoint, reflectedDirection);
				Vec reflection = calcColor(reflectedRay, recusionLevel).mult(hitSurface.reflectionIntensity());
				color = color.add(reflection);
			}
			if (renderRefractions && hitSurface.isTransparent()){
				Vec refractedDirection = Ops.refract(ray.direction(), minHit.getNormalToSurface(), hitSurface.n1(minHit), hitSurface.n2(minHit)).normalize();
				Ray refractedRay = new Ray(intersectionPoint, refractedDirection);
				Vec refraction = calcColor(refractedRay, recusionLevel).mult(hitSurface.refractionIntensity());
				color = color.add(refraction);
			}

		} else {
			color = backgroundColor;
		}

		return color;
	}

	private Hit findMinHit(Ray ray){
		double minTValue = Double.MAX_VALUE;
		Hit minHit = null;

		for (Surface surface : surfaces) {
			Hit currentHit = surface.intersect(ray);
			if((currentHit != null) && (currentHit.t() < minTValue)){
				minHit = currentHit;
				minTValue = currentHit.t();
			}
		}

		return minHit;
	}

	private boolean isShadowed(Light lightSource, Surface hitSurface, Ray rayToLight){
		for (Surface surface : surfaces) {
				if(!surface.isTransparent() && lightSource.isOccludedBy(surface, rayToLight)){
					return true;
				}
		}

		return false;
	}

	// need to check if the calc of rgb is correct
	private Vec calcAmbientColorVec(Surface hitSurface){
		double red = hitSurface.Ka().x * ambient.x;
		double green = hitSurface.Ka().y * ambient.y;
		double blue = hitSurface.Ka().z * ambient.z;

		return new Vec(red, green, blue);
	}

	private Vec calcDiffuseColorVec(Hit hit, Ray ray, Light lightSource, Point intersectionPoint){
		Vec Kd = hit.getSurface().Kd();
		Ray rayFromSurfaceToLight = lightSource.rayToLight(intersectionPoint);
		Vec L = rayFromSurfaceToLight.direction().normalize();
		Vec normal = hit.getNormalToSurface();
		double cosTheta = L.dot(normal);
		Vec lightSourceIntensityInHitPoint = lightSource.intensity(intersectionPoint, rayFromSurfaceToLight);


		return cosTheta > 0 ? Kd.mult(cosTheta).mult(lightSourceIntensityInHitPoint) : new Vec(0);
	}

	private Vec calcSpecularColorVec(Hit hit, Ray ray, Light lightSource, Point intersectionPoint){
		Surface hitSurface = hit.getSurface();
		// L
		Vec directionFromLightToSurface = lightSource.rayToLight(intersectionPoint).direction().mult(-1).normalize();
		// R
		Vec reflectedDirection = Ops.reflect(directionFromLightToSurface, hit.getNormalToSurface());
		// V
		Vec directionFromSurfaceToCamera = ray.direction().mult(-1).normalize();
		Ray rayFromSurfaceToLight = lightSource.rayToLight(intersectionPoint);
		Vec lightSourceIntensityInHitPoint = lightSource.intensity(intersectionPoint, rayFromSurfaceToLight);
		double mult = Math.pow(directionFromSurfaceToCamera.dot(reflectedDirection), hitSurface.shininess());

		return hitSurface.Ks().mult(mult).mult(lightSourceIntensityInHitPoint);
	}
}
