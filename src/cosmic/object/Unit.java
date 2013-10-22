package cosmic.object;

import java.util.HashMap;
import java.util.Random;


import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.ui.Picture;

import cosmic.core.Player;
import cosmic.core.states.PlayerGameState;

//the object for any unit in the game. contains what weapons it has, destination vector, cooldowns, engine status
public class Unit extends CosmicObject{
	HashMap<String, Ability> cooldowns = new HashMap<String, Ability>(); //keeps track of ability cooldowns
	
	public static Player player;

	ColorRGBA team; //to show what team it is on (EX: 2v2)
	String race;
	int player_id;
	String unit_id;
	Mesh lineMesh;
	Geometry lineGeometry;
	AudioNode sound;
	Geometry geoRed, geoGreen;
	Mesh meshRed, meshGreen;
	
	//Collision
	//CharacterControl characterControl;
	
	//stats
	float acc; //acceleration
	float vel; //velocity
	float trn; //turn speed
	public float hp;
	public float maxhp;
	float def;
	float att;
	float range;
	float energy; //weapons take energy too?
	Unit target = null;
	boolean attacking = false;
	boolean alive = true; //outside loop needs to check this every update
	int supplyCost;
	public boolean selected = false;

	//========acc and vel variables====================================
	Vector3f dest;
	float curacc;
	float curspeed;
	boolean calcTripVec;
	boolean atDest;
	boolean slowDown = false;
	float deaccDist;
	float minDist;
	Vector3f tripVec;
	float conservedAcc;
	//=================================================================
	
	final float HPLENGTH = 1;

	public Unit(AssetManager assetManager, int p_id, Vector3f p) {
		super(assetManager, p_id);
		this.move(p);
		pos = p;
		dest = p;
		curspeed = 0;
		//deaccDist = calcMaxVelDistance();
		cooldowns.put("attack", new Ability(0, 3));
		cooldowns.put("MothershipAttack", new Ability(0.9f, 1f));
		cooldowns.put("laser", new Ability(0, 1));
		cooldowns.put("missile", new Ability(0, 5));
		
		//laser
		lineMesh = new Mesh();
		lineMesh.setMode(Mesh.Mode.Lines);
		lineMesh.setBuffer(VertexBuffer.Type.Position, 3, new float[]{ 0,0,0,0,0,0});
		lineMesh.setBuffer(VertexBuffer.Type.Index, 2, new short[]{ 0, 1 });
		lineMesh.setLineWidth(4);
		lineMesh.updateBound();
		lineMesh.updateCounts();
		
		//health bar
		int lengthMod = 1;
		if(this instanceof Mothership){
			lengthMod = 3;
		}
		float modHPLENGTH = HPLENGTH * lengthMod;
		
		meshGreen = new Mesh();
		meshGreen.setMode(Mesh.Mode.Lines);
		meshGreen.setBuffer(VertexBuffer.Type.Position, 3, new float[]{ 0-modHPLENGTH/2,.4f,1.01f,modHPLENGTH/2,.4f,1.01f });
		meshGreen.setBuffer(VertexBuffer.Type.Index, 2, new short[]{ 0, 1 });
		meshGreen.setLineWidth(4);
		meshGreen.updateBound();
		meshGreen.updateCounts();
		
		meshRed = new Mesh();		
		meshRed.setMode(Mesh.Mode.Lines);		
		meshRed.setBuffer(VertexBuffer.Type.Position, 3, new float[]{modHPLENGTH/2,.4f,1.01f,modHPLENGTH/2,.4f,1.01f});
		meshRed.setBuffer(VertexBuffer.Type.Index, 2, new short[]{ 0, 1 });
		meshRed.setLineWidth(4);
		meshRed.updateBound();
		meshRed.updateCounts();
		
//		Picture picG = new Picture("ghp");
//		picG.setImage(assetManager, "assets/textures/greenhp.png", true);
//		
//		picG.setHeight(10);
//		picG.setWidth(10);
//		picG.setPosition(this.getLocalTranslation().x*10+100,this.getLocalTranslation().y*10+100);
//		this.attachChild(picG);
		
		
		geoRed = new Geometry("hpRed"+unit_id, meshRed);
		geoGreen = new Geometry("hpGreen"+unit_id, meshGreen);
		geoRed.setQueueBucket(Bucket.Translucent);
		geoGreen.setQueueBucket(Bucket.Translucent);
		
		Material redMaterial =  assetManager.loadMaterial("assets/textures/RedHPColor.j3m");
		
		Material greenMaterial =  assetManager.loadMaterial("assets/textures/GreenHPColor.j3m");
		
		greenMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		redMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		
		geoRed.setMaterial(redMaterial);
		geoGreen.setMaterial(greenMaterial);
		//attach to this object
		this.attachChild(geoRed);
		this.attachChild(geoGreen);
		
		//Collision
		//SphereCollisionShape sp = new SphereCollisionShape(20);
		//characterControl = new CharacterControl(sp, 0);
		//this.addControl(characterControl);
		//PlayerGameState.bulletApp.getPhysicsSpace().add(this);
		//characterControl.setJumpSpeed(0);
		//characterControl.setFallSpeed(0);
		//characterControl.setGravity(0);
		
		//laser sound
		sound = new AudioNode(assetManager, "assets/sounds/laser.wav", false);
		sound.setVolume(.75f);
		attachChild(sound);
	}
	
