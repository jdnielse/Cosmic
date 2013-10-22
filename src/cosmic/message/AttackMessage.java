package cosmic.message;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class AttackMessage extends AbstractMessage {
	
	public String unit;
	public int owner;
	
	public AttackMessage() {}
	
	public AttackMessage (String u, int o) {unit = u; owner = o;}

}