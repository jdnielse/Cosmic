package cosmic.listener;

import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;

import cosmic.core.ServerMain;

public class ServerConnListener implements ConnectionListener {
	
	private ServerMain serverApp;
	
	public ServerConnListener(ServerMain server){
		serverApp = server;
	}

	@Override
	public void connectionAdded(Server server, HostedConnection conn) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectionRemoved(Server server, HostedConnection conn) {
		
		/* exit the server if we lose all connections */		
		if(server.getConnections().size() == 0) {
			System.exit(0);
		}

	}

}
