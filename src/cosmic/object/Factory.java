package cosmic.object;

import java.util.Vector;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;

public class Factory extends Builder{
	public static int count = 0; //keep track of how many of this unit we have
	//private static String texture = "assets/textures/beetle.png";
	private static float scale = 0.80f;
	
	private Vector<Unit> buildQueue = new Vector<Unit>();
	
	public Factory(AssetManager assetManager, int p_id, String u_id, Vector3f p) {
		super(assetManager, p_id, p);
		player_id = p_id;

		if(u_id == null){
			unit_id = p_id + "_Factory_" + count;
			count += 1;
		}
		else {
			unit_id = u_id;
		}
		
		setName(unit_id);
		geo.scale(scale);
		geo.setMaterial(Textures.mats.get("Factory").get(p_id));
		//mat.setTexture("ColorMap", assetManager.loadTexture(texture));
		//geo.setMaterial(mat);
		
		maxhp = hp = 100;
		energy = 100;
		vel = 3f;
		acc = .01f;
		range = 100f;
	}
	
	public void PlayerAttack(){
		//cannot attack
	}
	
	public void ServerAttack(){
		//cannot attack
	}
	//createUnit
	//bool canCreateUnit(Unit)
	//Vector<Unit> getBuildQueue()

}
