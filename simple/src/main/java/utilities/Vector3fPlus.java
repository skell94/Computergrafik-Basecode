package utilities;

import javax.vecmath.*;

public class Vector3fPlus extends Vector3f{
	
	public Vector3fPlus(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3fPlus() {
		this.x = 1;
		this.y = 1;
		this.z = 1;
	}

	public void mul(Matrix3f matrix) {
		Matrix3f temp = new Matrix3f(this.x, this.y, this.z,
				0.f, 0.f, 0.f,
				0.f, 0.f, 0.f);
		temp.mul(matrix);
		this.x = temp.m00;
		this.y = temp.m01;
		this.z = temp.m02;
	}

	public void mul(Matrix4f matrix) {
		Matrix4f temp = new Matrix4f(this.x, this.y, this.z, 0.f,
				0.f, 0.f, 0.f, 0.f,
				0.f, 0.f, 0.f, 0.f, 
				0.f, 0.f, 0.f, 0.f);
		temp.mul(matrix);
		this.x = temp.m00;
		this.y = temp.m01;
		this.z = temp.m02;
	}
	
	public void mul(float scalar) {
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
	}
	
	public void add (Vector3f vector) {
		this.x += vector.x;
		this.y += vector.y;
		this.z += vector.z;
	}
	
	public void sub (Vector3f vector) {
		this.x -= vector.x;
		this.y -= vector.y;
		this.z -= vector.z;
	}
	
	public void setToLength(float length) {
		normalize();
		this.x *= length;
		this.y *= length;
		this.z *= length;
	}
	
	public Vector3fPlus clone() {
		return new Vector3fPlus(this.x, this.y, this.z);
	}
	
	public static Vector3fPlus clone(Vector3f vector) {
		return new Vector3fPlus(vector.x, vector.y, vector.z);
	}
}
