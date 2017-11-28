package exercise5;

import jrtr.*;
import jrtr.glrenderer.*;
import utilities.ShapeHelpers;
import utilities.Vector3fPlus;
import jrtr.gldeferredrenderer.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;

import javax.vecmath.*;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implements a simple application that opens a 3D rendering window and 
 * shows a rotating cube.
 */
public class crop
{	
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static GraphSceneManager sceneManager;
	static float basicstep;

	/**
	 * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to 
	 * provide a call-back function for initialization. Here we construct
	 * a simple 3D scene and start a timer task to generate an animation.
	 */ 
	public final static class SimpleRenderPanel extends GLRenderPanel
	{
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		public void init(RenderContext r)
		{
			renderContext = r;
								
			sceneManager = new GraphSceneManager();
			
			Camera camera = new Camera(new Vector3f(45, 2, 45), new Vector3f(0, 2, 0), new Vector3f(0, 1, 0));
			sceneManager.setCamera(camera);
			
			VertexData vertexData = renderContext.makeVertexData(0);
			try {
				vertexData = ObjReader.read("../obj/buddha_smooth.obj", 1, renderContext);

			} catch (IOException e) {
				System.out.println("\n\nError ObjReader\n\n");
			}

			Shape teapot = new Shape(vertexData);
			Matrix4f t, transl = new Matrix4f();
			
			Group root = new TransformGroup();
			for(int i=0; i<30; ++i) {
				for(int j=0; j<30; ++j) {
					Group group = new TransformGroup();
					transl.set(new Vector3f(3*i, 0, 3*j));
					t = group.getTransformation();
					t.mul(transl);
					group.setTransformation(t);
					root.addChild(group);
					group.addChild(new ShapeNode(teapot));
				}
			}
			
			sceneManager.setRoot(root);

			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);

			// Load some more shaders
			Shader normalShader = renderContext.makeShader();
			try {
				normalShader.load("../jrtr/shaders/normal.vert", "../jrtr/shaders/normal.frag");
			} catch (Exception e) {
				System.out.print("Problem with shader:\n");
				System.out.print(e.getMessage());
			}
			
			renderContext.useShader(normalShader);
			
			
			// Register a timer task
		    Timer timer = new Timer();
		    basicstep = 0.01f;
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
			Matrix4f t, rot = new Matrix4f();
			t = sceneManager.getCamera().getCameraMatrix();
			rot.rotY(basicstep);
			t.invert();
			t.mul(rot);
			t.invert();
			sceneManager.getCamera().setCameraMatrix(t);
    		
    		// Trigger redrawing of the render window
    		renderPanel.getCanvas().repaint(); 
		}
	}
	
	/**
	 * A key listener for the main window. Use this to process key events.
	 * Currently this provides the following controls: 's': stop animation 'p':
	 * play animation '+': accelerate rotation '-': slow down rotation 'd':
	 * default shader 'n': shader using surface normals 'm': use a material for
	 * shading
	 */
	public static class SimpleKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyChar()) {
			case 'a': {
				Matrix4f t, rot = new Matrix4f();
				t = sceneManager.getCamera().getCameraMatrix();
				rot.rotY(basicstep);
				t.invert();
				t.mul(rot);
				t.invert();
				sceneManager.getCamera().setCameraMatrix(t);
				break;
			}
			case 'd': {
				Matrix4f t, rot = new Matrix4f();
				t = sceneManager.getCamera().getCameraMatrix();
				rot.rotY(-basicstep);
				t.invert();
				t.mul(rot);
				t.invert();
				sceneManager.getCamera().setCameraMatrix(t);
				break;
			}
			}

			// Trigger redrawing
			renderPanel.getCanvas().repaint();
		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
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
		renderPanel.getCanvas().addKeyListener(new SimpleKeyListener());
		renderPanel.getCanvas().setFocusable(true);   	    	    
	    
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}
