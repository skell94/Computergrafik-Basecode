package exercise1;

import jrtr.*;
import jrtr.glrenderer.*;
import src.main.java.exercise1.SWRenderPanel;
import src.main.java.exercise1.torus.AnimationTask;
import src.main.java.exercise1.torus.SimpleKeyListener;
import src.main.java.exercise1.torus.SimpleMouseListener;
import src.main.java.exercise1.torus.SimpleRenderPanel;
import jrtr.gldeferredrenderer.*;

import javax.swing.*;
import java.awt.event.*;
import javax.vecmath.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class torus {
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
		 * create VertexData for Torus
		 * 
		 * @param r the render context, innerR the inner radius, outerR the outer radius
		 */
		private VertexData torus(float innerR, float outerR){
			int innerSections = 50;
			int outerSections = 50;
			// The vertex positions of the torus
			ArrayList<Float> vList = new ArrayList<Float>();
			for(int i=0; i<innerSections; ++i){
				double innerAngle = i*(1.0/innerSections)*2*Math.PI;
				for(int j=0; j<outerSections; ++j){
					double outerAngle = j*(1.0/outerSections)*2*Math.PI;
					vList.addAll(Arrays.asList((float)(Math.sin(innerAngle)*(outerR*Math.cos(outerAngle)+innerR)), (float)(Math.cos(innerAngle)*(outerR*Math.cos(outerAngle)+innerR)), (float)(Math.sin(outerAngle)*outerR)));
				}
			}
			float v[] = new float[vList.size()];
			for(int i=0; i<vList.size(); ++i){
				v[i] = vList.get(i);
			}
			
			// The colors
			ArrayList<Float> cList = new ArrayList<Float>();
			for(int i=0; i<innerSections; i+=2){
				for(int j=0; j<outerSections; ++j)
					cList.addAll(Arrays.asList(0.0f, 1.0f, 0.0f));
				if(i+1 < innerSections)
					for(int j=0; j<outerSections; ++j)
							cList.addAll(Arrays.asList(0.0f, 0.0f, 1.0f));
			}
			float c[] = new float[cList.size()];
			for(int i=0; i<cList.size(); ++i){
				c[i] = cList.get(i);
			}
			
			// The triangles
			ArrayList<Integer> iList = new ArrayList<Integer>();
			for(int i=0; i<innerSections; ++i){
				for(int j=0; j<outerSections; ++j){
					iList.addAll(Arrays.asList(j+i*outerSections, j+((i+1)%innerSections)*outerSections, (j+1)%outerSections+i*outerSections));
					iList.addAll(Arrays.asList(j+((i+1)%innerSections)*outerSections, (j+1)%outerSections+((i+1)%innerSections)*outerSections, (j+1)%outerSections+i*outerSections));
				}
			}
			int indices[] = new int[iList.size()];
			for(int i=0; i<iList.size(); ++i){
				indices[i] = iList.get(i);
			}
			
			VertexData vertexData = renderContext.makeVertexData(innerSections*outerSections);
			vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
			vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
			vertexData.addIndices(indices);
			return vertexData;
		}
		
		private void initTorus(){
				VertexData vertexData = torus(2.0f, 0.5f);
				
				// Make a scene manager and add the object
				sceneManager = new SimpleSceneManager();
				shape = new Shape(vertexData);
				sceneManager.addShape(shape);
		}
		
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		public void init(RenderContext r)
		{
			renderContext = r;
			
			initTorus();

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
		JFrame jframe = new JFrame("exercise1");
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
