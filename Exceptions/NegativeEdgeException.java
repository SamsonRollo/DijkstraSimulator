package Exceptions;

public class NegativeEdgeException extends Exception{
	public NegativeEdgeException(String message){
		super("Negative edge found at: "+message);
	}
}