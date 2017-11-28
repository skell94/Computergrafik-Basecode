package jrtr;
import java.util.ListIterator;

import javax.vecmath.*;

import jrtr.VertexData.VertexElement;

/**
 * Represents a 3D object. The shape references its geometry, 
 * that is, a triangle mesh stored in a {@link VertexData} 
 * object, its {@link Material}, and a transformation {@link Matrix4f}.
 */
public class Shape {

	private Material material;
	private VertexData vertexData;
	private Matrix4f t;
	private Vector3f sphereCenter;
	private float sphereRadius;
	
	/**
	 * Make a shape from {@link VertexData}. A shape contains the geometry 
	 * (the {@link VertexData}), material properties for shading (a 
	 * refernce to a {@link Material}), and a transformation {@link Matrix4f}.
	 *  
	 *  
	 * @param vertexData the vertices of the shape.
	 */
	public Shape(VertexData vertexData)
	{
		this.vertexData = vertexData;
		t = new Matrix4f();
		t.setIdentity();
		
		material = null;
		
		ListIterator<VertexElement> it = vertexData.getElements().listIterator();
		while(it.hasNext()) {
			VertexElement element = it.next();
			if(element.getSemantic() == VertexData.Semantic.POSITION) {
				calculateSphere(element.getData());
			}
		}
	}
	
	public VertexData getVertexData()
	{
		return vertexData;
	}
	
	public void setTransformation(Matrix4f t)
	{
		this.t = t;
	}
	
	public Matrix4f getTransformation()
	{
		return t;
	}
	
	/**
	 * Set a reference to a material for this shape.
	 * 
	 * @param material
	 * 		the material to be referenced from this shape
	 */
	public void setMaterial(Material material)
	{
		this.material = material;
	}

	/**
	 * To be implemented in the "Textures and Shading" project.
	 */
	public Material getMaterial()
	{
		return material;
	}
	
	public Vector3f getSphereCenter() {
		return new Vector3f(sphereCenter);
	}

	public float getSphereRadius() {
		return sphereRadius;
	}

	private void calculateSphere(float[] data) {
		float x = 0, y = 0, z = 0;
		for(int i=0; i<data.length; i+=3) {
			x += data[i];
			y += data[i+1];
			z += data[i+2];
		}
		sphereCenter = new Vector3f(x/(data.length/3f), y/(data.length/3f), z/(data.length/3f));
		sphereRadius = 0;
		for(int i=0; i<data.length; i+=3) {
			Vector3f connection = new Vector3f(sphereCenter.x - data[i], sphereCenter.y - data[i+1], sphereCenter.z - data[i+2]);
			if(connection.length() > sphereRadius)
				sphereRadius = connection.length();
		}
	}

}
