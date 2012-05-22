public class Vector4 {
	private double[] v_;

	public Vector4() {
		initialize();
	}

	public Vector4(double v1, double v2, double v3, double v4) {
		initialize();
		set(v1, v2, v3, v4);
	}

	public Vector4(Vector4 vector4) {
		initialize();
		set(vector4);
	}

	private void initialize() {
		v_ = new double[4];
		for (int i = 0; i < 4; i++)
			v_[i] = 0.0;
	}

	public void set(double v1, double v2, double v3, double v4) {
		v_[0] = v1;
		v_[1] = v2;
		v_[2] = v3;
		v_[3] = v4;
	}

	public void set(Vector4 vector) {
		for (int i = 0; i < 4; i++)
			v_[0] = vector.v_[i];
	}

	public boolean equals(Object object) {
		Vector4 vector = (Vector4) object;

		return v_[0] == vector.v_[0] && v_[1] == vector.v_[1]
				&& v_[2] == vector.v_[2] && v_[3] == vector.v_[3];
	}

	public double getElement(int i) {
		return v_[i];
	}

	public void setElement(int i, double value) {
		v_[i] = value;
	}
	
	public void multWith(double d){
		v_[0] *= d;
		v_[1] *= d;
		v_[2] *= d;
		v_[3] *= d;
	}

	public String toString() {
		return ("Vector4: [" + v_[0] + "," + v_[1] + "," + v_[2] + "," + v_[3] + "]");
	}
}