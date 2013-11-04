package server;

public interface ServerDelegate {
    public void ServerDidConnect(Server server);
	public void ServerDidDisconnect(Server server);
}
