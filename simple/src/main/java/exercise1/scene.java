package exercise1;

import jrtr.*;
import jrtr.glrenderer.*;
import src.main.java.exercise1.SWRenderPanel;
import src.main.java.exercise1.scene.AnimationTask;
import src.main.java.exercise1.scene.SimpleKeyListener;
import src.main.java.exercise1.scene.SimpleMouseListener;
import src.main.java.exercise1.scene.SimpleRenderPanel;
import jrtr.gldeferredrenderer.*;

import javax.swing.*;
import java.awt.event.*;
import javax.vecmath.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class scene {
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader;
	static Shader diffuseShader;
	static Material material;
	static SimpleSceneManager sceneManager;
	static Shape[] shapes;
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
		private VertexData cylinder(int n){
			// The vertex positions of the cylinder
			ArrayList<Float> vList = new ArrayList<Float>();
			for(int i=0; i<n; ++i){
				double angle = i*(1.0/n)*2*Math.PI;
				vList.addAll(Arrays.asList((float)(Math.sin(angle)), (float)(Math.cos(angle)), 3.0f));
			}
			for(int i=0; i<n; ++i){
				double angle = i*(1.0/n)*2*Math.PI;
				vList.addAll(Arrays.asList((float)(Math.sin(angle)), (float)(Math.cos(angle)), -3.0f));
			}
			vList.addAll(Arrays.asList(0.0f, 0.0f, 3.0f, 0.0f, 0.0f, -3.0f));
			float v[] = new float[vList.size()];
			for(int i=0; i<vList.size(); ++i){
				v[i] = vList.get(i);
			}
			
			// The colors
			ArrayList<Float> cList = new ArrayList<Float>();
			for(int i=0; i<n; ++i){
				cList.addAll(Arrays.asList(0.0f, 1.0f, 0.0f));
				cList.addAll(Arrays.asList(0.0f, 0.0f, 1.0f));
			}
			cList.addAll(Arrays.asList(1.0f, 1.0f, 1.0f));
			cList.addAll(Arrays.asList(1.0f, 1.0f, 1.0f));
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
			return vertexData;
		}
		
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
		
		private void initScene() {
			sceneManager = new SimpleSceneManager();
			shapes = new Shape[4];
			shapes[0] = new Shape(torus(0.5f, 0.1f));
			shapes[1] = new Shape(torus(0.5f, 0.1f));
			shapes[2] = new Shape(torus(0.5f, 0.1f));
			shapes[3] = new Shape(torus(0.5f, 0.1f));
			Matrix4f torusRot = new Matrix4f();
			torusRot.rotX((float)(Math.PI/2));
			
			Matrix4f t = shapes[0].getTransformation();
			Matrix4f transl = new Matrix4f();
			transl.set(new Vector3f(-1.0f, 3.0f, 0.5f));
			t.mul(torusRot);
			t.mul(transl);
			shapes[0].setTransformation(t);
			
			t = shapes[1].getTransformation();
			transl.set(new Vector3f(-1.0f, 4.5f, 0.5f));
			t.mul(torusRot);
			t.mul(transl);
			shapes[1].setTransformation(t);
			
			t = shapes[2].getTransformation();
			transl.set(new Vector3f(1.0f, 3.0f, 0.5f));
			t.mul(torusRot);
			t.mul(transl);
			shapes[2].setTransformation(t);
			
			t = shapes[3].getTransformation();
			transl.set(new Vector3f(1.0f, 4.5f, 0.5f));
			t.mul(torusRot);
			t.mul(transl);
			shapes[3].setTransformation(t);
			
			for(Shape shape: shapes) {
				sceneManager.addShape(shape);
			}
		}
		
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		public void init(RenderContext r)
		{
			renderContext = r;
			
			initScene();

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
    		Matrix4f rotZ = new Matrix4f();
    		rotZ.rotZ(currentstep);
    		
    		double closeRadius = Math.sqrt(Math.pow(3,2)+Math.pow(1, 2));
    		double closeAngle = Math.atan(1.0/3);
    		double farRadius = Math.sqrt(Math.pow(4.5,2)+Math.pow(1, 2));
    		double farAngle = Math.atan(1.0/4.5);
    		
    		Matrix4f wheelRot = new Matrix4f();
			wheelRot.set(new Vector3f((float)(closeRadius*Math.sin(closeAngle+currentstep)+1),(float)(closeRadius*Math.cos(closeAngle+currentstep)-3), 0.0f));
			Matrix4f t = shapes[0].getTransformation();
//			t.mul(wheelRot);
//			t.mul(rotZ);
//			shapes[0].setTransformation(t);
//			
//			wheelRot.set(new Vector3f((float)(farRadius*Math.sin(farAngle+currentstep)+1),(float)(farRadius*Math.cos(farAngle+currentstep)-4.5), 0.0f));
//			t = shapes[1].getTransformation();
//			t.mul(wheelRot);
//			t.mul(rotZ);
//			shapes[1].setTransformation(t);
			
			wheelRot.set(new Vector3f((float)(closeRadius*Math.sin(closeAngle+currentstep)-1),(float)(closeRadius*Math.cos(closeAngle+currentstep)-3), 0.0f));
			t = shapes[2].getTransformation();
			t.mul(wheelRot);
			t.mul(rotZ);
			shapes[2].setTransformation(t);
			
			wheelRot.set(new Vector3f((float)(farRadius*Math.sin(farAngle+currentstep)-1),(float)(farRadius*Math.cos(farAngle+currentstep)-4.5), 0.0f));
			t = shapes[3].getTransformation();
			t.mul(wheelRot);
			t.mul(rotZ);
			shapes[3].setTransformation(t);
    		
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
					for(Shape shape: shapes)
						shape.setMaterial(null);
					renderContext.useShader(normalShader);
					break;
				}
				case 'd': {
					// Remove material from shape, and set "default" shader
					for(Shape shape: shapes)
						shape.setMaterial(null);
					renderContext.useDefaultShader();
					break;
				}
				case 'm': {
					// Set a material for more complex shading of the shape
					if(shapes[0].getMaterial() == null) {
						for(Shape shape: shapes)
							shape.setMaterial(material);
					} else
					{
						for(Shape shape: shapes)
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