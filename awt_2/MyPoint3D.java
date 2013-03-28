import java.awt.geom.Point2D;
import static java.lang.Math.*;
import java.lang.management.MonitorInfo;
import java.util.*;
import static java.util.Arrays.fill;
import static java.util.Arrays.sort;


public class MyPoint3D{
	static final int SZ = 4;
	private MyMatrix coords;
	public MyPoint3D(double x, double y, double z) {
		coords = new MyMatrix(new double[] {x, y, z, 1});
	}
	public MyPoint3D(MyPoint3D p) {
		coords = new MyMatrix(1, 4);
		coords.set(p.coords);
	}
	public void set(double x, double y, double z) {
		coords.set(new double[] {x, y, z, 1});
	}
		
	public void set(MyPoint3D p) {
		coords.set(p.coords);
	}
	
	public void add(double dx, double dy, double dz){
		coords.m[0][0] += dx; coords.m[0][1] += dy; coords.m[0][2] += dz;
	}
	double x(){ return coords.m[0][0]; }
	double y(){ return coords.m[0][1]; }
	double z(){ return coords.m[0][2]; }
	

	
	void multiple(MyMatrix mul){
		coords.multiple(mul);
	}
	void multiple(double mul){
		for(int i = 0; i < 3; i++)
		coords.m[0][i] *= mul;
	}
	double getNorm(){
		return sqrt(sqr(coords.m[0][0]) + sqr(coords.m[0][1]) + sqr(coords.m[0][2]));
	}
	void normolize(){
		multiple(1 / getNorm());
	}
	public MyPoint3D() {
		
	}
	
	
	@Override
	public String toString() {
		return Arrays.toString(coords.m[0]);
	}
	
	
	/*** others ***/
	static public double sqr(double x){
		return x * x;
	}
	static public double dist(MyPoint3D a, MyPoint3D b){
		return sqrt(sqr(a.x() - b.x()) + sqr(a.y() - b.y()) + sqr(a.z() - b.z()));
	}
	
}
