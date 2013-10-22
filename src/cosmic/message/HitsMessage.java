package cosmic.message;

import java.util.Vector;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class HitsMessage extends AbstractMessage {
	
	public String unit;
	public Vector<String> targets;
	
	public HitsMessage() {}
	
	public HitsMessage (String u, Vector<String> t) {
		unit = u;
		targets = t;
	}

}
