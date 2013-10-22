package cosmic.message;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class MoveMessage extends AbstractMessage {
	
	public int player_id;
	public String unit;
	public Vector3f dest;
	
	public MoveMessage() {}
	
	/**
	 * 
	 * @param p_id
	 * @param s
	 * @param vec
	 */
	public MoveMessage (int p_id, String s, Vector3f vec) {
		player_id = p_id;
		unit = s;
		dest = vec;
	}

}
