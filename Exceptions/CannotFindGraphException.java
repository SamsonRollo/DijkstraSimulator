package Exceptions;

public class CannotFindGraphException extends Exception{
	public CannotFindGraphException(String message){
		super(message);
	}
	public CannotFindGraphException(){
		super("Cannot Find Graph!");
	}
}