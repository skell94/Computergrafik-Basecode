package utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.vecmath.Vector3f;

import jrtr.RenderContext;
import jrtr.VertexData;

public class FractalLandscape {

	private float[][] landscape;
	private int height;
	private int size;
	private final float WIDTH = 50.f;

	public FractalLandscape(int power, int height) {
		this.size = (int) Math.pow(2, power) + 1;
		landscape = new float[size][size];
		this.height = height;

		Random random = new Random();
		landscape[0][0] = (random.nextFloat() - 0.5f) * height;
		landscape[0][size-1] = (random.nextFloat() - 0.5f) * height;
		landscape[size-1][0] = (random.nextFloat() - 0.5f) * height;
		landscape[size-1][size-1] = (random.nextFloat() - 0.5f) * height;

		diamond(0, size-1, 0, size-1);
//		for(int i = 0; i<size; ++i) {
//			for(int j=0; j<size; ++j) {
//				System.out.print(landscape[i][j]+" ");
//			}
//			System.out.println();
//		}
	}

	public VertexData getVertexData(RenderContext renderContext) {
		ArrayList<Float> vList = new ArrayList<Float>();
		for(int i = 0; i < size; ++i) {
			for(int j = 0; j < size; ++j) {
				vList.addAll(Arrays.asList((float)i/size*WIDTH-WIDTH/2, (float)j/size*WIDTH-WIDTH/2, landscape[i][j]));
			}
		}
		float v[] = new float[vList.size()];
		for(int i=0; i<vList.size(); ++i){
			v[i] = vList.get(i);
		}
		
		ArrayList<Float> nList = new ArrayList<Float>();
		for(int i = 0; i < size; ++i) {
			for(int j = 0; j < size; ++j) {
				Vector3fPlus normal = new Vector3fPlus(0, 0, 0);
				Vector3fPlus firstVector, secondVector, cross = new Vector3fPlus(), point = new Vector3fPlus(i, j, landscape[i][j]);
				if(i != 0 && j != 0) {
					firstVector = new Vector3fPlus(i, j-1, landscape[i][j-1]);
					secondVector = new Vector3fPlus(i-1, j-1, landscape[i-1][j-1]);
					firstVector.sub(point);
					secondVector.sub(point);
					cross.cross(secondVector, firstVector);
					cross.normalize();
					normal.add(cross);
					
					firstVector = new Vector3fPlus(i-1, j-1, landscape[i-1][j-1]);
					secondVector = new Vector3fPlus(i-1, j, landscape[i-1][j]);
					firstVector.sub(point);
					secondVector.sub(point);
					cross.cross(secondVector, firstVector);
					cross.normalize();
					normal.add(cross);
				}
				if(i != 0 && j != size-1) {
					firstVector = new Vector3fPlus(i-1, j, landscape[i-1][j]);
					secondVector = new Vector3fPlus(i, j+1, landscape[i][j+1]);
					firstVector.sub(point);
					secondVector.sub(point);
					cross.cross(secondVector, firstVector);
					cross.normalize();
					normal.add(cross);
				}
				if(i != size-1 && j != 0) {
					firstVector = new Vector3fPlus(i+1, j, landscape[i+1][j]);
					secondVector = new Vector3fPlus(i, j-1, landscape[i][j-1]);
					firstVector.sub(point);
					secondVector.sub(point);
					cross.cross(secondVector, firstVector);
					cross.normalize();
					normal.add(cross);
				}
				if(i != size-1 && j != size-1) {
					firstVector = new Vector3fPlus(i, j+1, landscape[i][j+1]);
					secondVector = new Vector3fPlus(i+1, j+1, landscape[i+1][j+1]);
					firstVector.sub(point);
					secondVector.sub(point);
					cross.cross(secondVector, firstVector);
					cross.normalize();
					normal.add(cross);
					
					firstVector = new Vector3fPlus(i+1, j+1, landscape[i+1][j+1]);
					secondVector = new Vector3fPlus(i+1, j, landscape[i+1][j]);
					firstVector.sub(point);
					secondVector.sub(point);
					cross.cross(secondVector, firstVector);
					cross.normalize();
					normal.add(cross);
				}
				normal.normalize();
				nList.addAll(Arrays.asList(normal.x, normal.y, normal.z));
			}
		}
		float n[] = new float[nList.size()];
		for(int i=0; i<nList.size(); ++i){
			n[i] = nList.get(i);
		}

		// The vertex colors
		ArrayList<Float> cList = new ArrayList<Float>();
		for(int i = 0; i < size; ++i) {
			for(int j = 0; j < size; ++j) {
				cList.addAll(Arrays.asList(calculateColor(landscape[i][j])));
			}
		}
		float c[] = new float[cList.size()];
		for(int i=0; i<cList.size(); ++i){
			c[i] = cList.get(i);
		}
		// Construct a data structure that stores the vertices, their
		// attributes, and the triangle mesh connectivity
		VertexData vertexData = renderContext.makeVertexData(v.length/3);
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
		vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);

