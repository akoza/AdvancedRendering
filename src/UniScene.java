/**
 *        .     _///_,
 *   .      / ` ' '>
 *     )   o'  __/_'>
 *    (   /  _/  )_\'>
 *     ' "__/   /_/\_>
 *         ____/_/_/_/
 *        /,---, _/ /
 *       ""  /_/_/_/
 *          /_(_(_(_                 \
 *         (   \_\_\\_               )\
 *          \'__\_\_\_\__            ).\
 *          //____|___\__)           )_/
 *          |  _  \'___'_(           /'
 *           \_ (-'\'___'_\      __,'_'
 *           __) \  \\___(_   __/.__,'
 *        ,((,-,__\  '", __\_/. __,'
 *                     '"./_._._-'
 *          Sascha Brauer - 6495401
 */

import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.cg.CGcontext;
import com.sun.opengl.cg.CGparameter;
import com.sun.opengl.cg.CGprogram;
import com.sun.opengl.cg.CgGL;
import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.GLUT;

public class UniScene extends JoglTemplate {

	private static final long serialVersionUID = 1L;
		
	private InnerSceneNode scene;	
	private GL gl;
	private GLU glu;
	private GLUT glut;	
	private int texWidth = 1280, texHeight = 800;
	
	private CGcontext cgContext;
	private CGprogram cgSSAO = null;
	private CGparameter cgProjMat, cgProjMatInv, cgPixelX, cgPixelY;	
	private int cgVertexProfile, cgFragProfile;
	
