package jrtr;

import javax.vecmath.Matrix4f;

/**
 * Stores the specification of a viewing frustum, or a viewing
 * volume. The viewing frustum is represented by a 4x4 projection
 * matrix. You will extend this class to construct the projection 
 * matrix from intuitive parameters.
 * <p>
 * A scene manager (see {@link SceneManagerInterface}, {@link SimpleSceneManager}) 
 * stores a frustum.
 */
public class Frustum {

	private Matrix4f projectionMatrix;
	
	/**
	 * Construct a default viewing frustum. The frustum is given by a 
	 * default 4x4 projection matrix.
	 */
	public Frustum()
	{
		projectionMatrix = new Matrix4f();
		float f[] = {2.f, 0.f, 0.f, 0.f, 
					 0.f, 2.f, 0.f, 0.f,
				     0.f, 0.f, -1.02f, -2.02f,
				     0.f, 0.f, -1.f, 0.f};
		projectionMatrix.set(f);
	}
	
	public Frustum(float nearPlane, float farPlane, float aspectRatio, float verticalFieldOfView){
		projectionMatrix = new Matrix4f();
		float f[] = {(float) (1.0/(aspectRatio*Math.tan(verticalFieldOfView/2))), 0.f, 0.f, 0.f, 
					 0.f, (float) (1.0/(Math.tan(verticalFieldOfView/2))), 0.f, 0.f,
				     0.f, 0.f, (nearPlane + farPlane)/(nearPlane - farPlane), 2*nearPlane*farPlane/(nearPlane - farPlane),
				     0.f, 0.f, -1.f, 0.f};
		projectionMatrix.set(f);
	}
	
	/**
	 * Return the 4x4 projection matrix, which is used for example by 
	 * the renderer.
	 * 
	 * @return the 4x4 projection matrix
	 */
	public Matrix4f getProjectionMatrix()
	{
		return projectionMatrix;
	}
	
	public void setProjectionMatrix(Matrix4f m)
	{
		this.projectionMatrix = m;
	}
}
