import java.io.File;
import java.util.Random;

import javax.media.opengl.GL;
import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

public class ParticleSystemNode extends SceneNode {

	private final float LIFETIME;
	private final float DECAY;
	private final float[] COLOR = new float[] { 0.7f, 0.7f, 1f };
	private final float[] POS = new float[] { 0, 0, 0 };
	private final float[] SPEED = new float[] { 0.0025f, 0.03f, 0.0025f };
	private final float[] SPEEDCHANGE = new float[] { 0, 0.0001f, 0 };
	private final int MAXPARTICLE;
	private final int MAXNEW;
	private final int LIFETIMERANGE = 5000000;
	private final float size = 0.01f;
	private final float[] steps = new float[] { 3.6f, 3.85f, 4 };
	private Texture particle;

	private Random rand;

	private Particle[] particles;

	public ParticleSystemNode(GL gl) {
		super(gl);
		rand = new Random();
		MAXNEW = 10;
		MAXPARTICLE = 5000;
		DECAY = 0.002f;
		LIFETIME = 1;
		particles = new Particle[MAXPARTICLE];
    	try{     
    		particle = TextureIO.newTexture(new File("particle.png"), true);    		
    	} catch (Exception e){
    		System.err.println(e.getMessage());
    	}
	}

	public ParticleSystemNode(GL gl, float life, float decay, int max,
			int maxnew) {
		super(gl);
		rand = new Random();
		MAXNEW = maxnew;
		MAXPARTICLE = max;
		DECAY = decay;
		LIFETIME = life;
		particles = new Particle[MAXPARTICLE];
    	try{     
    		particle = TextureIO.newTexture(new File("particle.png"), true);    		
    	} catch (Exception e){
    		System.err.println(e.getMessage());
    	}
	}

	public void setColor(float r, float g, float b) {
		COLOR[0] = r;
		COLOR[1] = g;
		COLOR[2] = b;
	}

	public void setSpeed(float x, float y, float z) {
		SPEED[0] = x;
		SPEED[1] = y;
		SPEED[2] = z;
	}

	public void setPos(float x, float y, float z) {
		POS[0] = x;
		POS[1] = y;
		POS[2] = z;
	}

	public void setSpeedChange(float x, float y, float z) {
		SPEEDCHANGE[0] = x;
		SPEEDCHANGE[1] = y;
		SPEEDCHANGE[2] = z;
	}

	public void drawMe() {	
		particle.bind();
		int newP = 0;
		for (int i = 0; i < MAXPARTICLE; ++i) {
			if (particles[i] == null) {
				particles[i] = new Particle();
				createParticle(i);
				if (newP++ >= MAXNEW)
					break;
			}
			draw(i);
			evolve(i);
			if (!isAlive(i))
				reset(i);
		}		
	}

	private void createParticle(int i) {
		particles[i].pos = new float[3];
		particles[i].speed = new float[3];
		particles[i].speedChange = new float[3];
		particles[i].maxY = new float[3];
		particles[i].stepsDone = new boolean[] { false, false, false };
		for (int j = 0; j < 3; ++j) {
			particles[i].pos[j] = POS[j];
			if (j == 1)
				particles[i].speed[j] = SPEED[j]
						* (rand.nextFloat() * 0.25f + 0.75f);
			else
				particles[i].speed[j] = SPEED[j] * (rand.nextFloat() * 2 - 1);
			particles[i].speedChange[j] = SPEEDCHANGE[j];
			particles[i].maxY[j] = steps[j]*(rand.nextFloat()*0.2f+0.8f);
		}
		particles[i].lifetime = (float) rand.nextInt(LIFETIMERANGE)
				/ (float) LIFETIMERANGE * LIFETIME;
		particles[i].decay = DECAY;		
	}

