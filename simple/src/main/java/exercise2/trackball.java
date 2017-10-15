package exercise2;

import jrtr.*;
import jrtr.glrenderer.*;
import jrtr.gldeferredrenderer.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;

import javax.vecmath.*;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implements a simple application that opens a 3D rendering window and shows a
 * rotating cube.
 */
public class trackball {
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader;
	static Shader diffuseShader;
	static Material material;
	static SimpleSceneManager sceneManager;
	static Shape shape;
	static float currentstep, basicstep;

	/**
	 * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to provide
	 * a call-back function for initialization. Here we construct a simple 3D
	 * scene and start a timer task to generate an animation.
	 */
	public final static class SimpleRenderPanel extends GLRenderPanel {
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r
		 *            the render context that is associated with this render
		 *            panel
		 */
		public void init(RenderContext r) {
			renderContext = r;

			// Make a scene manager and add the object
			sceneManager = new SimpleSceneManager();
			VertexData vertexData = renderContext.makeVertexData(0);
			try {
				vertexData = ObjReader.read("../obj/teapot.obj", 1, renderContext);

			} catch (IOException e) {
				System.out.println("\n\nError ObjReader\n\n");
			}
			float[] c = new float[vertexData.getNumberOfVertices() * 3];
			Random rand = new Random();
			for (int i = 0; i < c.length; ++i)
				c[i] = (rand.nextInt(101)) * 0.01f;
			vertexData.addElement(c, VertexData.Semantic.COLOR, 3);

			shape = new Shape(vertexData);
			sceneManager.addShape(shape);

			Frustum frustum = new Frustum(1.f, 100.f, 1.f, (float) (Math.PI / 3));
			sceneManager.setFrustum(frustum);

			Camera camera = new Camera(new Vector3f(0.f, 0.f, 3.f), new Vector3f(0.f, 0.f, 0.f),
					new Vector3f(0.f, 1.f, 0.f));
			sceneManager.setCamera(camera);

			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);

			// Load some more shaders
			normalShader = renderContext.makeShader();
			try {
				normalShader.load("../jrtr/shaders/normal.vert", "../jrtr/shaders/normal.frag");
			} catch (Exception e) {
				System.out.print("Problem with shader:\n");
				System.out.print(e.getMessage());
			}

			diffuseShader = renderContext.makeShader();
			try {
				diffuseShader.load("../jrtr/shaders/diffuse.vert", "../jrtr/shaders/diffuse.frag");
			} catch (Exception e) {
				System.out.print("Problem with shader:\n");
				System.out.print(e.getMessage());
			}

			// Make a material that can be used for shading
			material = new Material();
			material.shader = diffuseShader;
			material.diffuseMap = renderContext.makeTexture();
			try {
				material.diffuseMap.load("../textures/plant.jpg");
			} catch (Exception e) {
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
		}
	}

	/**
	 * A mouse listener for the main window of this application. This can be
	 * used to process mouse events.
	 */
	public static class SimpleMouseMotionListener implements MouseMotionListener {
		private float radius;
		private double x;
		private double y;
		private Vector3f position;
		
		public SimpleMouseMotionListener(float radius, Vector3f position){
			this.radius = radius;
			this.position = position;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			this.x = e.getLocationOnScreen().getX();
			this.y = e.getLocationOnScreen().getY();	
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			double newX = e.getX();
			double newY = e.getY();
			double deltaX = (this.x - newX)/50;
			double xAngle = Math.tan(deltaX / radius);
			double deltaY = (this.y - newY)/50;
			double yAngle = Math.tan(deltaY / radius);
			if(deltaX == 0 && deltaY == 0)
				return;
			
			Matrix4f newVector = new Matrix4f(position.x, position.y, position.z, 0.f,
					0.f, 0.f, 0.f, 0.f,
					0.f, 0.f, 0.f, 0.f,
					0.f, 0.f, 0.f, 0.f);
			Matrix4f rotX, rotY;
			rotX = new Matrix4f();
			rotX.rotX((float) xAngle);
			rotY = new Matrix4f();
			rotY.rotY((float) yAngle);
			
			newVector.mul(rotX);
			newVector.mul(rotY);
			
			Vector3f newPosition = new Vector3f(newVector.m00, newVector.m01, newVector.m02);

			Vector3f crossProduct = new Vector3f();
			crossProduct.cross(position, newPosition);

			AxisAngle4f rot = new AxisAngle4f(crossProduct.x, crossProduct.y, crossProduct.z,
					position.angle(newPosition));

			Matrix4f rotCamera = new Matrix4f();
			rotCamera.set(rot);

			Matrix4f cameraMatrix = sceneManager.getCamera().getCameraMatrix();
			cameraMatrix.mul(rotCamera);
			sceneManager.getCamera().setCameraMatrix(cameraMatrix);

			this.position = newPosition;
			this.x = newX;
			this.y = newY;

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
				if (shape.getMaterial() == null) {
					shape.setMaterial(material);
				} else {
					shape.setMaterial(null);
					renderContext.useDefaultShader();
				}
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
	 * {@link SimpleRenderPanel}. {@link SimpleRenderPanel} is then called
	 * backed for initialization automatically. It then constructs a simple 3D
	 * scene, and starts a timer task to generate an animation.
	 */
	public static void main(String[] args) {
		// Make a render panel. The init function of the renderPanel
		// (see above) will be called back for initialization.
		renderPanel = new SimpleRenderPanel();

		// Make the main window of this application and add the renderer to it
		JFrame jframe = new JFrame("simple");
		jframe.setSize(500, 500);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas
																// into a JFrame
																// window

		// Add a mouse and key listener
		renderPanel.getCanvas().addMouseMotionListener(new SimpleMouseMotionListener(3.f, new Vector3f(0.f, 0.f, 3.f)));
		renderPanel.getCanvas().addKeyListener(new SimpleKeyListener());
		renderPanel.getCanvas().setFocusable(true);

		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setVisible(true); // show window
	}
}
