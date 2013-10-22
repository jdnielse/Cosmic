package cosmic.object;

import com.jme3.effect.ParticleEmitter;

public class Effect {
	
	public ParticleEmitter effect;
	public float life;
	
	public Effect(ParticleEmitter e, float f){
		effect = e;
		life = f;
	}

}
