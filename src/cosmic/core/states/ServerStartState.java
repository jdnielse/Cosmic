package cosmic.core.states;

import java.util.Iterator;
import java.util.Random;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector2f;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import com.jme3.system.AppSettings;

import cosmic.core.ServerMain;
import cosmic.message.BasicMessage;
import cosmic.message.StartMessage;

public class ServerStartState extends AbstractAppState {


	private ServerMain server;
	private Server myServer;
	private AssetManager assetManager;
	private AppSettings settings;
	private int player_count;
	private boolean start;

	public ServerStartState(Server myserver, int p_count){
		myServer = myserver;
		player_count = p_count;
		start = true;
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app); 
		this.server = (ServerMain) app;          // cast to a more specific class
		assetManager = app.getAssetManager();
		//this.setEnabled(false);
	}

	@Override
	public void cleanup() {
		super.cleanup();
		// unregister all my listeners, detach all my nodes, etc...

	}

	@Override
	public void setEnabled(boolean enabled) {
		// Pause and unpause
		super.setEnabled(enabled);
		if(enabled){
			// init stuff that is in use while this state is RUNNING
		} 
		else {
			// take away everything not needed while this state is PAUSED

		}
	}

	// Note that update is only called while the state is both attached and enabled.
	@Override
	public void update(float tpf) {
		if(this.isEnabled() == true){
			
			if(myServer.getConnections().size() == player_count && start == true){

				/* use an iterator to loop through connections, the index for a HostedConnection is incremneted
				 * by one even if the client connection prior terminates, so you can have a Connection with an
				 * index of 1 even if the connection with index 0 has terminated.
				 */
				int i = 0;
				Random r = new Random(System.currentTimeMillis());
				Iterator<HostedConnection> connIter = myServer.getConnections().iterator();
				while (connIter.hasNext()) {

					float x = server.map.start_pos[i].x + r.nextFloat() * 6 - 3;
					float y = server.map.start_pos[i].y + r.nextFloat() * 6 - 3;

					myServer.broadcast(Filters.equalTo(connIter.next()), new StartMessage("test_map", player_count, x, y));
					i++;
				}
				
				start = false;
			}
			
			if(server.ready == player_count){
				server.changeState(server.gameState);
				myServer.broadcast(new BasicMessage("ready"));
			}
		}
	}

}