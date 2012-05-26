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
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.Calendar;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.cg.CGcontext;
import com.sun.opengl.cg.CGparameter;
import com.sun.opengl.cg.CGprogram;
import com.sun.opengl.cg.CgGL;
import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

public class UniScene extends JoglTemplate {

	private static final long serialVersionUID = 1L;
		
	private InnerSceneNode scene, sceneBump;	
	private GL gl;
	private GLU glu;
	private GLUT glut;	
	private int texWidth = 1920, texHeight = 1200;
	
	//Uhr:
	private SceneNode uhr_sek;
	private SceneNode uhr_min;
	private SceneNode uhr_h;
	
	private Camera cam;

	private CGcontext cgContext;
	private CGprogram cgSSAO = null, cgVP = null, cgFP = null, cgBumpV = null, cgBumpF = null, cgPhongV = null, cgPhongF = null;
	private CGparameter cgModelProj, cgBloomAlpha, cgThresh;
	private CGparameter cgModelView, cgModelViewProj, cgLightPosition;
	private CGparameter cgIa, cgId, cgBump;	
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
	
	CGprogram load(String path, int profile){
		CGprogram ret;
		ret = CgGL.cgCreateProgramFromFile(cgContext,
				CgGL.CG_SOURCE, path, profile, null, null);
		if (ret == null)
		{
			int err = CgGL.cgGetError();
			System.err.println("Compile shader ["+path+"] "
					+ CgGL.cgGetErrorString(err));
			if (CgGL.cgGetLastListing(cgContext) != null)
			{
				System.err.println(CgGL.cgGetLastListing(cgContext) + "\n");
			}
			System.exit(1);
		}
		return ret;
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
		 
		cgSSAO = load("src/shader/ssao.cg", cgFragProfile);
		CgGL.cgGLLoadProgram(cgSSAO);
		cgVP = load("src/shader/vp.cg", cgVertexProfile);
		CgGL.cgGLLoadProgram(cgVP);	
		cgFP = load("src/shader/fp.cg", cgFragProfile);	
		CgGL.cgGLLoadProgram(cgFP);
		cgBumpV = load("src/shader/v_bump_new.cg", cgVertexProfile);
		CgGL.cgGLLoadProgram(cgBumpV);
		cgBumpF = load("src/shader/f_bump_new.cg", cgFragProfile);
		CgGL.cgGLLoadProgram(cgBumpF);
		
		cgBloomAlpha = CgGL.cgGetNamedParameter(cgSSAO, "alpha");			
		cgThresh = CgGL.cgGetNamedParameter(cgSSAO, "threshold");
		cgModelProj = CgGL.cgGetNamedParameter(cgVP, "modelViewProj");
		cgLightPosition = CgGL.cgGetNamedParameter(cgBumpV, "lightPosition");
		cgModelViewProj = CgGL.cgGetNamedParameter(cgBumpV, "modelViewProj");		
		cgModelView = CgGL.cgGetNamedParameter(cgBumpV, "modelView");
		cgIa = CgGL.cgGetNamedParameter(cgBumpF, "Ia");
		cgId = CgGL.cgGetNamedParameter(cgBumpF, "Id");
		cgBump = CgGL.cgGetNamedParameter(cgBumpF, "bumpP");
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
    	gl.glHint(GL.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST);
    	gl.glEnable(GL.GL_POLYGON_SMOOTH);
    	gl.glEnable(GL.GL_DOUBLEBUFFER);
    	scene = new InnerSceneNode (gl);
    	sceneBump = new InnerSceneNode (gl);
    	
    	cam = new Camera();

    	loadShaders();    	
    	
    	loadClock();
    	
    	loadCampus();
    	loadLights();
    	loadSky();
    	rand = new Random();
    	randTex = genRandTex();
    }
    
