package exercise5;

import jrtr.*;
import jrtr.glrenderer.*;
import utilities.ShapeHelpers;
import jrtr.gldeferredrenderer.*;

import javax.swing.*;
import java.awt.event.*;
import javax.vecmath.*;
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
			body = new TransformGroup();
			neck = new TransformGroup();
			rightShoulder = new TransformGroup();
			leftShoulder = new TransformGroup();
			rightElbow = new TransformGroup();
			leftElbow = new TransformGroup();
			rightHip = new TransformGroup();
			leftHip = new TransformGroup();
			rightKnee = new TransformGroup();
			leftKnee = new TransformGroup();
			
			Shape bodyShape = ShapeHelpers.createCylinder(renderContext, 50, 2, 0.5f, new Vector3f(0, 0, 1), new Vector3f(0, 0, 1));
			Shape headShape = ShapeHelpers.createCylinder(renderContext, 50, 0.5f, 0.25f, new Vector3f(0, 1, 0), new Vector3f(0, 1, 0));
			Shape upperArmShape = ShapeHelpers.createCylinder(renderContext, 50, 1.1f, 0.15f, new Vector3f(1, 1, 0), new Vector3f(1, 1, 0));
			Shape lowerArmShape = ShapeHelpers.createCylinder(renderContext, 50, 1.1f, 0.15f, new Vector3f(1, 0, 0), new Vector3f(1, 0, 0));
			Shape upperLegShape = ShapeHelpers.createCylinder(renderContext, 50, 1.1f, 0.15f, new Vector3f(0, 1, 1), new Vector3f(0, 1, 1));
			Shape lowerLegShape = ShapeHelpers.createCylinder(renderContext, 50, 1.1f, 0.15f, new Vector3f(1, 0, 1), new Vector3f(1, 0, 1));
			
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
			
			t = body.getTransformation();
			transl.set(new Vector3f(2, 0, 0));
			t.mul(rot);
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
			t.mul(transl);
			rightElbow.setTransformation(t);
			
			t = leftElbow.getTransformation();
			transl.set(new Vector3f(0, 0, -1.2f));
			t.mul(transl);
			leftElbow.setTransformation(t);
			
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
			t.mul(transl);
			rightKnee.setTransformation(t);
			
			t = leftKnee.getTransformation();
			transl.set(new Vector3f(0, 0, -1.2f));
			t.mul(transl);
			leftKnee.setTransformation(t);
			
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
			
			rightHip.addChild(new ShapeNode(upperLegShape));
			rightHip.addChild(rightKnee);
			
			rightKnee.addChild(new ShapeNode(lowerLegShape));
			
			leftHip.addChild(new ShapeNode(upperLegShape));
			leftHip.addChild(leftKnee);
			
			leftKnee.addChild(new ShapeNode(lowerLegShape));
			
			sceneManager.setRoot(body);

			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);
			
			// Load some more shaders
		    Shader normalShader = renderContext.makeShader();
		    try {
		    	normalShader.load("../jrtr/shaders/normal.vert", "../jrtr/shaders/normal.frag");
		    } catch(Exception e) {
		    	System.out.print("Problem with shader:\n");
		    	System.out.print(e.getMessage());
		    }
	
		    Shader diffuseShader = renderContext.makeShader();
		    try {
		    	diffuseShader.load("../jrtr/shaders/diffuse.vert", "../jrtr/shaders/diffuse.frag");
		    } catch(Exception e) {
		    	System.out.print("Problem with shader:\n");
		    	System.out.print(e.getMessage());
		    }

		    // Make a material that can be used for shading
			Material material = new Material();
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
		private boolean forwards = true;
		
		public void run()
		{
    		Matrix4f t, swingRotForwards = new Matrix4f(), swingRotBackwards = new Matrix4f(), bodyRot = new Matrix4f();
    		swingRotForwards.rotX(basicstep);
    		swingRotBackwards.rotX(-basicstep);
    		bodyRot.rotY(basicstep);
    		
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
    		if(!forwards)
    			t.mul(swingRotForwards);
    		else
    			t.mul(swingRotBackwards);
    		leftHip.setTransformation(t);
    		
    		t= rightHip.getTransformation();
    		if(forwards)
    			t.mul(swingRotForwards);
    		else
    			t.mul(swingRotBackwards);
    		rightHip.setTransformation(t);
    		
    		swingCounter++;
    		if(swingCounter == 90) {
    			swingCounter = 1;
    			forwards = !forwards;
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
