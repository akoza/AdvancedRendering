import javax.media.opengl.GL;


public class ObjectSceneNode extends SceneNode {

	private int list;
	
	public ObjectSceneNode(GL gl, String path) {
		super(gl);
		OBJLoader objLoader = new OBJLoader(path, 1.0f, gl);
		list = objLoader.getDisplayList();
		this.objBounds = objLoader.getBounds();		
		this.updateBounds();
	}
	
	public void drawMe() {			
		gl.glCallList(list);
	}

}
