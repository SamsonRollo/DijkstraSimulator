package Exceptions;

public class VertexAlreadyExistsException extends Exception{
	public VertexAlreadyExistsException(String message){
		super("Vertex "+message+" already exists");
	}
}