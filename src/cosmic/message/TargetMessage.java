package cosmic.message;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class TargetMessage extends AbstractMessage {
	
	public String unit;
	public String target;
	public int unit_player;
	public int target_player;
	
	public TargetMessage() {}
	
	public TargetMessage (String u, int u_p, String t, int t_p) {unit = u; target = t; unit_player = u_p; target_player = t_p;}

}
