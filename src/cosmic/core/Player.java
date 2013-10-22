package cosmic.core;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.audio.AudioNode;
import com.jme3.math.Vector3f;
import com.jme3.network.*;
import com.jme3.network.serializing.Serializer;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.system.AppSettings;

import cosmic.core.states.*;
import cosmic.listener.ClientListener;
import cosmic.listener.ServerConnListener;
import cosmic.listener.ServerListener;
import cosmic.message.*;
import cosmic.object.*;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.render.TextRenderer;

public class Player extends SimpleApplication {
	
	private Camera camera;
	public Nifty nifty;
	Client myClient;
	MapLoader loader;
	
	private AppState loadingState;
	PlayerGameState gameState;
	private AppState currentState;
	public AppState endState;
	
	private AudioNode audio_music;
	CosmicCam cosmicCam;
	public LinkedList<Delay> commands;
	public Mothership mothership;

	public int player_id;
	int num_players;
	Vector3f start;
	
	float sum;
	Player player = this;
	
	public int maxResources;
	public int resources;

	@Override
	public void simpleInitApp() {
		myClient = null;
		camera = this.cam;
		
		/* Read HUD XML and initialize the controller */
		NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
		nifty = niftyDisplay.getNifty();
		nifty.fromXml("assets/Interface/screen.xml", "main", new CosmicScreen(player));
			
		/* add the nifty display */
		guiViewPort.addProcessor(niftyDisplay);
		
		/* disable the fly cam */
		flyCam.setDragToRotate(true);
		flyCam.setEnabled(false);
		
		commands = new LinkedList<Delay>();
		
		//set our resource limit
		maxResources = resources = 15000 ;
		
		Unit.player = this;
		
		//initAudio();
	}
	
	public void startGame(String text, String port, boolean cGame) {
		
		/* try to parse the game port */
		int gamePort;
		try {
			gamePort = Integer.parseInt(port);
		} catch (NumberFormatException e) {
			
			/* just use the default port */
			gamePort = 3333;
		}
		
		/* create a game */
		if(cGame) {
			/* try to connect */
			try {
				myClient = Network.connectToServer("localhost", gamePort);
				System.out.println("Connected successfully.");
			} catch (IOException e1) {
				String args[] = new String[2]; args[0] = text;
				args[1] = ""+gamePort;
				/* failed to connect, call ServerMain.main statically */			
				System.out.println("Failed to connect, creating server.");
//				try {
//					String cmd = "java -jar server.jar " + args[0] + " "+ args[1];
//					System.out.println(cmd);
//					Process proc = Runtime.getRuntime().exec(cmd, null, null);
//					int read = proc.getInputStream().read();
//					
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					System.out.println("Couldn't create server");
//					e.printStackTrace();
//				}
				System.out.println(args[0]);
				ServerMain.main(args);
				
				/* try to connect again */
				try {
					myClient = Network.connectToServer("localhost", gamePort);
					System.out.println("Connected successfully.");
				} catch (IOException e2) {
					
					/* still failed to connect, exit */
					System.out.println("Failed to connect again, error state.");
					nifty.gotoScreen("errorc");
				}
			}
		}
		else {
			
			/* otherwise try to join the provided IP */
			try {
				myClient = Network.connectToServer(text, gamePort);
				System.out.println("Connected successfully.");
			} catch (IOException e1) {
				
				/* failed to connect, exit */			
				System.out.println("Failed to connect, error state.");
				nifty.gotoScreen("errorj");
			}
		}
		
		
		
		Serializer.registerClass(BasicMessage.class);
		Serializer.registerClass(StartMessage.class);
		Serializer.registerClass(MoveMessage.class);
		Serializer.registerClass(CreateMessage.class);
		Serializer.registerClass(SyncMessage.class);
		Serializer.registerClass(TargetMessage.class);
		Serializer.registerClass(AttackMessage.class);
		Serializer.registerClass(HitsMessage.class);
		
		myClient.addMessageListener(new ClientListener(this), BasicMessage.class);
		myClient.addMessageListener(new ClientListener(this), StartMessage.class);
		myClient.addMessageListener(new ClientListener(this), MoveMessage.class);
		myClient.addMessageListener(new ClientListener(this), CreateMessage.class);
		myClient.addMessageListener(new ClientListener(this), SyncMessage.class);
		myClient.addMessageListener(new ClientListener(this), TargetMessage.class);
		myClient.addMessageListener(new ClientListener(this), AttackMessage.class);
		myClient.addMessageListener(new ClientListener(this), HitsMessage.class);
		
		loadingState = new PlayerLoadState(this.settings);
		loadingState.initialize(stateManager, this);
		loadingState.setEnabled(false);
		
		gameState = new PlayerGameState(this.settings);
		gameState.initialize(stateManager, this);
		gameState.setEnabled(false);
		
		endState = new PlayerEndState(this.settings);
		endState.initialize(stateManager, this);
		endState.setEnabled(false);
		
		this.stateManager.attach(loadingState);
		this.stateManager.attach(gameState);
		this.stateManager.attach(endState);
		
		loadingState.setEnabled(true);
		currentState = loadingState;
		
		//load up all textures
		Textures.runOnce(assetManager);
		
		//give builder access to player
		//cosmic.object.Builder.player = this;
		
		myClient.start();
	}
	
