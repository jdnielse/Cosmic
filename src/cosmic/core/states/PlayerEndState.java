package cosmic.core.states;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;

import cosmic.core.Player;

public class PlayerEndState extends AbstractAppState {
	
	private Player player;
	private AssetManager assetManager;
	private AppSettings settings;
	private Geometry background;
	
	public PlayerEndState(AppSettings s){
		settings = s;
	}
	
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
    	super.initialize(stateManager, app); 
    	this.player = (Player) app;          // cast to a more specific class
    	assetManager = app.getAssetManager();
    	
    	//background = new Geometry("background", new Box(1, 1, 0f));
    	//background.move(0, 0, player.getCamera().distanceToNearPlane(Vector3f.ZERO));
		
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
    		//Material bmat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    		
    		
        	//remove things we don't need
    		Picture pic = new Picture("Winnage");
        	if(player.mothership.hp <= 0){
        		System.out.println("You lost");
        		//bmat.setTexture("ColorMap", assetManager.loadTexture("assets/textures/loser.png"));
        		pic.setImage(assetManager, "assets/textures/loser.png", true);
        	}else{
        		System.out.println("You won");
        		//bmat.setTexture("ColorMap", assetManager.loadTexture("assets/textures/winner.png"));
        		
        		pic.setImage(assetManager, "assets/textures/winner.png", true);
        		
        	}
        	pic.setWidth(settings.getWidth());
    		pic.setHeight(settings.getHeight());
    		pic.setPosition(0, 0);
    		player.getRootNode().attachChild(pic);
        	//background.setMaterial(bmat);
    		
    		//player.getRootNode().attachChild(background);
    		//player.restart();
    	} 
    	else {
    		// take away everything not needed while this state is PAUSED
    		player.getRootNode().detachChildNamed("background");
    	}
    }
 
    // Note that update is only called while the state is both attached and enabled.
    @Override
    public void update(float tpf) {
    	// do the following while game is RUNNING
    	if(isEnabled()){
    		//display if you lost or won
    		//show a button to go back to the main menu
    	}
    }
}
