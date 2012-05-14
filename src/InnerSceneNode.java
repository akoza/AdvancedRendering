import javax.media.opengl.GL;


public class InnerSceneNode extends SceneNode {
	
	public InnerSceneNode(GL gl) {
		super(gl);
	}

	public void drawMe() {
		// Do Nothing!
		// Inner Nodes do not contain geometry
	}

}
