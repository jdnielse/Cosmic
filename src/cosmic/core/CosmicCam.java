package cosmic.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;

import cosmic.core.states.PlayerGameState;
import cosmic.message.*;
import cosmic.object.CosmicObject;
import cosmic.object.Delay;
import cosmic.object.Formations;
import cosmic.object.RelayNode;
import cosmic.object.Unit;
import de.lessvoid.nifty.elements.render.TextRenderer;

public class CosmicCam{

	private Camera camera;
	private Player player;
	private int player_id;

	private Vector3f worldPosition;
	private Vector3f center;
	private Vector3f offSet;

	private Vector3f targetPosition;
	private boolean followingTarget = false;
	public CosmicObject curTarget;

	private Vector3f velocity;	 // current speed and direction
	private float maxMovementSpeed = 25f;

	private float zoom = 15.0f;	 // scaling
	private float rotation = 0f; // In radians?
	private float maxZoomIn = 0.5f;
	private float maxZoomOut = 75f;
	private InputManager inputManager;
	private HashMap<String,Geometry> selectedUnits;
	private PlayerGameState gameState;
	private float distance;
	private Vector3f mouseStart;
	private Vector3f mouseEnd;
	private boolean selectingUnits = false;
	private ArrayList<Node>  selectableObjects;
	private ArrayList<Node>  selection;
	private boolean justSelected = false;
	public boolean mothership = false;
	public boolean factory = false;
	public boolean inMenu = false;
	private Mesh lineMesh;
	public boolean shift = false;
	public boolean ctrl = false;
	private long[] lastPressTime = new long[6]; //keeps track of the last time we pressed a number button
	private HashMap<String,HashMap<String,Geometry>> controlGroups = new HashMap<String,HashMap<String,Geometry>>();
	private boolean alreadyHitNumber = false;
	private boolean released = false;

	public CosmicCam(Camera cosmicCam, Player player){
		this.player = player;
		this.camera = cosmicCam;
		gameState = (PlayerGameState) player.getState("game");
		distance = camera.distanceToNearPlane(new Vector3f(0,0,1));
		player_id = player.player_id;
		for(int i=0;i<lastPressTime.length;i+=1){
			lastPressTime[i]=1000;
		}
		initialize();
	}


	public void initialize(){
		selectedUnits = new HashMap<String, Geometry>();
		inputManager = player.getInputManager();
		setMappings();
	}