    public void keyPressed(KeyEvent e) {    
    	//super.keyPressed(e);
    	
		if(e.getKeyCode() == KeyEvent.VK_D) cam.moveRight(1);
		if(e.getKeyCode() == KeyEvent.VK_A) cam.moveLeft(1);
		if(e.getKeyCode() == KeyEvent.VK_W) cam.moveForward(1);
		if(e.getKeyCode() == KeyEvent.VK_S) cam.moveBackward(1);
		if(e.getKeyCode() == KeyEvent.VK_SPACE) cam.moveUp(1);
		if(e.getKeyCode() == KeyEvent.VK_SHIFT) cam.moveDown(1);

    	if (e.getKeyCode() == KeyEvent.VK_1){
    		ssao = !ssao;
    		System.out.println("Shader "+ssao);
    	}
    	if (e.getKeyCode() == KeyEvent.VK_2){
    		bump = !bump;
    		System.out.println("Normal Mapping Shader "+bump);
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
    
    boolean ssao = false, bump = false;
    int alpha = 0, celThreshold = 0;
    Random rand;
    
    public void display(GLAutoDrawable drawable) {
    	
    	gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fboId);    	
		CgGL.cgGLEnableProfile(cgVertexProfile);
		CgGL.cgGLBindProgram(cgBumpV);
		CgGL.cgGLEnableProfile(cgFragProfile);
		CgGL.cgGLBindProgram(cgBumpF);	    	
    	drawToFBO(bump);    		    	
    	CgGL.cgGLEnableProfile(cgVertexProfile);
    	CgGL.cgGLBindProgram(cgVP);
    	CgGL.cgGLEnableProfile(cgFragProfile);
    	CgGL.cgGLBindProgram(cgFP);
    	gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_TEXTURE_2D, fboTexNormals, 0);    	
    	drawToFBO(false);
    	gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_TEXTURE_2D, fboTexId, 0);
    	gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
    	CgGL.cgGLDisableProfile(cgVertexProfile);
	    CgGL.cgGLDisableProfile(cgFragProfile);    	
	
