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
		private double x;
		private double y;
		
		@Override
		public void mouseMoved(MouseEvent e) {
			this.x = e.getX();
			this.y = e.getY();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			Vector3f position = sceneManager.getCamera().getCenterOfProjection();
			Vector3f up = sceneManager.getCamera().getUpVector();
			Vector3f cross = new Vector3f();
			cross.cross(up,  position);
			cross.normalize();
			up.normalize();
			float radius = position.length();
			
			double newX = e.getX();
			double newY = e.getY();
			float deltaX = (float)(this.x - newX)/50;
			float deltaY = (float)(newY - this.y)/50;
			
			Vector3f newPosition = new Vector3f(position.x + deltaX*cross.x + deltaY*up.x, position.y + deltaX*cross.y + deltaY*up.y, position.z + deltaX*cross.z + deltaY*up.z);
			
			newPosition.normalize();
			newPosition = new Vector3f(radius*newPosition.x, radius*newPosition.y, radius*newPosition.z);

			Vector3f crossProduct = new Vector3f();
			crossProduct.cross(position, newPosition);

			AxisAngle4f rot = new AxisAngle4f(crossProduct.x, crossProduct.y, crossProduct.z,
					position.angle(newPosition));

			Matrix4f rotCamera = new Matrix4f();
			rotCamera.set(rot);

			Matrix4f upMatrix = new Matrix4f(up.x, 0.f, 0.f, 0.f,
					up.y, 0.f, 0.f, 0.f,
					up.z, 0.f, 0.f, 0.f,
					0.f, 0.f, 0.f, 0.f);
			upMatrix.mul(rotCamera);
			Vector3f newUp = new Vector3f(upMatrix.m00, upMatrix.m10, upMatrix.m20);
			sceneManager.setCamera(new Camera(newPosition, sceneManager.getCamera().getLookAtPoint(), newUp));

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
		renderPanel.getCanvas().addMouseMotionListener(new SimpleMouseMotionListener());
		renderPanel.getCanvas().addKeyListener(new SimpleKeyListener());
		renderPanel.getCanvas().setFocusable(true);

		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setVisible(true); // show window
	}
}
