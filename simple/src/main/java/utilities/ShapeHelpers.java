package utilities;

import java.util.ArrayList;
import java.util.Arrays;

import javax.vecmath.Vector3f;

import jrtr.*;

public abstract class ShapeHelpers {
	
	public static Shape createCylinder(RenderContext renderContext, int n, float length, float radius, Vector3f color1, Vector3f color2) {
		ArrayList<Float> vList = new ArrayList<Float>();
		for(int i=0; i<n; ++i){
			double angle = i*(1.0/n)*2*Math.PI;
			vList.addAll(Arrays.asList((float)(radius * Math.sin(angle)), (float)(radius * Math.cos(angle)), length/2));
		}
		for(int i=0; i<n; ++i){
			double angle = i*(1.0/n)*2*Math.PI;
			vList.addAll(Arrays.asList((float)(radius * Math.sin(angle)), (float)(radius * Math.cos(angle)), -length/2));
		}
		vList.addAll(Arrays.asList(0.0f, 0.0f, length/2, 0.0f, 0.0f, -length/2));
		float v[] = new float[vList.size()];
		for(int i=0; i<vList.size(); ++i){
			v[i] = vList.get(i);
		}
		
		// The colors
		ArrayList<Float> cList = new ArrayList<Float>();
		for(int i=0; i<n; ++i){
			cList.addAll(Arrays.asList(color1.x, color1.y, color1.z));
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
		for(int i=0; i<n; ++i){
			iList.addAll(Arrays.asList(2*n, i, (i+1)%n));
			iList.addAll(Arrays.asList(2*n+1, i+n, (i+1)%n+n));
			iList.addAll(Arrays.asList(i, i+n, (i+1)%n));
			iList.addAll(Arrays.asList(i+n, (i+1)%n+n, (i+1)%n));
		}
		int indices[] = new int[iList.size()];
		for(int i=0; i<iList.size(); ++i){
			indices[i] = iList.get(i);
		}
		
		VertexData vertexData = renderContext.makeVertexData(2*n+2);
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
		vertexData.addIndices(indices);
		
		return new Shape(vertexData);
	}
}
