public class Camera{
	public float pos[] = new float[3];
	public float at[] = new float[3];
	public float up[] = new float[3];
	
	public Camera(){
		reset();
	}
	
	public void set(float posX, float posY, float posZ,
					float atX, float atY, float atZ,
					float upX, float upY, float upZ){
		pos[0] = posX;
		pos[1] = posY;
		pos[2] = posZ;
		at[0] = atX;
		at[1] = atY;
		at[2] = atZ;
		up[0] = upX;
		up[1] = upY;
		up[2] = upZ;
	}
	
	public void lookAt(float x, float y, float z){
		at[0] = x;
		at[1] = y;
		at[2] = z;
	}
	
	public float getLength(){
		float dirX, dirY, dirZ;
		dirX = at[0] - pos[0];
		dirY = at[1] - pos[1];
		dirZ = at[2] - pos[2];
		return (float) Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
	}
	
	public void zoomIn(float stepRange){
		float dirX, dirY, dirZ;
		dirX = at[0] - pos[0];
		dirY = at[1] - pos[1];
		dirZ = at[2] - pos[2];

		pos[0] -= dirX / 30 * stepRange;
		pos[1] -= dirY / 30 * stepRange;
		pos[2] -= dirZ / 30 * stepRange;
	}
	
	public void cropToLength(float length, float height){
		float actualLength = getLength();
		pos[0] = at[0] - (at[0] - pos[0]) / actualLength * length;
		pos[1] = at[1] + height;
		pos[2] = at[2] - (at[2] - pos[2]) / actualLength * length;
	}
	
	public void moveRight(float factor){
		Vector3 toVec = new Vector3();
		toVec.set(at[0] - pos[0], at[1] - pos[1], at[2] - pos[2]);
		Vector3 upVec = new Vector3();
		upVec.set(up[0], up[1], up[2]);
		
		toVec.normalize();
		toVec.crossProduct(upVec);
		toVec.multWith(factor);
		
		pos[0] += toVec.getElement(0);
		at[0] += toVec.getElement(0);
		pos[1] += toVec.getElement(1);
		at[1] += toVec.getElement(1);
		pos[2] += toVec.getElement(2);
		at[2] += toVec.getElement(2);
	}
	
	public void moveLeft(float factor){
		Vector3 toVec = new Vector3();
		toVec.set(at[0] - pos[0], at[1] - pos[1], at[2] - pos[2]);
		Vector3 upVec = new Vector3();
		upVec.set(up[0], up[1], up[2]);
		
		toVec.normalize();
		upVec.crossProduct(toVec);
		upVec.multWith(factor);
		
		pos[0] += upVec.getElement(0);
		at[0] += upVec.getElement(0);
		pos[1] += upVec.getElement(1);
		at[1] += upVec.getElement(1);
		pos[2] += upVec.getElement(2);
		at[2] += upVec.getElement(2);
	}
	
	public void moveForward(float factor){
		Vector3 toVec = new Vector3();
		toVec.set(at[0] - pos[0], at[1] - pos[1], at[2] - pos[2]);
		
		toVec.normalize();
		toVec.multWith(factor);
		
		pos[0] += toVec.getElement(0);
		at[0] += toVec.getElement(0);
		pos[1] += toVec.getElement(1);
		at[1] += toVec.getElement(1);
		pos[2] += toVec.getElement(2);
		at[2] += toVec.getElement(2);
	}
	
	public void moveBackward(float factor){
		Vector3 toVec = new Vector3();
		toVec.set(at[0] - pos[0], at[1] - pos[1], at[2] - pos[2]);
		
		toVec.normalize();
		toVec.multWith(factor);
		
		pos[0] -= toVec.getElement(0);
		at[0] -= toVec.getElement(0);
		pos[1] -= toVec.getElement(1);
		at[1] -= toVec.getElement(1);
		pos[2] -= toVec.getElement(2);
		at[2] -= toVec.getElement(2);
	}
	
	public void moveUp(float factor){
		pos[1] += factor;
		at[1] += factor;
	}
	
	public void moveDown(float factor){
		pos[1] -= factor;
		at[1] -= factor;
	}
	
	/**
	 * moves Camera "smoothly" to given position
	 * @param x X position of desired position
	 * @param y Y position of desired position
	 * @param z Z position of desired position
	 * @param movingFactor Speed of moving the camera considering the distance [0..1]
	 * @param minMoving Speed of moving the camera as additional constant not considering the distance
	 */
	public void moveToDesiredPosition(float x, float y, float z, float movingFactor, float minMoving){
		float length = (float) Math.sqrt((x - pos[0]) * (x - pos[0]) + (y - pos[1]) * (y - pos[1]) + (z - pos[2]) * (z - pos[2]));
		
		if(length * movingFactor + minMoving > length){
			pos[0] = x;
			pos[1] = y;
			pos[2] = z;
		} else {
			pos[0] += (x - pos[0]) * (movingFactor + minMoving / length);
			pos[1] += (y - pos[1]) * (movingFactor + minMoving / length);
			pos[2] += (z - pos[2]) * (movingFactor + minMoving / length);
		}
	}
	
	public void reset(){
		up[0] = 0;
		up[1] = 1;
		up[2] = 0;
		
		pos[0] = 0;
		pos[1] = 3;
		pos[2] = 10;
		
		at[0] = 0;
		at[1] = 0;
		at[2] = 0;
	}
}
