package agent;

import java.io.Serializable;

public abstract class Agent implements Runnable,  Serializable{

	private static final long serialVersionUID = 1L;

	public Agent(){	
	}
	
	abstract public void run();
}