	/**
	 * Restart the game when it ends
	 * make sure to add back all states and listeners
	 */
	public void restart(){
		myClient.close();
		stateManager.detach(loadingState);
		stateManager.detach(gameState);
		stateManager.detach(endState);
		guiViewPort.clearProcessors();
		
		myClient = null;
		camera = this.cam;
		
		/* Read HUD XML and initialize the controller */
		NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
		nifty = niftyDisplay.getNifty();
		nifty.fromXml("assets/Interface/screen.xml", "main", new CosmicScreen(player));
			
		/* add the nifty display */
		guiViewPort.addProcessor(niftyDisplay);
		
		/* disable the fly cam */
		flyCam.setDragToRotate(true);
		flyCam.setEnabled(false);
		
		commands = new LinkedList<Delay>();
		
		rootNode.detachAllChildren();
		
		//set our resource limit
		maxResources = resources = 15000 ;

	}
	
	
	public void simpleUpdate(float delta){
		//System.out.println(myClient.getId());
	}
	
	@Override
	public void destroy(){
		if(myClient != null) {
			myClient.close();
		}
		super.destroy();
	}

	public void changeState(final AppState state){
		currentState.setEnabled(false);
		currentState = state;
		currentState.setEnabled(true);
	}

	public void gameInit(final String map, final int num, final float x, final float y){
		this.enqueue(new Callable<AppState>() {
			public AppState call() throws Exception {

				player_id = myClient.getId();
				loader = new MapLoader(assetManager, map, num);
				
				num_players = num;
				
				start = new Vector3f(x, y, 1f);
				
				gameState.ready(num,loader);
				myClient.send(new BasicMessage("ready"));
				
				
				return currentState;
			}
		});
	}
	
	public void gameStart(){
		this.enqueue(new Callable<AppState>() {
			public AppState call() throws Exception {
				
				changeState(gameState);
				
				String id = gameState.createUnit(myClient.getId(), "mothership", start);
				camera.setLocation(new Vector3f(start.x, start.y, camera.getLocation().z));
				mothership = (Mothership) gameState.getUnits().get(id);
				myClient.send(new CreateMessage(myClient.getId(), id, start));
				
				rootNode.attachChild(loader.background);
				
				cosmicCam = new CosmicCam(camera, player);
				return currentState;
				
			}
		});
		
		
	}
	
/*	public void gameEnd(){
		this.enqueue(new Callable<AppState>(){
			public AppState call() throws Exception {
				changeState(endState);
				
				
				return currentState;
				
			}
		});
	}
	*/
	public CosmicCam getCosmicCam() {
		return cosmicCam;
	}
	
	public LinkedList<Delay> getCommands() {
		return commands;
	}
	
	/* initialize audio */
	private void initAudio() {
		
		/* background music */
		audio_music = new AudioNode(assetManager, "assets/sounds/Presenterator.ogg", false);
		audio_music.setLooping(true);
		audio_music.setVolume(3);
		rootNode.attachChild(audio_music);
		audio_music.play();
	}
	
	public void sendMessage(Message msg){
		myClient.send(msg);
	}
	
	/**
	 * Get the current player state
	 * @param state string identifying the desired state
	 * @return currentState
	 */
	public AppState getState(String state){
		switch(state){
		case "current":
			return currentState;
		case "loading":
			return loadingState;
		case "game":
			return gameState;
		case "end":
			return endState;
		}
		return null;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		java.util.logging.Logger.getLogger("").setLevel(Level.SEVERE);
		Player player = new Player();
		//player.setShowSettings(false);
		AppSettings newSetting = new AppSettings(true);
		newSetting.setFrameRate(60);
		newSetting.setResolution(1024, 768);
		player.setSettings(newSetting);
		player.setPauseOnLostFocus(false);
		player.setDisplayStatView(false);
		player.start();

	}

}
