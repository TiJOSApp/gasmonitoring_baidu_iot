package net.tijos.gas.base;

import java.io.IOException;

/**
 * �ɼ���������
 * @author Mars 
 *
 */
public abstract class Collector extends Module {
	


	public Collector(int id, String name) {
		super(id, name);
	}

	public abstract void start() throws IOException;
	public abstract void stop();
	
}
