package Graph;

public class Stack<T>{
	Node<T> first= null;
	int size = 0;

	public Stack(){}

	public void push(T content){
		if(isEmpty()){
			first = new Node<>(content);
			size++;
			return;
		}

		Node<T> newNode = new Node<>(content, first);
		first = newNode;
		size++;
	}

	@SuppressWarnings("unchecked")
	public T pop(){
		if(isEmpty())
			return null;
		T obj = first.getContent();
		first = first.getNext();
		size--;

		return obj;
	}

	public T peek(){
		return first.getContent();
	}

	public boolean isEmpty(){
		return first==null?true:false;
	}

	public int getSize(){
		return size;
	}

	public Node<T> getFirstCopy(){
		return first;
	}
}