		if (ssao){		
			CgGL.cgGLSetParameter1f(cgBloomAlpha, alpha);			
			CgGL.cgGLSetParameter1f(cgThresh, celThreshold);			
			CgGL.cgGLEnableProfile(cgFragProfile);
			CgGL.cgGLBindProgram(cgSSAO);	
    		drawToScreen();    		
    		CgGL.cgGLDisableProfile(cgFragProfile);    		
    	}else{
    		
    		drawToScreen();    	
    	}

    }
    
    private int fboTexId, depthTexId;   
    private int fboId;
    private int fboTexNormals;
    
    private void prepareFBO(){    	
    	int[] tmp = new int[1];
    	gl.glGenTextures(1, tmp, 0);
    	fboTexId = tmp[0];
    	gl.glBindTexture(GL.GL_TEXTURE_2D, fboTexId);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);  
    	gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA16, texWidth, texHeight, 0,
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
    	
    	gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
    	    	
    	//FBO for normals
    	tmp = new int[1];
    	gl.glGenTextures(1, tmp, 0);
    	fboTexNormals = tmp[0];
    	gl.glBindTexture(GL.GL_TEXTURE_2D, fboTexNormals);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
    	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);  
    	gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, texWidth, texHeight, 0,
    			GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
    	gl.glBindTexture(GL.GL_TEXTURE_2D, 0);  
    	
    	if (gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT) != GL.GL_FRAMEBUFFER_COMPLETE_EXT){
    		System.err.println ("Something went wrong");
    		System.exit(1);
    	}    	    
    	
    	gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
    }   
    
    private void drawToFBO(boolean bump){        	    
    	
    	update_uhr();    	
    	
		gl.glClearColor(0,1,1,0);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glPushMatrix();		
		/*applyMouseTranslation(gl);
		applyMouseRotation(gl);*/		
		glu.gluLookAt(cam.pos[0], cam.pos[1], cam.pos[2], cam.at[0], cam.at[1], cam.at[2], cam.up[0], cam.up[1], cam.up[2]);			
						
		
		CgGL.cgGLSetParameter1f(cgBump, bump?1:0);
		sceneBump.drawSorted(false, new float[] {cam.pos[0], cam.pos[1], cam.pos[2]}, new CGparameter[] {cgModelProj, cgModelViewProj, cgModelView});
		CgGL.cgGLSetParameter1f(cgBump, 0);
		scene.drawSorted(false, new float[] {cam.pos[0], cam.pos[1], cam.pos[2]}, new CGparameter[] {cgModelProj, cgModelViewProj, cgModelView});

				
		
		CgGL.cgGLDisableProfile(cgFragProfile);
    	CgGL.cgGLDisableProfile(cgVertexProfile);
		
        gl.glPopMatrix();                            
    }
    
    private Texture randTex = null;
    
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
		Texture celRand = null;
		if (ssao){			
			gl.glActiveTexture(GL.GL_TEXTURE1);		
			gl.glBindTexture(GL.GL_TEXTURE_2D, depthTexId);
			gl.glActiveTexture(GL.GL_TEXTURE2);
			gl.glBindTexture(GL.GL_TEXTURE_2D, fboTexNormals);
			gl.glActiveTexture(GL.GL_TEXTURE3);			
			randTex.bind();
			gl.glActiveTexture(GL.GL_TEXTURE4);
			celRand = genRandTex();
			celRand.bind();			
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
		else
			celRand.dispose();
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPopMatrix();
    }
    
    private void loadCampus(){
        sceneBump.addChild(new ObjectSceneNode(gl, "src/models/ground"));    //src/models/Campus
        scene.addChild(new ObjectSceneNode(gl, "src/models/a"));  
        scene.addChild(new ObjectSceneNode(gl, "src/models/b"));  
        scene.addChild(new ObjectSceneNode(gl, "src/models/c"));  
        scene.addChild(new ObjectSceneNode(gl, "src/models/d"));  
        scene.addChild(new ObjectSceneNode(gl, "src/models/g")); 
        scene.addChild(new ObjectSceneNode(gl, "src/models/g_schrift"));               
        scene.addChild(new ObjectSceneNode(gl, "src/models/me"));  
        scene.addChild(new ObjectSceneNode(gl, "src/models/brunnen")); 
        scene.addChild(new ObjectSceneNode(gl, "src/models/bank1")); 
        scene.addChild(new ObjectSceneNode(gl, "src/models/bank2")); 
        scene.addChild(new ObjectSceneNode(gl, "src/models/lampe1"));    
        scene.addChild(new ObjectSceneNode(gl, "src/models/buchstaben"));
        sceneBump.addChild(new ObjectSceneNode(gl, "src/models/steine"));
        sceneBump.addChild(new ObjectSceneNode(gl, "src/models/baeume"));    
        sceneBump.addChild(new ObjectSceneNode(gl, "src/models/buesche1")); 
        sceneBump.addChild(new ObjectSceneNode(gl, "src/models/buesche2"));
        sceneBump.addChild(new ObjectSceneNode(gl, "src/models/buesche3"));
        sceneBump.addChild(new ObjectSceneNode(gl, "src/models/buesche4"));
        sceneBump.addChild(new ObjectSceneNode(gl, "src/models/buesche5"));
        sceneBump.addChild(new ObjectSceneNode(gl, "src/models/buesche6"));
    }
    
    private void loadClock(){
    	scene.addChild(new ObjectSceneNode(gl, "src/models/uhr_basic"));  
    	uhr_h = new ObjectSceneNode(gl, "src/models/uhr_h");
    	uhr_h.translate(-55.5f, 3.4f, -49.2f);
    	scene.addChild(uhr_h);
    	uhr_min = new ObjectSceneNode(gl, "src/models/uhr_min");
    	uhr_min.translate(-55.5f, 3.4f, -47.6f);
    	scene.addChild(uhr_min);
    	uhr_sek = new ObjectSceneNode(gl, "src/models/uhr_sek");
    	uhr_sek.translate(-55.5f, 3.4f, -46f);
    	scene.addChild(uhr_sek);
    }
    
    private void loadLights(){
    	gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_LIGHT0);
		float[] light = {0.6f, 0.6f, 0.6f, 1, 
						1f, 1f, 1f, 1, 
						1f, 1f, 1f, 1};
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, new float[]{119f, 40f, 147f, 1f}, 0); 
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, new float[]{0.6f, 0.6f, 0.6f, 1}, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, new float[]{1f, 1f, 1f, 1f}, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, new float[]{1f, 1f, 1f, 1f}, 0);
		CgGL.cgGLSetParameter3fv(cgLightPosition, new float[]{119f, 40f, 147f, 1f}, 0);
		CgGL.cgGLSetParameter3fv(cgIa, light, 0);
		CgGL.cgGLSetParameter3fv(cgId, light, 4);	
		/*gl.glEnable(GL.GL_LIGHT1);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, new float[]{-119f,40f,147f,1f}, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, new float[]{0.3f, 0.3f, 0.3f, 1}, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, new float[]{1,1,1,1}, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, new float[]{1,1,1,1}, 0);
		gl.glEnable(GL.GL_LIGHT2);
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_POSITION, new float[]{119f,40f,-147f,1f}, 0);
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_AMBIENT, new float[]{0.3f, 0.3f, 0.3f, 1}, 0);
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_DIFFUSE, new float[]{1,1,1,1}, 0);
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_SPECULAR, new float[]{1,1,1,1}, 0);*/
    }
    
    private void loadSky(){
    	scene.addChild(new SkyboxNode(gl, new String[]{"src/textures/sright37.jpg", "src/textures/sleft37.jpg", "src/textures/stop37.jpg", "src/textures/stop37.jpg", "src/textures/sfront37.jpg", "src/textures/sback37.jpg"}));
    }
    
    private Texture genRandTex(){
    	BufferedImage image = new BufferedImage(texWidth, texHeight, BufferedImage.TYPE_INT_RGB);
    	for (int i = 0; i < texWidth; ++i)
    		for (int j = 0; j < texHeight; ++j)
    			image.setRGB(i, j, rand.nextInt(0xffffff));
    	return TextureIO.newTexture(image, true);
    }

	public void mousePressed(MouseEvent e)
	{
		prevMouseX = e.getX();
		prevMouseY = e.getY();
	}
	
	public void mouseDragged(MouseEvent e)
	{
		// get current mouse x and y coordinate
		int x = e.getX();
		int y = e.getY();
		// get size of component
		Dimension size = e.getComponent().getSize();

		// left button is dragged (rotation)
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0)
		{
			// store thetaY movement for rotation around x-axis
			float thetaY = 360f * ((float) (x - prevMouseX) / (float) size.width);
			// store thetaX movement for rotation around y-axis
			float thetaX = 360f * ((float) (prevMouseY - y) / (float) size.height);
			// store current x and y coordinate for future calculations
			prevMouseX = x;
			prevMouseY = y;
			
			Matrix4x4 rot = new Matrix4x4();
			rot.rotateX(-(cam.pos[2] - cam.at[2]) / cam.getLength() * (thetaX) * Math.PI / 180);
			rot.rotateY((thetaY) * Math.PI / 180);
			rot.rotateZ((cam.pos[0] - cam.at[0]) / cam.getLength() * (thetaX) * Math.PI / 180);
			Vector4 vec = new Vector4(cam.pos[0] - cam.at[0], cam.pos[1] - cam.at[1], cam.pos[2] - cam.at[2], 0);
			vec = rot.multiply(vec);
			cam.pos[0] = cam.at[0] + (float)vec.getElement(0);
			cam.pos[1] = cam.at[1] + (float)vec.getElement(1);
			cam.pos[2] = cam.at[2] + (float)vec.getElement(2);
		}
		// right button is dragged (translation)
		if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
		{
			// thetaX movement forprivate CGprogram bla = null; x translation
			float thetaX = 0.63f * ((float) (x - prevMouseX)); // store
			// thetaY movement for y translation
			float thetaY = 0.63f * ((float) (prevMouseY - y)); // store
			// store current x and y coordinate for future calculations
			prevMouseX = x;
			prevMouseY = y;
			// change x and y rotation value
			
			float lengthX, lengthX2, lengthZ, lengthZ2;
			lengthX = (cam.at[0] - cam.pos[0]) * thetaX * 0.00166f;
			lengthX2 = (cam.at[0] - cam.pos[0]) * thetaY * 0.00166f;
			lengthZ = (cam.at[2] - cam.pos[2]) * thetaX * 0.00166f;
			lengthZ2 = (cam.at[2] - cam.pos[2]) * thetaY * 0.00166f;
			
			cam.pos[0] += lengthZ;
			cam.at[0] += lengthZ;
			cam.pos[2] -= lengthX;
			cam.at[2] -= lengthX;
			cam.pos[0] -= lengthX2;
			cam.at[0] -= lengthX2;
			cam.pos[2] -= lengthZ2;
			cam.at[2] -= lengthZ2;
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e)
	{uhr_h.translate(-55.5f, 3.4f, -49.2f);
		cam.zoomIn(e.getWheelRotation() * 2.8f);
	}
	
	private void update_uhr(){
		   	java.util.Calendar today = new java.util.GregorianCalendar();
		   	int std = today.get(Calendar.HOUR_OF_DAY);
		   	int min = today.get(Calendar.MINUTE);
		   	int sek = today.get(Calendar.SECOND);		   			   	
		   	uhr_sek.rotate(sek*6,0,0);		   	
		   	uhr_min.rotate(min*6,0,0);		   	
		   	uhr_h.rotate(std*30,0,0);		   	
	   }

}
