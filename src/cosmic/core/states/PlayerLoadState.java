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

import cosmic.core.Player;

public class PlayerLoadState extends AbstractAppState {
	
	
	private Player player;
	private Geometry background;
	private AssetManager assetManager;
	private AppSettings settings;
	
	public PlayerLoadState(AppSettings s){
		settings = s;
	}
	
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
    	super.initialize(stateManager, app); 
    	this.player = (Player) app;          // cast to a more specific class
    	assetManager = app.getAssetManager();
    	
		background = new Geometry("background", new Box(1, 1, 0f));
		Material bmat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		bmat.setTexture("ColorMap", assetManager.loadTexture("assets/textures/loading.jpg"));
		background.setMaterial(bmat);
		background.move(0, 0, player.getCamera().distanceToNearPlane(Vector3f.ZERO));
		
		player.getCamera().setLocation(new Vector3f(0,0,10));
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
    		player.getRootNode().attachChild(background);
    	} 
    	else {
    		// take away everything not needed while this state is PAUSED
    		//player.getRootNode().detachAllChildren();
    		player.getRootNode().detachChildNamed("background");
    	}
    }
 
    // Note that update is only called while the state is both attached and enabled.
    @Override
    public void update(float tpf) {
    	// do the following while game is RUNNING
    	if(isEnabled()){
    		
    	}
    }

}
