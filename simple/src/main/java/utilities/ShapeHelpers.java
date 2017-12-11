package utilities;

import java.util.ArrayList;
import java.util.Arrays;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import jrtr.*;

public abstract class ShapeHelpers {
	
	public static Shape createCylinder(RenderContext renderContext, int n, float length, float radius, Vector3f color1, Vector3f color2) {
		ArrayList<Float> vList = new ArrayList<Float>();
		ArrayList<Float> nList = new ArrayList<Float>();
		for(int i=0; i<n; ++i){
			double angle = i*(1.0/n)*2*Math.PI;
			Vector3f point = new Vector3f((float)(radius * Math.sin(angle)), (float)(radius * Math.cos(angle)), length/2);
			vList.addAll(Arrays.asList(point.x, point.y, point.z));
			vList.addAll(Arrays.asList(point.x, point.y, point.z));
			point.z = 0;
			point.normalize();
			nList.addAll(Arrays.asList(point.x, point.y, point.z));
			nList.addAll(Arrays.asList(0f, 0f, 1f));
		}
		for(int i=0; i<n; ++i){
			double angle = i*(1.0/n)*2*Math.PI;
			Vector3f point = new Vector3f((float)(radius * Math.sin(angle)), (float)(radius * Math.cos(angle)), -length/2);
			vList.addAll(Arrays.asList(point.x, point.y, point.z));
			vList.addAll(Arrays.asList(point.x, point.y, point.z));
			point.z = 0;
			point.normalize();
			nList.addAll(Arrays.asList(point.x, point.y, point.z));
			nList.addAll(Arrays.asList(0f, 0f, -1f));
		}
		
		vList.addAll(Arrays.asList(0.0f, 0.0f, length/2, 0.0f, 0.0f, -length/2));
		nList.addAll(Arrays.asList(0f, 0f, 1f, 0f, 0f, -1f));
		float v[] = new float[vList.size()];
		for(int i=0; i<vList.size(); ++i){
			v[i] = vList.get(i);
		}
		float normals[] = new float[nList.size()];
		for(int i=0; i<nList.size(); ++i) {
			normals[i] = nList.get(i);
		}
		
		// The colors
		ArrayList<Float> cList = new ArrayList<Float>();
		for(int i=0; i<n; ++i){
			cList.addAll(Arrays.asList(color1.x, color1.y, color1.z));
			cList.addAll(Arrays.asList(color1.x, color1.y, color1.z));
			cList.addAll(Arrays.asList(color2.x, color2.y, color2.z));
			cList.addAll(Arrays.asList(color2.x, color2.y, color2.z));
		}
		cList.addAll(Arrays.asList(color1.x, color1.y, color1.z));
		cList.addAll(Arrays.asList(color2.x, color2.y, color2.z));
		float c[] = new float[cList.size()];
		for(int i=0; i<cList.size(); ++i){
			c[i] = cList.get(i);
		}
		
		// The triangles
		ArrayList<Integer> iList = new ArrayList<Integer>();
		for(int i=0; i<2*n; i+=2){
			iList.addAll(Arrays.asList(4*n, i+1, (i+3)%(2*n)));
			iList.addAll(Arrays.asList(4*n+1, i+1+2*n, (i+3)%(2*n)+2*n));
			iList.addAll(Arrays.asList(i, i+2*n, (i+2)%(2*n)));
			iList.addAll(Arrays.asList(i+2*n, (i+2)%(2*n)+2*n, (i+2)%(2*n)));
		}
		int indices[] = new int[iList.size()];
		for(int i=0; i<iList.size(); ++i){
			indices[i] = iList.get(i);
		}
		
		VertexData vertexData = renderContext.makeVertexData(4*n+2);
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
		vertexData.addElement(normals, VertexData.Semantic.NORMAL, 3);
		vertexData.addIndices(indices);
		
		return new Shape(vertexData);
	}
	
