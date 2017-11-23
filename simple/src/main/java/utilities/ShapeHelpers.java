package utilities;

import java.util.ArrayList;
import java.util.Arrays;

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
}
