package cosmic.core;

import cosmic.core.states.*;
import cosmic.listener.ServerConnListener;
import cosmic.listener.ServerListener;
import cosmic.message.*;
import cosmic.object.Textures;

import java.io.IOException;
import java.util.logging.Level;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.math.Vector3f;
import com.jme3.network.*;
import com.jme3.network.serializing.Serializer;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;

public class ServerMain extends SimpleApplication {
	
	Server myServer;
	public ServerStartState startState;
	public ServerGameState gameState;
	public MapLoader map;
	AppState currentState;
	
	String SERVER_STATE;
	static int players;
	public int ready;
	static int port = 3333;

	@Override
	public void simpleInitApp() {
		myServer = null;
		ready = 0;
		
		SERVER_STATE = "setup";
		
		try {
			myServer = Network.createServer(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Serializer.registerClass(BasicMessage.class);
		Serializer.registerClass(StartMessage.class);
		Serializer.registerClass(MoveMessage.class);
		Serializer.registerClass(CreateMessage.class);
		Serializer.registerClass(SyncMessage.class);
		Serializer.registerClass(TargetMessage.class);
		Serializer.registerClass(AttackMessage.class);
		Serializer.registerClass(HitsMessage.class);
		
		myServer.addMessageListener(new ServerListener(this), BasicMessage.class);
		myServer.addMessageListener(new ServerListener(this), CreateMessage.class);
		myServer.addMessageListener(new ServerListener(this), MoveMessage.class);
		myServer.addMessageListener(new ServerListener(this), TargetMessage.class);
		myServer.addMessageListener(new ServerListener(this), AttackMessage.class);
		myServer.addConnectionListener(new ServerConnListener(this));
		
		startState = new ServerStartState(myServer, players);
		startState.initialize(stateManager, this);
		startState.setEnabled(false);
		
		gameState = new ServerGameState(myServer, players);
		gameState.initialize(stateManager, this);
		gameState.setEnabled(false);
		
		stateManager.attach(startState);
		stateManager.attach(gameState);
		
		currentState = startState;
		startState.setEnabled(true);
		
		Textures.runOnce(assetManager);
		map = new MapLoader(assetManager, "test_map", players);
		
		myServer.start();
		System.out.println('r');
	}
	
	public void simpleUpdate(float delta){
		
	}

	public void changeState(AppState state){
		currentState.setEnabled(false);
		currentState = state;
		currentState.setEnabled(true);
	}
	
	/**
	 * Relay a message from a client to all other clients
	 * @param src client who sent the message
	 * @param msg what was sent
	 */
	public void relayMessage(HostedConnection src, Message msg){
		myServer.broadcast(Filters.notEqualTo(src), msg);
	}

	@Override
	public void destroy(){
		
		myServer.close();
		super.destroy();
	}
	
	/**
	 * Restart the server after all players exit
	 * make sure to add back all states and listeners
	 */
	public void restart(){
		myServer.close();
		stateManager.detach(startState);
		stateManager.detach(gameState);
		ready = 0;
		
		try {
			myServer = Network.createServer(3333);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Serializer.registerClass(BasicMessage.class);
		Serializer.registerClass(StartMessage.class);
		Serializer.registerClass(MoveMessage.class);
		Serializer.registerClass(CreateMessage.class);
		
		myServer.addMessageListener(new ServerListener(this), BasicMessage.class);
		myServer.addMessageListener(new ServerListener(this), CreateMessage.class);
		myServer.addMessageListener(new ServerListener(this), MoveMessage.class);
		myServer.addConnectionListener(new ServerConnListener(this));
		
		startState = new ServerStartState(myServer, players);
		startState.initialize(stateManager, this);
		startState.setEnabled(false);
		
		gameState = new ServerGameState(myServer, players);
		gameState.initialize(stateManager, this);
		gameState.setEnabled(false);
		
		stateManager.attach(startState);
		stateManager.attach(gameState);
		
		currentState = startState;
		startState.setEnabled(true);
		
		myServer.start();
		
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		java.util.logging.Logger.getLogger("").setLevel(Level.SEVERE);
		ServerMain server = new ServerMain();
		AppSettings newSetting = new AppSettings(true);
		newSetting.setFrameRate(60);
		server.setSettings(newSetting);
		
		/* set the players */
		if (args[0].contentEquals("2")) players = 2;
		else if (args[0].contentEquals("3")) players = 3;
		else if (args[0].contentEquals("4")) players = 4;
		else if (args[0].contentEquals("5")) players = 5;
		else if (args[0].contentEquals("6")) players = 6;
		else players = 2;
		
		if(args.length > 1)
			port = Integer.parseInt(args[1]);
		
		
		server.start(JmeContext.Type.Headless);
	}
}
