package exercise2;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import jrtr.Camera;
import jrtr.Frustum;
import jrtr.Light;
import jrtr.Material;
import jrtr.ObjReader;
import jrtr.RenderContext;
import jrtr.RenderPanel;
import jrtr.Shader;
import jrtr.Shape;
import jrtr.SimpleSceneManager;
import jrtr.VertexData;
import jrtr.glrenderer.GLRenderPanel;
import utilities.FractalLandscape;
import utilities.Vector3fPlus;

public class airplane {
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader;
	static Shader diffuseShader;
	static Material material;
	static SimpleSceneManager sceneManager;
	static Shape shape;
	static Shape plane;
	static float currentstep, basicstep;

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
			FractalLandscape f = new FractalLandscape(7, 100);
			VertexData vertexData = f.getVertexData(renderContext);

			// Make a scene manager and add the object
			sceneManager = new SimpleSceneManager();
			Frustum frustum = new Frustum(1.f, 1000.f, 1.f, (float)(Math.PI/3));
			sceneManager.setFrustum(frustum);

			Camera camera = new Camera(new Vector3f(0.f, -100.f, 50.f), new Vector3f(0.f, 0.f, 50.f), new Vector3f(0.f, 0.f, 1.f));
			sceneManager.setCamera(camera);

			Light light = new Light();
			light.setPosition(new Vector3f(0.f, 0.f, 100.f));
			light.setDirection(new Vector3f(0.f, 0.f, -1.f));
			sceneManager.addLight(light);

			shape = new Shape(vertexData);

			try {
				vertexData = ObjReader.read("../obj/airplane.obj", 1, renderContext);
			} catch (IOException e) {
				System.out.println("\n\nError ObjReader\n\n");
			}
			float[] c = new float[vertexData.getNumberOfVertices() * 3];
			for (int i = 0; i < c.length; i+=3) {
				c[i] = 0.f;
				c[i+1] = 0.f;
				c[i+2] = 1.f;
			}
			vertexData.addElement(c, VertexData.Semantic.COLOR, 3);

			plane = new Shape(vertexData);
			sceneManager.addShape(shape);
			sceneManager.addShape(plane);

			Matrix4f planeTransl = new Matrix4f(), rotX = new Matrix4f(), rotY = new Matrix4f(), t = plane.getTransformation();
			planeTransl.set(new Vector3f(0.f, -96.f, 49.f));
			rotX.rotX((float) Math.PI/2.f);
			rotY.rotY((float) -Math.PI/2.f);

