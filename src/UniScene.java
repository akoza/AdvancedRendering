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
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.cg.CGcontext;
import com.sun.opengl.cg.CGparameter;
import com.sun.opengl.cg.CGprogram;
import com.sun.opengl.cg.CgGL;
import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

public class UniScene extends JoglTemplate {

	private static final long serialVersionUID = 1L;
		
	private InnerSceneNode scene;	
	private GL gl;
	private GLU glu;
	private GLUT glut;	
	private int texWidth = 1280, texHeight = 800;
	
	private CGcontext cgContext;
	private CGprogram cgSSAO = null;
	private CGparameter cgProjMat, cgProjMatInv, cgPixelX, cgPixelY, cgBloomAlpha, cgThresh;	
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
		cgProjMat = CgGL.cgGetNamedParameter(cgSSAO, "projMat");
		cgProjMatInv = CgGL.cgGetNamedParameter(cgSSAO, "projMatInv");
		cgPixelX = CgGL.cgGetNamedParameter(cgSSAO, "pixelSizeX");
		cgPixelY = CgGL.cgGetNamedParameter(cgSSAO, "pixelSizeY");
		cgBloomAlpha = CgGL.cgGetNamedParameter(cgSSAO, "alpha");			
		cgThresh = CgGL.cgGetNamedParameter(cgSSAO, "threshold");
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
    	loadSky();
    	rand = new Random();
    }

    public void keyPressed(KeyEvent e) {    
    	super.keyPressed(e);
    	if (e.getKeyCode() == KeyEvent.VK_1){
    		ssao = !ssao;
    		System.out.println("Shader "+ssao);
    	}
    	if (e.getKeyCode() == KeyEvent.VK_UP && alpha < 20){
    		++alpha;
    		System.out.println("Bloom alpha "+alpha*0.05);
    	}
    	if (e.getKeyCode() == KeyEvent.VK_DOWN && alpha > 0){
    		--alpha;
    		System.out.println("Bloom alpha "+alpha*0.05);
    	}
    	if (e.getKeyCode() == KeyEvent.VK_RIGHT && celThreshold < 20){
    		++celThreshold;
    		System.out.println("CelShader Threshold "+celThreshold*0.05);
    	}
    	if (e.getKeyCode() == KeyEvent.VK_LEFT && celThreshold > 0){
    		--celThreshold;
    		System.out.println("CelShader Threshold "+celThreshold*0.05);
    	}
    		
    	// Insert Key Interactions
    	/** if (e.getKeyCode() == KeyEvent.VK_SPACE)
	    	state = ++state % 2; */
    }
    
    boolean ssao = false;
    int alpha = 0, celThreshold = 0;
    Random rand;
    
    public void display(GLAutoDrawable drawable) {
    	    	
    	drawToFBO();
    	
    	Texture rand;    
    	
		if (ssao){
			gl.glActiveTexture(GL.GL_TEXTURE2);
			rand = genRandTex();
			rand.bind();
			gl.glActiveTexture(GL.GL_TEXTURE0);
			CgGL.cgGLSetStateMatrixParameter(cgProjMat,
				CgGL.CG_GL_PROJECTION_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
			CgGL.cgGLSetStateMatrixParameter(cgProjMatInv,
				CgGL.CG_GL_PROJECTION_MATRIX, CgGL.CG_GL_MATRIX_INVERSE);
			CgGL.cgGLSetParameter1f(cgPixelX, 1/texWidth);
			CgGL.cgGLSetParameter1f(cgPixelY, 1/texHeight);
			CgGL.cgGLSetParameter1f(cgBloomAlpha, alpha);			
			CgGL.cgGLSetParameter1f(cgThresh, celThreshold);
			CgGL.cgGLEnableProfile(cgFragProfile);
			CgGL.cgGLBindProgram(cgSSAO);	
    		drawToScreen();
    		CgGL.cgGLDisableProfile(cgFragProfile);
    		rand.dispose();
    	}else
    		drawToScreen();    	
    	

    }
    
    private int fboTexId, depthTexId;   
    private int fboId;
    
    private void prepareFBO(){    	
    	int[] tmp = new int[1];
    	gl.glGenTextures(1, tmp, 0);
    	fboTexId = tmp[0];
    	gl.glBindTexture(GL.GL_TEXTURE_2D, fboTexId);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);  
    	gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, texWidth, texHeight, 0,
    			GL.GL_RGBA, GL.GL_FLOAT, null);
    	gl.glBindTexture(GL.GL_TEXTURE_2D, 0);   
    	
    	gl.glGenTextures(1, tmp, 0);
    	depthTexId = tmp[0];
    	gl.glBindTexture(GL.GL_TEXTURE_2D, depthTexId);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);    
    	gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_DEPTH_COMPONENT24, texWidth, texHeight, 0,
    			GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, null);
    	gl.glBindTexture(GL.GL_TEXTURE_2D, 0);     
    	
    	tmp = new int[1];
    	gl.glGenFramebuffersEXT(1, tmp, 0);
    	fboId = tmp[0];
    	gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fboId);
    	
    	gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_TEXTURE_2D, fboTexId, 0);
    	
    	gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, GL.GL_TEXTURE_2D, depthTexId, 0);
    	    	
    	
    	if (gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT) != GL.GL_FRAMEBUFFER_COMPLETE_EXT){
    		System.err.println ("Something went wrong");
    		System.exit(1);
    	}
    	
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
		gl.glActiveTexture(GL.GL_TEXTURE0);		
		gl.glBindTexture(GL.GL_TEXTURE_2D, fboTexId);
		if (ssao){			
			gl.glActiveTexture(GL.GL_TEXTURE1);		
			gl.glBindTexture(GL.GL_TEXTURE_2D, depthTexId);
			gl.glActiveTexture(GL.GL_TEXTURE0);
		}
		else
			gl.glEnable(GL.GL_TEXTURE_2D);
		
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
    	scene.addChild(new ObjectSceneNode(gl, "src/models/Campus"));    	
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
    	scene.addChild(new SkyboxNode(gl, new String[]{"src/textures/sright37.jpg", "src/textures/sleft37.jpg", "src/textures/stop37.jpg", "src/textures/stop37.jpg", "src/textures/sfront37.jpg", "src/textures/sback37.jpg"}));
    }
    
    private Texture genRandTex(){
    	BufferedImage image = new BufferedImage(texWidth, texHeight, BufferedImage.TYPE_INT_RGB);
    	for (int i = 0; i < texWidth; ++i)
    		for (int j = 0; j < texHeight; ++j)
    			image.setRGB(i, j, rand.nextInt(255));
    	return TextureIO.newTexture(image, true);
    }
}
