package cosmic.core.states;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Callable;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

import cosmic.core.MapLoader;
import cosmic.core.Player;
import cosmic.message.CreateMessage;
import cosmic.message.MoveMessage;
import cosmic.message.TargetMessage;
import cosmic.object.*;
import de.lessvoid.nifty.effects.impl.Hint;
import de.lessvoid.nifty.elements.render.TextRenderer;

public class PlayerGameState extends AbstractAppState {


	private Player player;
	private AssetManager assetManager;
	private AppSettings settings;
	public Node rootNode;
	public Vector<HashMap<String, Unit>> units;
	public Vector<HashMap<String, Unit>> stationId;
	private Vector<Node> playerRoot;
	private Vector<Unit> deadUnits;
	public Vector<Mothership> mothers;
	public Vector<RelayNode> relays;
	public static BulletAppState bulletApp;
	public HashMap<String, Integer> unitCost = new HashMap<String,Integer>();
	public HashMap<String, Float> unitTimeCost = new HashMap<String, Float>();
	private LinkedList<Effect> effects = new LinkedList<Effect>();
	int num_players;
	boolean mother;
	StringBuilder buildQueueText = new StringBuilder();

	public PlayerGameState(AppSettings s){
		settings = s;

		//add stuff to our cost map
		unitCost.put("Fighter", 1000);
		unitCost.put("LaserCannon", 2500);
		unitCost.put("RelayNode", 1500);
		unitCost.put("Factory", 5000);
		unitCost.put("MissilePlatform", 4567);

		// add stuff to our time cost map
		// time in seconds
		unitTimeCost.put("Fighter", 10f);
		unitTimeCost.put("LaserCannon", 35f);
		unitTimeCost.put("RelayNode",7f);
		unitTimeCost.put("Factory", 30f);
		unitTimeCost.put("MissilePlatform", 40f);
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app); 
		this.player = (Player) app;          // cast to a more specific class
		assetManager = app.getAssetManager();
		rootNode = new Node("gameRoot");
		units = new Vector<HashMap<String, Unit>>();
		playerRoot = new Vector<Node>();
		deadUnits = new Vector<Unit>();
		mothers = new Vector<Mothership>();
		relays = new Vector<RelayNode>();
		bulletApp = new BulletAppState();
		player.getStateManager().attach(bulletApp);
	}

	public void ready(int num_players, MapLoader loader){


		units = new Vector<HashMap<String, Unit>>();
		for(int i = 0; i < num_players; i++){
			units.add(new HashMap<String, Unit>());
			Node n = new Node("player_"+i+"Root");
			playerRoot.add(n);
			rootNode.attachChild(n);
		}

		units.add(new HashMap<String, Unit>());
		Node n = new Node("player_"+(num_players + 1)+"Root");
		playerRoot.add(n);
		rootNode.attachChild(n);

		for(SpaceStation station : loader.getStations()){
			playerRoot.add(station);
			rootNode.attachChild(station);
			units.get(num_players).put(station.getId(), station);
		}

		for(Nebula nebulas : loader.getNebulas()){
			//			playerRoot.add(nebulas);
			rootNode.attachChild(nebulas);
		}

		this.num_players = num_players;
	}

	@Override
	public void cleanup() {
		super.cleanup();
		// unregister all my listeners, detach all my nodes, etc...
		player.getRootNode().detachChildNamed(rootNode.getName());
		player.getInputManager().clearMappings();
	}

	@Override
	public void setEnabled(boolean enabled) {
		// Pause and unpause
		super.setEnabled(enabled);
		if(enabled){
			// init stuff that is in use while this state is RUNNING
			player.getRootNode().attachChild(rootNode);
		} 
		else {
			// take away everything not needed while this state is PAUSED

		}
	}

	// Note that update is only called while the state is both attached and enabled.
	@Override
	public void update(float tpf) {

		// do the following while game is RUNNING
		if(isEnabled()){
			//end game
			if(player.mothership.isDead()){
				//System.out.println(currentTime + " " + totalTime +" DELAY " + (currentTime - totalTime));
				System.out.println("Endstate");
				//player.restart();
				player.nifty.gotoScreen("end");
				player.changeState(player.endState);
				cleanup();
			}

			//see if you are the last alive
			boolean anyoneElseAlive = false;
			if(mothers.size()>0){ //makes sure other motherships actually exist first
				for(Mothership m:mothers){
					if(!m.isDead()) anyoneElseAlive = true;

				}

				if(!anyoneElseAlive){
					//player.restart();
					player.nifty.gotoScreen("end");
					player.changeState(player.endState);
					cleanup();
				}
			}


			for(Unit unit: deadUnits){
				System.out.println("removing " + unit.getId());
				playerRoot.get(unit.getOwnerId()).detachChildNamed(unit.getId());
				units.get(unit.getOwnerId()).remove(unit.getId());
				player.getCosmicCam().removeUnit(unit.getId());

				addExplosion(unit);
			}
			deadUnits.clear();

			// Update all units
			for(HashMap<String, Unit> map: units){
				for(Unit unit: map.values()){
					if(unit.isDead()){
						deadUnits.add(unit);
						continue;
					}

					unit.update(tpf);
					if(unit.getOwnerId() == player.player_id){
						if(unit instanceof Builder){
							Builder b = (Builder) unit;
							if(!b.buildQueue.isEmpty() && b.buildQueue.getFirst().time <= 0){
								player.commands.addFirst(b.buildQueue.removeFirst());
								//Collections.sort(player.commands);
								//								int player_id = (int) d.data.get(0);
								//								Builder u = (Builder) d.data.get(1);
								//								String type = (String) d.data.get(2);
								//								u.canBuild = true;
								//								Random r = new Random();
								//								float x = r.nextFloat() * 2 - 1f;
								//								float y = r.nextFloat() * 2 - 1f;
								//								Vector3f pos = new Vector3f(u.pos.x + x, u.pos.y + y, 1);
								//								String id = createUnit(player_id, type , pos);
								//								player.sendMessage(new CreateMessage(player_id, id, pos));
							}
						}
					}
				}
			}
			List<Delay> rem = new LinkedList<Delay>();
			for(Delay d: player.getCommands()){

				d.update(tpf);

				if(d.time <= 0){
					switch(d.command){
					case "move":
						int player_id = (int) d.data.get(0);
						String id = (String) d.data.get(1);
						Vector3f dest = (Vector3f) d.data.get(2);
						moveUnit(player_id, id, dest);
						player.sendMessage(new MoveMessage(player_id, id, dest));
						break;

					case "attack":
						id = (String) d.data.get(0);
						player_id = (int) d.data.get(1);
						String target = (String) d.data.get(2);
						int target_id = (int) d.data.get(3);
						setTarget(id, player_id, target, target_id);
						player.sendMessage(new TargetMessage(id, player_id, target, target_id));
						break;


					case "build":

						player_id = (int) d.data.get(0);
						Builder u = (Builder) d.data.get(1);
						String type = (String) d.data.get(2);
						u.canBuild = true;
						Random r = new Random();
						float x = r.nextFloat() * 2 - 1f;
						float y = r.nextFloat() * 2 - 1f;
						Vector3f pos = new Vector3f(u.pos.x + x, u.pos.y + y, 1);
						id = createUnit(player_id, type , pos);
						player.sendMessage(new CreateMessage(player_id, id, pos));
						break;
					}
					rem.add(d);
				}
			}

			for(Delay d: rem){
				player.commands.remove(d);
			}

			//			while(player.getCommands().size() > 0){
			//				
			//				if(player.getCommands().getFirst().time > 0)
			//					break;
			//				player.getCommands().removeFirst();
			//			}
			//			
			//			int i = 0; 
			//			while(player.getCommands().size() > i){
			//				if(player.getCommands().get(i).time<=0){
			//					player.getCommands().remove(i);
			//				}else{
			//					i+=1;
			//				}				
			//			}

			for(Effect effect: effects){
				effect.life -= tpf;
				if(effect.life <= 0)
					rootNode.detachChildNamed(effect.effect.getName());
			}


			CosmicObject target = player.getCosmicCam().curTarget;
			//check if the current target can is a mothership or factory
			if(target instanceof Builder){

				LinkedList<cosmic.object.Delay> queue = ((Builder) target).buildQueue;
				mother = player.nifty.getCurrentScreen().getScreenId().equals("mother");
				//if there is nothing in the queue display the 'empty queue' message on the appropriate screen
				
				if(queue.isEmpty()){
					if(mother)
						player.nifty.getCurrentScreen().findElementByName("buildQueueText").getRenderer(TextRenderer.class).setText("Build Queue: \nEmpty");
					else if(player.nifty.getCurrentScreen().getScreenId().equals("factory"))
						player.nifty.getCurrentScreen().findElementByName("buildQueueF").getRenderer(TextRenderer.class).setText("Build Queue: \nEmpty");
				}
				else{
					buildQueueText.delete(0, buildQueueText.length());
					buildQueueText.append("Build Queue: \n");
					for(int i = 0;i<queue.size();i++)
						buildQueueText.append(queue.get(i).toString()+"\n");

					if(mother){
						player.nifty.getCurrentScreen().findElementByName("buildQueueText").getRenderer(TextRenderer.class).setText(buildQueueText.toString());
					}
					else if(player.nifty.getCurrentScreen().getScreenId().equals("factory")){
						player.nifty.getCurrentScreen().findElementByName("buildQueueF").getRenderer(TextRenderer.class).setText(buildQueueText.toString());
					}
				}
			}



		}
	}

	public Node getRoot(int id){
		return playerRoot.get(id);
	}

	/**
	 * Create a unit and add it to the scene
	 * called on next update
	 * @param id
	 * @param unit_id
	 * @param pos
	 */
	public void createEnemyUnit(final int p_id, final String unit_id, final Vector3f pos){
		player.enqueue(new Callable<Unit>() {
			public Unit call() throws Exception {

				Unit u;
				if(unit_id.contains("mothership")){
					u = new Mothership(assetManager, p_id, unit_id, pos);
					mothers.add((Mothership) u);
				}
				else if(unit_id.contains("unit"))
					u = new Default(assetManager, p_id, unit_id, pos);
				else if(unit_id.contains("Fighter"))
					u = new Fighter(assetManager, p_id, unit_id, pos);
				else if(unit_id.contains("LaserCannon"))
					u = new LaserCannon(assetManager, p_id, unit_id, pos);
				else if(unit_id.contains("RelayNode"))
					u = new RelayNode(assetManager, p_id, unit_id, pos);
				else if(unit_id.contains("MissilePlatform"))
					u = new MissilePlatform(assetManager, p_id, unit_id, pos);
				else if(unit_id.contains("Factory"))
					u = new Factory(assetManager, p_id, unit_id, pos);
				else
					u = new Default(assetManager, p_id, unit_id, pos);

				units.get(p_id).put(unit_id, u);

				playerRoot.get(p_id).attachChild(u);
				return u;
			}
		});
	}

	public String createUnit(int p_id, String type, Vector3f pos){
		Unit u;

		if(type.equals("mothership"))
			u = new Mothership(assetManager, p_id, null, pos);
		else if(type.equals("unit"))
			u = new Default(assetManager, p_id, null, pos);
		else if(type.equals("Fighter"))
			u = new Fighter(assetManager, p_id, null, pos);

		else if(type.equals("LaserCannon"))
			u = new LaserCannon(assetManager, p_id, null, pos);
		else if(type.equals("RelayNode")){
			u = new RelayNode(assetManager, p_id, null, pos);
			relays.add((RelayNode) u);
		}
		else if(type.equals("MissilePlatform"))
			u = new MissilePlatform(assetManager, p_id, null, pos);
		else if(type.equals("Factory"))
			u = new Factory(assetManager, p_id, null, pos);
		else
			u = new Default(assetManager, p_id, null, pos);

		units.get(p_id).put(u.getId(), u);

		playerRoot.get(p_id).attachChild(u);
		return u.getId();
	}



	public HashMap<String, Unit> getUnits(){

		return units.get(player.player_id);
	}


	/**
	 * Move a unit
	 * @param p_id player who owns the unit
	 * @param unit unit id
	 * @param dest destination
	 */
	public void moveUnit(final int p_id, final String unit, final Vector3f dest){
		player.enqueue(new Callable<Unit>() {
			public Unit call() throws Exception {
				Unit u = units.get(p_id).get(unit);
				if(u == null)
					return u;

				if(u.getTarget() != null && u.getTarget().pos.distance(dest) > u.getRange()){
					u.stopAttacking();
				}
				u.setDestination(dest);
				return u;
			}
		});
	}

	public void setTarget(final String unit, final int u_player, final String target, final int t_player){
		player.enqueue(new Callable<Unit> () {
			public Unit call() throws Exception {
				Unit u = units.get(u_player).get(unit);
				Unit t = units.get(t_player).get(target);
				if(u != null && t != null)
					u.setTarget(t);
				return u;
			}
		});
	}

	public void unitAttack(final String unit, final int u_player){
		player.enqueue(new Callable<Unit> () {
			public Unit call() throws Exception {
				Unit u = units.get(u_player).get(unit);
				if(u!=null){
					if(u.getTarget() instanceof SpaceStation){
						SpaceStation s = (SpaceStation) u.getTarget();
						u.PlayerAttack();

						if(s.captured) {
							changeOwner(s, u.getOwnerId());
							if(u.getOwnerId() == player.player_id)
								s.player = player;
							u.stopAttacking();
						}
					}
					else{
						u.PlayerAttack();
					}
				}
				return u;
			}
		});
	}

	public void multiAttack(final String unit, final Vector<String> targets){
		player.enqueue(new Callable<Unit> () {
			public Unit call() throws Exception {
				int u_player = Integer.parseInt(unit.substring(0, 1));
				Unit u = units.get(u_player).get(unit);
				if(u!=null){
					u.PlayerAttack();

					for(String t: targets){
						int t_player = Integer.parseInt(t.substring(0, 1));
						Unit target = units.get(t_player).get(t);
						if(target != null){
							if(target instanceof SpaceStation){
								SpaceStation s = (SpaceStation) target;
								u.attack(target);

								if(s.captured) {
									changeOwner(s, u.getOwnerId());
									if(u.getOwnerId() == player.player_id)
										s.player = player;
									if(target.equals(u.getTarget()))
										u.stopAttacking();
								}
							}
							else{
								u.attack(target);
							}
						}
					}
				}
				return u;
			}
		});
	}

	/**
	 * 
	 * @param p_id
	 * @param unit
	 * @param dest
	 * @param speed
	 */
	public void SyncUnit(final int p_id, final String unit, final Vector3f pos, final float speed){
		player.enqueue(new Callable<Unit>() {
			public Unit call() throws Exception {
				Unit u = units.get(p_id).get(unit);
				if(u == null)
					return u;

				u.setLocalTranslation(pos);
				u.pos = pos;
				u.setVel(speed);
				return u;
			}
		});
	}

	private void changeOwner(SpaceStation s, int owner){
		units.get(s.last_id).remove(s.getId());
		s.changeId(owner);
		units.get(s.getOwnerId()).put(s.getId(), s);
		s.captured = false;
		s.getParent().detachChildNamed(s.getId());
		playerRoot.get(s.getOwnerId()).attachChild(s);
	}


	private void addExplosion(Unit u){
		ParticleEmitter explosion = 
				new ParticleEmitter("Emitter"+u.getId(), ParticleMesh.Type.Triangle, 30);
		Material mat_red = new Material(assetManager, 
				"Common/MatDefs/Misc/Particle.j3md");
		mat_red.setTexture("Texture", assetManager.loadTexture(
				"Effects/Explosion/flash.png"));
		explosion.setMaterial(mat_red);
		explosion.setImagesX(2); 
		explosion.setImagesY(2); // 2x2 texture animation
		explosion.setEndColor(  new ColorRGBA(1f, 0f, 0f, 1f));   // red
		explosion.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
		explosion.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
		explosion.setStartSize(1.5f);
		explosion.setEndSize(0.1f);
		explosion.setGravity(0, 0, 0);
		explosion.setRandomAngle(true);
		explosion.setLowLife(1f);
		explosion.setHighLife(3f);
		explosion.getParticleInfluencer().setVelocityVariation(0.3f);
		explosion.setLocalTranslation(u.pos);
		effects.addLast(new Effect(explosion, 2f));
		rootNode.attachChild(explosion);
	}

}
