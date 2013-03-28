import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.lang.Math.*;
import java.lang.management.MonitorInfo;
import java.util.*;
import java.util.Map.Entry;

import com.sun.xml.internal.ws.encoding.policy.MtomPolicyMapConfigurator;

import sun.java2d.loops.FillRect;
import static java.lang.Math.*;
import static java.util.Arrays.fill;
import static java.util.Arrays.sort;


public class MyPolyhedron {
	HashMap<Integer, MyPoint3D> p3d;
	
	
	
	HashMap<Integer, Flat3D> flats;
	HashMap<Integer, ArrayList<Integer>> edges;
	
	private MyPoint3D center;
	public static final int offsetPaintX = 300;
	public static final int offsetPaintY = 300;
	public static final double minScale = 0.001;
	public static final double controlShellRange = 250;
	public static final int maxP = 250;
	double scale;
	public MyPoint3D getCenter(){
		return center;
	}
	
	static int picture[][] = new int[1000][1000];
	
	public MyPolyhedron() {
		scale = 1;
		flats = new HashMap<Integer, Flat3D>();
		center = new MyPoint3D(0, 0, 0);
		p3d = new HashMap<Integer, MyPoint3D>();
		edges = new HashMap<Integer, ArrayList<Integer>>();
	}
	public MyPolyhedron(int offestX, int offsetY) {
		scale = 1;
		flats = new HashMap<Integer, Flat3D>();
		center = new MyPoint3D(0, 0, 0);
		p3d = new HashMap<Integer, MyPoint3D>();
		edges = new HashMap<Integer, ArrayList<Integer>>();
	}
	public void addPoint(int id, double x, double y, double z){
		if(p3d.containsKey(id)) throw new RuntimeException("Id = " + id + " already exist");
		p3d.put(id, new MyPoint3D(x, y, z));
	}
	public void addEdge(int id1, int id2){
		if(!p3d.containsKey(id1)) throw new RuntimeException("Id = " + id1 + " is not exist");
		if(!p3d.containsKey(id2)) throw new RuntimeException("Id = " + id2 + " is not exist");
		addDirectEdge(id1, id2);
		addDirectEdge(id2, id1);
	}
	public void addConvexFlat(int idFlat, int ...ids){
		HashMap<Integer, MyPoint3D> fe = new HashMap<Integer, MyPoint3D>();
		int order[] = new int[ids.length];
		int pos = 0;
		for(int id : ids){
			fe.put(id, p3d.get(id));
			order[pos++] = id;
		}
//		for(int i = 0; i < pos; i++)
//			addEdge(order[i], order[i + 1] % pos);
		flats.put(idFlat, new Flat3D(idFlat, fe, order));
		
	}
	private void addDirectEdge(int id1, int id2){
		if(!edges.containsKey(id1))
			edges.put(id1, new ArrayList<Integer>());
		edges.get(id1).add(id2);
	}
	
	/*** MORIONS ***/
	public void translate(double dx, double dy, double dz){
		center.add(dx, dy, dz);
	}
	public void changeScale(double delta){
		if(scale * ( 1 + delta) < 1e-3){
			System.err.println("bad idea");
			return;
		}
		scale *= (1 + delta);
	}
	
	double getTheta(MyPoint3D p){
		return atan2(p.z(), sqrt(sqr(p.x()) + sqr(p.y())));
	}
	
	double getPhi(MyPoint3D p){
		return atan2(p.y(), p.x());
	}
	
	
	MyPoint3D translateToInternalFormat(Point2D a){
		double x = a.getX() - center.x() - offsetPaintX;
		double y = a.getY() - center.y() - offsetPaintY;
		double z = 0;
//		System.err.println("prev " + x + " " + y);
		double k = controlShellRange / sqrt(sqr(x) + sqr(y));
		if(k < 1){
			x *= k;
			y *= k;
		} else {
			z = -sqrt(sqr(controlShellRange) - sqr(x) - sqr(y));
		}
		return new MyPoint3D(x, y, z);
	}
	
	
	void rotateBetween(Point2D fromP, Point2D toP){
		MyPoint3D from = translateToInternalFormat(fromP);
		MyPoint3D to = translateToInternalFormat(toP);

		MyPoint3D normale = vectProduct(from, to);
		double alpha = asin(norm(normale) / norm(from) / norm(to));
		rotateAroundAxis(normale, alpha);
		
		
	}
	void rotateAroundAxis(MyPoint3D axis, double alpha){
		double angleOZ = -getPhi(axis);
		this.rotateOZ(angleOZ);
		MyMatrix movePhi = MyMatrix.getOZ(angleOZ);
		axis.multiple(movePhi);
		double angleOY = -atan2(axis.z(), axis.x());
		this.rotateOY(angleOY);
		MyMatrix moveThera = MyMatrix.getOY(angleOY);
		axis.multiple(moveThera);
		this.rotateOX(-alpha);
		this.rotateOY(-angleOY);
		this.rotateOZ(-angleOZ);
	}
	