	public static void main(String[] args) {
    	UniScene template = new UniScene();
    	template.setVisible(true);
    }

	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height)
	{
		// get a gl object
		GL gl = drawable.getGL();
		// set the OpenGL Viewport to the actual width and height of the window
		gl.glViewport(0, 0, texWidth, texHeight);
		// choose your type of projection(ortho or perspective)
		// choose the projection matrix-mode
		gl.glMatrixMode(GL.GL_PROJECTION);
		// and load it with the identity matrix
		gl.glLoadIdentity();
		// perspective projection
		glu.gluPerspective(30, (float) texWidth / texHeight, 1, 10000);
		// make sure to use the modelview matrix-mode
		gl.glMatrixMode(GL.GL_MODELVIEW);
		// load the identity matrix as the modelview (resets all previous
		// transformations)
		gl.glLoadIdentity();
	}
	
	public void loadShaders(){		
		cgContext = CgGL.cgCreateContext();
		cgVertexProfile = CgGL.cgGLGetLatestProfile(CgGL.CG_GL_VERTEX);
		if (cgVertexProfile == CgGL.CG_PROFILE_UNKNOWN)
		{
			System.err.println("Invalid vertex profile");
			System.exit(1);
		}
		CgGL.cgGLSetOptimalOptions(cgVertexProfile);

		cgFragProfile = CgGL.cgGLGetLatestProfile(CgGL.CG_GL_FRAGMENT);
		if (cgFragProfile == CgGL.CG_PROFILE_UNKNOWN)
		{
			System.err.println("Invalid fragment profile");
			System.exit(1);
		}
		CgGL.cgGLSetOptimalOptions(cgFragProfile);
		
		// Load SSAO
		cgSSAO = CgGL.cgCreateProgramFromFile(cgContext,
				CgGL.CG_SOURCE, "src/shader/ssao.cg", cgFragProfile, null, null);
		if (cgSSAO == null)
		{
			int err = CgGL.cgGetError();
			System.err.println("Compile shader [SSAO] "
					+ CgGL.cgGetErrorString(err));
			if (CgGL.cgGetLastListing(cgContext) != null)
			{
				System.err.println(CgGL.cgGetLastListing(cgContext) + "\n");
			}
			System.exit(1);
		}

		CgGL.cgGLLoadProgram(cgSSAO);

		int err = CgGL.cgGetError();
		if (err != CgGL.CG_NO_ERROR)
		{
			System.out.println("Load shader [SSAO]: "
					+ CgGL.cgGetErrorString(err));
			System.exit(1);
		}
		cgProjMat = CgGL.cgGetNamedParameter(cgSSAO, "projMat");
		cgProjMatInv = CgGL.cgGetNamedParameter(cgSSAO, "projMatInv");
		cgPixelX = CgGL.cgGetNamedParameter(cgSSAO, "pixelSizeX");
		cgPixelY = CgGL.cgGetNamedParameter(cgSSAO, "pixelSizeY");
		
	}
	
    public void init(GLAutoDrawable drawable) {
    	super.init(drawable);
    	gl = drawable.getGL();
    	glu = this.getGlu();
    	glut = this.getGlut();
    	prepareFBO();
    	gl.glEnable(GL.GL_DEPTH_TEST);
    	gl.glEnable(GL.GL_COLOR_MATERIAL);  
    	gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
    	gl.glEnable(GL.GL_LINE_SMOOTH);
    	gl.glEnable(GL.GL_DOUBLEBUFFER);
    	scene = new InnerSceneNode (gl);
    	
    	loadShaders();
    	
    	loadCampus();
    	loadLights();
    	//loadSky();
    }

    public void keyPressed(KeyEvent e) {    
    	super.keyPressed(e);
    	if (e.getKeyCode() == KeyEvent.VK_1)
    		ssao = !ssao;
    	// Insert Key Interactions
    	/** if (e.getKeyCode() == KeyEvent.VK_SPACE)
	    	state = ++state % 2; */
    }
    
    boolean ssao = false;
    
    public void display(GLAutoDrawable drawable) {
    	    	
    	drawToFBO();
    	    	
		if (ssao){
			CgGL.cgGLSetStateMatrixParameter(cgProjMat,
				CgGL.CG_GL_PROJECTION_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
			CgGL.cgGLSetStateMatrixParameter(cgProjMatInv,
				CgGL.CG_GL_PROJECTION_MATRIX, CgGL.CG_GL_MATRIX_INVERSE);
			CgGL.cgGLSetParameter1f(cgPixelX, 1/texWidth);
			CgGL.cgGLSetParameter1f(cgPixelY, 1/texHeight);
			CgGL.cgGLEnableProfile(cgFragProfile);
			CgGL.cgGLBindProgram(cgSSAO);
		}
    	drawToScreen();
    	if (ssao)
    		CgGL.cgGLDisableProfile(cgFragProfile);    	

    }
    
    private int fboTexId;
    private int fboRboId; 
    private int fboId;
    
    private void prepareFBO(){    	
    	int[] tmp = new int[1];
    	gl.glGenTextures(1, tmp, 0);
    	fboTexId = tmp[0];
    	gl.glBindTexture(GL.GL_TEXTURE_2D, fboTexId);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
    	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_GENERATE_MIPMAP, GL.GL_TRUE); // automatic mipmap
    	gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, texWidth, texHeight, 0,
    			GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
    	gl.glBindTexture(GL.GL_TEXTURE_2D, 0);   
    	
    	tmp = new int[1];
    	gl.glGenRenderbuffersEXT(1, tmp, 0);
    	fboRboId = tmp[0];
    	gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, fboRboId);
    	gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT, GL.GL_DEPTH_COMPONENT, texWidth, texHeight);
    	gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, 0);
    	
    	tmp = new int[1];
    	gl.glGenFramebuffersEXT(1, tmp, 0);
    	fboId = tmp[0];
    	gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fboId);
    	
    	gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_TEXTURE_2D, fboTexId, 0);
    	
    	gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, GL.GL_RENDERBUFFER_EXT, fboRboId);
    	
    	if (gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT) != GL.GL_FRAMEBUFFER_COMPLETE_EXT)
    		System.err.println ("Something went wrong");
    	
    	gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
    }   
    
    private void drawToFBO(){        	    
    	    	
    	gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fboId);    	
		gl.glClearColor(0,1,1,0);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glPushMatrix();		
		applyMouseTranslation(gl);
		applyMouseRotation(gl);		
		
		float[] modelview = new float[16];
		gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, modelview, 0);
		scene.drawSorted(false, new float[] {-modelview[12], -modelview[13], -modelview[14]});		
        
        gl.glPopMatrix();              
        
        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
    }
    
    private void drawToScreen(){
    	gl.glPushMatrix();		
    	
    	gl.glColor3f(1,1,1);
		gl.glClearColor(0,0,0,0);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);		
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
				
		gl.glOrtho(0, texWidth, 0, texHeight, -1, 1);
		if (ssao){
			gl.glActiveTexture(GL.GL_TEXTURE0);		
			gl.glBindTexture(GL.GL_TEXTURE_2D, fboTexId);
			gl.glActiveTexture(GL.GL_TEXTURE1);		
			gl.glBindTexture(GL.GL_TEXTURE_2D, fboRboId);
		} else{			
			gl.glActiveTexture(GL.GL_TEXTURE0);
			gl.glBindTexture(GL.GL_TEXTURE_2D, fboTexId);
			gl.glEnable(GL.GL_TEXTURE_2D);
		}
		
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0,0);
		gl.glVertex3f(0,0,0);
		gl.glTexCoord2f(1, 0);		
		gl.glVertex3f(texWidth, 0,0);
		gl.glTexCoord2f(1, 1);
		gl.glVertex3f(texWidth, texHeight, 0);
		gl.glTexCoord2f(0,1);
		gl.glVertex3f(0, texHeight,0);
		gl.glEnd();		
		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);		
		
		if (!ssao)
			gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPopMatrix();
    }
    
    private void loadCampus(){
    	//scene.addChild(new ObjectSceneNode(gl, "src/models/Campus"));    	
    	scene.addChild(new ObjTexNode(gl,"src/bunny", "src/textures/rasen.png", ""));
    }
    
    private void loadLights(){
    	gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_LIGHT0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, new float[]{119,40f,147,0f}, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, new float[]{0.3f, 0.3f, 0.3f, 1}, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, new float[]{1,1,1,1}, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, new float[]{1,1,1,1}, 0);
		gl.glEnable(GL.GL_LIGHT1);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, new float[]{-119f,40f,147f,1f}, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, new float[]{0.3f, 0.3f, 0.3f, 1}, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, new float[]{1,1,1,1}, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, new float[]{1,1,1,1}, 0);
		gl.glEnable(GL.GL_LIGHT2);
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_POSITION, new float[]{119f,40f,-147f,1f}, 0);
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_AMBIENT, new float[]{0.3f, 0.3f, 0.3f, 1}, 0);
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_DIFFUSE, new float[]{1,1,1,1}, 0);
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_SPECULAR, new float[]{1,1,1,1}, 0);
    }
    
    private void loadSky(){
    	InnerSceneNode skybox = new InnerSceneNode(gl);
    	TexturedSquareNode sky = new TexturedSquareNode(gl, "src/textures/stop37.jpg");
    	sky.translate(120, 50, 150);
    	sky.scale (-460, 1, -520);
    	sky.rotate(90, 0, 0);
    	skybox.addChild(sky);    	
    	sky = new TexturedSquareNode(gl, "src/textures/sback37.jpg");
    	sky.translate(-340, -50, -370);
    	sky.scale(460, 100, 1);
     	skybox.addChild(sky);    	
    	sky = new TexturedSquareNode(gl, "src/textures/sleft37.jpg");
    	sky.translate(-340, -50, 150);
    	sky.scale(1, 100, 520);
    	sky.rotate(0, 90, 0);
     	skybox.addChild(sky);
     	sky = new TexturedSquareNode(gl, "src/textures/sright37.jpg");
    	sky.translate(120, -50, 150);
    	sky.scale(1, 100, 520);
    	sky.rotate(0, 90, 0);
     	skybox.addChild(sky);
    	sky = new TexturedSquareNode(gl, "src/textures/sfront37.jpg");
    	sky.translate(-340, -50, 150);
    	sky.scale(460, 100, 1);    	
    	skybox.addChild(sky);
    	scene.addChild(skybox);
    }
}
