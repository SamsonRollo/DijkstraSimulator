package Exceptions;

public class ErrorReport{
	
	public ErrorReport(String message, String title){
		javax.swing.JOptionPane.showMessageDialog(null, message, title, javax.swing.JOptionPane.ERROR_MESSAGE);
	}
}
