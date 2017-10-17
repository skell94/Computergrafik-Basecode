package exercise2;

import jrtr.*;
import jrtr.glrenderer.*;
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
	static final int WIDTH = 500;
	static final int HEIGHT = 500;

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
		private float x;
		private float y;
		private final float SCREENRADIUS = Math.min(WIDTH/2.f, HEIGHT/2.f);

		@Override
		public void mouseMoved(MouseEvent e) {
			setXY(e);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			Vector3fPlus zAxis = Vector3fPlus.clone(sceneManager.getCamera().getCenterOfProjection());
			Vector3fPlus yAxis = Vector3fPlus.clone(sceneManager.getCamera().getUpVector());
			Vector3fPlus xAxis = new Vector3fPlus();
			xAxis.cross(zAxis,  yAxis);
			xAxis.normalize();
			yAxis.normalize();
			zAxis.normalize();
			
			Vector2f old2DPosition = new Vector2f(this.x, this.y);
			setXY(e);
			Vector2f new2DPosition = new Vector2f(this.x, this.y);
			
			Vector3f old3DPosition = new Vector3f(old2DPosition.x, old2DPosition.y, (float) Math.sqrt(1 - (old2DPosition.length() > 1 ? 1 : old2DPosition.length())));
			Vector3f new3DPosition = new Vector3f(new2DPosition.x, new2DPosition.y, (float) Math.sqrt(1 - (new2DPosition.length() > 1 ? 1 : new2DPosition.length())));
			
			Vector3fPlus oldPosition = new Vector3fPlus(0,0,0), newPosition = new Vector3fPlus(0,0,0);
			Vector3fPlus tempX = xAxis.clone(), tempY = yAxis.clone(), tempZ = zAxis.clone();
			tempX.mul(old3DPosition.x);
			tempY.mul(old3DPosition.y);
			tempZ.mul(old3DPosition.z);
			oldPosition.add(tempX);
			oldPosition.add(tempY);
			oldPosition.add(tempZ);
			
			tempX = xAxis.clone();
			tempY = yAxis.clone();
			tempZ = zAxis.clone();
			tempX.mul(new3DPosition.x);
			tempY.mul(new3DPosition.y);
			tempZ.mul(new3DPosition.z);
			newPosition.add(tempX);
			newPosition.add(tempY);
			newPosition.add(tempZ);
			
			Vector3f crossProduct = new Vector3f();
			System.out.println(this.x+" "+this.y);
			if(this.x < -0.8 || this.y < -0.8 || this.x > 0.7 || this.y > 0.7) {
				crossProduct = Vector3fPlus.clone(sceneManager.getCamera().getCenterOfProjection());
			} else {
			crossProduct.cross(oldPosition, newPosition);
			}
			
			AxisAngle4f rot = new AxisAngle4f(crossProduct.x, crossProduct.y, crossProduct.z, -oldPosition.angle(newPosition));

			Matrix4f rotCamera = new Matrix4f();
			rotCamera.setIdentity();
			rotCamera.setRotation(rot);

			Vector3fPlus newUp = Vector3fPlus.clone(sceneManager.getCamera().getUpVector());
			newUp.mul(rotCamera);
			Vector3fPlus newCenter = Vector3fPlus.clone(sceneManager.getCamera().getCenterOfProjection());
			float radius = newCenter.length();
			newCenter.mul(rotCamera);
			newCenter.setToLength(radius);
			
			sceneManager.setCamera(new Camera(newCenter, sceneManager.getCamera().getLookAtPoint(), newUp));

			// Trigger redrawing of the render window
			renderPanel.getCanvas().repaint();
		}
		
		private void setXY(MouseEvent e) {
			float tempX = e.getX()/SCREENRADIUS - 1;
			float tempY = e.getY()/SCREENRADIUS - 1;
			Vector2f xy = new Vector2f(tempX, tempY);
			if(xy.length() > 1)
				xy.normalize();
			this.x = xy.x;
			this.y = xy.y;
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
		jframe.setSize(WIDTH, HEIGHT);
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
