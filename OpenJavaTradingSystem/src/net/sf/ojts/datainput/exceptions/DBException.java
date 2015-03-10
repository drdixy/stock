/*
 * Created on 29.08.2004 by CS
 *
 */
package net.sf.ojts.datainput.exceptions;

/**
 * @author CS
 *
 */
public class DBException extends Exception {

	private static final long serialVersionUID = 3257290248768991797L;
	
	public DBException() {
		super();
	}
	public DBException(String arg0) {
		super(arg0);
	}
	public DBException(Throwable arg0) {
		super(arg0);
	}
	public DBException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
