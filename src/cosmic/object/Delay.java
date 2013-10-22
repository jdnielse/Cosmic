package cosmic.object;

import java.util.Vector;

public class Delay implements Comparable<Delay>{
	
	public float time;
	public String command;
	public Vector<Object> data;
	
	public Delay(String c, float t, Vector<Object> d){
		time = t;
		command = c;
		data = d;
	}
	
	public float update(float delta){
		time = time - delta;
		return time;
	}

	@Override
	public int compareTo(Delay o) {
		return Float.compare(time, o.time);

	}


}
