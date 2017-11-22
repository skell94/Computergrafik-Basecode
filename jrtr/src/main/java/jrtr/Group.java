package jrtr;

import java.util.LinkedList;

import javax.vecmath.Matrix4f;

public abstract class Group implements Node {
	
	private Matrix4f transformation;
	private LinkedList<Node> children;
	
	public Group() {
		transformation = new Matrix4f();
		transformation.setIdentity();
		children = new LinkedList<Node>();
	}
	
	public void setTransformation(Matrix4f transformation) {
		this.transformation = transformation;
	}
	
	public Matrix4f getTransformation() {
		return (Matrix4f) transformation.clone();
	}
	
	public LinkedList<Node> getChildren(){
		return children;
	}
	
	public Shape getShape() {
		return null;
	}

	public void addChild(Node child) {
		children.add(child);
	}
	
	public void removeChild(int i) {
		children.remove(i);
	}
}
