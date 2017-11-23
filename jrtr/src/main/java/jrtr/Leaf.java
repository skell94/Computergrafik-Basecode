package jrtr;

import java.util.LinkedList;

import javax.vecmath.Matrix4f;

public abstract class Leaf implements Node {
	
	private Shape shape;
	private Light light;
	
	public Leaf(Shape shape) {
		this.shape = shape;
		light = null;
	}
	
	public Leaf(Light light) {
		this.light = light;
		shape = null;
	}

	public Matrix4f getTransformation() {
		return null;
	}
	
	public LinkedList<Node> getChildren(){
		return null;
	}
	
	public Shape getShape() {
		return shape;
	}
	
	public Light getLight() {
		return light;
	}
}
