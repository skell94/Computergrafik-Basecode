package jrtr;

import java.util.LinkedList;

import javax.vecmath.Matrix4f;

public abstract class Leaf implements Node {
	
	private Shape shape;
	
	public Leaf(Shape shape) {
		this.shape = shape;
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
}
