package Graph;

public class VertexMinQueue<T>{
	
	private Node<T> front=null;
	private double[] distance;
	private DijkstraGraphController controller;

	public VertexMinQueue(DijkstraGraphController controller, double[] distance){
		this.controller = controller;
		this.distance = distance;
	}

	@SuppressWarnings("unchecked")
	public void enqueue(T content){
		if(isEmpty()){
			front = new Node<>(content);
			return;
		}

		Node<T> newNode = new Node<>(content);
		arrangeNode(newNode);
	}

	@SuppressWarnings("unchecked")
	public T dequeue(){
		if(isEmpty())
			return null;

		T obj = front.getContent();

		if(front.getNext()==null){
			front =null;
		}
		else
			front = front.getNext(); //warning unchecked conversion

		return obj;
	}

	public T peek(){
		return front.getContent();
	}

	public boolean isEmpty(){
		return front==null?true:false;
	}

	@SuppressWarnings("unchecked")
	private void arrangeNode(Node<T> newNode){
		int idx = controller.indexRetriever(String.valueOf(newNode.getContent()));
		Node<T> curNode = front;
		Node<T> prevNode = null;

		while(curNode.getNext()!=null){
			if(distance[controller.indexRetriever(String.valueOf(curNode.getContent()))]>distance[idx]){
				if(curNode==front){ //add at the front
					front.setNext(front);
					front = newNode;
					return;
				}else{//add in between
					newNode.setNext(curNode);
					prevNode.setNext(newNode);
					return;
				}
			}

			prevNode = curNode;
			curNode = curNode.getNext();
		}
		curNode.setNext(newNode);//add at the end
	}
}