	void rotateOZ(double phi){
		MyMatrix move = MyMatrix.getOZ(phi);
		for(Entry<Integer, MyPoint3D> en: p3d.entrySet())
			en.getValue().multiple(move);
	}
	void rotateOX(double phi){
		MyMatrix move = MyMatrix.getOX(phi);
		for(Entry<Integer, MyPoint3D> en: p3d.entrySet())
			en.getValue().multiple(move);
	}
	void rotateOY(double phi){
		MyMatrix move = MyMatrix.getOY(phi);
		for(Entry<Integer, MyPoint3D> en: p3d.entrySet())
			en.getValue().multiple(move);
	}
	/*************/
	
	
	void paint(Graphics2D g){
		
		

		for(Entry<Integer, Flat3D> en : flats.entrySet())
			en.getValue().reCalc();
		for(Entry<Integer, MyPoint3D> en : p3d.entrySet()){
			MyPoint3D p = en.getValue();
			double x1 = p.x();
			double y1 = p.y();
			double z1 = p.z();
			double minZ = getMinZ(x1, y1);
			if(z1 > minZ + 1e-4) continue;

			drawNonScaledNonMovedRange(x1 - 1, y1 - 1, x1 + 1, y1 + 1, g, true);
			drawNonScaledNonMovedRange(x1 - 1, y1 + 1, x1 + 1, y1 - 1, g, true);
		}
			
		ArrayList<Flat2D> proj = Projection.getProjection(p3d, flats, new HashMap<Integer, Integer>());
		for(Flat2D fl : proj){
			MyPoint2D inside = Flat2D.getPointInside(fl);
			drawFlat2D(fl, g, getVisibleFlatId(inside.x , inside.y));
		}
		
		
		
		g.setColor(Color.black);
		for(Entry<Integer, ArrayList<Integer>> en : edges.entrySet()){
			int a = en.getKey();
			for(int b : en.getValue()){
				if(a > b) continue; // not print twice
				MyPoint3D p1 = p3d.get(a);
				MyPoint3D p2 = p3d.get(b);
				
				double x1 = p1.x();
				double y1 = p1.y();
				double z1 = p1.z();
				double x2 = p2.x();
				double y2 = p2.y();
				double z2 = p2.z();
				
				boolean open = false;
				double startX = 0, startY = 0;
				int step = 15;
				double stepX = (double) (x2 - x1) / step;
				double stepY = (double) (y2 - y1) / step;
				double stepZ = (double) (z2 - z1) / step;
				
				for(int st = 0; st <= step; st++){
					double curX = x1 + st * stepX;
					double curY = y1 + st * stepY;
					double curZ = z1 + st * stepZ;
					double minZ = getMinZ(curX, curY);
//					System.err.println(st + " " + curZ + " " + minZ);
					if(curZ > minZ + 1e-4 ){
						if(open)
							drawNonScaledNonMovedRange(startX, startY, curX - stepX, curY - stepY, g, debugEdgeColor(a, b));
						open = false;
					} else {
						if(!open){
							startX = curX;
							startY = curY;
							open = true;
						}
					}
				}
				if(open)
					drawNonScaledNonMovedRange(startX, startY, x2, y2, g, debugEdgeColor(a, b));
//				g.drawLine(x1, y1, x2, y2);
			}
		}
	}
	
