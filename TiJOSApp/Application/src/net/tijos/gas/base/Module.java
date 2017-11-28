package net.tijos.gas.base;

/**
 * ����ģ�鳬��
 * @author Mars
 *
 */
public abstract class Module {
	
	private int id;
	private String name;
	
	public Module(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

}
