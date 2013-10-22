package cosmic.object;

import java.util.ArrayList;
import java.util.HashMap;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;

//this creates textures once so we don't have to create a new one for each unit.
public class Textures {

	static String[] list = {"unit", "mothership", "Fighter", "Factory", "LaserCannon", "MissilePlatform", "RelayNode","Station","Nebula"};
	static String[] textures = {"plane.png", "mothership.png", "beetle.png", "factory.png", "LaserCannon.png", "missilePlatform.png", "relayNode.png","mothership.png","Fire.png"};
	public static HashMap<String, ArrayList<Material>> mats = new HashMap<String, ArrayList<Material>>();
	static Material spacestation;

	public static void runOnce(AssetManager assetManager){
		//setup beetles
		
		spacestation = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		spacestation.setTexture("ColorMap", assetManager.loadTexture("assets/textures/GAME ART/bugs/caterpillar.png"));
		spacestation.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		spacestation.getAdditionalRenderState().setDepthWrite(false);
		
		for(int s = 0; s < list.length; s++){
			
			mats.put(list[s], new ArrayList<Material>());
			for(int i = 0; i < 6; i++){
				Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
				mat.setTexture("ColorMap", assetManager.loadTexture("assets/textures/" + textures[s]));
				mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
				mat.getAdditionalRenderState().setDepthWrite(false);
				mats.get(list[s]).add(mat);
			}
			mats.get(list[s]).get(0).setColor("Color", new ColorRGBA(.75f, .75f, 1f, 1f));
			mats.get(list[s]).get(1).setColor("Color", new ColorRGBA(1f, .6f, .6f, 1f));
			mats.get(list[s]).get(2).setColor("Color", new ColorRGBA(.25f, 1f, .52f, 1f));
			mats.get(list[s]).get(3).setColor("Color", new ColorRGBA(.75f, .75f, .25f, 1f));
			mats.get(list[s]).get(4).setColor("Color", new ColorRGBA(.25f, 1f, 1f, 1f));
			mats.get(list[s]).get(5).setColor("Color", new ColorRGBA(.4f, 1f, .5f, 1f));
		}
	}
}