	boolean debugEdgeColor(int a, int b){
		return false;
//		if(a > b){
//			int c = a; a = b; b = c;
//		}
//		return a == 6 && b == 7 || a == 7 && b == 8 || a == 8 && b == 13 || a == 12 && b == 13 || a == 6 && b == 12;
//		return false;
		
	}
	void drawNonScaledNonMovedRange(double x1, double y1, double x2, double y2, Graphics2D g, boolean f){
		if(f) g.setColor(Color.red);
		else
			g.setColor(Color.black);
		int ix1 = (int)(x1 * scale + center.x() + offsetPaintX);
		int iy1 = (int)(y1 * scale + center.y() + offsetPaintY);
		
		int ix2 = (int)(x2 * scale + center.x() + offsetPaintX);
		int iy2 = (int)(y2 * scale + center.y() + offsetPaintY);
//		System.err.println(ix1 + " " + ix2 + " | " + ix2 + " " + iy2);
		g.drawLine(ix1, iy1, ix2, iy2);
		
	}
	void drawNonScaledNonMovedRange(double x1, double y1, double x2, double y2, Graphics2D g, Color f){
		g.setColor(f);
		int ix1 = (int)(x1 * scale + center.x() + offsetPaintX);
		int iy1 = (int)(y1 * scale + center.y() + offsetPaintY);
		
		int ix2 = (int)(x2 * scale + center.x() + offsetPaintX);
		int iy2 = (int)(y2 * scale + center.y() + offsetPaintY);
//		System.err.println(ix1 + " " + ix2 + " | " + ix2 + " " + iy2);
		g.drawLine(ix1, iy1, ix2, iy2);
		
	}

	static int bufPolyX[] = new int[3];
	static int bufPolyY[] = new int[3];
	
	void drawNonScaledNonMovedTriangle(ProjPoint2D p[], int idFlat, Graphics2D g){
		if(idFlat == -1) {
			g.setColor(Color.white);
		} else {
			if(idFlat == 1) 
				g.setColor(Color.gray);
			else
				g.setColor(Color.BLUE);
		}
		for(int i = 0; i < 3; i++){
			bufPolyX[i] = (int)(p[i].x * scale + center.x() + offsetPaintX);
			bufPolyY[i] = (int)(p[i].y * scale + center.y() + offsetPaintY);
		}
		g.fillPolygon(bufPolyX, bufPolyY, 3);
//		Arrays.fillR
	}
	
	int bufX[] = new int[maxP * 100];
	int bufY[] = new int[maxP * 100];
	final static Color COLORS[] = new Color[] {Color.red, Color.gray, Color.green, Color.pink, Color.yellow, Color.blue, Color.orange, Color.yellow, Color.cyan, Color.magenta}; 
	void drawFlat2D(Flat2D fl, Graphics2D g, int idVisibleFlat){
		ArrayList<MyPoint2D> ps = fl.p2d;
		int size = ps.size();
		for(int i = 0; i < size; i++){
			bufX[i] = (int)(ps.get(i).x * scale + center.x() + offsetPaintX);
			bufY[i] = (int)(ps.get(i).y * scale + center.y() + offsetPaintY);
		}
		Color color;
		if(idVisibleFlat == -1) 
			color = g.getBackground();
		else{
			color = COLORS[idVisibleFlat % COLORS.length];
//			color = new Color(140, 140, 140);
		}
		g.setColor(color);
		g.fillPolygon(bufX, bufY, size);
	}
	void drawFlat2DBorder(Flat2D fl, Graphics2D g){
		g.setColor(Color.red);
		ArrayList<MyPoint2D> ps = fl.p2d;
		int size = ps.size();
		for(int i = 0; i < size; i++){
			MyPoint2D a = ps.get(i);
			MyPoint2D b = ps.get((i + 1) % size);
			drawNonScaledNonMovedRange(a.x, a.y, b.x, b.y, g, Color.red);
		}
	}
	
	double getMinZ(double x, double y){
		double minZ = Double.MAX_VALUE;
		for(Entry<Integer, Flat3D> en : flats.entrySet()){
			Flat3D cfl = en.getValue();
			minZ = min(minZ, cfl.getZIts(x, y));
		}
		return minZ;
	}
	
	int getVisibleFlatId(double x, double y){
		double minZ = Double.MAX_VALUE;
		int ret = -1;
		for(Entry<Integer, Flat3D> en : flats.entrySet()){
			Flat3D cfl = en.getValue();
			double z = cfl.getZIts(x, y);
			if(minZ > z){
				minZ = z;
				ret = cfl.id;
			}
		}
		return ret;
	}
	
	/*** others ***/
	public static double sqr(double x){
		return x * x;
	}
	public static double norm(MyPoint3D p){
		return sqrt(sqr(p.x()) + sqr(p.y()) + sqr(p.z()));
	}
	public static MyPoint3D vectProduct(MyPoint3D a, MyPoint3D b){
		return new MyPoint3D(a.y() * b.z() - a.z() * b.y(), 
							-a.x() * b.z() + a.z() * b.x(),
							a.x() * b.y() - a.y() * b.x()
				);
	}
	
}
