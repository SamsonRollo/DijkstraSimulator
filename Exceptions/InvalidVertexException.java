package Exceptions;

public class InvalidVertexException extends Exception{
	public InvalidVertexException(String message){
		super(message);
	}

	public InvalidVertexException(String message, int type){
		super("Vertex not found:"+message);
	}
}