	public boolean isDead(){
		return !alive;
	}

	//bool inRange(Unit)
	public boolean inRange(CosmicObject u){
		//sees if this unit is within the range of another
		if(this.pos.distance(u.pos)<range){
			return true;
		}
		return false;
	}

	//bool didCollide(Unit)

	public boolean didCollide(Unit u){
		CollisionResults results = new CollisionResults();
		this.collideWith(u, results);
		if(results.size()>0){
			return true;
		}
		return false;
	}

	//
	//takeDamage(float)

	public float takeDamage(float d){
		hp -= d;
		//System.out.println("hp: "+hp+" maxhp: "+maxhp+" sending: "+hp/maxhp);
		updateHPDisplay(hp/maxhp);
		if(hp<=0){
			//die
			alive = false;
			parent.detachChildNamed("line"+unit_id);
		}
		return hp;
	}
	//attack(Unit)
	public void attack(Unit u){
		if(u != null){
			if(u instanceof SpaceStation){
				float hp = u.takeDamage(att - u.def);
				if(hp <= 0){
					SpaceStation s = (SpaceStation) u;
					s.last_id = s.getOwnerId();
				}
			}
			else
				u.takeDamage(att - u.def);
		}
	}
	
	public boolean serverAttack(){
		if(inRange(target) && cooldowns.get("attack").val <= 0){
			attack(target);
			//System.out.println("Server: " +target.unit_id + " " + target.hp + ", " + target.att);
			//System.out.println("Server: " +unit_id + " " + hp + ", " + att);
			cooldowns.get("attack").val = 3f;
			return true;
		}
		else {
			return false;
		}
	}
	

	public Vector3f setDestination(Vector3f d){
		Vector3f tmp = dest;
		//d = d.add((float) .5 - (float)Math.random(),(float) .5- (float)Math.random(), 0f);
		dest = d;
		float angle;

		/*if(atDest){ //for my  velocity ones
			calcTripVec = false;
		}
		atDest = false;

		curacc = curspeed;*/

		//=========for Josh's ideas============
		curacc = conservedAcc;
		atDest = false;

		//System.out.println(curspeed);


		/* get the local rotation */
		Quaternion rotation = geo.getLocalRotation();

		/* get the position */
		Vector3f pos = this.getLocalTranslation();

		/* compute the angle */
		float deltaY = dest.y - pos.y;
		float deltaX = dest.x - pos.x;

		if (pos.equals(dest)) {
			return tmp;
		}

		if (deltaX == 0){
			deltaX = .0001f;
		}

		angle = (float) (Math.atan(deltaY/deltaX) + 1.57);

		if (deltaX > 0) {
			angle = - (float) (Math.atan(-deltaY/deltaX) + 1.57);
		}

		/* update the rotation */
		rotation.fromAngleAxis(angle, Vector3f.UNIT_Z);		
		return tmp;
	}

	public void accToDestTest(float tpf){
		float multval = curspeed;
		//If we are not at our destination, we need to move there
		if(!atDest){
			multval = curacc + acc;
			curacc += acc;
			
			if(multval > vel){
				multval = vel;
			}
			curspeed = multval;
			conservedAcc = curacc; //save our current acc
			
			Vector3f path = dest.subtract(this.getWorldTranslation()).normalize().mult(multval*tpf);
			//characterControl.setWalkDirection(path);
			float dist = dest.distance(this.getWorldTranslation());
			if(dist - multval*tpf < 0 || multval < 0){
				atDest = true;
				curspeed = 0;
				conservedAcc = 0;
				this.setLocalTranslation(dest);
				return;
			}
			
			this.move(path);
		} else { // If we are at our destination we should stop moving, while at a destination we should be standing still
			curspeed = 0;
			conservedAcc = 0;
		}
		
	}
	