			t.mul(planeTransl);
			t.mul(rotX);
			t.mul(rotY);
			plane.setTransformation(t);


			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);

			// Load some more shaders
			normalShader = renderContext.makeShader();
			try {
				normalShader.load("../jrtr/shaders/first.vert", "../jrtr/shaders/first.frag");
			} catch(Exception e) {
				System.out.print("Problem with shader:\n");
				System.out.print(e.getMessage());
			}

			// Make a material that can be used for shading
			material = new Material();
			material.shader = normalShader;
			material.diffuseMap = renderContext.makeTexture();
			try {
				material.diffuseMap.load("../textures/plant.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
			shape.setMaterial(material);
			plane.setMaterial(material);
			renderContext.useShader(normalShader);

			basicstep = 1.f;
			currentstep = basicstep;
		}
	}

	public static class SimpleMouseMotionListener implements MouseMotionListener {
		private int x;
		private int y;

		@Override
		public void mouseMoved(MouseEvent e) {
			setXY(e);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int oldX = this.x;
			int oldY = this.y;
			setXY(e);
			int deltaX = this.x - oldX;
			int deltaY = this.y - oldY;
			float xAngle = (float) (-deltaY*Math.PI/500);
			float zAngle = (float) (deltaX*Math.PI/500);
			
			rotateX(xAngle);
			rotateZ(zAngle);

			// Trigger redrawing of the render window
			renderPanel.getCanvas().repaint();
		}

		private void rotateX(float angle) {
			Matrix4f transl = new Matrix4f(), t = plane.getTransformation(), planeRotZ = new Matrix4f();
			Matrix4f rotX = new Matrix4f();
			Vector3fPlus center = Vector3fPlus.clone(sceneManager.getCamera().getCenterOfProjection());
			Vector3fPlus up = Vector3fPlus.clone(sceneManager.getCamera().getUpVector());
			Vector3fPlus lookAt = Vector3fPlus.clone(sceneManager.getCamera().getLookAtPoint());
			Vector3fPlus sideLeft = new Vector3fPlus();
			Vector3fPlus distance = lookAt.clone();
			distance.sub(center);
			sideLeft.cross(up, distance);

			transl.set(new Vector3f(4.f, 1.f, 0.f));
			t.mul(transl);

			AxisAngle4f rot = new AxisAngle4f(sideLeft.x, sideLeft.y, sideLeft.z, angle);
			rotX.setIdentity();
			rotX.setRotation(rot);

			distance.mul(rotX);
			up.mul(rotX);

			planeRotZ.rotZ(angle);
			t.mul(planeRotZ);

			distance.add(center);
			lookAt = distance.clone();

			transl.invert();
			t.mul(transl);

			plane.setTransformation(t);
			sceneManager.setCamera(new Camera(center, lookAt, up));
		}

		private void rotateZ(float angle) {
			Matrix4f transl = new Matrix4f(), t = plane.getTransformation(), planeRotY = new Matrix4f(), corner = new Matrix4f();
			Matrix4f rotZ = new Matrix4f();
			Vector3fPlus axis = new Vector3fPlus(0.f, 0.f, 1.f);
			Vector3fPlus center = Vector3fPlus.clone(sceneManager.getCamera().getCenterOfProjection());
			Vector3fPlus up = Vector3fPlus.clone(sceneManager.getCamera().getUpVector());
			Vector3fPlus lookAt = Vector3fPlus.clone(sceneManager.getCamera().getLookAtPoint());
			Vector3fPlus distance = lookAt.clone();
			distance.sub(center);
			t.invert();
			axis.mul(t);
			t.invert();

			transl.set(new Vector3f(4.f, 1.f, 0.f));
			t.mul(transl);

			//z-Rotation
			rotZ.rotZ(angle);
			distance.mul(rotZ);
			up.mul(rotZ);

			planeRotY.set(new AxisAngle4f(axis.x, axis.y, axis.z, angle));
			t.mul(planeRotY);

			distance.add(center);
			lookAt = distance.clone();

			transl.invert();
			t.mul(transl);

			plane.setTransformation(t);
			sceneManager.setCamera(new Camera(center, lookAt, up));
		}

		private void setXY(MouseEvent e) {
			this.x = e.getX();
			this.y = e.getY();
		}
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
			Matrix4f t = plane.getTransformation(), transl = new Matrix4f();
			Vector3fPlus translVector;
			Vector3fPlus center = Vector3fPlus.clone(sceneManager.getCamera().getCenterOfProjection());
			Vector3fPlus up = Vector3fPlus.clone(sceneManager.getCamera().getUpVector());
			Vector3fPlus lookAt = Vector3fPlus.clone(sceneManager.getCamera().getLookAtPoint());
			Vector3fPlus sideLeft = new Vector3fPlus();
			Vector3fPlus distance = lookAt.clone();
			distance.sub(center);
			sideLeft.cross(up, distance);
			switch(e.getKeyChar())
			{
			case 'w': {
				distance.setToLength(basicstep);
				lookAt.add(distance);
				center.add(distance);
				translVector = new Vector3fPlus(-1.f, 0.f, 0.f);
				translVector.setToLength(basicstep);
				transl.set(translVector);
				t.mul(transl);
				break;
			}
			case 'a': {
				sideLeft.setToLength(basicstep);
				lookAt.add(sideLeft);
				center.add(sideLeft);
				translVector = new Vector3fPlus(0.f, 0.f, 1.f);
				translVector.setToLength(basicstep);
				transl.set(translVector);
				t.mul(transl);
				break;
			}
			case 's': {
				distance = lookAt.clone();
				distance.sub(center);
				distance.setToLength(basicstep);
				lookAt.sub(distance);
				center.sub(distance);
				translVector = new Vector3fPlus(1.f, 0.f, 0.f);
				translVector.setToLength(basicstep);
				transl.set(translVector);
				t.mul(transl);
				break;
			}
			case 'd': {
				sideLeft.setToLength(basicstep);
				lookAt.sub(sideLeft);
				center.sub(sideLeft);
				translVector = new Vector3fPlus(0.f, 0.f, -1.f);
				translVector.setToLength(basicstep);
				transl.set(translVector);
				t.mul(transl);
				break;
			}
			}
			plane.setTransformation(t);
			sceneManager.setCamera(new Camera(center, lookAt, up));
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
		renderPanel.getCanvas().addKeyListener(new SimpleKeyListener());
		renderPanel.getCanvas().addMouseMotionListener(new SimpleMouseMotionListener());
		renderPanel.getCanvas().setFocusable(true);

		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setVisible(true); // show window
	}
}
