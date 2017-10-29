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
		SWVertexData vertexData = (SWVertexData) renderItem.getShape().getVertexData();
		Matrix4f t = renderItem.getShape().getTransformation();
		
		ListIterator<VertexData.VertexElement> itr = vertexData.getElements()
				.listIterator(0);
		while (itr.hasNext()) {
			VertexData.VertexElement e = itr.next();

			switch (e.getSemantic()) {
			case POSITION:
				drawVertices(e, t);
				break;
			case NORMAL:
				break;
			case COLOR:
				break;
			case TEXCOORD:
				break;
			}
		}
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