		// The triangles (three vertex indices for each triangle)
		ArrayList<Integer> indicesList = new ArrayList<Integer>();
		for(int i = 0; i < size-1; ++i) {
			for(int j = 0; j < size-1; ++j) {
				indicesList.addAll(Arrays.asList(i+j*size, i+(j+1)*size, (i+1)+(j+1)*size));
				indicesList.addAll(Arrays.asList(i+j*size, (i+1)+(j+1)*size, (i+1)+j*size));
			}
		}
		int indices[] = new int[indicesList.size()];
		for(int i=0; i<indicesList.size(); ++i){
			indices[i] = indicesList.get(i);
		}

		vertexData.addIndices(indices);
		return vertexData;
	}

	private void square(int startX, int endX, int startY, int endY) {
		if(endX - startX == 1 || endY - startY == 1)
			return;

		int middleX = (startX+endX)/2;
		int middleY = (startY+endY)/2;

		float average;
		if(startX < 0) {
			average = (landscape[middleX][startY] + landscape[endX][middleY] + landscape[middleX][endY])/3.f;
		} else if(endX > size-1) {
			average = (landscape[startX][middleY] + landscape[middleX][startY] + landscape[middleX][endY])/3.f;
		} else if (startY < 0) {
			average = (landscape[startX][middleY] + landscape[endX][middleY] + landscape[middleX][endY])/3.f;
		} else if (endY > size-1) {
			average = (landscape[startX][middleY] + landscape[endX][middleY] + landscape[middleX][startY])/3.f;
		} else {
			average = (landscape[startX][middleY] + landscape[middleX][startY] + landscape[endX][middleY] + landscape[middleX][endY])/4.f;
		}
		Random random = new Random();

		landscape[middleX][middleY] = average + (random.nextFloat() - 0.5f) * height * ((startX - endX) / (size-1.f));

		if(startX >= 0 && startY >= 0) {
			diamond(startX, middleX, startY, middleY);
		} 
		if(startY >= 0 && endX <= size-1) {
			diamond(middleX, endX, startY, middleY);
		} 
		if (startX >= 0 && endY <= size-1) {
			diamond(startX, middleX, middleY, endY);
		} 
		if (endX <= size-1 && endY <= size-1) {
			diamond(middleX, endX, middleY, endY);
		}
	}

	private void diamond(int startX, int endX, int startY, int endY) {
		if(endX - startX == 1 || endY - startY == 1)
			return;
		float average = (landscape[startX][startY] + landscape[startX][endY] + landscape[endX][startY] + landscape[endX][endY])/4.f;
		Random random = new Random();

		int middleX = (startX+endX)/2;
		int middleY = (startY+endY)/2;

		landscape[middleX][middleY] = average + (random.nextFloat() - 0.5f) * height * ((startX - endX) / (size-1.f));

		square(startX-(endX-startX)/2, middleX, startY, endY);
		square(middleX, endX+(endX-startX)/2, startY, endY);
		square(startX, endX, startY-(endY-startY)/2, middleY);
		square(startX, endX, middleY, endY+(endY-startY)/2);
	}
	
	private Float[] calculateColor(float z) {
		Float[] green = {0.f, 0.3f, 0.f};
		float average = (z+height/2)/height;
		Random random = new Random();
		
		if(average <= 0.65f) {
			return green;
		}
		if(average > 0.65f && average < 0.75f) {
			float greyProbability = average*10 - 6.5f;
			float randomValue = random.nextFloat();
			if(randomValue <= greyProbability) {
				return getGrey(average);
			} else {
				return green;
			}
		}
		return getGrey(average);
	}
	
	private Float[] getGrey(float average){
		Random random = new Random();
		float variation = random.nextFloat() / 20;
		float color = average + variation;
		if(color > 1)
			color = 1;
		Float[] array = {color, color, color};
		return array;
	}
}
