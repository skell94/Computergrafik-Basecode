package exercise5;

import jrtr.*;
import jrtr.glrenderer.*;
import utilities.ShapeHelpers;
import utilities.Vector3fPlus;
import jrtr.gldeferredrenderer.*;

import javax.swing.*;
import java.awt.event.*;
import javax.vecmath.*;

import exercise2.airplane.SimpleKeyListener;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Implements a simple application that opens a 3D rendering window and 
 * shows a rotating cube.
 */
public class robot
{	
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static GraphSceneManager sceneManager;
	static float basicstep;
	
	static Group body;
	static Group neck;
	static Group rightShoulder;
	static Group leftShoulder;
	static Group rightElbow;
	static Group leftElbow;
	static Group rightHip;
	static Group leftHip;
	static Group rightKnee;
	static Group leftKnee;
	static Group leftHand;

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
			
			Camera camera = new Camera(new Vector3f(0, 5, 20), new Vector3f(0, 0, 0), new Vector3f(0, 4, -1));
			sceneManager.setCamera(camera);
			
			Group ground = new TransformGroup();
			body = new TransformGroup();
			neck = new TransformGroup();
			rightShoulder = new TransformGroup();
			leftShoulder = new TransformGroup();
			rightElbow = new TransformGroup();
			leftElbow = new TransformGroup();
			leftHand = new TransformGroup();
			rightHip = new TransformGroup();
			leftHip = new TransformGroup();
			rightKnee = new TransformGroup();
			leftKnee = new TransformGroup();
			
			Shape groundShape = ShapeHelpers.createPlane(renderContext, 50, 50, new Vector3f(1, 1, 1));
			Shape bodyShape = ShapeHelpers.createCylinder(renderContext, 50, 2, 0.5f, new Vector3f(0, 0, 1), new Vector3f(0, 0, 1));
			Shape headShape = ShapeHelpers.createCylinder(renderContext, 50, 0.5f, 0.25f, new Vector3f(0, 1, 0), new Vector3f(0, 1, 0));
			Shape upperArmShape = ShapeHelpers.createCylinder(renderContext, 50, 1.1f, 0.15f, new Vector3f(1, 1, 0), new Vector3f(1, 1, 0));
			Shape lowerArmShape = ShapeHelpers.createCylinder(renderContext, 50, 1.1f, 0.15f, new Vector3f(1, 0, 0), new Vector3f(1, 0, 0));
			Shape upperLegShape = ShapeHelpers.createCylinder(renderContext, 50, 1.1f, 0.15f, new Vector3f(0, 1, 1), new Vector3f(0, 1, 1));
			Shape lowerLegShape = ShapeHelpers.createCylinder(renderContext, 50, 1.1f, 0.15f, new Vector3f(1, 0, 1), new Vector3f(1, 0, 1));
			
			Light handLight = new Light();
			handLight.position = new Vector3f(0,0,0);
			handLight.type = Light.Type.POINT;
			
			Matrix4f t, transl = new Matrix4f(), rot = new Matrix4f();
			rot.rotX((float)-Math.PI/2);
			
			t = headShape.getTransformation();
			transl.set(new Vector3f(0, 0, 0.25f));
			t.mul(transl);
			headShape.setTransformation(t);
			
			t = upperArmShape.getTransformation();
			transl.set(new Vector3f(0, 0, -0.55f));
			t.mul(transl);
			upperArmShape.setTransformation(t);
			
			t = lowerArmShape.getTransformation();
			transl.set(new Vector3f(0, 0, -0.55f));
			t.mul(transl);
			lowerArmShape.setTransformation(t);
			
			t = upperLegShape.getTransformation();
			transl.set(new Vector3f(0, 0, -0.55f));
			t.mul(transl);
			upperLegShape.setTransformation(t);
			
			t = lowerLegShape.getTransformation();
			transl.set(new Vector3f(0, 0, -0.55f));
			t.mul(transl);
			lowerLegShape.setTransformation(t);
			
			t = ground.getTransformation();
			transl.set(new Vector3f(0, 0, -3.5f));
			t.mul(rot);
			t.mul(transl);
			ground.setTransformation(t);
			
