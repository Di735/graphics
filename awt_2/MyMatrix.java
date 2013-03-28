import static java.lang.Math.*;

public class MyMatrix {
	static double buffer[][] = new double[4][4];
	static final int SZ = 4;
	double m[][];
	public MyMatrix() {
		m = new double[SZ][SZ];
	}
	public MyMatrix(int h, int w) {
		m = new double[h][w];
	}
	public MyMatrix(double ar[]){
		m = new double[1][ar.length];
		for(int i = 0; i < ar.length; i++)
			m[0][i] = ar[i];
	}
	public MyMatrix(double matrix[][]) {
		if(matrix.length != SZ) throw new RuntimeException();
		for(int i = 0; i < SZ; i++) if(matrix[i].length != SZ) throw new RuntimeException();
		m = matrix;
	}
	static MyMatrix getOX(double d){
		double sin = sin(d);
		double cos = cos(d);
		MyMatrix ret = new MyMatrix(new double[][] {
				{1,		0, 		0, 		0},
				{0,		cos,	-sin,	0},
				{0, 	sin,	cos,	0},
				{0,		0,		0,		1}
		});
		return ret;
	}
	static MyMatrix getOY(double d){
		double sin = sin(d);
		double cos = cos(d);
		MyMatrix ret = new MyMatrix(new double[][] {
				{cos,	0, 		sin,	0},
				{0,		1,		0,		0},
				{-sin, 	0,		cos,	0},
				{0,		0,		0,		1}
		});
		return ret;
	}
	static MyMatrix getOZ(double d){
		double sin = sin(d);
		double cos = cos(d);
		MyMatrix ret = new MyMatrix(new double[][] {
				{cos,	sin, 	0, 		0},
				{-sin,	cos,	0,	 	0},
				{0, 	0,		1,		0},
				{0,		0,		0,		1}
		});
		return ret;
	}
	static MyMatrix getTranslation(double dx, double dy, double dz){
		MyMatrix ret = new MyMatrix(new double[][] {
				{1,		0, 		0, 		0},
				{0,		1,		0,	 	0},
				{0, 	0,		1,		0},
				{dx,	dy,		dz,		1}
		});
		return ret;
	}
	
	
	public void multiple(MyMatrix mul){
		for(int i = 0; i < SZ; i++)
			for(int j = 0; j < SZ; j++)
				buffer[i][j] = 0;
		int a = m.length;
		int b = mul.m.length;
		int c = mul.m[0].length;

		for(int i = 0; i < a; i++)
			for(int j = 0; j < c; j++){
				if(m[i].length != b) throw new RuntimeException();
				for(int k = 0; k < b; k++){
					buffer[i][j] += m[i][k] * mul.m[k][j];
				}
			}
		for(int i = 0; i < a; i++)
			for(int j = 0; j < c; j++)
				m[i][j] = buffer[i][j];
	}
	public void set(MyMatrix e){
		if(e.m.length != m.length || e.m[0].length != m[0].length) throw new RuntimeException();
		for(int i = 0; i < e.m.length; i++)
			for(int j = 0; j < e.m[i].length; j++)
				m[i][j] = e.m[i][j];
	}
	public void set(double ar[]){
		if(ar.length != m[0].length || m.length != 1) throw new RuntimeException();
		for(int i = 0; i < ar.length; i++)
			m[0][i] = ar[i];
	}
}