	public void setMappings() {

		/* set the controls */
		inputManager.clearMappings();
		inputManager.addMapping("ESC",	new KeyTrigger(KeyInput.KEY_ESCAPE));
		inputManager.addMapping("Left",   new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("Right",  new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("Up",   new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("Down",   new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("RotateR", new KeyTrigger(KeyInput.KEY_E));
		inputManager.addMapping("RotateL", new KeyTrigger(KeyInput.KEY_Q));
		inputManager.addMapping("selectAll", new KeyTrigger(KeyInput.KEY_L));
		inputManager.addMapping("clearSelect", new KeyTrigger(KeyInput.KEY_C));
		inputManager.addMapping("createUnit", new KeyTrigger(KeyInput.KEY_SPACE));

		//control groups
		inputManager.addMapping("ctrl", new KeyTrigger(KeyInput.KEY_LCONTROL));
		inputManager.addMapping("shift", new KeyTrigger(KeyInput.KEY_LSHIFT));
		//inputManager.addMapping("1", new KeyTrigger(KeyInput.KEY_1));
		//fancy loop for setting up number keys
		for(int i = 1; i <= 6; i+=1){
			inputManager.addMapping(Integer.toString(i), new KeyTrigger(i+1));

		}

		inputManager.addMapping("ZoomIn", new MouseAxisTrigger(MouseInput.AXIS_WHEEL,false));
		inputManager.addMapping("ZoomOut", new MouseAxisTrigger(MouseInput.AXIS_WHEEL,true));
		inputManager.addMapping("pick target", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addMapping("rightClick", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
		inputManager.addMapping("centerOnMothership", new KeyTrigger(KeyInput.KEY_M));

		//inputManager.addMapping("Line",   new KeyTrigger(KeyInput.KEY_L));
		//inputManager.addListener(actionListener, new String[]{"Up","Down","Left","Right","RotateR","RotateL"});

		inputManager.addListener(analogListener, new String[]{"centerOnMothership","ESC","Up","Down","Left","Right","RotateR","RotateL","ZoomIn","ZoomOut","pick target","ctrl","shift"});
		inputManager.addListener(actionListener, new String[]{"pick target", "createUnit", "selectAll", "rightClick", "clearSelect","ctrl","shift","1","2","3","4","5","6"});
	}

	public void removeUnit(String u){
		if(selectedUnits.get(u) == null)
			return;

		if(selectedUnits.get(u).getParent() instanceof Unit){
			Unit unit = (Unit) selectedUnits.get(u).getParent();
			unit.selected = false;
		}
		selectedUnits.remove(u);
	}

	public void setCurrentTarget(CosmicObject tar){
		curTarget = tar;
	}

	private Vector3f getClickPos(){
		Vector2f click2d = inputManager.getCursorPosition();
		Vector3f click3d = camera.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
		Vector3f dir = camera.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();

		Vector3f clickPos3d = camera.getWorldCoordinates(new Vector2f(click2d.x, click2d.y),0);
		clickPos3d = clickPos3d.subtract(camera.getLocation());
		//System.out.println("Pos before mult: "+clickPos3d);
		distance = camera.distanceToNearPlane(new Vector3f(0,0,1));
		clickPos3d = clickPos3d.mult(distance);
		clickPos3d.z = 1;
		clickPos3d = clickPos3d.add(new Vector3f(camera.getLocation().x,camera.getLocation().y,0));
		return clickPos3d;
	}

	private void addCommand(String type, Unit u, Vector<Object> data){

		//now in time instead of distance
		float dist = u.getWorldTranslation().distance(player.mothership.pos)/75;

		for(RelayNode relay : gameState.relays){
			float d = u.getWorldTranslation().distance(relay.pos)/35; //relay nodes degrade signal faster
			if(d<dist){
				dist = d;
			}
		}

		switch(type){
		case "move":
			float time = dist;
			Delay d = new Delay(type, time, data);
			player.commands.add(d);
			break;

		case "attack":
			time = dist;
			d = new Delay(type, time, data);
			player.commands.add(d);
			break;
		}
	}

	private AnalogListener analogListener = new AnalogListener() {
		public void onAnalog(String name, float intensity, float tpf) {
			float value = 3.0E-2f;



			switch(name){
			case "pick target":{
				if(selectingUnits){
					//make this better
					player.gameState.rootNode.detachChildNamed("selectGeo");

					//System.out.println("Dynamic boxing!");
					//draw the box
					mouseEnd = getClickPos();
					mouseEnd.z = 1.01f;

					Geometry geo;
					Material mat;
					Box box = new Box(mouseStart, mouseEnd);
					geo = new Geometry("selectGeo", box);
					geo.move(0, 0, 1);
					mat = new Material(player.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");

					mat.setColor("Color", new ColorRGBA(0f, 0f, 1f, 0.15f));
					mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
					mat.getAdditionalRenderState().setDepthWrite(false);

					geo.setQueueBucket(Bucket.Translucent);
					geo.setMaterial(mat);
					player.gameState.rootNode.attachChild(geo);
				}
			}
			case "RotateR":
				//curTarget.rotate(0, 0, value*maxMovementSpeed);
				break;
			case "RotateL":

				//curTarget.rotate(0, 0, -1*value*maxMovementSpeed);
				break;
			case "Right":

				camera.setLocation(camera.getLocation().add(new Vector3f(15*value, 0, 0)));
				break;
			case "Left":

				camera.setLocation(camera.getLocation().add(new Vector3f(-15*value, 0, 0)));
				break;
			case "Down":

				camera.setLocation(camera.getLocation().add(new Vector3f(0, -15*value, 0)));
				break;
			case "Up":
				camera.setLocation(camera.getLocation().add(new Vector3f(0, 15*value, 0)));
				break;
			case "ZoomIn":
				if(camera.getLocation().z -5 > maxZoomIn)
					camera.setLocation(camera.getLocation().add(new Vector3f(0, 0, -5)));
				break;
			case "ZoomOut":
				if(camera.getLocation().z + 5 < maxZoomOut)
					camera.setLocation(camera.getLocation().add(new Vector3f(0, 0, 5)));
				break;

			case "centerOnMothership":
				camera.setLocation(new Vector3f(player.mothership.pos.x,player.mothership.pos.y,camera.getLocation().z));
				break;

			case "ctrl":
				if(released){
					released = false;
					ctrl = false;
				}else{
					ctrl = true;
				}
				break;

			case "shift":
				if(released){
					released = false;
					shift = false;
				}else{
					shift = true;
				}
				//System.out.println(" shift: "+shift);
				break;

			case "ESC":

				/* if we're not already in the menu */
				if (!inMenu) {
					/* clear the control mappings so that we can disable input */
					inputManager.clearMappings();
					player.nifty.gotoScreen("pause");
					inMenu = true;
					inputManager.addMapping("ESC",	new KeyTrigger(KeyInput.KEY_ESCAPE));
					inputManager.addListener(analogListener, new String[]{"ESC"});
				}

				/* otherwise we need to resume */
				else {
					/* resume */
					if (mothership) {
						player.nifty.gotoScreen("mother");
					}else if(factory){
						player.nifty.gotoScreen("factory");
					}
					else {
						player.nifty.gotoScreen("hud");
					}
					inMenu = false;
					setMappings();					
				}
				break;			
			}
		}
	};

	private ActionListener actionListener = new ActionListener() {

		@Override
		public void onAction(String name, boolean isPressed, float tpf) {

			switch(name){
			case "ctrl":
				if(!isPressed){
					ctrl = false;
					released = true;
				}
				break;

			case "shift":
				if(!isPressed){
					shift = false;
					released = true;
				}
				//System.out.println(" shift: "+shift);
				break;

			case "1":
			case "2":
			case "3":
			case "4":
			case "5":
			case "6":{
				if(isPressed){
					//check if shift is pressed - if it is, then add selectedunits to the control group
					//System.out.println(shift);
					if(shift){
						controlGroups.put(name, (HashMap<String,Geometry>) selectedUnits.clone());

						//						System.out.println("Wrote control group "+name);
						//						for(String s: controlGroups.get(name).keySet()){
						//							System.out.print(s+", ");
						//						}
						//						System.out.println();
					}				
					
					//else, set selected units to that controlGroup if it is not null
					else if(controlGroups.get(name)!=null){
						//empty out the old one
						for(Geometry g:selectedUnits.values()){
							if(g.getParent() instanceof Unit){
								Unit u = (Unit) g.getParent();
								u.selected = false;
							}
						}

						selectedUnits = (HashMap<String,Geometry>)controlGroups.get(name).clone();
						//see if any of these are dead and select live ones
						//do we need to check if g is null?
						for(Geometry g:selectedUnits.values()){
							if(g.getParent() instanceof Unit){
								Unit u = (Unit) g.getParent();
								if(!u.isDead()){
									u.selected = true;
								}else{
									selectedUnits.remove(u.getId());
								}
							}
						}
						//
						//						System.out.println("Went to control group "+name);
						//						System.out.println("Selected units is now:");
						//						for(String s: selectedUnits.keySet()){
						//							System.out.print(s+", ");
						//						}
						//						System.out.println();
					}

					//if it was pressed twice really fast, center the camera
					if((System.currentTimeMillis()-lastPressTime[Integer.parseInt(name)-1]) < 500){
						//System.out.println("You pressed it within "+(System.currentTimeMillis() - lastPressTime[Integer.parseInt(name)-1])+" milliseconds!");
						if(!selectedUnits.isEmpty()){ //if there is something in the control group
							for(String s:selectedUnits.keySet()){
								//just grab the first unit
								Geometry u = selectedUnits.get(s);
								camera.setLocation(new Vector3f(u.getWorldTranslation().x,u.getWorldTranslation().y,camera.getLocation().z));
								break;
							}
						}

					}
				}
				lastPressTime[Integer.parseInt(name)-1] = System.currentTimeMillis();

				break;
			}
			case "pick target": {

				// Convert screen click to 3d position
				Vector2f click2d = inputManager.getCursorPosition();
				Vector3f click3d = camera.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
				Vector3f dir = camera.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();

				Vector3f clickPos3d = camera.getWorldCoordinates(new Vector2f(click2d.x, click2d.y),0);
				clickPos3d = clickPos3d.subtract(camera.getLocation());
				//System.out.println("Pos before mult: "+clickPos3d);
				distance = camera.distanceToNearPlane(new Vector3f(0,0,1));
				clickPos3d = clickPos3d.mult(distance);
				clickPos3d.z = 1.01f;
				clickPos3d = clickPos3d.add(new Vector3f(camera.getLocation().x,camera.getLocation().y,0));

				if(!selectingUnits && isPressed){


					mouseStart = clickPos3d;
					mouseStart.z = .99f;

					selectingUnits = true;
					//player.gameState.rootNode.detachChildNamed("selectGeo");
				}
				else if(selectingUnits && !isPressed){
					player.gameState.rootNode.detachChildNamed("selectGeo");
					mouseEnd = clickPos3d;
					mouseEnd.z = 1.01f;

					Geometry geo;
					Material mat;
					Box box = new Box(mouseStart, mouseEnd);
					geo = new Geometry("selectGeo", box);
					geo.move(0, 0, 1);
					mat = new Material(player.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");

					mat.setColor("Color", new ColorRGBA(0f, 0f, 1f, 0.15f));
					mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

					geo.setQueueBucket(Bucket.Transparent);
					geo.setMaterial(mat);
					player.gameState.rootNode.attachChild(geo);

					Node root = gameState.getRoot(player_id);
					CollisionResults hitResults = new CollisionResults();
					root.collideWith(geo.getWorldBound(), hitResults);

					// no units select move instead
					if(hitResults.size() <= 0){

						// If the click was within the world bounds
						if(Math.abs(mouseEnd.x) < 100 && Math.abs(mouseEnd.y) < 100){
							ArrayList<Unit> moveUnits = new ArrayList<Unit>();

							// Get all selected units at the time of the mouse click
							for(Geometry ge : selectedUnits.values()){
								Unit unit = (Unit)ge.getParent();

								// If the unit selected belongs to the current player (on this machine) add it to the list to be moved
								if(unit.getOwnerId() == player_id){
									moveUnits.add((Unit)ge.getParent());
								}
							}

							// Don't move nothing
							if(moveUnits.size() != 0){

								// Move all units in a box formation
								mouseEnd.z = 1f;
								ArrayList<Vector3f> ret = Formations.moveUnits(moveUnits, mouseEnd, Formations.BOX_FORMATION);

								// Our returned ArrayList contains all the points we need to move units to, add commands to do so
								for(int i = 0; i < ret.size(); i++){
									Vector<Object> data = new Vector<Object>();
									data.add(moveUnits.get(i).getOwnerId());
									data.add(moveUnits.get(i).getId());
									data.add(ret.get(i));
									addCommand("move",moveUnits.get(i),data);
								}
							}
						}
					}

					// selecting new units
					else{
						mothership = false;
						factory = false;
						for(Geometry g:selectedUnits.values()){
							if(g.getParent() instanceof Unit){
								Unit u = (Unit) g.getParent();
								u.selected = false;
							}
						}
						selectedUnits.clear();

						for(CollisionResult res : hitResults){
							Node n = res.getGeometry().getParent();
							if(n instanceof Unit){
								Unit u = (Unit) n;
								//System.out.println(u.getId());
								/* check if this unit is a mothership */
								if (u.getId().contains("mothership")) {
									mothership = true;
									factory = false;
								}else if(u.getId().contains("Factory")) {
									factory = true;
									mothership = false;
								}

								selectedUnits.put(u.getId(),u.geo);
								u.selected = true;
								curTarget = u;
							}
						}

					}

					/* if a mothership was selected switch to the mothership HUD */
					if (mothership) {
						player.nifty.gotoScreen("mother");
						player.nifty.getCurrentScreen().findElementByName("resources").getRenderer(TextRenderer.class).setText("Resources: "+player.resources);
					}else if(factory){
						player.nifty.gotoScreen("factory");
						player.nifty.getCurrentScreen().findElementByName("resources").getRenderer(TextRenderer.class).setText("Resources: "+player.resources);
					}
					else {
						player.nifty.gotoScreen("hud");
					}							

					justSelected = true;
					selectingUnits = false;
					player.gameState.rootNode.detachChildNamed("selectGeo");
				}

				/*if(!justSelected){
						for(Geometry ge : selectedUnits.values()){
							distance = camera.distanceToNearPlane(new Vector3f(0,0,1));
							Unit u = (Unit) ge.getParent();
							if(u.getOwnerId() == player_id){
								Vector2f clickPos = inputManager.getCursorPosition();
								System.out.println(clickPos);
								clickPos3d = camera.getWorldCoordinates(new Vector2f(clickPos.x, clickPos.y),0);
								clickPos3d = clickPos3d.subtract(camera.getLocation());
								clickPos3d = clickPos3d.mult(distance);
								clickPos3d.z = 1;
								clickPos3d = clickPos3d.add(new Vector3f(camera.getLocation().x,camera.getLocation().y,0));
								gameState.moveUnit(u.getOwnerId(), u.getId(), clickPos3d);
								player.myClient.send(new MoveMessage(u.getOwnerId(), u.getId(), clickPos3d));
							}
						} // end for
				}// end if*/

				justSelected = false;
			} // end if pick target
			break;
			case "Line":
				Unit p1unit = (Unit) player.gameState.units.get(0).values().toArray()[0];
				Unit p2unit = (Unit) player.gameState.units.get(1).values().toArray()[0];

				lineMesh = new Mesh();
				lineMesh.setMode(Mesh.Mode.Lines);
				lineMesh.setBuffer(VertexBuffer.Type.Position, 3, new float[]{ p1unit.pos.x,p1unit.pos.y,p1unit.pos.z, p2unit.pos.x,p2unit.pos.y,p2unit.pos.z});
				lineMesh.setBuffer(VertexBuffer.Type.Index, 2, new short[]{ 0, 1 });
				lineMesh.setLineWidth(4);
				lineMesh.updateBound();
				lineMesh.updateCounts();
				Geometry lineGeometry = new Geometry("line", lineMesh);

				Material lineMaterial = player.getAssetManager().loadMaterial("Common/Materials/RedColor.j3m");
				lineGeometry.setMaterial(lineMaterial);
				player.gameState.rootNode.attachChild(lineGeometry);
				break;

			case "createUnit":
				if(isPressed && curTarget != null  && curTarget instanceof Unit){
					Unit u = (Unit) curTarget;
					for(int i = 0; i < 10; i++){
						if(u.getOwnerId() == player_id){
							Random r = new Random();
							float x = r.nextFloat() * 2 - 1f;
							float y = r.nextFloat() * 2 - 1f;
							Vector3f pos = new Vector3f(u.pos.x + x, u.pos.y + y, 1);
							String id = gameState.createUnit(player_id, "MissilePlatform", pos);
							player.sendMessage(new CreateMessage(player_id, id, pos));
						}
					}
				}
				break;

			case "selectAll":
				for(Unit u : gameState.getUnits().values()){
					selectedUnits.put(u.getId(), u.geo);
					u.selected = true;
				}

				break;

			case "clearSelect":
				for(Geometry g:selectedUnits.values()){
					if(g.getParent() instanceof Unit){
						Unit u = (Unit) g.getParent();
						u.selected = false;
					}
				}
				selectedUnits.clear();
				player.nifty.gotoScreen("hud");

				break;


			case "rightClick":

				if(isPressed){
					Vector3f clickPos3d = getClickPos();
					System.out.println(clickPos3d);
					Box box = new Box(clickPos3d, 0.5f, 0.5f, .01f);
					//System.out.println(clickPos3d);
					CollisionResults results = new CollisionResults();
					Geometry geo = new Geometry("rightClickGeo", box);
					geo.move(0, 0, 1);
					gameState.rootNode.collideWith(geo.getWorldBound(), results);
					if(results.size()<=0)break;
					if(results.getClosestCollision().getGeometry().getParent() instanceof Unit)
					{
						Unit target = (Unit) results.getClosestCollision().getGeometry().getParent();
						//System.out.println(target.getId());
						if(target.getOwnerId() != player_id){
							//System.out.println("enemy targeted");
							for(Geometry ge : selectedUnits.values()){
								distance = camera.distanceToNearPlane(new Vector3f(0,0,1));
								Unit u = (Unit) ge.getParent();
								if(u.getOwnerId() == player_id){
									Vector<Object> data = new Vector<Object>();
									data.add(u.getId());
									data.add(player_id);
									data.add(target.getId());
									data.add(target.getOwnerId());
									addCommand("attack", u, data);
									//gameState.setTarget(u.getId(), player_id, target.getId(), target.getOwnerId());
									//player.myClient.send(new TargetMessage(u.getId(), player_id, target.getId(), target.getOwnerId()));
								}
							} // end for
						}
					}
				}

				break;
			}

			//reset our modifier variables
			ctrl = false;
			shift = false;
		}

	};


}
