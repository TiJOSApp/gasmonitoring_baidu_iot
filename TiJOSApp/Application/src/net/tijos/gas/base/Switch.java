package net.tijos.gas.base;

import java.io.IOException;

/**
 * ���س�����
 * @author Mars 
 *
 */
public interface Switch {
	
	public void on() throws IOException;
	public void off() throws IOException;
	public boolean isOff();

}
