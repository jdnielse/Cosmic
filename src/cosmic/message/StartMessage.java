package cosmic.message;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class StartMessage extends AbstractMessage {
	
	public String map;
	public int id;
	public float x;
	public float y;
	
	public StartMessage() {}
	
	public StartMessage (String map, int id, float x, float y) {
		this.map = map;
		this.id = id;
		this.x = x;
		this.y = y;
	}

}
