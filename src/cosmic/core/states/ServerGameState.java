package cosmic.core.states;

import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.Callable;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.network.Server;
import com.jme3.scene.Node;
import cosmic.core.ServerMain;
import cosmic.message.*;
import cosmic.object.Default;
import cosmic.object.Factory;
import cosmic.object.Fighter;
import cosmic.object.LaserCannon;
import cosmic.object.MissilePlatform;
import cosmic.object.Mothership;
import cosmic.object.Nebula;
import cosmic.object.RelayNode;
import cosmic.object.SpaceStation;
import cosmic.object.Unit;

public class ServerGameState extends AbstractAppState {


	private ServerMain server;
	private Server myServer;
	private AssetManager assetManager;
	private Node rootNode;
	private Vector<HashMap<String, Unit>> units;
	private Vector<Node> playerRoot;
	private Vector<Unit> deadUnits;
	private int player_count;
	private float time = 0f;

	public ServerGameState(Server myserver, int p_count){
		myServer = myserver;
		player_count = p_count;
		rootNode = new Node("gameRoot");
		units = new Vector<HashMap<String, Unit>>();
		playerRoot = new Vector<Node>();
		deadUnits = new Vector<Unit>();

		for(int i = 0; i < p_count; i++){
			units.add(new HashMap<String, Unit>());
			Node n = new Node("player_"+i+"Root");
			playerRoot.add(n);
			rootNode.attachChild(n);
		}

		units.add(new HashMap<String, Unit>());
		Node n = new Node("player_"+(player_count + 1)+"Root");
		playerRoot.add(n);
		rootNode.attachChild(n);
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app); 
		this.server = (ServerMain) app;          // cast to a more specific class
		assetManager = app.getAssetManager();

	}

	@Override
	public void cleanup() {
		super.cleanup();
		// unregister all my listeners, detach all my nodes, etc...

	}

	@Override
	public void setEnabled(boolean enabled) {
		// Pause and unpause
		super.setEnabled(enabled);
		if(enabled){
			// init stuff that is in use while this state is RUNNING
			for(SpaceStation station : server.map.getStations()){
				playerRoot.add(station);
				rootNode.attachChild(station);
				units.get(player_count).put(station.getId(), station);
			}
			
			for(Nebula nebulas : server.map.getNebulas()){
				playerRoot.add(nebulas);
				rootNode.attachChild(nebulas);
			}
		} 
		else {
			// take away everything not needed while this state is PAUSED
		}
	}

	// Note that update is only called while the state is both attached and enabled.
	@Override
	public void update(float tpf) {
		if(isEnabled()){
			/* return to setup if we lose players */
			if(myServer.getConnections().size() < player_count){
				server.changeState(server.startState);
				server.ready = 0;
			}


			for(Unit unit: deadUnits){
				System.out.print("removing " + unit.getId() + ", " + unit.isDead());
				playerRoot.get(unit.getOwnerId()).detachChildNamed(unit.getId());
				units.get(unit.getOwnerId()).remove(unit.getId());
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

					if(unit.getTarget() == null && !unit.hasMoved()){
						targetEnemy(unit);
					}

					if(unit.getTarget() != null && unit.serverAttack()) {

						if(unit instanceof LaserCannon){
							myServer.broadcast(new HitsMessage(unit.getId(), ((LaserCannon) unit).attackedUnits));
						}
						else if(unit instanceof MissilePlatform){
							myServer.broadcast(new HitsMessage(unit.getId(), ((MissilePlatform) unit).attackedUnits));
						}
						else{
							myServer.broadcast(new AttackMessage(unit.getId(), unit.getOwnerId()));
						}

						if(unit.getTarget() instanceof SpaceStation){
							SpaceStation s = (SpaceStation) unit.getTarget();
							if(s.captured){
								changeOwner(s, unit.getOwnerId());
								unit.stopAttacking();
							}
						}
					}

				}
			}

			time += tpf;
			// Sync
			if(time > 1){

				for(HashMap<String, Unit> map: units){
					for(Unit unit: map.values()){
						if(unit.hasMoved()){
							SyncMessage msg = new SyncMessage(unit.getOwnerId(), unit.getId(), unit.pos, unit.getVel());
							msg.setReliable(false);
							myServer.broadcast(msg);
						}
					}
				}

				time = 0f;
			}
		}
	}

	public void targetEnemy(Unit unit){
		// Update all units
		for(int i = 0; i < units.size(); i++){
			if(unit.getOwnerId() != i){
				HashMap<String, Unit> map = units.get(i);
				for(Unit target: map.values()){

					if(unit.inRange(target) && target.getOwnerId() != unit.getOwnerId()){
						unit.setTarget(target);
						TargetMessage message = new TargetMessage(unit.getId(), unit.getOwnerId(), target.getId(), target.getOwnerId());
						myServer.broadcast(message);
						return;
					}
				}
			}
		}
	}

	/**
	 * Create a unit and add it to the scene
	 * called on next update
	 * @param id
	 * @param unit_id
	 * @param pos
	 */
	public void createUnit(final int p_id, final String unit_id, final Vector3f pos){
		server.enqueue(new Callable<Unit>() {
			public Unit call() throws Exception {
				Unit u;
				if(unit_id.contains("mothership"))
					u = new Mothership(assetManager, p_id, unit_id, pos);
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
				return null;
			}
		});
	}

	private void changeOwner(final SpaceStation s, final int owner){
		server.enqueue(new Callable<Unit>() {
			public Unit call() throws Exception {
				units.get(s.last_id).remove(s.getId());
				s.changeId(owner);
				units.get(s.getOwnerId()).put(s.getId(), s);
				s.captured = false;
				s.getParent().detachChildNamed(s.getId());
				playerRoot.get(s.getOwnerId()).attachChild(s);
				s.hp = s.maxhp;
				return s;
			}
		});
	}

	/**
	 * Move a unit
	 * @param p_id player who owns the unit
	 * @param unit unit id
	 * @param dest destination
	 */
	public void moveUnit(final int p_id, final String unit, final Vector3f dest){
		Unit u = units.get(p_id).get(unit);
		if(u == null) {
			System.out.println("Server: unit "+unit+" is not found");
			return;
		}

		if(u.getTarget() != null && u.getTarget().pos.distance(dest) > u.getRange()){
			u.stopAttacking();
		}
		u.setDestination(dest);
	}

	public void setTarget(final String unit, final int u_player, final String target, final int t_player){
		Unit u = units.get(u_player).get(unit);
		Unit t = units.get(t_player).get(target);
		if(u != null && t != null)
			u.setTarget(t);
		else
			System.out.println("Server: Unit  "+u+" or Target "+t+" not found");
	}

}