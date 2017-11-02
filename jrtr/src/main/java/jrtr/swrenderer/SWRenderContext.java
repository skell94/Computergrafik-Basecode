package jrtr.swrenderer;

import jrtr.RenderContext;
import jrtr.RenderItem;
import jrtr.SceneManagerInterface;
import jrtr.SceneManagerIterator;
import jrtr.Shader;
import jrtr.Texture;
import jrtr.VertexData;
import jrtr.VertexData.VertexElement;
import jrtr.glrenderer.GLRenderPanel;

import java.awt.Color;
import java.awt.image.*;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.media.opengl.GL3;
import javax.vecmath.*;


/**
 * A skeleton for a software renderer. It works in combination with
 * {@link SWRenderPanel}, which displays the output image. In project 3 
 * you will implement your own rasterizer in this class.
 * <p>
 * To use the software renderer, you will simply replace {@link GLRenderPanel} 
 * with {@link SWRenderPanel} in the user application.
 */
public class SWRenderContext implements RenderContext {

	private SceneManagerInterface sceneManager;
	private BufferedImage colorBuffer;
	private Matrix4f viewPortMatrix;
	private float[][] zBuffer;
	private boolean useTexture = true;

	public void setSceneManager(SceneManagerInterface sceneManager)
	{
		this.sceneManager = sceneManager;
	}

	/**
	 * This is called by the SWRenderPanel to render the scene to the 
	 * software frame buffer.
	 */
	public void display()
	{
		if(sceneManager == null) return;

		beginFrame();

		SceneManagerIterator iterator = sceneManager.iterator();	
		while(iterator.hasNext())
		{
			draw(iterator.next());
		}		

		endFrame();
	}

	/**
	 * This is called by the {@link SWJPanel} to obtain the color buffer that
	 * will be displayed.
	 */
	public BufferedImage getColorBuffer()
	{
		return colorBuffer;
	}

	/**
	 * Set a new viewport size. The render context will also need to store
	 * a viewport matrix, which you need to reset here. 
	 */
	public void setViewportSize(int width, int height)
	{
		colorBuffer = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
	}

	/**
	 * Clear the framebuffer here.
	 */
	private void beginFrame()
	{
	}

	private void endFrame()
	{		
	}

	/**
	 * The main rendering method. You will need to implement this to draw
	 * 3D objects.
	 */
	private void draw(RenderItem renderItem)
	{
		setViewportSize(colorBuffer.getWidth(), colorBuffer.getHeight());
		
		BufferedImage texture = null;
		if(useTexture)
			texture = renderItem.getShape().getMaterial().texture.getBufferedImage();
		
		SWVertexData vertexData = (SWVertexData) renderItem.getShape().getVertexData();
		LinkedList<VertexElement> elements = vertexData.getElements();
		float[][] position = new float[3][4];
		float[][] color = new float[3][3];
		float[][] tex = new float[3][2];
		zBuffer = new float[colorBuffer.getWidth()][colorBuffer.getHeight()];
		int[] indices = vertexData.getIndices();

		int maxX = colorBuffer.getWidth();
		int maxY = colorBuffer.getHeight();
		Matrix4f t = renderItem.getShape().getTransformation();
		Matrix4f cameraMatrix = (Matrix4f) sceneManager.getCamera().getCameraMatrix().clone();
		Matrix4f projectionMatrix = sceneManager.getFrustum().getProjectionMatrix();
		setViewPortMatrix(maxY, maxX);

		int k = 0;
		for(int j=0; j < indices.length; ++j) {
			int i = indices[j];
			ListIterator<VertexData.VertexElement> itr = elements.listIterator(0);
			while (itr.hasNext()) {
				VertexData.VertexElement e = itr.next();
				float[] data = e.getData();

				switch (e.getSemantic()) {
				case POSITION:
					Vector4f v = new Vector4f(data[i*3], data[i*3+1], data[i*3+2], 1.f);
					t.transform(v);
					cameraMatrix.transform(v);
					projectionMatrix.transform(v);
					viewPortMatrix.transform(v);

					position[k][0] = v.x;
					position[k][1] = v.y;
					position[k][2] = v.z;
					position[k][3] = v.w;
					break;
				case NORMAL:
					break;
				case COLOR:
					color[k][0] = data[i*3];
					color[k][1] = data[i*3+1];
					color[k][2] = data[i*3+2];
					break;
				case TEXCOORD:
					tex[k][0] = data[i*2];
					tex[k][1] = data[i*2+1];
					break;
				}
			}
			++k;
			if(k == 3) {
				drawTriangle(position, color, tex, texture);
				k = 0;
			}
		}
	}

