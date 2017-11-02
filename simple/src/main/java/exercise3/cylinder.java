package exercise3;

import jrtr.*;
import jrtr.glrenderer.*;
import jrtr.swrenderer.SWRenderPanel;
import jrtr.gldeferredrenderer.*;

import javax.swing.*;
import java.awt.event.*;
import javax.vecmath.*;

import java.util.ArrayList;
import java.util.Arrays;
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
	public final static class SimpleRenderPanel extends SWRenderPanel
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
			
			// The texture
			ArrayList<Float> tList = new ArrayList<Float>();
			for(int i=0; i<n; ++i){
				tList.addAll(Arrays.asList(i/(float)n, 1.f));
			}
			for(int i=0; i<n; ++i){
				tList.addAll(Arrays.asList(i/(float)n, 0.f));
			}
			tList.addAll(Arrays.asList(0.0f, 0.0f, 1.0f, 1.0f));
			float t[] = new float[tList.size()];
			for(int i=0; i<tList.size(); ++i){
				t[i] = tList.get(i);
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
			vertexData.addElement(t, VertexData.Semantic.TEXCOORD, 2);
			vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
			vertexData.addIndices(indices);
			return vertexData;
		}
		
		private void initCylinder(){
			VertexData vertexData = cylinder(20);
			
			// Make a scene manager and add the object
			sceneManager = new SimpleSceneManager();
			shape = new Shape(vertexData);
			shape.setMaterial(new Material());
			shape.getMaterial().setTexture("../textures/wood.jpg");
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
			
			initCylinder();

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
		JFrame jframe = new JFrame("exercise1");
		jframe.setSize(500, 500);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

		// Add a mouse and key listener
		renderPanel.getCanvas().setFocusable(true);   	    	    
	    
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}
