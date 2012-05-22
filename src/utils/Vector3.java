public class Vector3 {
	private double[] v_;

	public Vector3() {
		initialize();
	}

	public Vector3(double v1, double v2, double v3) {
		initialize();
		set(v1, v2, v3);
	}

	public Vector3(Vector3 vector3) {
		initialize();
		set(vector3);
	}

	private void initialize() {
		v_ = new double[3];
		for (int i = 0; i < 3; i++)
			v_[i] = 0.0;
	}

	public void set(double v1, double v2, double v3) {
		v_[0] = v1;
		v_[1] = v2;
		v_[2] = v3;
	}

	public void set(Vector3 vector) {
		for (int i = 0; i < 3; i++)
			v_[i] = vector.v_[i];
	}

	public boolean equals(Object object) {
		Vector3 vector = (Vector3) object;

		return v_[0] == vector.v_[0] && v_[1] == vector.v_[1]
				&& v_[2] == vector.v_[2];
	}

	public double getElement(int i) {
		return v_[i];
	}

	public void setElement(int i, double value) {
		v_[i] = value;
	}
	
	public void crossProduct(Vector3 vector){
		double tmp[] = new double[3];
		tmp[0] = v_[0];
		tmp[1] = v_[1];
		tmp[2] = v_[2];
		v_[0] = tmp[1] * vector.getElement(2) - tmp[2] * vector.getElement(1);
		v_[1] = tmp[2] * vector.getElement(0) - tmp[0] * vector.getElement(2);
		v_[2] = tmp[0] * vector.getElement(1) - tmp[1] * vector.getElement(0);
	}

	public String toString() {
		return ("Vector3: [" + v_[0] + "," + v_[1] + "," + v_[2] + "]");
	}
	
	public void normalize(){
		double length = Math.sqrt(v_[0] * v_[0] + v_[1] * v_[1] + v_[2] * v_[2]);
		v_[0] /= length;
		v_[1] /= length;
		v_[2] /= length;
	}
	
	public void multWith(double factor){
		v_[0] *= factor;
		v_[1] *= factor;
		v_[2] *= factor;
	}
}
