package exercise1;

import jrtr.*;
import jrtr.glrenderer.*;
import src.main.java.exercise1.SWRenderPanel;
import src.main.java.exercise1.cylinder.AnimationTask;
import src.main.java.exercise1.cylinder.SimpleKeyListener;
import src.main.java.exercise1.cylinder.SimpleMouseListener;
import src.main.java.exercise1.cylinder.SimpleRenderPanel;
import jrtr.gldeferredrenderer.*;

import javax.swing.*;
import java.awt.event.*;
import javax.vecmath.*;
import java.util.Timer;
import java.util.TimerTask;

public class cylinder {
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
	public final static class SimpleRenderPanel extends GLRenderPanel
	{
		/**
		 * create VertexData for Cylinder
		 * 
		 * @param r the render context, n the number of sections the cylinder is divided into
		 */
		private VertexData cylinder(RenderContext r, int n){
			// The vertex positions of the cylinder
			float v[] = new float[(2*n+2)*3];
			v[v.length-6] = 0;
			v[v.length-5] = 0;
			v[v.length-4] = 3;
			v[v.length-3] = 0;
			v[v.length-2] = 0;
			v[v.length-1] = -3;
			
			for(int i = 0; i < n*3; i+=3){
				double angle = (i/3.0)*(1.0/n)*2*Math.PI;
				v[i] = v[i+n*3] = (float) (Math.sin(angle));
				v[i+1] = v[i+1+n*3] = (float) (Math.cos(angle));
				v[i+2] = 3;
				v[i+2+n*3] = -3;
			}
			
			// The colors
			float c[] = new float[(2*n+2)*3];
			c[c.length-6] = c[c.length-5] = c[c.length-4] = c[c.length-3] = c[c.length-2] = c[c.length-1] = 1;
			
			for(int i = 0; i< n*3; i+=6){
				c[i] = c[i+2] = c[i+n*3] = c[i+2+n*3] = 0;
				c[i+1] = c[i+1+n*3] = 1;
				
				if(i+3 < n*3){
					c[i+3] = c[i+4] = c[i+3+n*3] = c[i+4+n*3] = 0;
					c[i+5] = c[i+5+n*3] = 1;
				}
			}
			
			// The triangles
			int indices[] = new int[n*2*3*3];
			for(int i = 0; i<n*3; i+=3){
				int node = i/3;
				
				indices[i] = 2*n;
				indices[i+1] = node;
				indices[i+2] = (node+1)%n;
				
				indices[i+n*3] = 2*n+1;
				indices[i+1+n*3] = node + n;
				indices[i+2+n*3] = (node+1)%n + n;
			}
			for(int i = 0; i<n*6; i+=6){
				int node = i/6;
				int index = i+n*6;
				
				indices[index] = node;
				indices[index+1] = node + n;
				indices[index+2] = (node+1)%n;
				
				indices[index+3] = node + n;
				indices[index+4] = (node+1)%n + n;
				indices[index+5] = (node+1)%n;
			}
			
			VertexData vertexData = renderContext.makeVertexData(2*n+2);
			vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
			vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
			vertexData.addIndices(indices);
			return vertexData;
		}
		
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		public void init(RenderContext r)
		{
			renderContext = r;
			
			VertexData vertexData = cylinder(renderContext, 50);
								
			// Make a scene manager and add the object
			sceneManager = new SimpleSceneManager();
			shape = new Shape(vertexData);
			sceneManager.addShape(shape);

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
		JFrame jframe = new JFrame("cylinder");
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
