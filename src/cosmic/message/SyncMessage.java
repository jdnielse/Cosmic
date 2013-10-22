package cosmic.message;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class SyncMessage extends AbstractMessage {
	
	public int p_id;
	public String u_id;
	public Vector3f pos;
	public float speed;
	
	public SyncMessage() {}
	
	public SyncMessage (int pid, String id, Vector3f p, float s)	{
		p_id = pid;
		u_id = id;
		pos = p;
		speed = s;
	}

}
