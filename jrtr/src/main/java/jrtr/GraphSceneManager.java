package jrtr;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

import javax.vecmath.Matrix4f;

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
		return lights.iterator();
	}
	
	public SceneManagerIterator iterator()
	{
		return new GraphSceneManagerItr(this);
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
			Matrix4f t = peekedItem.getTransformation();
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
				
				items.push(new StackItem(t, iterator));
				
				nextNode = iterator.next();
			}
			currentLeaf = nextNode;
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
