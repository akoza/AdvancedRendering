public class Matrix {
	
	public static float[] genIdent(){
		return new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};
	}
	
	public static float[] genTrans(float transX, float transY, float transZ){
		return new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, transX, transY, transZ, 1};
	}
	
	public static float[] genRotX (float rot){
		float arcrot = (float)(rot*2*Math.PI/360);
		return new float[] {1, 0, 0, 0, 0, (float) Math.cos(arcrot), (float)Math.sin(arcrot), 0, 0, (float)-Math.sin(arcrot), (float)Math.cos(arcrot), 0, 0, 0, 0, 1};
	}
	public static float[] genRotY (float rot){
		float arcrot = (float)(rot*2*Math.PI/360);
		return new float[] {(float)Math.cos(arcrot), 0, (float)-Math.sin(arcrot), 0, 0, 1, 0, 0, (float)Math.sin(arcrot), 0, (float)Math.cos(arcrot), 0, 0, 0, 0, 1};
	}
	
	public static float[] genRotZ (float rot){
		float arcrot = (float)(rot*2*Math.PI/360);
		return new float[] {(float)Math.cos(arcrot), (float)Math.sin(arcrot), 0, 0, (float)-Math.sin(arcrot), (float)Math.cos(arcrot), 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};
	}

	public static float[] genScale(float scaleX, float scaleY, float scaleZ){
		return new float[] {scaleX, 0, 0, 0, 0, scaleY, 0, 0, 0, 0, scaleZ, 0, 0, 0, 0, 1};
	}
	
	public static float[] mult (float[] mat, float v1, float v2, float v3){
		return mult(mat, new float[]{v1, v2, v3}, 0);
	}
	
	public static float[] mult (float[] mat, float[] vec, int offset){
		float[] ret = {0,0,0};
		float[] tmp1 = {0,0,0,0};
		float[] tmp2 = {0,0,0,1};		
		for (int i = 0; i < 3; ++i)
			tmp2[i] = vec[i+offset];				
		for (int i = 0; i < 4; ++i)			
			for (int j = 0; j < 4; ++j)
				tmp1[i] += mat[i+j*4]*tmp2[j];
		for (int i = 0; i < 3; ++i)
			ret[i] = tmp1[i] / tmp1[3];
		return ret;
	}
	
	public static float[] matMult(float[] mat1, float[]mat2){
		float[] ret = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		for (int i = 0; i < 4; ++i)
			for (int j = 0; j < 4; ++j)				
				for (int k = 0; k < 4; ++k)
					ret[i*4+j] +=mat1[j+k*4]*mat2[i*4+k]; 
		return ret;
	}
	
}
