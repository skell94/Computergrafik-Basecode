package exercise6;

import jrtr.*;
import jrtr.glrenderer.*;
import utilities.ShapeHelpers;
import utilities.Vector3fPlus;
import jrtr.gldeferredrenderer.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;

import javax.vecmath.*;

import exercise6.brezier.SimpleKeyListener;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implements a simple application that opens a 3D rendering window and shows a
 * rotating cube.
 */
public class shadow {
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader;
	static Shader diffuseShader;
	static Material material;
	static GraphSceneManager sceneManager;
	static Shape shape;
	static Light light;
	static float currentstep, basicstep, zoomstep;
	static final int WIDTH = 1000;
	static final int HEIGHT = 1000;

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
			sceneManager = new GraphSceneManager();
			VertexData vertexData = renderContext.makeVertexData(0);
			try {
				vertexData = ObjReader.read("../obj/sphere.obj", 1, renderContext);

			} catch (IOException e) {
				System.out.println("\n\nError ObjReader\n\n");
			}
			
			Group ground = new TransformGroup();
			Group sphere1 = new TransformGroup();
			Group sphere2 = new TransformGroup();
			Group sphere3 = new TransformGroup();
			Group cylinder = new TransformGroup();
			
			Shape groundShape = ShapeHelpers.createPlane(renderContext, 3, 3, new Vector3f(1, 1, 1));
			Shape sphereShape = new Shape(vertexData);
			Shape cylinderShape = ShapeHelpers.createCylinder(renderContext, 50, 1f, 0.15f, new Vector3f(1, 1, 1), new Vector3f(1, 1, 1));
			
			Matrix4f t, transl = new Matrix4f(), rot = new Matrix4f();
			rot.rotX((float)-Math.PI/2);
			
			t = sphereShape.getTransformation();
			transl.set(0.25f);
			t.mul(transl);
			sphereShape.setTransformation(t);
			
			t = cylinderShape.getTransformation();
			transl.set(new Vector3f(0, 0, 0.5f));
			t.mul(transl);
			cylinderShape.setTransformation(t);
			
			t = ground.getTransformation();
			t.mul(rot);
			t.mul(transl);
			ground.setTransformation(t);
			
			t = sphere1.getTransformation();
			transl.set(new Vector3f(0.1f, 0.6f, 0.4f));
			t.mul(transl);
			sphere1.setTransformation(t);
			
			t = sphere2.getTransformation();
			transl.set(new Vector3f(0, -0.6f, 0.6f));
			t.mul(transl);
			sphere2.setTransformation(t);
			
			t = sphere3.getTransformation();
			transl.set(new Vector3f(0.7f, 0, 0.5f));
			t.mul(transl);
			sphere3.setTransformation(t);
			
			ground.addChild(new ShapeNode(groundShape));
			ground.addChild(sphere1);
			ground.addChild(sphere2);
			ground.addChild(sphere3);
			ground.addChild(cylinder);
			
			sphere1.addChild(new ShapeNode(sphereShape));
			sphere2.addChild(new ShapeNode(sphereShape));
			sphere3.addChild(new ShapeNode(sphereShape));
			cylinder.addChild(new ShapeNode(cylinderShape));
			
			sceneManager.setRoot(ground);

			Frustum frustum = new Frustum(1.f, 100.f, 1.f, (float) (Math.PI / 3));
			sceneManager.setFrustum(frustum);

			Camera camera = new Camera(new Vector3f(0.f, 0.f, 3.f), new Vector3f(0.f, 0.f, 0.f),
					new Vector3f(0.f, 1.f, 0.f));
			sceneManager.setCamera(camera);
			
			light = new Light();
			light.type = Light.Type.POINT;
			light.position = new Vector3f(-5.f, 0.f, 3.f);
			light.diffuse = new Vector3f(0.5f, 0.5f, 0.5f);
			sceneManager.addLight(light);

			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);

			// Load some more shaders
			normalShader = renderContext.makeShader();
			try {
				normalShader.load("../jrtr/shaders/first.vert", "../jrtr/shaders/first.frag");
			} catch (Exception e) {
				System.out.print("Problem with shader:\n");
				System.out.print(e.getMessage());
			}

			// Make a material that can be used for shading
			material = new Material();
			material.shader = normalShader;
			material.shininess = 20f;
//			material.diffuse = new Vector3f(0.5f, 0.5f, 0.5f);
//			material.diffuse = new Vector3f(0f, 1f, 0f);
//			material.diffuse = new Vector3f(0f, 0f, 1f);
//			material.diffuse = new Vector3f(0.5f, 0.5f, 0f);
			material.diffuseMap = renderContext.makeTexture();
			try {
				material.diffuseMap.load("../textures/plant.jpg");
			} catch (Exception e) {
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
			groundShape.setMaterial(material);
			sphereShape.setMaterial(material);
			cylinderShape.setMaterial(material);
			renderContext.useShader(normalShader);
			
			zoomstep = 0.1f;
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
			if(this.x < -0.8 || this.y < -0.8 || this.x > 0.7 || this.y > 0.7) {
				crossProduct = Vector3fPlus.clone(sceneManager.getCamera().getCenterOfProjection());
			} else {
			crossProduct.cross(oldPosition, newPosition);
			}
			
			AxisAngle4f rot = new AxisAngle4f(crossProduct.x, crossProduct.y, crossProduct.z, oldPosition.angle(newPosition));

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
	
	public static class SimpleKeyListener implements KeyListener
	{
		public void keyPressed(KeyEvent e)
		{
			switch(e.getKeyChar())
			{
				case 'w': {
					Camera camera = sceneManager.getCamera();
					Vector3f newCenter = camera.getCenterOfProjection();
					float length = newCenter.length();
					if(length > 1) {
						newCenter.normalize();
						newCenter.scale(length - zoomstep);
					}
					sceneManager.setCamera(new Camera(newCenter, camera.getLookAtPoint(), camera.getUpVector()));
					break;
				}
				case 's': {
					Camera camera = sceneManager.getCamera();
					Vector3f newCenter = camera.getCenterOfProjection();
					float length = newCenter.length();
					newCenter.normalize();
					newCenter.scale(length + zoomstep);
					sceneManager.setCamera(new Camera(newCenter, camera.getLookAtPoint(), camera.getUpVector()));
					break;
				}
				case 'l': {
					Camera camera = sceneManager.getCamera();
					Vector3f newPosition = (Vector3f) camera.getCenterOfProjection().clone();
					newPosition.normalize();
					newPosition.scale(5);
					light.position = newPosition;
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
