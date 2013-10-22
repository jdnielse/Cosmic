package cosmic.object;

import java.util.LinkedList;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;

public class Builder extends Unit {
	public LinkedList<Delay> buildQueue = new LinkedList<Delay>();
	public boolean canBuild = true; //keeps track of it's build status
	int count = 0;
	public Builder(AssetManager assetManager, int p_id, Vector3f p) {
		super(assetManager, p_id, p);
		
	}

	public void update(float tpf){
		super.update(tpf);
		//update delay build time of first 
//		if(count != buildQueue.size()){
//			System.out.println(buildQueue.size());
//			count = buildQueue.size();
//		}
		if(!buildQueue.isEmpty()){
			Delay d = buildQueue.getFirst();
			d.update(tpf);
			
		}
		//System.out.println(buildQueue.toString());
		
	}
	
}
