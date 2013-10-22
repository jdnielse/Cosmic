package cosmic.object;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.VertexBuffer;

import cosmic.core.Player;
import de.lessvoid.nifty.elements.render.TextRenderer;

public class SpaceStation extends Unit{
	public static int count = 0; //keep track of how many of this unit we have
	//private static String texture = "assets/textures/beetle.png";
	private static float scale = 0.30f;
	public boolean captured = false;
	public Player player = null;
	public int last_id;

	public SpaceStation(AssetManager assetManager, int p_id, String u_id, Vector3f pos) {
		super(assetManager, 5, pos);
		player_id = p_id;

		if(u_id == null){
			unit_id = p_id + "_SpaceStation_" + count;
			count += 1;
		}
		else {
			unit_id = u_id;
		}

		setName(unit_id);
		geo.scale(scale);
		bounds.scale(scale, scale, 1f);
		geo.setMaterial(Textures.spacestation);
		//mat.setTexture("ColorMap", assetManager.loadTexture(texture));
		//geo.setMaterial(mat);
		
		cooldowns.put("give", new Ability(0, 30));


		maxhp = hp = 100;
		energy = 20;
		vel = 0f;
		acc = 0f;
		att = 0f;
		range = 0f;
		lineGeometry = new Geometry("line"+unit_id, lineMesh);
		Material lineMaterial =  assetManager.loadMaterial("Common/Materials/RedColor.j3m");

		lineGeometry.setMaterial(lineMaterial);
		lineGeometry.move(0, 0, 1);
	}
	
	public float takeDamage(float d){
		hp -= d;
		//System.out.println("hp: "+hp+" maxhp: "+maxhp+" sending: "+hp/maxhp);
		updateHPDisplay(hp/maxhp);
		if(hp<=0){
			//die
			captured = true;
		}
		return hp;
	}
	
	public void changeId(int newId){
		player_id = newId;
		unit_id = newId + unit_id.substring(1);
		setName(unit_id);
		hp = maxhp;
		updateHPDisplay(hp/maxhp);
		player = null;
	}
	
	public void update(float tpf){
		super.update(tpf);
		
		if(cooldowns.get("give").val <= 0 && player != null){
			player.resources += 1000;
			if(player.nifty.getCurrentScreen().findElementByName("resources") != null)
				player.nifty.getCurrentScreen().findElementByName("resources").getRenderer(TextRenderer.class).setText("Resources: "+player.resources);
			cooldowns.get("give").val = cooldowns.get("give").max;
		}
	}

	public void PlayerAttack(){
		//cannot attack
	}

	public boolean serverAttack(){
		//cannot attack
		return false;
	}

	public void updateAttack(float tpf){
		//cannot attack
	}

}
