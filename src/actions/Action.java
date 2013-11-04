package actions;

import java.util.Map;

public abstract class Action 
{
	protected final ActionDelegate delegate;
	protected final Map<String,String> parameters;
	
	public Action(ActionDelegate delegate, Map<String,String> parameters)
	{
		this.delegate 	= delegate;
		this.parameters = parameters;
	}
	
	public abstract void start();
}
