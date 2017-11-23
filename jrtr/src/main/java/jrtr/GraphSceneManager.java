package jrtr;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

import javax.vecmath.*;

public class GraphSceneManager implements SceneManagerInterface {
	
	private Node root;
	private LinkedList<Light> lights;
	private Camera camera;
	private Frustum frustum;
	
	public GraphSceneManager()
	{
		lights = new LinkedList<Light>();
		camera = new Camera();
		frustum = new Frustum();
	}
	
	public Camera getCamera()
	{
		return camera;
	}
	
	public void setCamera(Camera camera)
	{
		this.camera = camera;
	}
	
	public Frustum getFrustum()
	{
		return frustum;
	}
	
	public void setFrustum(Frustum frustum)
	{
		this.frustum = frustum;
	}
	
	public void setRoot(Node root)
	{
		this.root = root;
	}
	
	public Node getRoot() {
		return root;
	}
	
	public void addLight(Light light)
	{
		lights.add(light);
	}
	
	public Iterator<Light> lightIterator()
	{
		return new GraphLightIterator(this);
	}
	
	public SceneManagerIterator iterator()
	{
		return new GraphSceneManagerItr(this);
	}
	
	private class GraphLightIterator implements Iterator<Light>{
		
		private Iterator<Light> iterator;
		LinkedList<Light> lights;
		
		public GraphLightIterator(GraphSceneManager sceneManager) {
			lights = new LinkedList<Light>(sceneManager.lights);
			Matrix4f t = new Matrix4f();
			t.setIdentity();
			findLights(sceneManager.getRoot(), t);
			iterator = lights.iterator();
		}

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public Light next() {
			return iterator.next();
		}
		
		private void findLights(Node node, Matrix4f t) {
			if(node instanceof Group) {
				t.mul(node.getTransformation());
				ListIterator<Node> childrenIterator = node.getChildren().listIterator();
				while(childrenIterator.hasNext()) {
					findLights(childrenIterator.next(), new Matrix4f(t));
				}
			} else {
				if(node instanceof LightNode) {
					Light light = node.getLight().clone();
					Vector4f newPosition = new Vector4f(light.position.x, light.position.y, light.position.z, 1);
					t.transform(newPosition);
					light.position = new Vector3f(newPosition.x, newPosition.y, newPosition.z);
					lights.add(light);
				}
			}
		}
		
	}
	
	private class GraphSceneManagerItr implements SceneManagerIterator {
		
		private Stack<StackItem> items;
		private Node currentLeaf;
		
		public GraphSceneManagerItr(GraphSceneManager sceneManager)
		{
			items = new Stack<StackItem>();
			fillStack(sceneManager.getRoot());
		}
		
		public boolean hasNext()
		{
			return !items.empty();
		}
		
		public RenderItem next()
		{
			StackItem peekedItem = items.peek();
			
			Shape shape = currentLeaf.getShape();
			Matrix4f t = new Matrix4f(peekedItem.getTransformation());
			t.mul(shape.getTransformation());
			
			while(!items.empty()) {
				peekedItem = items.peek();
				
				if(peekedItem.getIterator().hasNext()) {
					fillStack(peekedItem.getIterator().next());
					break;
				}
				items.pop();
			}
			
			return new RenderItem(shape, t);
		}
		
		private void fillStack(Node node) {
			Node nextNode = node;
			Matrix4f t = new Matrix4f();
			t.setIdentity();
			if(!items.empty())
				t = items.peek().getTransformation();
			while(nextNode instanceof Group) {
				ListIterator<Node> iterator = nextNode.getChildren().listIterator();
				t.mul(nextNode.getTransformation());
				
				items.push(new StackItem(new Matrix4f(t), iterator));
				
				nextNode = iterator.next();
			}
			if(nextNode.getShape() != null) {
				currentLeaf = nextNode;
			} else {
				while(!items.empty()) {
					StackItem peekedItem = items.peek();
					if(peekedItem.getIterator().hasNext()) {
						break;
					}
					items.pop();
				}
				if(!items.empty()) {
					fillStack(items.peek().getIterator().next());
				}
			}
		}
		
		private class StackItem {
			
			private Matrix4f transformation;
			private ListIterator<Node> iterator;
			
			public StackItem(Matrix4f transformation, ListIterator<Node> iterator) {
				this.transformation = transformation;
				this.iterator = iterator;
			}
			
			public Matrix4f getTransformation() {
				return (Matrix4f) transformation.clone();
			}
			
			public ListIterator<Node> getIterator(){
				return iterator;
			}
		}
	}
}