	public void updateCooldowns(float tpf){
		for(Ability a : cooldowns.values()){
			if(a.val > 0)
				a.val -= tpf;
		}
	}

	public void PlayerAttack(){
		if(target != null) {
			lineMesh.setBuffer(VertexBuffer.Type.Position, 3, new float[]{ pos.x,pos.y,pos.z,target.pos.x,target.pos.y,target.pos.z});
			lineMesh.updateBound();
			lineMesh.updateCounts();
			
			FrustumIntersect vis = player.getCamera().contains(bounds.getWorldBound());
			if(vis == FrustumIntersect.Inside)
				sound.play();
			
			parent.attachChild(lineGeometry);
			cooldowns.get("laser").val = .6f;
			attack(target);
		}
	}
	
	public void updateAttack(float tpf){
		if(cooldowns.get("laser").val < 0){
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
	
	public void update(float tpf){
		if(alive){
			if(attacking && target != null){ //chase and attack?
				if(target.getOwnerId() == player_id)
					stopAttacking();
				else
					updateAttack(tpf);
			}else{
				//moveToDest(tpf);
				
				//turnToUpdate(tpf);
				//gotoDestNoAcc(tpf);
			}
			
			
			pos = this.getLocalTranslation();
			updateCooldowns(tpf);
			accToDestTest(tpf);
			//add ramming in here?
			
			//show hp bars only if selected
			if(selected || this instanceof Mothership){
				//show hp bar
				if(geoRed.getCullHint() != CullHint.Dynamic){
					geoRed.setCullHint(CullHint.Dynamic);
					geoGreen.setCullHint(CullHint.Dynamic);
				}
			}else{
				if(geoRed.getCullHint() != CullHint.Always){
					geoRed.setCullHint(CullHint.Always);
					geoGreen.setCullHint(CullHint.Always);
				}
			}
			//meshRed.setBuffer(VertexBuffer.Type.Position, 3, new float[]{ pos.x,pos.y,0,pos.x+1,pos.y,0});
			//meshGreen.setBuffer(VertexBuffer.Type.Position, 3, new float[]{ pos.x+1,pos.y,0,pos.x+3,pos.y,0 });
			//geoRed = new Geometry("hpRed"+unit_id, meshRed);
			//geoGreen = new Geometry("hpGreen"+unit_id, meshGreen);
			
		}
	}

	/**
	 * Returns player id
	 * @return
	 */
	public int getOwnerId(){
		return player_id;
	}
	
	/**
	 * Sets the owner of the unit 
	 * @param x
	 * @return player_id
	 */
	public int setOwner(int x){
		player_id = x;
		return player_id;
	}

	/**
	 * Return unique unit identifier
	 * @return
	 */
	public String getId() {
		return unit_id;
	}

	public float getVel() {
		return curacc;
	}
	
	public void setVel(float acc){
		curacc = acc;
	}

	/**
	 * True if the unit has moved last update,
	 * False if at destination
	 * @return
	 */
	public boolean hasMoved(){
		return !atDest;
	}

	public Unit getTarget() {
		return target;
	}
	
	public float getRange() {
		return range;
	}
	
	public void stopAttacking(){
		attacking = false;
		parent.detachChildNamed("line"+unit_id);
		target = null;
	}

	public void setTarget(Unit target) {
		this.target = target;
		attacking = true;
	}
	
	protected void updateHPDisplay(float percent){
		int lengthMod = 1;
		if(this instanceof Mothership){
			lengthMod = 3;
		}
		float modHPLENGTH = HPLENGTH * lengthMod;
		float splitpoint = (percent * modHPLENGTH) - (modHPLENGTH / 2);
		meshGreen.setBuffer(VertexBuffer.Type.Position, 3, new float[]{ 0-modHPLENGTH/2,.4f,1.01f,splitpoint,.4f,1.01f });
		meshRed.setBuffer(VertexBuffer.Type.Position, 3, new float[]{splitpoint,.4f,1.01f,modHPLENGTH/2,.4f,1.01f});
		//System.out.println("split: " + splitpoint + " HPLENGTH: " + HPLENGTH);
	}

}