			t = body.getTransformation();
			transl.set(new Vector3f(5, 0, 3.5f));
			t.mul(transl);
			body.setTransformation(t);
			
			t = neck.getTransformation();
			transl.set(new Vector3f(0, 0, 1.1f));
			t.mul(transl);
			neck.setTransformation(t);
			
			t = rightShoulder.getTransformation();
			transl.set(new Vector3f(-0.7f, 0, 1));
			t.mul(transl);
			rightShoulder.setTransformation(t);
			
			t = leftShoulder.getTransformation();
			transl.set(new Vector3f(0.7f, 0, 1));
			t.mul(transl);
			leftShoulder.setTransformation(t);
			
			t = rightElbow.getTransformation();
			transl.set(new Vector3f(0, 0, -1.2f));
			rot.rotX(-(float)(5*Math.PI/8));
			t.mul(transl);
			t.mul(rot);
			rightElbow.setTransformation(t);
			
			t = leftElbow.getTransformation();
			transl.set(new Vector3f(0, 0, -1.2f));
			t.mul(transl);
			t.mul(rot);
			leftElbow.setTransformation(t);
			
			t = leftHand.getTransformation();
			transl.set(new Vector3f(0, 0, -1.2f));
			t.mul(transl);
			t.mul(rot);
			leftHand.setTransformation(t);
			
			t = rightHip.getTransformation();
			transl.set(new Vector3f(-0.35f, 0, -1.1f));
			t.mul(transl);
			rightHip.setTransformation(t);
			
			t = leftHip.getTransformation();
			transl.set(new Vector3f(0.35f, 0, -1.1f));
			t.mul(transl);
			leftHip.setTransformation(t);
			
			t = rightKnee.getTransformation();
			transl.set(new Vector3f(0, 0, -1.2f));
			rot.rotX((float)(3*Math.PI/4));
			t.mul(transl);
			t.mul(rot);
			rightKnee.setTransformation(t);
			
			t = leftKnee.getTransformation();
			transl.set(new Vector3f(0, 0, -1.2f));
			t.mul(transl);
			leftKnee.setTransformation(t);
			
			ground.addChild(new ShapeNode(groundShape));
			ground.addChild(body);
			
			body.addChild(new ShapeNode(bodyShape));
			body.addChild(neck);
			body.addChild(rightShoulder);
			body.addChild(leftShoulder);
			body.addChild(rightHip);
			body.addChild(leftHip);
			
			neck.addChild(new ShapeNode(headShape));
			
			rightShoulder.addChild(new ShapeNode(upperArmShape));
			rightShoulder.addChild(rightElbow);
			
			rightElbow.addChild(new ShapeNode(lowerArmShape));
			
			leftShoulder.addChild(new ShapeNode(upperArmShape));
			leftShoulder.addChild(leftElbow);
			
			leftElbow.addChild(new ShapeNode(lowerArmShape));
//			leftElbow.addChild(leftHand);
			
			leftHand.addChild(new LightNode(handLight));
			
			rightHip.addChild(new ShapeNode(upperLegShape));
			rightHip.addChild(rightKnee);
			
			rightKnee.addChild(new ShapeNode(lowerLegShape));
			
			leftHip.addChild(new ShapeNode(upperLegShape));
			leftHip.addChild(leftKnee);
			
			leftKnee.addChild(new ShapeNode(lowerLegShape));
			
			sceneManager.setRoot(ground);

			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);
			
//			Light lightWhite = new Light();
//			lightWhite.type = Light.Type.POINT;
//			lightWhite.position = new Vector3f(5.f, 5.f, 20.f);
//			sceneManager.addLight(lightWhite);
			
			Light lightWhite2 = new Light();
			lightWhite2.type = Light.Type.POINT;
			lightWhite2.position = new Vector3f(-5.f, 5.f, 20.f);
			lightWhite2.diffuse = new Vector3f(0.5f, 0.5f, 0.5f);
			sceneManager.addLight(lightWhite2);
			
