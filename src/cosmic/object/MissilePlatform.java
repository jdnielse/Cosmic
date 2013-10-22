package cosmic.object;

import java.util.HashMap;
import java.util.Vector;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Sphere;

public class MissilePlatform extends Unit{
	public static int count = 0; //keep track of how many of this unit we have
	//private static String texture = "assets/textures/beetle.png";
	private static float scale = 0.60f;
	private Geometry diskGeometry;
	public Vector<String> attackedUnits;
	
	public MissilePlatform(AssetManager assetManager, int p_id, String u_id, Vector3f pos) {
		super(assetManager, p_id, pos);
		player_id = p_id;

		if(u_id == null){
			unit_id = p_id + "_MissilePlatform_" + count;
			count += 1;
		}
		else {
			unit_id = u_id;
		}
		
		attackedUnits = new Vector<String>();

		setName(unit_id);
		geo.scale(scale);
		bounds.scale(scale, scale, 1f);
		geo.setMaterial(Textures.mats.get("MissilePlatform").get(p_id));
		//mat.setTexture("ColorMap", assetManager.loadTexture("Fire.jpg"));
		//geo.setMaterial(mat);


		maxhp = hp = 200;
		energy = 10;
		vel = 3f;
		acc = 0.1f;
		att = 10f;
		range = 7f;
		float radius = .75f;

		lineGeometry = new Geometry("line"+unit_id, lineMesh);
		Material lineMaterial =  assetManager.loadMaterial("Common/Materials/RedColor.j3m");
		Material diskMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		diskMaterial.setColor("Color", ColorRGBA.Red);
		//diskMaterial.setTexture("Texture", assetManager.loadTexture("Fire.png"));

		diskGeometry = new Geometry("disk"+unit_id, new Sphere(32, 32, radius));
		
		lineGeometry.setMaterial(lineMaterial);
		diskGeometry.setMaterial(diskMaterial);
		lineGeometry.move(0, 0, 1);
		diskGeometry.move(0, 0, 1);

	}
	
	public float takeDamage(float d){
		hp -= d;
		//System.out.println("hp: "+hp+" maxhp: "+maxhp+" sending: "+hp/maxhp);
		updateHPDisplay(hp/maxhp);
		if(hp<=0){
			//die
			alive = false;
			parent.detachChildNamed("line"+unit_id);
			parent.detachChildNamed("disk"+unit_id);
		}
		return hp;
	}
	
	public void updateAttack(float tpf){
		if(cooldowns.get("laser").val < 0){
			parent.detachChildNamed("line"+unit_id);
			parent.detachChildNamed("disk"+unit_id);
		}else{

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
			cooldowns.get("attack").val = 5f;
			diskGeometry.setLocalTranslation(target.pos);
			HashMap<Integer,Unit> attacked = new HashMap<Integer,Unit>();
			
			Node root = this.getParent().getParent();
			CollisionResults hitResults = new CollisionResults();
			root.collideWith(diskGeometry.getWorldBound(), hitResults);
			//System.out.println(hitResults.size());
			for(CollisionResult res : hitResults){
				Node n = res.getGeometry().getParent();
				if(n instanceof Unit){
					Unit u = (Unit) n;
					if(attacked.get(u.hashCode())==null && u.getOwnerId() != player_id){
						attacked.put(u.hashCode(),u);
						attack(u);
						attackedUnits.add(u.unit_id);
					}
				}
			}
			
			return true;
		}else{
			return false;
		}
	}


	public void PlayerAttack(){
		if(target != null) {
			
			lineMesh.setBuffer(VertexBuffer.Type.Position, 3, new float[]{ pos.x,pos.y,pos.z,target.pos.x,target.pos.y,target.pos.z});
			lineMesh.updateBound();
			lineMesh.updateCounts();
			
			diskGeometry.setLocalTranslation(target.pos);
			
			FrustumIntersect vis = player.getCamera().contains(bounds.getWorldBound());
			if(vis == FrustumIntersect.Inside)
				sound.play();
			
			parent.attachChild(lineGeometry);
			parent.attachChild(diskGeometry);
			cooldowns.get("laser").val = .6f;
			
			/*HashMap<Integer,Unit> attacked = new HashMap<Integer,Unit>();
			Node root = this.getParent().getParent();
			CollisionResults hitResults = new CollisionResults();
			root.collideWith(diskGeometry.getWorldBound(), hitResults);
			//System.out.println(hitResults.size());
			for(CollisionResult res : hitResults){
				Node n = res.getGeometry().getParent();
				if(n instanceof Unit){
					Unit u = (Unit) n;
					if(attacked.get(u.hashCode())==null){
						attacked.put(u.hashCode(),u);
						attack(u);
					}
				}
			}*/
		}
	}
	
	
	public void stopAttacking(){
		attacking = false;
		parent.detachChildNamed("line"+unit_id);
		parent.detachChildNamed("disk"+unit_id);
		target = null;
	}

}
