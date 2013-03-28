import java.lang.Math.*;
import java.lang.management.MonitorInfo;
import java.util.*;
import java.util.Map.Entry;

import static java.lang.Math.*;
import static java.util.Arrays.fill;
import static java.util.Arrays.sort;


public class Flat2D {
	int id;
	ArrayList<MyPoint2D> p2d;
	ArrayList<Integer> order;
	public Flat2D(int id) {
		this.id = id;
		order = new ArrayList<Integer>();
		p2d = new ArrayList<MyPoint2D>(); 
	}
	void addPoint(int ind, MyPoint2D p){
		order.add(ind);
		p2d.add(p);
	}
	@Override
	public String toString() {
		return "FLAT = " + id + "\n" + order + "\n" + p2d + "\n" + getArea(this) + "\n";
	}
	public long getHash(){
		Collections.sort(order);
		return ((long) order.get(0) << 40) + ((long) order.get(1) << 20) + ((long) order.get(2));  
	}
	static double getArea(Flat2D fl){
		double ret = 0;
		for(int i = 0; i < fl.p2d.size(); i++)
			ret += MyPoint2D.crossProduct(fl.p2d.get(i), fl.p2d.get((i + 1) % fl.p2d.size()));
		return abs(ret);
	}
	static MyPoint2D getPointInside(Flat2D fl){
		ArrayList<MyPoint2D> p2d = fl.p2d;
		MyPoint2D a = p2d.get(0);
		MyPoint2D b = p2d.get(1);
		MyPoint2D c = p2d.get(2);
		return new MyPoint2D((a.x + b.x + c.x) / 3, (a.y + b.y + c.y) / 3);
	}
	public boolean inside(Projection.ResultP p) {
		for(int i = 0; i < 3; i++){
			MyPoint2D a = p2d.get(i);
			MyPoint2D b = p2d.get((i + 1) % 3);
			MyPoint2D c = p2d.get((i + 2) % 3);
			MyPoint2D ab = new MyPoint2D(b.x - a.x, b.y - a.y);
			MyPoint2D ac = new MyPoint2D(c.x - a.x, c.y - a.y);
			MyPoint2D ap = new MyPoint2D(p.x - a.x, p.y - a.y);
			double vec1 = MyPoint2D.crossProduct(ab, ac);
			double vec2 = MyPoint2D.crossProduct(ab, ap);
//			System.err.println(vec1 + " " + vec2);
			if(vec1 * vec2 < 1e-5) return false;
		}
		
		return true;
	}
}