//			Light lightWhite3 = new Light();
//			lightWhite3.type = Light.Type.POINT;
//			lightWhite3.position = new Vector3f(0.f, 5.f, 0.f);
//			sceneManager.addLight(lightWhite3);

			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);

			// Load some more shaders
			Shader normalShader = renderContext.makeShader();
			try {
				normalShader.load("../jrtr/shaders/second.vert", "../jrtr/shaders/second.frag");
			} catch (Exception e) {
				System.out.print("Problem with shader:\n");
				System.out.print(e.getMessage());
			}

			// Make a material that can be used for shading
			Material material = new Material();
			material.shader = normalShader;
			material.diffuse = new Vector3f(0, 0, 1);
			
			// Make a material that can be used for shading
			Material material2 = new Material();
			material2.shader = normalShader;
			material2.diffuse = new Vector3f(0, 1, 0);
			
			groundShape.setMaterial(material2);
			bodyShape.setMaterial(material);
			headShape.setMaterial(material);
			upperArmShape.setMaterial(material);
			lowerArmShape.setMaterial(material);
			upperLegShape.setMaterial(material);
			lowerLegShape.setMaterial(material);
			
			renderContext.useShader(normalShader);

			// Register a timer task
		    Timer timer = new Timer();
		    basicstep = (float) Math.PI/180;
		    timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
		}
	}

	/**
	 * A timer task that generates an animation. This task triggers
	 * the redrawing of the 3D scene every time it is executed.
	 */
	public static class AnimationTask extends TimerTask
	{
		private int swingCounter = 45;
		private int kneeBend = 0;
		private boolean forwards = false;
		private boolean kneeBending = false;
		
		public void run()
		{
    		Matrix4f t, swingRotForwards = new Matrix4f(), swingRotBackwards = new Matrix4f(), bodyRot = new Matrix4f();
    		swingRotForwards.rotX(basicstep);
    		swingRotBackwards.rotX(-basicstep);
    		bodyRot.rotZ(basicstep/2);
    		
    		t = body.getTransformation();
    		t.invert();
    		t.mul(bodyRot);
    		t.invert();
    		body.setTransformation(t);
    		
    		t= leftShoulder.getTransformation();
    		if(forwards)
    			t.mul(swingRotForwards);
    		else
    			t.mul(swingRotBackwards);
    		leftShoulder.setTransformation(t);
    		
    		t= rightShoulder.getTransformation();
    		if(!forwards)
    			t.mul(swingRotForwards);
    		else
    			t.mul(swingRotBackwards);
    		rightShoulder.setTransformation(t);
    		
    		t= leftHip.getTransformation();
    		if(!forwards) {
    			t.mul(swingRotForwards);
    		} else {
    			t.mul(swingRotBackwards);
    			Matrix4f t2 = leftKnee.getTransformation();
    			if(kneeBending) {
    				t2.mul(swingRotForwards);
    				t2.mul(swingRotForwards);
    				t2.mul(swingRotForwards);
    			} else {
    				t2.mul(swingRotBackwards);
    				t2.mul(swingRotBackwards);
    				t2.mul(swingRotBackwards);
    			}
    			leftKnee.setTransformation(t2);
    		}
    		leftHip.setTransformation(t);
    		
    		t= rightHip.getTransformation();
    		if(forwards) {
    			t.mul(swingRotForwards);
    		} else { 
    			t.mul(swingRotBackwards);
    			Matrix4f t2 = rightKnee.getTransformation();
    			if(kneeBending) {
    				t2.mul(swingRotForwards);
    				t2.mul(swingRotForwards);
    				t2.mul(swingRotForwards);
    			} else {
    				t2.mul(swingRotBackwards);
    				t2.mul(swingRotBackwards);
    				t2.mul(swingRotBackwards);
    			}
    			rightKnee.setTransformation(t2);
    		}
    		rightHip.setTransformation(t);
    		
    		swingCounter++;
    		if(swingCounter == 90) {
    			swingCounter = 0;
    			forwards = !forwards;
    		}
    		
    		kneeBend++;
    		if(kneeBend == 45) {
    			kneeBend = 0;
    			kneeBending = !kneeBending;
    		}
    		
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
		JFrame jframe = new JFrame("simple");
		jframe.setSize(1000, 1000);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

		// Add a mouse and key listener
		renderPanel.getCanvas().setFocusable(true);   	    	    
	    
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}
