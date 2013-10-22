package cosmic.object;

import java.util.Vector;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.VertexBuffer;

public class Mothership extends Builder{
	public static int count = 0; //keep track of how many of this unit we have
	//private static String texture = "assets/textures/beetle.png";
	private static float scale = 1.5f;
	
	public Vector<Unit> buildQueue = new Vector<Unit>();
	public int currTime = 0; //time left for current unit to build
	
	public Mothership(AssetManager assetManager, int p_id, String u_id, Vector3f p) {
		super(assetManager, p_id, p);
		player_id = p_id;

		if(u_id == null){
			unit_id = p_id + "_mothership_" + count;
			count += 1;
		}
		else {
			unit_id = u_id;
		}
		
		setName(unit_id);
		geo.scale(scale);
		bounds.scale(scale, scale, 1f);
		geo.setMaterial(Textures.mats.get("mothership").get(p_id));
		//mat.setTexture("ColorMap", assetManager.loadTexture(texture));
		//geo.setMaterial(mat);
		
		maxhp = hp = 300;
		energy = 100;
		vel = 1.4f;
		acc = .0005f;
		att = 10;
		range = 10f;
		
		lineGeometry = new Geometry("line"+unit_id, lineMesh);
		Material lineMaterial =  assetManager.loadMaterial("Common/Materials/WhiteColor.j3m");
		
		lineGeometry.setMaterial(lineMaterial);
		lineGeometry.move(0, 0, 1);
	}
	
	public void PlayerAttack(){
		if(target != null) {
			lineMesh.setBuffer(VertexBuffer.Type.Position, 3, new float[]{ pos.x,pos.y,pos.z,target.pos.x,target.pos.y,target.pos.z});
			lineMesh.updateBound();
			lineMesh.updateCounts();
			
			sound.play();
			
			parent.attachChild(lineGeometry);
			cooldowns.get("MothershipAttack").val = 0.9f;
			attack(target);
		}
	}
	
	public boolean serverAttack(){
		if(inRange(target) && cooldowns.get("attack").val <= 0){
			attack(target);
			//System.out.println("Server: " +target.unit_id + " " + target.hp + ", " + target.att);
			//System.out.println("Server: " +unit_id + " " + hp + ", " + att);
			cooldowns.get("attack").val = 0.9f;
			return true;
		}
		else {
			return false;
		}
	}

	public void updateAttack(float tpf){
		if(cooldowns.get("MothershipAttack").val < 0){
			parent.detachChildNamed("line"+unit_id);
		}else{
			lineMesh.setBuffer(VertexBuffer.Type.Position, 3, new float[]{ pos.x,pos.y,pos.z,target.pos.x,target.pos.y,target.pos.z});
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
	
	//createUnit
	//bool canCreate(Unit asdf)
	//Vector<Unit> getBuildQueue()
}