	private void reset(int i) {
		for (int j = 0; j < 3; ++j) {
			particles[i].pos[j] = POS[j];
			if (j == 1)
				particles[i].speed[j] = SPEED[j]
						* (rand.nextFloat() * 0.25f + 0.75f);
			else
				particles[i].speed[j] = SPEED[j] * (rand.nextFloat() * 2 - 1);
			particles[i].speedChange[j] = SPEEDCHANGE[j];
			particles[i].stepsDone[j] = false;
		}
		particles[i].lifetime = (float) rand.nextInt(LIFETIMERANGE)
				/ (float) LIFETIMERANGE * LIFETIME;
	}

	private void evolve(int i) {
		particles[i].lifetime -= particles[i].decay;
		for (int j = 0; j < 3; ++j) {
			particles[i].pos[j] += particles[i].speed[j];
			// particles[i].speed[j] -= particles[i].speedChange[j];
			// particles[i].speedChange[j] *= 1.001f;
		}
		if (particles[i].pos[1] > particles[i].maxY[0] && !particles[i].stepsDone[0]) {
			particles[i].speed[1] /= 2;
			particles[i].stepsDone[0] = true;
		} else {
			if (particles[i].pos[1] > particles[i].maxY[1] && !particles[i].stepsDone[1]) {
				particles[i].speed[1] /= 2;
				particles[i].stepsDone[1] = true;
			} else {
				if (particles[i].pos[1] > particles[i].maxY[2]
						&& !particles[i].stepsDone[2]) {
					particles[i].speed[1] = -SPEED[1]
							* (rand.nextFloat() * 0.25f + 0.75f);
					particles[i].stepsDone[2] = true;
				}
			}
		}
	}

	private boolean isAlive(int i) {
		return (particles[i].lifetime > 0) && (particles[i].pos[1] > -0.5f);
	}

	private void draw(int i) {
		/*
		 * gl.glBegin(GL.GL_POINTS); gl.glVertex3f(particles[i].pos[0],
		 * particles[i].pos[1], particles[i].pos[2]); gl.glEnd();
		 */		
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0,0);
		gl.glVertex3f(-size + particles[i].pos[0], -size + particles[i].pos[1],
				size + particles[i].pos[2]);
		gl.glTexCoord2f(1,0);
		gl.glVertex3f(size + particles[i].pos[0], -size + particles[i].pos[1],
				size + particles[i].pos[2]);
		gl.glTexCoord2f(1,1);
		gl.glVertex3f(size + particles[i].pos[0], size + particles[i].pos[1],
				size + particles[i].pos[2]);
		gl.glTexCoord2f(0,1);
		gl.glVertex3f(-size + particles[i].pos[0], size + particles[i].pos[1],
				size + particles[i].pos[2]);

		gl.glTexCoord2f(0,0);
		gl.glVertex3f(size + particles[i].pos[0], -size + particles[i].pos[1],
				size + particles[i].pos[2]);
		gl.glTexCoord2f(1,0);
		gl.glVertex3f(size + particles[i].pos[0], -size + particles[i].pos[1],
				-size + particles[i].pos[2]);
		gl.glTexCoord2f(1,1);
		gl.glVertex3f(size + particles[i].pos[0], size + particles[i].pos[1],
				-size + particles[i].pos[2]);
		gl.glTexCoord2f(0,1);
		gl.glVertex3f(size + particles[i].pos[0], size + particles[i].pos[1],
				size + particles[i].pos[2]);

		gl.glTexCoord2f(0,0);
		gl.glVertex3f(-size + particles[i].pos[0], size + particles[i].pos[1],
				size + particles[i].pos[2]);
		gl.glTexCoord2f(1,0);
		gl.glVertex3f(size + particles[i].pos[0], size + particles[i].pos[1],
				size + particles[i].pos[2]);
		gl.glTexCoord2f(1,1);
		gl.glVertex3f(size + particles[i].pos[0], size + particles[i].pos[1],
				-size + particles[i].pos[2]);
		gl.glTexCoord2f(0,1);
		gl.glVertex3f(-size + particles[i].pos[0], size + particles[i].pos[1],
				-size + particles[i].pos[2]);
		gl.glEnd();
	}

}

class Particle {
	public float lifetime, decay;
	public float[] pos, speed, speedChange, maxY;
	public boolean[] stepsDone;

	public Particle() {
		// Stub
	}
}