package cosmic.object;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.*;
import com.jme3.scene.shape.Box;


//contains info all objects in the game need (coordinates, speed, rotation, graphic)
public class CosmicObject extends Node{ //changed from node to geometry - need to fix geo
	public Material mat;
	public Geometry geo;
	public Geometry bounds;
	String name;
	public Vector3f pos;
	AssetManager assetManager;
	public CosmicObject(AssetManager assetManager, int p_id){
		this.assetManager = assetManager;
		Box box = new Box(Vector3f.ZERO, 1f, 1f, 0f);
		Box boxbounds = new Box(Vector3f.ZERO, 1f, 1f, 1f);
		geo = new Geometry("geo", box);
		bounds = new Geometry("bounds", boxbounds);
		geo.setMaterial(Textures.mats.get("unit").get(p_id));
		bounds.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"));
		bounds.setCullHint(CullHint.Always);
		geo.setQueueBucket(Bucket.Transparent);
		pos = new Vector3f(0f,0f,1f);
		geo.move(pos);
		bounds.move(0, 0, .5f);
		this.attachChild(geo);
		this.attachChild(bounds);
	}
	
	public CosmicObject(AssetManager assetManager, Geometry g, Material m, Vector3f p){
		Box boxbounds = new Box(Vector3f.ZERO, 1f, 1f, 1f);
		bounds = new Geometry("bounds", boxbounds);
		bounds.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"));
		bounds.setCullHint(CullHint.Always);
		geo = g;
		mat = m;
		pos = p;
		mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha); //we probably won't change this ever
		mat.getAdditionalRenderState().setDepthTest(false);
		geo.setMaterial(mat);
		geo.move(pos);
		geo.setQueueBucket(Bucket.Translucent);
		this.attachChild(geo);
		this.attachChild(bounds);
	}
	
}
