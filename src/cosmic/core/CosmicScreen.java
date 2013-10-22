package cosmic.core;

import java.util.Random;
import java.util.Vector;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector3f;

import cosmic.message.CreateMessage;
import cosmic.object.Builder;
import cosmic.object.Delay;
import cosmic.object.RelayNode;
import cosmic.object.Unit;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

public class CosmicScreen extends AbstractAppState implements ScreenController {
	
	private Nifty nifty;
	private Screen screen;
	private SimpleApplication app;
	private boolean join = false;
	Player player;
	DropDown<String> players;
	
	public CosmicScreen(Player player) {
		
		/* get a reference to player */
		this.player = player;
	}
	

	@Override
	public void bind(Nifty nifty, Screen screen) {
		this.nifty = nifty;
		this.screen = screen;
		
		/* populate dropdown menu */
		players = screen.findNiftyControl("players", DropDown.class);
		players.addItem("2");
		players.addItem("3");
		players.addItem("4");
		players.addItem("5");
		players.addItem("6");
		
		/* populate port field */
		TextField port = screen.findNiftyControl("Port", TextField.class);
		port.setText("Default Port");
		
	}

	@Override
	public void onEndScreen() {
	}

	@Override
	public void onStartScreen() {
	}
	
	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);
		this.app=(SimpleApplication)app;
		
	}

	@Override
	public void update(float tpf) { 
		
	}
	
	@SuppressWarnings("deprecation")
	public void startGame() {
		/* get the player selection */
		String tempPlayers = players.getSelection();
		
		/* get the port */
		TextField port = screen.findNiftyControl("Port", TextField.class);
		String tempPort = port.getText();
		
		if(tempPort.contentEquals("Default Port")) {
			tempPort = "3333";
		}
		
		/* remove the main menu and load the HUD */
		nifty.gotoScreen("hud");
		
		/* create a server */
		player.startGame(tempPlayers, tempPort, true);
		
	}
	
	@SuppressWarnings("deprecation")
	public void joinGame() {
		if(!join) {
			/* load the join screen */
			nifty.gotoScreen("join");
			TextField port = screen.findNiftyControl("PortJoin", TextField.class);
			port.setText("Default Port");
			join = true;
		}
		else {
			/* get the IP/Hostname and try to join */
			TextField IP = screen.findNiftyControl("IPText", TextField.class);
			TextField port = screen.findNiftyControl("PortJoin", TextField.class);
			String tempPort = port.getText();
			
			if(tempPort.contentEquals("Default Port")) {
				tempPort = "3333";
			}
			
			nifty.gotoScreen("hud");
			
			player.startGame(IP.getText(), tempPort, false);
		}
	}
	
	public void createUnit(String type) {
		/* create a single unit around the current target */
		Builder u = (Builder) player.cosmicCam.curTarget;
		
		//don't create it if we can't
		//if(!u.canBuild) return;
		
		if(player.gameState.unitCost.get(type) != null){
			player.resources -= player.gameState.unitCost.get(type);
			//System.out.println(player.resources);
			if(player.resources < 0 || u.buildQueue.size()>=6){ //set a queue limit of 5
				player.resources += player.gameState.unitCost.get(type);
				return;
			}
		}
		//System.out.println(player.resources);

		//create a delay with {player_id, pos, creator unit, unit type}
		//player.sendMessage(new CreateMessage(player.player_id, id, pos));
		Vector<Object> data = new Vector<Object>();
		data.add(player.player_id); data.add(u); data.add(type);
		//u.canBuild = false;
		float dist = u.getWorldTranslation().distance(player.mothership.pos)/75;
		for(RelayNode relay : player.gameState.relays){
			float d = u.getWorldTranslation().distance(relay.pos)/35;
			if(d<dist){
				dist = d;
			}
		}
		Delay d = new Delay("build",player.gameState.unitTimeCost.get(type) + dist,data);
		//player.commands.add(d);
		u.buildQueue.add(d);
		
		nifty.getCurrentScreen().findElementByName("resources").getRenderer(TextRenderer.class).setText("Resources: "+player.resources);
		
	}
	
	public void createHint(String type,String unit){
		//System.out.println("creating hint: " +type);
		nifty.getCurrentScreen().findElementByName(type).setVisible(true);
	}
	
	public void deactivateHint(String type){
		//System.out.println("removing hint: " + type);
		nifty.getCurrentScreen().findElementByName(type).hide();
		//System.out.println("wtf");
	}
	
	public int getResources(){
		return player.resources;
	}
	
	public int getMaxResources(){
		return player.maxResources;
	}
	

	public void resume() {
		
		/* return to the correct HUD */
		if (player.cosmicCam.mothership) {
			nifty.gotoScreen("mother");
		}else if(player.cosmicCam.factory){
			nifty.gotoScreen("factory");
		}
		else {
			nifty.gotoScreen("hud");
		}
		
		/* re-enable controls */		
		player.cosmicCam.setMappings();
		player.cosmicCam.inMenu = false;
	}
	
	public void back() {
		/* return to the main menu */
		join = false;
		nifty.exit();
		nifty.fromXml("assets/Interface/screen.xml", "main", this);
	}
	
	public void quitGame() {
		
		/* quit */
		player.stop();
	}
}
