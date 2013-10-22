package cosmic.object;

import java.util.HashMap;
import java.util.Vector;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;

import cosmic.core.states.PlayerGameState;

public class LaserCannon extends Unit{
	public static int count = 0; //keep track of how many of this unit we have
	//private static String texture = "assets/textures/beetle.png";
	private static float scale = 0.50f;
	Vector3f end = Vector3f.ZERO;
	public Vector<String> attackedUnits;


	public LaserCannon(AssetManager assetManager, int p_id, String u_id, Vector3f pos) {
		super(assetManager, p_id, pos);
		player_id = p_id;

		if(u_id == null){
			unit_id = p_id + "_LaserCannon_" + count;
			count += 1;
		}
		else {
			unit_id = u_id;
		}
		
		attackedUnits = new Vector<String>();

		setName(unit_id);
		geo.scale(scale);
		bounds.scale(scale, scale, 1f);
		geo.setMaterial(Textures.mats.get("LaserCannon").get(p_id));
		//mat.setTexture("ColorMap", assetManager.loadTexture(texture));
		//geo.setMaterial(mat);

		maxhp = hp = 200;
		energy = 100;
		vel = 3f;
		acc = .01f;
		att = 10f;
		range = 6.5f;
		lineGeometry = new Geometry("line"+unit_id, lineMesh);
		Material lineMaterial =  assetManager.loadMaterial("Common/Materials/RedColor.j3m");

		lineGeometry.setMaterial(lineMaterial);
		lineGeometry.move(0, 0, 1);
	}

	public void updateAttack(float tpf){
		if(cooldowns.get("laser").val < 0){
			parent.detachChildNamed("line"+unit_id);
		}else{
			lineMesh.setBuffer(VertexBuffer.Type.Position, 3, new float[]{ pos.x,pos.y,pos.z,end.x,end.y,end.z});
			lineMesh.updateBound();
		}
		if(target.alive){ //make sure you can attack it
			if( target.pos.distance(this.getWorldTranslation()) > this.range ){
				setDestination(this.getWorldTranslation().subtract(target.pos).normalize().mult(this.range - .15f).add(target.pos));
				//accToDestTest(tpf);
			}
		}else{
			stopAttacking();
		}
	}


	public boolean serverAttack(){

		if(inRange(target) && cooldowns.get("attack").val <= 0){
			
			Ray laser = new Ray(pos, target.pos.subtract(pos).normalize());
			Node root = this.getParent().getParent();
			CollisionResults hitResults = new CollisionResults();
			root.collideWith(laser, hitResults);
			HashMap<Integer,Unit> attacked = new HashMap<Integer,Unit>();
			cooldowns.get("attack").val = 3f;
			for(CollisionResult res : hitResults){
				Node n = res.getGeometry().getParent();
				if(n instanceof Unit){
					Unit u = (Unit) n;
					if((u.getOwnerId() != player_id) && attacked.get(u.hashCode()) == null && res.getDistance() <= range){
						attacked.put(u.hashCode(), u);
						attack(u);
						attackedUnits.add(u.unit_id);
					}
				}
			}
			return true;
		}
		else{
			return false;
		}
	}


	public void PlayerAttack(){
		if(target != null) {

			end = target.pos.subtract(pos).normalize().mult(range).add(pos);
			lineMesh.setBuffer(VertexBuffer.Type.Position, 3, new float[]{ pos.x,pos.y,pos.z,end.x,end.y,end.z});
			lineMesh.updateBound();
			lineMesh.updateCounts();

			/*Ray laser = new Ray(pos, end.subtract(pos).normalize());
			Node root = this.getParent().getParent();
			CollisionResults hitResults = new CollisionResults();
			root.collideWith(laser, hitResults);
			//System.out.println(hitResults.size());
			for(CollisionResult res : hitResults){
				Node n = res.getGeometry().getParent();
				if(n instanceof Unit){
					Unit u = (Unit) n;
					if((u.getOwnerId() != player_id) && res.getDistance() <= range){
						//System.out.println("Attacking: " + u.getId());
						attack(u);
					}
				}
			}*/

			FrustumIntersect vis = player.getCamera().contains(bounds.getWorldBound());
			if(vis == FrustumIntersect.Inside)
				sound.play();

			parent.attachChild(lineGeometry);
			cooldowns.get("laser").val = .4f;

		}
	}

}
