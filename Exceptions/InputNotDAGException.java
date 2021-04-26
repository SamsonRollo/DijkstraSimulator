package Exceptions;

public class InputNotDAGException extends Exception{
	public InputNotDAGException(String message){
		super(message);
	}

	public InputNotDAGException(){
		super("Graph not DAG!");
	}
}