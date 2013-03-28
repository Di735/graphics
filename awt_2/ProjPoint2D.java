import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ProjPoint2D extends MyPoint2D implements Comparable<ProjPoint2D>{
	final static double EPS = 1e-6;
	int id;
	double alpha;
	HashSet<Integer> convexFlatIds;
	public ProjPoint2D(int id, double x, double y) {
		super(x, y);
		this.id = id;
		alpha = atan2(y, x);
		convexFlatIds = new HashSet<Integer>();
	}
	public ProjPoint2D(int id, MyPoint2D p) {
		super(p);
		this.id = id;
		alpha = atan2(y, x);
		convexFlatIds = new HashSet<Integer>();
	}
	public ProjPoint2D(ProjPoint2D p) {
		super(p.x, p.y);
		this.id = p.id;
		convexFlatIds.clear();
		convexFlatIds.addAll(p.convexFlatIds);
		alpha = atan2(y, x);
	}
	void addFlat(int idF){
			convexFlatIds.add(idF);
	}
	void set(int id, double x, double y){
		convexFlatIds.clear();
		super.set(x, y);
		this.id = id;
		alpha = atan2(y, x);
	}
	@Override
	public int compareTo(ProjPoint2D p) {
		double cNorm = getNorm();
		double pNorm = p.getNorm();
		if(abs(cNorm - pNorm) > EPS) return cNorm > pNorm ? 1 : -1;
		if(abs(alpha - p.alpha) > EPS) return alpha > p.alpha ? 1 : -1;
		return id - p.id;
	}
	@Override
	public String toString() {
		return super.toString() + "| " + alpha + "|";
	}
	
}
