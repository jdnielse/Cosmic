package cosmic.message;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class BasicMessage extends AbstractMessage {
	
	public String str;
	
	public BasicMessage() {}
	
	public BasicMessage (String s) {str = s;}

}
