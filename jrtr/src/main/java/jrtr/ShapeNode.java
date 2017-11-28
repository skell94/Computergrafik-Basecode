package jrtr;

import javax.vecmath.*;

public class ShapeNode extends Leaf {
	
	public ShapeNode(Shape shape) {
		super(shape);
	}
	
	public boolean insidePlane(Matrix4f transformation, Vector4f plane) {
		transformation.mul(shape.getTransformation());
		Vector4f center4 = new Vector4f(shape.getSphereCenter().x, shape.getSphereCenter().y, shape.getSphereCenter().z, 1);
		transformation.transform(center4);
		Vector3f center = new Vector3f(center4.x, center4.y, center4.z);
		Vector3f normal = new Vector3f(plane.x, plane.y, plane.z);
		float distance = normal.dot(center) + plane.w;
		if(distance <= shape.getSphereRadius())
			return true;
		return false;
	}
}
