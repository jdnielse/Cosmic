package cosmic.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.Vector;

import com.jme3.asset.*;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;

import cosmic.object.Nebula;
import cosmic.object.SpaceStation;
import cosmic.object.Textures;

public class MapLoader {
	
	String map;
	Geometry background;
	String texture;
	float width;
	float height;
	Vector<SpaceStation> stations = new Vector<SpaceStation>();
	Vector<Nebula> nebulas = new Vector<Nebula>();
	public Vector2f[] start_pos = {new Vector2f(-65,40), new Vector2f(65,-40), new Vector2f(-65, -40), new Vector2f(65,40),
			new Vector2f(0,85), new Vector2f(0,-85)};
	
	public MapLoader(AssetManager assetManager, String map,int player_count){
		
		/* find the map  */
		AssetKey mapKey = new AssetKey("assets/maps/" + map);
		AssetInfo mapInfo = assetManager.locateAsset(mapKey);
		
		/* read and parse the map file */
		try {
			String line = null;
			InputStream mapFile = mapInfo.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(mapFile));
			int i = 1;
			while ((line = reader.readLine()) != null) {
				switch (i) {
				
		            case 1:	/* simply the background texture name */
		            	texture = line;
		            	i++;
		            	break;
		            	
		            case 2: /* width and height separated by a space */
		            	Scanner scanner = new Scanner(line);
		            	scanner.useDelimiter(" ");
		            	for (int j = 0; j < 2; j++) {
		            		if (scanner.hasNext()) {
		            			switch (j) {		            				
		            				case 0: /* width */
		            					width = Float.valueOf(scanner.next().trim()).floatValue();
		            					break;
		            					
		            				case 1: /* height */
		            					height = Float.valueOf(scanner.next().trim()).floatValue();
		            					break;
		            			}
		            		}
		            		else { /* malformed map file */		            			
		            			System.out.println("Error in mapfile w!" + j);
		            			System.exit(1);
		            		}
		            	}
		            	i++;
		            	break;
		            	
		            case 3: /* start points */
		            	i++;
		            	break;
		            	
		            case 4: /* planets */
		            	i++;
		            	break;
		            	
		            default: /* malformed map file */
		            	System.out.println("Error in mapfile!");
            			System.exit(1);
		            	break;
		        }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		background = new Geometry("background", new Quad(width, height));
		Material bmat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		bmat.setTexture("ColorMap", assetManager.loadTexture("assets/textures/"+texture));
		background.setMaterial(bmat);
		background.move(-500, -500, -500); 
		
		
		SpaceStation station = new SpaceStation(assetManager, player_count, "2_SpaceStation_0", new Vector3f(0, 0, 1));	
		stations.add(station);
		station = new SpaceStation(assetManager, player_count, "2_SpaceStation_1", new Vector3f(-75, 0, 1));
		stations.add(station);
		station = new SpaceStation(assetManager, player_count, "2_SpaceStation_2", new Vector3f(75, 0, 1));
		stations.add(station);
		station = new SpaceStation(assetManager, player_count, "2_SpaceStation_3", new Vector3f(-45, 70, 1));
		stations.add(station);
		station = new SpaceStation(assetManager, player_count, "2_SpaceStation_4", new Vector3f(-45, -70, 1));
		stations.add(station);
		station = new SpaceStation(assetManager, player_count, "2_SpaceStation_5", new Vector3f(45, 70, 1));
		stations.add(station);
		station = new SpaceStation(assetManager, player_count, "2_SpaceStation_6", new Vector3f(45, -70, 1));
		stations.add(station);
		
		Geometry geo = new Geometry("box", new Box(Vector3f.ZERO, 1f, 1f, 0f));
		Geometry geo2 = new Geometry("box", new Box(Vector3f.ZERO, 1f, 1f, 0f));
		Geometry geo3 = new Geometry("box", new Box(Vector3f.ZERO, 1f, 1f, 0f));
		Material neb = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		Material neb2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		Material neb3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		
		neb.setTexture("ColorMap", assetManager.loadTexture("assets/textures/Nebula.png"));
		Nebula nebula = new Nebula(assetManager, geo, neb, new Vector3f(35,40,2f));		
		geo.scale(22f);
		nebulas.add(nebula);
		
		neb2.setTexture("ColorMap", assetManager.loadTexture("assets/textures/Nebula2.png"));
		Nebula nebula2 = new Nebula(assetManager, geo2, neb2, new Vector3f(-35,40,2f));
		geo2.scale(22f);
		nebulas.add(nebula2);
		
		neb3.setTexture("ColorMap", assetManager.loadTexture("assets/textures/Nebula3.png"));
		Nebula nebula3 = new Nebula(assetManager, geo3, neb3, new Vector3f(0,-40,2f));
		geo3.scale(22f);
		nebulas.add(nebula3);

	}
	
	public Vector<SpaceStation> getStations(){
		return stations;
	}
	
	public Vector<Nebula> getNebulas(){
		return nebulas;
	}
	
	
}
