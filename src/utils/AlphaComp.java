import java.util.Comparator;


public class AlphaComp implements Comparator<SceneNode>{

	float[] cam;
	
	public AlphaComp (float[]cameraPos){		
		cam = cameraPos;
	}
	
	public int compare(SceneNode o1, SceneNode o2) {
		float o1Dist = Integer.MAX_VALUE, o2Dist = Integer.MAX_VALUE;
		float[] sceneBounds = o1.getTrueBoundsInWorld();
		for (int i = 0; i <= 3; i = i+3)
			for (int j = 1; j <= 4; j = j+3)
				for (int k = 2; k <= 5; k = k+3){
					float tmp = (float)Math.pow(sceneBounds[i]-cam[0], 2)+
							    (float)Math.pow(sceneBounds[j]-cam[1], 2)+
							    (float)Math.pow(sceneBounds[k]-cam[2], 2);					
					if (tmp < o1Dist)
						o1Dist = tmp;
				}
		sceneBounds = o2.getTrueBoundsInWorld();
		for (int i = 0; i <= 3; i = i+3)
			for (int j = 1; j <= 4; j = j+3)
				for (int k = 2; k <= 5; k = k+3){
					float tmp = (float)Math.pow(sceneBounds[i]-cam[0], 2)+
							    (float)Math.pow(sceneBounds[j]-cam[1], 2)+
							    (float)Math.pow(sceneBounds[k]-cam[2], 2);					
					if (tmp < o2Dist)
						o2Dist = tmp;
				}					
		return (-1)*Float.compare(o1Dist, o2Dist);
	}

}