	public static Shape createPlane(RenderContext renderContext, float height, float width, Vector3f color) {
		float[] v = {-width/2, height/2, 0,
					width/2, height/2, 0,
					width/2, -height/2, 0,
					-width/2, -height/2, 0 };
		
		float[] c = {color.x, color.y, color.z,
					color.x, color.y, color.z,
					color.x, color.y, color.z,
					color.x, color.y, color.z };
		
		float[] n = {0, 0, 1,
					0, 0, 1,
					0, 0, 1,
					0, 0, 1 };
		
		int[] indices = {0, 3, 1,
						3, 2, 1	};
		
		VertexData vertexData = renderContext.makeVertexData(4);
		vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
		vertexData.addIndices(indices);
		
		return new Shape(vertexData);
	}
	
	public static VertexData surfaceOfRevolution(RenderContext renderContext, int n, Vector2f[] controlPoints, int numberOfPoints, int rotationSteps) {
		assert controlPoints.length == (n-1)*3 + 4;
		
		ArrayList<Float> vList = new ArrayList<Float>();
		ArrayList<Float> nList = new ArrayList<Float>();
		ArrayList<Float> tList = new ArrayList<Float>();
		ArrayList<Integer> iList = new ArrayList<Integer>();
		
		for(int i=0; i < numberOfPoints; ++i) {
			float u = i * (((float) n)/numberOfPoints);
			int segment = (int) u;
			float t = u - segment;
			
			Vector2f p0 = controlPoints[segment*3];
			Vector2f p1 = controlPoints[segment*3+1];
			Vector2f p2 = controlPoints[segment*3+2];
			Vector2f p3 = controlPoints[segment*3+3];
			
			Vector2f q0 = new Vector2f((1-t)*p0.x + t*p1.x, (1-t)*p0.y + t*p1.y);
			Vector2f q1 = new Vector2f((1-t)*p1.x + t*p2.x, (1-t)*p1.y + t*p2.y);
			Vector2f q2 = new Vector2f((1-t)*p2.x + t*p3.x, (1-t)*p2.y + t*p3.y);
			
			Vector2f q3 = new Vector2f((1-t)*q0.x + t*q1.x, (1-t)*q0.y + t*q1.y);
			Vector2f q4 = new Vector2f((1-t)*q1.x + t*q2.x, (1-t)*q1.y + t*q2.y);
			
			Vector3f point = new Vector3f((1-t)*q3.x + t*q4.x, (1-t)*q3.y + t*q4.y, 0);
			Vector3f tangent = new Vector3f(q4.x - q3.x, q4.y - q3.y, 0);
			Vector3f normal = new Vector3f(-tangent.y, tangent.x, 0);
			normal.normalize();
			
			Matrix4f rot = new Matrix4f();
			rot.rotX((float)Math.PI*2 / rotationSteps);
			
			for(int j=0; j<rotationSteps; ++j) {
				vList.addAll(Arrays.asList(point.x, point.y, point.z));
				tList.addAll(Arrays.asList(i * 1f/(numberOfPoints-1), j * 1f/(rotationSteps-1)));
				nList.addAll(Arrays.asList(normal.x, normal.y, normal.z));
				
				rot.transform(point);
				rot.transform(normal);
				normal.normalize();
				
				if(i>0) {
					iList.addAll(Arrays.asList(i*rotationSteps + j, i*(rotationSteps-1) + j, i*(rotationSteps-1) + ((j+1)%rotationSteps)));
					iList.addAll(Arrays.asList(i*rotationSteps + j, i*(rotationSteps-1) + ((j+1)%rotationSteps), i*rotationSteps + ((j+1)%rotationSteps)));
				}
			}
		}
		
		float v[] = new float[vList.size()];
		for(int i=0; i<vList.size(); ++i){
			v[i] = vList.get(i);
		}
		
		float norm[] = new float[nList.size()];
		for(int i=0; i<nList.size(); ++i){
			norm[i] = nList.get(i);
		}
		
		float t[] = new float[tList.size()];
		for(int i=0; i<tList.size(); ++i){
			t[i] = tList.get(i);
		}
		
		int indices[] = new int[iList.size()];
		for(int i=0; i<iList.size(); ++i){
			indices[i] = iList.get(i);
		}
		
		VertexData vertexData = renderContext.makeVertexData(numberOfPoints * rotationSteps);
		vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
		vertexData.addElement(norm, VertexData.Semantic.NORMAL, 3);
		vertexData.addElement(t, VertexData.Semantic.TEXCOORD, 2);
		vertexData.addIndices(indices);
		
		return vertexData;
	}
}
