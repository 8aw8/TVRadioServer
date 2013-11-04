package actions;

public interface ActionDelegate 
{
	public void didFinishAction(Action action);
	public String getUserIdForAction(Action action);
}
