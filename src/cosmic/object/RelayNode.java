package cosmic.object;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

public class RelayNode extends Unit{
	public static int count = 0; //keep track of how many of this unit we have
	//private static String texture = "assets/textures/beetle.png";
	private static float scale = 0.30f;

	public RelayNode(AssetManager assetManager, int p_id, String u_id, Vector3f pos) {
		super(assetManager, p_id, pos);
		player_id = p_id;

		if(u_id == null){
			unit_id = p_id + "_RelayNode_" + count;
			count += 1;
		}
		else {
			unit_id = u_id;
		}
		
		setName(unit_id);
		geo.scale(scale);
		bounds.scale(scale, scale, 1f);
		geo.setMaterial(Textures.mats.get("RelayNode").get(p_id));
		//mat.setTexture("ColorMap", assetManager.loadTexture(texture));
		//geo.setMaterial(mat);
		

		hp = 20;
		energy = 100;
		vel = 2f;
		acc = .05f;
		att = 5f;
		range = 3.5f;
		lineGeometry = new Geometry("line"+unit_id, lineMesh);
		Material lineMaterial =  assetManager.loadMaterial("Common/Materials/RedColor.j3m");
		
		lineGeometry.setMaterial(lineMaterial);
		lineGeometry.move(0, 0, 1);
	}

}
