package cosmic.object;

import com.jme3.asset.AssetManager;

//The object for resources. contains what resource it has, how much, mine rate, graphic to use
public class Resource extends CosmicObject{
	String type;
	int max;
	int curr;
	boolean active;

	public Resource(AssetManager assetManager) {
		super(assetManager, -1);
		type = "rocks";
		max = 100;
		curr = max;
		active = true;
	}
	public Resource(AssetManager assetManager, String t, int m) {
		super(assetManager, -1);
		type = t;
		max = m;
		curr = max;
		active = true;
	}

}
