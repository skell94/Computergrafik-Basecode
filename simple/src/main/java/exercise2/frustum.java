package exercise2;

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
public class frustum
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
		public static Shape makeHouse()
		{
			// A house
			float vertices[] = {-4,-4,4, 4,-4,4, 4,4,4, -4,4,4,		// front face
								-4,-4,-4, -4,-4,4, -4,4,4, -4,4,-4, // left face
								4,-4,-4,-4,-4,-4, -4,4,-4, 4,4,-4,  // back face
								4,-4,4, 4,-4,-4, 4,4,-4, 4,4,4,		// right face
								4,4,4, 4,4,-4, -4,4,-4, -4,4,4,		// top face
								-4,-4,4, -4,-4,-4, 4,-4,-4, 4,-4,4, // bottom face
		
								-20,-4,20, 20,-4,20, 20,-4,-20, -20,-4,-20, // ground floor
								-4,4,4, 4,4,4, 0,8,4,				// the roof
								4,4,4, 4,4,-4, 0,8,-4, 0,8,4,
								-4,4,4, 0,8,4, 0,8,-4, -4,4,-4,
								4,4,-4, -4,4,-4, 0,8,-4};
		
			float normals[] = {0,0,1,  0,0,1,  0,0,1,  0,0,1,		// front face
							   -1,0,0, -1,0,0, -1,0,0, -1,0,0,		// left face
							   0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1,		// back face
							   1,0,0,  1,0,0,  1,0,0,  1,0,0,		// right face
							   0,1,0,  0,1,0,  0,1,0,  0,1,0,		// top face
							   0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0,		// bottom face
		
							   0,1,0,  0,1,0,  0,1,0,  0,1,0,		// ground floor
							   0,0,1,  0,0,1,  0,0,1,				// front roof
							   0.707f,0.707f,0, 0.707f,0.707f,0, 0.707f,0.707f,0, 0.707f,0.707f,0, // right roof
							   -0.707f,0.707f,0, -0.707f,0.707f,0, -0.707f,0.707f,0, -0.707f,0.707f,0, // left roof
							   0,0,-1, 0,0,-1, 0,0,-1};				// back roof
							   
			float colors[] = {1,0,0, 1,0,0, 1,0,0, 1,0,0,
							  0,1,0, 0,1,0, 0,1,0, 0,1,0,
							  1,0,0, 1,0,0, 1,0,0, 1,0,0,
							  0,1,0, 0,1,0, 0,1,0, 0,1,0,
							  0,0,1, 0,0,1, 0,0,1, 0,0,1,
							  0,0,1, 0,0,1, 0,0,1, 0,0,1,
			
							  0,0.5f,0, 0,0.5f,0, 0,0.5f,0, 0,0.5f,0,			// ground floor
							  0,0,1, 0,0,1, 0,0,1,							// roof
							  1,0,0, 1,0,0, 1,0,0, 1,0,0,
							  0,1,0, 0,1,0, 0,1,0, 0,1,0,
							  0,0,1, 0,0,1, 0,0,1,};
		
			// Set up the vertex data
			VertexData vertexData = renderContext.makeVertexData(42);
		
			// Specify the elements of the vertex data:
			// - one element for vertex positions
			vertexData.addElement(vertices, VertexData.Semantic.POSITION, 3);
			// - one element for vertex colors
			vertexData.addElement(colors, VertexData.Semantic.COLOR, 3);
			// - one element for vertex normals
			vertexData.addElement(normals, VertexData.Semantic.NORMAL, 3);
			
			// The index data that stores the connectivity of the triangles
			int indices[] = {0,2,3, 0,1,2,			// front face
							 4,6,7, 4,5,6,			// left face
							 8,10,11, 8,9,10,		// back face
							 12,14,15, 12,13,14,	// right face
							 16,18,19, 16,17,18,	// top face
							 20,22,23, 20,21,22,	// bottom face
			                 
							 24,26,27, 24,25,26,	// ground floor
							 28,29,30,				// roof
							 31,33,34, 31,32,33,
							 35,37,38, 35,36,37,
							 39,40,41};	
		
			vertexData.addIndices(indices);
		
			Shape house = new Shape(vertexData);
			
			return house;
		}
		
		
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		public void init(RenderContext r)
		{
			renderContext = r;
			
			// Make a scene manager and add the object
			sceneManager = new SimpleSceneManager();
			
			Frustum frustum = new Frustum(1.f, 100.f, 1.f, (float)(Math.PI/3));
			sceneManager.setFrustum(frustum);
			
			Camera camera = new Camera(new Vector3f(0.f, 0.f, 40.f), new Vector3f(0.f, 0.f, 0.f), new Vector3f(0.f, 1.f, 0.f));
//			Camera camera = new Camera(new Vector3f(-10.f, 40.f, 40.f), new Vector3f(-5.f, 0.f, 0.f), new Vector3f(0.f, 1.f, 0.f));
			sceneManager.setCamera(camera);
			
			sceneManager.addShape(makeHouse());

			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);
			
			// Load some more shaders
		    normalShader = renderContext.makeShader();
		    try {
		    	normalShader.load("../jrtr/shaders/normal.vert", "../jrtr/shaders/normal.frag");
		    } catch(Exception e) {
		    	System.out.print("Problem with shader:\n");
		    	System.out.print(e.getMessage());
		    }
	
		    diffuseShader = renderContext.makeShader();
		    try {
		    	diffuseShader.load("../jrtr/shaders/diffuse.vert", "../jrtr/shaders/diffuse.frag");
		    } catch(Exception e) {
		    	System.out.print("Problem with shader:\n");
		    	System.out.print(e.getMessage());
		    }

		    // Make a material that can be used for shading
			material = new Material();
			material.shader = diffuseShader;
			material.diffuseMap = renderContext.makeTexture();
			try {
				material.diffuseMap.load("../textures/plant.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
		}
	}

	/**
	 * A mouse listener for the main window of this application. This can be
	 * used to process mouse events.
	 */
	public static class SimpleMouseListener implements MouseListener
	{
    	public void mousePressed(MouseEvent e) {}
    	public void mouseReleased(MouseEvent e) {}
    	public void mouseEntered(MouseEvent e) {}
    	public void mouseExited(MouseEvent e) {}
    	public void mouseClicked(MouseEvent e) {}
	}
	
	/**
	 * A key listener for the main window. Use this to process key events.
	 * Currently this provides the following controls:
	 * 's': stop animation
	 * 'p': play animation
	 * '+': accelerate rotation
	 * '-': slow down rotation
	 * 'd': default shader
	 * 'n': shader using surface normals
	 * 'm': use a material for shading
	 */
	public static class SimpleKeyListener implements KeyListener
	{
		public void keyPressed(KeyEvent e)
		{
			switch(e.getKeyChar())
			{
				case 's': {
					// Stop animation
					currentstep = 0;
					break;
				}
				case 'p': {
					// Resume animation
					currentstep = basicstep;
					break;
				}
				case '+': {
					// Accelerate roation
					currentstep += basicstep;
					break;
				}
				case '-': {
					// Slow down rotation
					currentstep -= basicstep;
					break;
				}
				case 'n': {
					// Remove material from shape, and set "normal" shader
					shape.setMaterial(null);
					renderContext.useShader(normalShader);
					break;
				}
				case 'd': {
					// Remove material from shape, and set "default" shader
					shape.setMaterial(null);
					renderContext.useDefaultShader();
					break;
				}
				case 'm': {
					// Set a material for more complex shading of the shape
					if(shape.getMaterial() == null) {
						shape.setMaterial(material);
					} else
					{
						shape.setMaterial(null);
						renderContext.useDefaultShader();
					}
					break;
				}
			}
			
			// Trigger redrawing
			renderPanel.getCanvas().repaint();
		}
		
		public void keyReleased(KeyEvent e)
		{
		}

		public void keyTyped(KeyEvent e)
        {
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
		jframe.setSize(500, 500);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

		// Add a mouse and key listener
	    renderPanel.getCanvas().addMouseListener(new SimpleMouseListener());
	    renderPanel.getCanvas().addKeyListener(new SimpleKeyListener());
		renderPanel.getCanvas().setFocusable(true);   	    	    
	    
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}
