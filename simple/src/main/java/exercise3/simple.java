package exercise3;

import jrtr.*;
import jrtr.glrenderer.*;
import jrtr.swrenderer.SWRenderPanel;
import jrtr.gldeferredrenderer.*;

import javax.swing.*;
import java.awt.event.*;
import javax.vecmath.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implements a simple application that opens a 3D rendering window and 
 * shows a rotating cube.
 */
public class simple
{	
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader;
	static Shader diffuseShader;
	static Material material;
	static SimpleSceneManager sceneManager;
	static Shape shape;
	static float currentstep, basicstep;

	/**
	 * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to 
	 * provide a call-back function for initialization. Here we construct
	 * a simple 3D scene and start a timer task to generate an animation.
	 */ 
	public final static class SimpleRenderPanel extends SWRenderPanel
	{
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		public void init(RenderContext r)
		{
			renderContext = r;
			
			// Make a simple geometric object: a cube
			
			// The vertex positions of the cube
			float v[] = {-1,-1,1, 1,-1,1, 1,1,1, -1,1,1,		// front face
				         -1,-1,-1, -1,-1,1, -1,1,1, -1,1,-1,	// left face
					  	 1,-1,-1,-1,-1,-1, -1,1,-1, 1,1,-1,		// back face
						 1,-1,1, 1,-1,-1, 1,1,-1, 1,1,1,		// right face
						 1,1,1, 1,1,-1, -1,1,-1, -1,1,1,		// top face
						-1,-1,1, -1,-1,-1, 1,-1,-1, 1,-1,1};	// bottom face

			// The vertex normals 
			float n[] = {0,0,1, 0,0,1, 0,0,1, 0,0,1,			// front face
				         -1,0,0, -1,0,0, -1,0,0, -1,0,0,		// left face
					  	 0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1,		// back face
						 1,0,0, 1,0,0, 1,0,0, 1,0,0,			// right face
						 0,1,0, 0,1,0, 0,1,0, 0,1,0,			// top face
						 0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0};		// bottom face

			// The vertex colors
			float c[] = {1,0,0, 1,0,0, 1,0,0, 1,0,0,
					     0,1,0, 0,1,0, 0,1,0, 0,1,0,
						 1,0,0, 1,0,0, 1,0,0, 1,0,0,
						 0,1,0, 0,1,0, 0,1,0, 0,1,0,
						 0,0,1, 0,0,1, 0,0,1, 0,0,1,
						 0,0,1, 0,0,1, 0,0,1, 0,0,1};

			// Texture coordinates 
			float uv[] = {0,0, 1,0, 1,1, 0,1,
					  0,0, 1,0, 1,1, 0,1,
					  0,0, 1,0, 1,1, 0,1,
					  0,0, 1,0, 1,1, 0,1,
					  0,0, 1,0, 1,1, 0,1,
					  0,0, 1,0, 1,1, 0,1};

			// Construct a data structure that stores the vertices, their
			// attributes, and the triangle mesh connectivity
			VertexData vertexData = renderContext.makeVertexData(24);
			vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
			vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
			vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
			vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);
			
			// The triangles (three vertex indices for each triangle)
			int indices[] = {0,2,3, 0,1,2,			// front face
							 4,6,7, 4,5,6,			// left face
							 8,10,11, 8,9,10,		// back face
							 12,14,15, 12,13,14,	// right face
							 16,18,19, 16,17,18,	// top face
							 20,22,23, 20,21,22};	// bottom face

			vertexData.addIndices(indices);
								
			// Make a scene manager and add the object
			sceneManager = new SimpleSceneManager();
			shape = new Shape(vertexData);
			shape.setMaterial(new Material());
			shape.getMaterial().setTexture("../textures/face.png");
			sceneManager.addShape(shape);
			
			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);

			// Register a timer task
		    Timer timer = new Timer();
		    basicstep = 0.01f;
		    currentstep = basicstep;
		    timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
		}
	}

	/**
	 * A timer task that generates an animation. This task triggers
	 * the redrawing of the 3D scene every time it is executed.
	 */
	public static class AnimationTask extends TimerTask
	{
		public void run()
		{
			// Update transformation by rotating with angle "currentstep"
    		Matrix4f t = shape.getTransformation();
    		Matrix4f rotX = new Matrix4f();
    		rotX.rotX(currentstep);
    		Matrix4f rotY = new Matrix4f();
    		rotY.rotY(currentstep);
    		t.mul(rotX);
    		t.mul(rotY);
    		shape.setTransformation(t);
    		
    		// Trigger redrawing of the render window
    		renderPanel.getCanvas().repaint(); 
		}
	}
	
	/**
	 * The main function opens a 3D rendering window, implemented by the class
	 * {@link SimpleRenderPanel}. {@link SimpleRenderPanel} is then called backed 
	 * for initialization automatically. It then constructs a simple 3D scene, 
	 * and starts a timer task to generate an animation.
	 */
	public static void main(String[] args)
	{		
		// Make a render panel. The init function of the renderPanel
		// (see above) will be called back for initialization.
		renderPanel = new SimpleRenderPanel();
		
		// Make the main window of this application and add the renderer to it
		JFrame jframe = new JFrame("simple");
		jframe.setSize(1000, 1000);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

		// Add a mouse and key listener
		renderPanel.getCanvas().setFocusable(true);   	    	    
	    
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}
