import java.io.File;

import javax.media.opengl.GL;

import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;


public class SkyboxNode extends SceneNode {
		
	private Texture[] tex = new Texture[6];
	private TextureData[] face = new TextureData[6];
	private final int size = 600;
	
	public SkyboxNode(GL gl, String[] tex){
		super(gl);						
		try{
			for (int i = 0; i < 6; ++i){
				face[i] = TextureIO.newTextureData(new File(tex[i]), GL.GL_RGBA,
					GL.GL_RGBA, false, TextureIO.JPG);
				this.tex[i] = TextureIO.newTexture(face[i]);
			}
		} catch (Exception e){
			System.err.println ("nöö");
		}
	}
	
	public void drawMe() {
		gl.glColor3f(1,1,1);
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		
		tex[0].bind();
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, 1); gl.glVertex3f(  0.5f*size, -0.5f*size, -0.5f*size );
		gl.glTexCoord2f(1, 1); gl.glVertex3f( -0.5f*size, -0.5f*size, -0.5f*size );
		gl.glTexCoord2f(1, 0); gl.glVertex3f( -0.5f*size,  0.5f*size, -0.5f*size );
		gl.glTexCoord2f(0, 0); gl.glVertex3f(  0.5f*size,  0.5f*size, -0.5f*size );
        gl.glEnd();        
		
        tex[4].bind();
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, 1); gl.glVertex3f(  0.5f*size, -0.5f*size,  0.5f*size );
		gl.glTexCoord2f(1, 1); gl.glVertex3f(  0.5f*size, -0.5f*size, -0.5f*size );
		gl.glTexCoord2f(1, 0); gl.glVertex3f(  0.5f*size,  0.5f*size, -0.5f*size );
		gl.glTexCoord2f(0, 0); gl.glVertex3f(  0.5f*size,  0.5f*size,  0.5f*size );
        gl.glEnd();        
        
        tex[1].bind();
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, 1); gl.glVertex3f( -0.5f*size, -0.5f*size,  0.5f*size );
		gl.glTexCoord2f(1, 1); gl.glVertex3f(  0.5f*size, -0.5f*size,  0.5f*size );
		gl.glTexCoord2f(1, 0); gl.glVertex3f(  0.5f*size,  0.5f*size,  0.5f*size );
		gl.glTexCoord2f(0, 0); gl.glVertex3f( -0.5f*size,  0.5f*size,  0.5f*size );
        gl.glEnd();
        
        tex[5].bind();
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, 1); gl.glVertex3f( -0.5f*size, -0.5f*size, -0.5f*size );
		gl.glTexCoord2f(1, 1); gl.glVertex3f( -0.5f*size, -0.5f*size,  0.5f*size );
		gl.glTexCoord2f(1, 0); gl.glVertex3f( -0.5f*size,  0.5f*size,  0.5f*size );
		gl.glTexCoord2f(0, 0); gl.glVertex3f( -0.5f*size,  0.5f*size, -0.5f*size );
        gl.glEnd();
        
        tex[3].bind();
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(1, 0); gl.glVertex3f( -0.5f*size,  0.5f*size, -0.5f*size );
		gl.glTexCoord2f(0, 0); gl.glVertex3f( -0.5f*size,  0.5f*size,  0.5f*size );
		gl.glTexCoord2f(0, 1); gl.glVertex3f(  0.5f*size,  0.5f*size,  0.5f*size );
		gl.glTexCoord2f(1, 1); gl.glVertex3f(  0.5f*size,  0.5f*size, -0.5f*size );
        gl.glEnd();               
        
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
		gl.glDisable(GL.GL_TEXTURE_2D);

	}

}
