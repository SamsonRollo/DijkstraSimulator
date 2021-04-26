package Graph;

public class Node<T>{

	Node<T> next = null;
	T content;

	public Node(){}

	public Node(T content){
		this.content = content;
	}

	public Node(T content, Node<T> next){
		this.content = content;
		this.next = next;
	}

	public void setNext(Node<T> neighbour){
		next = neighbour;
	}

	public Node getNext(){
		return next;
	}

	public void setContent(T content){
		this.content = content;
	}

	public T getContent(){
		return content;
	}
}
