package edu.cg.scene.camera;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;

public class PinholeCamera {
	//TODO Add your fields
	private Point cameraPosition;
	private Point middlePixelPosition;
	private Vec towardsVec;
	private Vec upVec;
	private Vec rightVec;
	private double distanceToPlain;
	private double pixelSize;
	private double viewPlainWidth;
	private double viewPlainHeight;
	private int numberOfPixelsY;
	private int numberOfPixelsX;
	private int midPixelIndexX;
	private int midPixelIndexY;

	/**
	 * Initializes a pinhole camera model with default resolution 200X200 (RxXRy) and image width 2.
	 * @param cameraPosition - The position of the camera.
	 * @param towardsVec - The towards vector of the camera (not necessarily normalized).
	 * @param upVec - The up vector of the camera.
	 * @param distanceToPlain - The distance of the camera (position) to the center point of the image-plain.
	 * 
	 */
	public PinholeCamera(Point cameraPosition, Vec towardsVec, Vec upVec, double distanceToPlain) {
		this.cameraPosition = cameraPosition;
		this.towardsVec = towardsVec;
		this.rightVec = upVec.cross(towardsVec).normalize();
		this.upVec = rightVec.cross(towardsVec).normalize();
		this.distanceToPlain = distanceToPlain;
		this.middlePixelPosition = cameraPosition.add(towardsVec.mult(distanceToPlain));
	}

	/**
	 * Initializes the resolution and width of the image.
	 * @param height - the number of pixels in the y direction.
	 * @param width - the number of pixels in the x direction.
	 * @param viewPlainWidth - the width of the image plain in world coordinates.
	 */
	public void initResolution(int height, int width, double viewPlainWidth) {
		this.numberOfPixelsX = width;
		this.numberOfPixelsY = height;
		this.viewPlainWidth = viewPlainWidth;
		this.pixelSize = viewPlainWidth / width;
		this.viewPlainHeight = numberOfPixelsY * pixelSize;
		this.midPixelIndexX = (numberOfPixelsX % 2 == 0) ? (numberOfPixelsX / 2) : (numberOfPixelsX / 2) + 1;
		this.midPixelIndexY = (numberOfPixelsY % 2 == 0) ? (numberOfPixelsY / 2) : (numberOfPixelsY / 2) + 1;
	}

	/**
	 * Transforms from pixel coordinates to the center point of the corresponding pixel in model coordinates.
	 * @param x - the index of the x direction of the pixel.
	 * @param y - the index of the y direction of the pixel.
	 * @return the middle point of the pixel (x,y) in the model coordinates.
	 */
	public Point transform(int x, int y) {
		int differenceX = x - midPixelIndexX;
		int differenceY = y - midPixelIndexY;
		// p = center + i*(width/number of width pixels)*Vright + j*(height/num of height pixels)*Vleft
		Vec multVright = rightVec.mult(differenceX * this.pixelSize);
		Vec multVup = upVec.mult(differenceY * this.pixelSize);
		return middlePixelPosition.add(multVright.mult(-1)).add(multVup);
	}
	
	/**
	 * Returns a copy of the camera position
	 * @return a "new" point representing the camera position.
	 */
	public Point getCameraPosition() {
		return new Point(cameraPosition.x, cameraPosition.y, cameraPosition.z);
	}
}
