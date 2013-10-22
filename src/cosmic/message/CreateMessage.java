package cosmic.message;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class CreateMessage extends AbstractMessage {
	
	public int player_id;
	public String unit;
	public Vector3f pos;
	
	public CreateMessage() {}
	
	public CreateMessage (int id, String s, Vector3f vec) {
		player_id = id;
		unit = s;
		pos = vec;
	}

}
