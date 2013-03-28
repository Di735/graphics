import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.lang.Math.*;
import java.lang.management.MonitorInfo;
import java.util.*;
import java.util.Map.Entry;
import static java.lang.Math.*;
import static java.util.Arrays.fill;
import static java.util.Arrays.sort;


public class Flat3D {
	HashMap<Integer, MyPoint3D> p3d;
	int id;
	double A, B, C, D;
	MyPoint3D normale;
	private MyPoint3D m[] = new MyPoint3D[3];
	int order[];
	Flat3D(int id, HashMap<Integer, MyPoint3D> p3d, int[] order){
		this.id = id;
		this.p3d = new HashMap<Integer, MyPoint3D>(p3d);
		if(p3d.size() < 3) throw new RuntimeException("Not enough ");
		reCalc();
		this.order = new int[order.length];
		for(int i = 0; i < order.length; i++){
			this.order[i] = order[i];
		}
	}
	
	public void reCalc(){
		int p = 0;
		for(Entry<Integer, MyPoint3D> es : p3d.entrySet()){
			MyPoint3D ps = es.getValue();
			m[p++] = ps;
			if(p == 3) break;
		}
		MyPoint3D a = new MyPoint3D(m[1].x() - m[0].x(), m[1].y() - m[0].y(), m[1].z() - m[0].z());
		MyPoint3D b = new MyPoint3D(m[2].x() - m[0].x(), m[2].y() - m[0].y(), m[2].z() - m[0].z());
		normale = MyPolyhedron.vectProduct(a, b);
		A = normale.x();
		B = normale.y();
		C = normale.z();
		D = 0;
		double k = 1 / sqrt(sqr(A) + sqr(B) + sqr(C) + sqr(D));
		A *= k;
		B *= k;
		C *= k;
		D = -calc(m[0]);
	}
	
	double getZIts(double x0, double y0){
		if(abs(C) < 1e-5) return Double.MAX_VALUE;
		double t = -(A * x0 + B * y0 + D) / C;
		if(!checkInside(new MyPoint3D(x0, y0, t))) return Double.MAX_VALUE;
		return t;
	}
	double calc(MyPoint3D p){
		return p.x() * A + p.y() * B + p.z() * C + D;
	}
	
	boolean checkInside(MyPoint3D p){
		int n = order.length;
		double xp = p.x();
		double yp = p.y();
		double zp = p.z();
		MyPoint3D ab = new MyPoint3D(0, 0, 0);
		MyPoint3D ac = new MyPoint3D(0, 0, 0);
		MyPoint3D ap = new MyPoint3D(0, 0, 0);
		MyPoint3D vectP = new MyPoint3D(0, 0, 0);
		MyPoint3D vectCur = new MyPoint3D(0, 0, 0);
		for(int i = 0; i < n; i++){
			MyPoint3D a = p3d.get(order[i]);
			MyPoint3D b = p3d.get(order[(i + 1) % n]);
			MyPoint3D c = p3d.get(order[(i + 2) % n]);
			ab.set(b.x() - a.x(), b.y() - a.y(), b.z() - a.z());
			ac.set(c.x() - a.x(), c.y() - a.y(), c.z() - a.z());
			ap.set(xp - a.x(), yp - a.y(), zp - a.z());

			vectCur = MyPolyhedron.vectProduct(ab, ac);
			vectP = MyPolyhedron.vectProduct(ab, ap);
			
			if(vectCur.x() * vectP.x() < -1e-4) return false;
			if(vectCur.y() * vectP.y() < -1e-4) return false;
			if(vectCur.z() * vectP.z() < -1e-4) return false;
		}
		return true;
	}
	
	public static double sqr(double x){
		return x * x;
	}
	
}
