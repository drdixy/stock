/*
 * Created on 29.08.2004 by CS
 *
 */
package net.sf.ojts.datainput.exceptions;

/**
 * @author CS
 *
 */
public class CannotHandleSubjectException extends Exception {

	private static final long serialVersionUID = 3257288041205608500L;
	
	public CannotHandleSubjectException() {
		super();
	}
	public CannotHandleSubjectException(String arg0) {
		super(arg0);
	}
	public CannotHandleSubjectException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
	public CannotHandleSubjectException(Throwable arg0) {
		super(arg0);
	}
}