	private void drawTriangle(float[][] position, float[][] color, float[][] tex, BufferedImage texture) {
		int minX = Math.round(Math.min(position[0][0]/position[0][3], Math.min(position[1][0]/position[1][3], position[2][0]/position[2][3])));
		int maxX = Math.round(Math.max(position[0][0]/position[0][3], Math.max(position[1][0]/position[1][3], position[2][0]/position[2][3])));
		int minY = Math.round(Math.min(position[0][1]/position[0][3], Math.min(position[1][1]/position[1][3], position[2][1]/position[2][3])));
		int maxY = Math.round(Math.max(position[0][1]/position[0][3], Math.max(position[1][1]/position[1][3], position[2][1]/position[2][3])));
		if(minX >= colorBuffer.getWidth() || maxX < 0 || minY >= colorBuffer.getHeight() || maxY < 0)
			return;
		if(minX < 0)
			minX = 0;
		if(minY < 0)
			minY = 0;
		if(maxX >= colorBuffer.getWidth())
			maxX = colorBuffer.getWidth()-1;
		if(maxY >= colorBuffer.getHeight())
			maxY = colorBuffer.getHeight()-1;
		
		Matrix3f edge = new Matrix3f(position[0][0], position[0][1], position[0][3],
				position[1][0], position[1][1], position[1][3],
				position[2][0], position[2][1], position[2][3]);
		edge.invert();
		
		Vector3f rv = new Vector3f(color[0][0], color[1][0], color[2][0]);
		Vector3f gv = new Vector3f(color[0][1], color[1][1], color[2][1]);
		Vector3f bv = new Vector3f(color[0][2], color[1][2], color[2][2]);
		
		Vector3f tx = new Vector3f(tex[0][0], tex[1][0], tex[2][0]);
		Vector3f ty = new Vector3f(tex[0][1], tex[1][1], tex[2][1]);
		
		edge.transform(rv);
		edge.transform(gv);
		edge.transform(bv);
		
		edge.transform(tx);
		edge.transform(ty);
		
		for(int i = minX; i <= maxX; ++i) {
			for(int j = minY; j <= maxY; ++j) {
				float a = i*edge.m00 + j*edge.m10 + edge.m20;
				float b = i*edge.m01 + j*edge.m11 + edge.m21;
				float c = i*edge.m02 + j*edge.m12 + edge.m22;
				
				float w = 1.f/(a + b + c);
				if(a <= 0 || b <= 0 || c <= 0 || zBuffer[i][j] >= 1/w)
					continue;
				
				zBuffer[i][j] = 1/w;
				
				float r = i*w*rv.x + j*w*rv.y + w*rv.z;
				float g = i*w*gv.x + j*w*gv.y + w*gv.z;
				float bl = i*w*bv.x + j*w*bv.y + w*bv.z;
				
				int rgb = (Math.round(r*255) << 16) | ((Math.round(g*255) << 8) | Math.round(bl*255));
				
				if(useTexture){
					float texXF = i*w*tx.x + j*w*tx.y + w*tx.z;
					float texYF = i*w*ty.x + j*w*ty.y + w*ty.z;
					texXF = Math.min(texXF, 1.f);
					texYF = Math.min(texYF, 1.f);
					texXF = Math.max(texXF, 0.f);
					texYF = Math.max(texYF, 0.f);
					
					texXF *= (texture.getWidth()-1);
					texYF = texture.getHeight()-1 - texYF*(texture.getHeight()-1);
					rgb = nearestNeighbor(texXF, texYF, texture);
					rgb = interpolate(texXF, texYF, texture);
				}
				colorBuffer.setRGB(i, j, rgb);
			}
		}
	}

