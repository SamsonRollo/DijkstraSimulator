package Graph;

public class Queue<T>{
	
	Node<T> front=null;
	Node<T> rear=null;

	public Queue(){}

	@SuppressWarnings("unchecked")
	public void enqueue(T content){
		if(isEmpty()){
			front = new Node<>(content);
			rear = front;
			return;
		}

		Node<T> newNode = new Node<>(content);
		rear.setNext(newNode);
		rear = rear.getNext(); //warning here unchecked conversion
	}

	@SuppressWarnings("unchecked")
	public T dequeue(){
		if(isEmpty())
			return null;

		T obj = front.getContent();

		if(front.getNext()==null){
			rear = null;
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
}