	private int interpolate(float texXF, float texYF, BufferedImage texture) {
		Color color, color1, color2, x1y1, x2y1, x1y2, x2y2;
		
		float xFract = texXF % 1;
		float yFract = texYF % 1;
		
		x1y1 = new Color(texture.getRGB((int)Math.floor(texXF), (int)Math.floor(texYF)));
		x1y2 = new Color(texture.getRGB((int)Math.floor(texXF), (int)Math.ceil(texYF)));
		x2y1 = new Color(texture.getRGB((int)Math.ceil(texXF), (int)Math.floor(texYF)));
		x2y2 = new Color(texture.getRGB((int)Math.ceil(texXF), (int)Math.ceil(texYF)));
		
		color1 = new Color(Math.round((1-xFract)*x1y1.getRed() + xFract*x2y1.getRed()),
						Math.round((1-xFract)*x1y1.getGreen() + xFract*x2y1.getGreen()),
						Math.round((1-xFract)*x1y1.getBlue() + xFract*x2y1.getBlue()));
		color2 = new Color(Math.round((1-xFract)*x1y2.getRed() + xFract*x2y2.getRed()),
				Math.round((1-xFract)*x1y2.getGreen() + xFract*x2y2.getGreen()),
				Math.round((1-xFract)*x1y2.getBlue() + xFract*x2y2.getBlue()));
		
		color = new Color(Math.round((1-yFract)*color1.getRed() + yFract*color2.getRed()),
				Math.round((1-yFract)*color1.getGreen() + yFract*color2.getGreen()),
				Math.round((1-yFract)*color1.getBlue() + yFract*color2.getBlue()));
		return color.getRGB();
	}

	private int nearestNeighbor(float texX, float texY, BufferedImage texture) {
		return texture.getRGB(Math.round(texX), Math.round(texY));
	}

	/**
	 * Does nothing. We will not implement shaders for the software renderer.
	 */
	public Shader makeShader()	
	{
		return new SWShader();
	}

	/**
	 * Does nothing. We will not implement shaders for the software renderer.
	 */
	public void useShader(Shader s)
	{
	}

	/**
	 * Does nothing. We will not implement shaders for the software renderer.
	 */
	public void useDefaultShader()
	{
	}

	/**
	 * Does nothing. We will not implement textures for the software renderer.
	 */
	public Texture makeTexture()
	{
		return new SWTexture();
	}

	public VertexData makeVertexData(int n)
	{
		return new SWVertexData(n);		
	}

	private void setViewPortMatrix(int height, int width) {
		viewPortMatrix = new Matrix4f(width/2.f, 0.f, 0.f, width/2.f,
				0.f, -height/2.f, 0.f, height/2.f,
				0.f, 0.f, 0.5f, 0.5f,
				0.f, 0.f, 0.f, 1.f);
	}

	private void drawVertices(VertexElement e, Matrix4f t) {
		float[] data = e.getData();
		int maxX = colorBuffer.getWidth();
		int maxY = colorBuffer.getHeight();
		Matrix4f cameraMatrix = (Matrix4f) sceneManager.getCamera().getCameraMatrix().clone();
		Matrix4f projectionMatrix = sceneManager.getFrustum().getProjectionMatrix();
		Matrix4f viewPortMatrix = new Matrix4f(maxX/2.f, 0.f, 0.f, maxX/2.f,
				0.f, -maxY/2.f, 0.f, maxY/2.f,
				0.f, 0.f, 0.5f, 0.5f,
				0.f, 0.f, 0.f, 1.f);
		for(int i=0; i < data.length; i+=3) {
			Vector4f v = new Vector4f(data[i], data[i+1], data[i+2], 1.f);
			t.transform(v);
			cameraMatrix.transform(v);
			projectionMatrix.transform(v);
			viewPortMatrix.transform(v);

			int pixelX = Math.round(v.x/v.w);
			int pixelY = Math.round(v.y/v.w);
			System.out.println((i+1)+": "+pixelX+" "+pixelY);
			if(pixelX >= 0 && pixelX <= maxX && pixelY >= 0 && pixelY <= maxY)
				colorBuffer.setRGB(pixelX, pixelY, ((255 << 16) | ((255 << 8) | 255)));
		}
	}
}
