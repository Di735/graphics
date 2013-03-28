import static java.lang.Math.*;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeMap;


import static java.util.Arrays.sort;
import static java.util.Arrays.fill;


public class Projection {

	static ProjPoint2D buf[] = new ProjPoint2D[MyPolyhedron.maxP];
	static final double EPS = 1e-6;
	static Range rangesPre[] = new Range[10000];
	static Range ranges[] = new Range[10000];
	static ProjPoint2D projes[] = new ProjPoint2D[10000];
	static int lastId = 0;
	
	static long getHash(int id1, int id2){
		return ((long)id1 << 30) + id2;
	}
	static long getHash(int ... id){
		sort(id);
		return ((long)id[0] << 40) + ((long)id[1] << 20) + id[2];
	}
	
	static HashMap<Integer, HashSet<Integer>> addedEdge;
	static void addEdge(int a, int b){
		if(!addedEdge.containsKey(a))
			addedEdge.put(a, new HashSet<Integer>());
		addedEdge.get(a).add(b);
		
		if(!addedEdge.containsKey(b))
			addedEdge.put(b, new HashSet<Integer>());
		addedEdge.get(b).add(a);
	}
	static boolean existsEdge(int a, int b){
		if(!addedEdge.containsKey(a) || !addedEdge.get(a).contains(b))
			return false;
		return true;
	}
	
	static boolean interest(int... id){
		sort(id);
		return id[0] == 2 && id[1] == 3 && id[2] == 6;
	}
	static public ArrayList<Flat2D> getProjection(HashMap<Integer, MyPoint3D> basic, HashMap<Integer, Flat3D> flats,
			HashMap<Integer, Integer> reId) {
		addedEdge = new HashMap<Integer, HashSet<Integer>>();
		HashMap<Integer, ProjPoint2D> remCloseP = new HashMap<Integer, ProjPoint2D>();
		TreeSet<ProjPoint2D> noCloseP = new TreeSet<ProjPoint2D>();
		lastId = 0;
		if(buf[0] == null){
			for(int i = 0; i < buf.length; i++)
				buf[i] = new ProjPoint2D(0, 0, 0);
			for(int i = 0; i < rangesPre.length; i++)
				rangesPre[i] = new Range();

			for(int i = 0; i < ranges.length; i++)
				ranges[i] = new Range();
		}
		int cnt = 0;
		for(Entry<Integer, MyPoint3D> en : basic.entrySet()){
			int id = en.getKey();
			MyPoint3D p3 = en.getValue();
			buf[cnt++].set(id, p3.x(), p3.y());
		}
		sort(buf, 0, cnt);
		for(int i = 0; i < cnt; i++){
			if(i != 0 && MyPoint2D.dist(buf[i], buf[i - 1]) < EPS){
				int prev = reId.get(buf[i - 1].id);
				reId.put(buf[i].id, prev);
			} else { 
				reId.put(buf[i].id, buf[i].id);
				remCloseP.put(buf[i].id, buf[i]);
				noCloseP.add(buf[i]);
			}
		}

		int cntRPre = 0;
		int cntR = 0;
		for(Entry<Integer, Flat3D> en : flats.entrySet()){
			int fid = en.getKey();
			Flat3D flat = en.getValue();
			for(int i = 0; i < flat.order.length; i++){
				int id1 = reId.get(flat.order[i]);
				int id2 = reId.get(flat.order[(i + 1) % flat.order.length]);
				if(fid != flat.id) throw new RuntimeException();
				ProjPoint2D p1 = remCloseP.get(id1); p1.addFlat(fid);
				ProjPoint2D p2 = remCloseP.get(id2); p2.addFlat(fid);
				lastId = max(lastId, id1);
				lastId = max(lastId, id2);
				if(id1 == id2) continue;
				rangesPre[cntRPre++].set(id1, p1, id2, p2);
			}
		}
		lastId++;
		
		for(int i = 0; i < cntRPre; i++)
			for(int j = i + 1; j < cntRPre; j++){
				ProjPoint2D p = itsINSIDE(rangesPre[i], rangesPre[j]);
				if(p != null){
//					System.err.println("new point " + p);
					ProjPoint2D hi = noCloseP.higher(p);
					if(hi != null && MyPoint2D.dist(hi, p) < EPS && abs(hi.x - p.x) < EPS && abs(hi.y - p.y) < EPS){
//						System.err.println("it's good 1" + p + " " + hi);
						p = hi;
					}
					ProjPoint2D lo = noCloseP.lower(p);
					if(lo != null && MyPoint2D.dist(lo, p) < EPS && abs(lo.x - p.x) < EPS && abs(lo.y - p.y) < EPS){
//						System.err.println("it's good 2" + p + " " + lo);
						p = lo;
					}
					
					rangesPre[i].addPoint(p.id, p);
					rangesPre[j].addPoint(p.id, p);
					if(!noCloseP.contains(p)){
						noCloseP.add(p);
						remCloseP.put(p.id, p);
					}
//					System.err.println("add its ");
				}
			}
//		System.err.println("RESULT = " + remCloseP.size() + " || " + noCloseP.size());
		
		HashMap<Integer, ResultP> result = new HashMap<Integer, ResultP>();
		ArrayList<Integer> edgeA = new ArrayList<Integer>();
		ArrayList<Integer> edgeB = new ArrayList<Integer>();
		for(int i = 0; i < cntRPre; i++){
			Range r = rangesPre[i];
			ArrayList<PointOnRange> pnp = r.points;
			Collections.sort(pnp);
			for(int j = 1; j < pnp.size(); j++){
				ProjPoint2D ap = remCloseP.get(pnp.get(j - 1).id);
				ProjPoint2D bp = remCloseP.get(pnp.get(j).id);
//				System.err.println("add edge " + ap + " " + bp);
				if(!result.containsKey(ap.id))
					result.put(ap.id, new ResultP(ap.id, ap));

				if(!result.containsKey(bp.id))
					result.put(bp.id, new ResultP(bp.id, bp));
				if(ap.id == bp.id) continue;
				ResultP a = result.get(ap.id);
				ResultP b = result.get(bp.id);
//				System.err.println(a + " ! " + b);
				a.addNeight(b);
				b.addNeight(a);
				edgeA.add(a.id);
				edgeB.add(b.id);
				ranges[cntR++].set(ap.id, ap, bp.id, bp);
//				edgeA.add(b.id);
//				edgeB.add(a.id);
			}
		}
		
		
		HashSet<Long> wasFlat = new HashSet<Long>();
		
		int fId = 0;
		ArrayList<Flat2D> ans = new ArrayList<Flat2D>();
		
		for(int i = 0; i < cntR ; i++){
			Range r = ranges[i];
//			System.err.println();
			MyPoint2D mp2d = new MyPoint2D((r.p0.x + r.p1.x) / 2, (r.p0.y + r.p1.y) / 2);
//			System.err.println(r.id0 + " ! " + r.id1);
			ResultP p = new ResultP(lastId, mp2d);
			result.put(lastId, p);
			lastId++;
		}
		
		for(int ridA = 0; ridA < cntR; ridA++){
			
			Range ra = ranges[ridA];
			int pa = ra.id0;
			int pb = ra.id1;
			{
				ResultP b = result.get(pb);
//					System.err.println("START 1 " + pa + " " + pb + " " + b.neigh);
				for(NeighbourPoint np = b.neigh.first(); np != null; np = b.neigh.higher(np)){
//					System.err.println("cnt1 = " + ans.size());
					int pc = np.id;
					if(pa == pc) continue;
//					if(interest(pa, pb, pc))
//						System.err.println("try1 " + pa + " " + pb + " " + pc);
					if(wasFlat.contains(getHash(pa, pb, pc))){
//						System.err.println("was " + getHash(pa, pb, pc));
						continue;
					}
//					System.err.println("addH " + getHash(pa, pb, pc));
					wasFlat.add(getHash(pa, pb, pc));
//					System.err.println("start " + pa + " " + pb + " " + pc);
//					if(!existsEdge(pa, pc)) continue;
					Flat2D curF = new Flat2D(fId++);
					curF.addPoint(pa, remCloseP.get(pa));
					curF.addPoint(pb, remCloseP.get(pb));
					curF.addPoint(pc, remCloseP.get(pc));
					if(Flat2D.getArea(curF) < 1e-8) continue;
					Range newR = new Range();
					newR.set(pa, remCloseP.get(pa), pc, remCloseP.get(pc));
					boolean ok = true;
					//check no intersect;
					for(int i = 0; i < cntR ; i++){
						ProjPoint2D p;
						if((p = itsINSIDE(ranges[i], newR)) != null){
							ok = false;
							break;
						}
					}
					//check no point inside
					if(ok)
						for(Entry<Integer, ResultP> pen : result.entrySet()){
							if(curF.inside(pen.getValue())){
								ok = false;
								break;
							}
						}
					// �������� ���������� �� �����, �.�. wasTree ��������� ����������� 3 ����� ������ ���
					if(ok){
//						System.err.println("add 1" + curF.order);
//						System.err.println(curF.p2d);
						ans.add(curF);
						ranges[cntR++] = newR;
						result.put(lastId, new ResultP(lastId, Flat2D.getPointInside(curF)));
						lastId++;
						ResultP a = result.get(pa);
						ResultP c = result.get(pc);
						a.neigh.add(new NeighbourPoint(c, a));
						c.neigh.add(new NeighbourPoint(a, c));
					}
					else
						fId--;
				}
			}
			// TWISE, swap a, b
			int d = pa;
			pa = pb;
			pb = d;
			{
				ResultP b = result.get(pb);
//				System.err.println("START 2 " + pa + " " + pb + " " + b.neigh);
				for(NeighbourPoint np = b.neigh.first(); np != null; np = b.neigh.higher(np)){
//					System.err.println("cnt2 = " + ans.size() + " | " + b.neigh.size() + " | " + np);
//					System.err.println(b.neigh);
					int pc = np.id;
					if(pa == pc) continue;
//					if(interest(pa, pb, pc))
//						System.err.println("try2 " + pa + " " + pb + " " + pc);
					if(wasFlat.contains(getHash(pa, pb, pc))){ 
//						System.err.println("was " + getHash(pa, pb, pc));
						continue;
					}
//					System.err.println("addH " + getHash(pa, pb, pc));
					wasFlat.add(getHash(pa, pb, pc));
//					if(!existsEdge(pa, pc)) continue;
					Flat2D curF = new Flat2D(fId++);
					curF.addPoint(pa, remCloseP.get(pa));
					curF.addPoint(pb, remCloseP.get(pb));
					curF.addPoint(pc, remCloseP.get(pc));
					if(Flat2D.getArea(curF) < 1e-8) continue;
					Range newR = new Range();
					newR.set(pa, remCloseP.get(pa), pc, remCloseP.get(pc));
					boolean ok = true;
					//check no intersect;
					for(int i = 0; i < cntR ; i++){
						ProjPoint2D p;
						if((p = itsINSIDE(ranges[i], newR)) != null){
//							System.err.println(p);
//							System.err.println(newR.p0 + " " + newR.p1);
							ok = false;
//							System.err.println("failed its");
//							System.err.println(curF.p2d);
//							System.err.println("with " + ranges[i].p0 + " " + ranges[i].p1);
							break;
						}
					}
					//check no point inside
					if(ok)
						for(Entry<Integer, ResultP> pen : result.entrySet()){
							if(curF.inside(pen.getValue())){
								ok = false;
//								System.err.println("failed inside");
								break;
							}
//							System.err.println("check " + pen.getKey());
						}
					// �������� ���������� �� �����, �.�. wasTree ��������� ����������� 3 ����� ������ ���
					if(ok){
						ans.add(curF);
//						System.err.println("add 2" + curF.order);
//						System.err.println(curF.p2d);
						ranges[cntR++] = newR;
						result.put(lastId, new ResultP(lastId, Flat2D.getPointInside(curF)));
						lastId++;
						ResultP a = result.get(pa);
						ResultP c = result.get(pc);
						a.neigh.add(new NeighbourPoint(c, a));
						c.neigh.add(new NeighbourPoint(a, c));
					}
					else
						fId--;
				}
			}
			
		}
		
		
		return ans;
		
	}
	

	static double test(ResultP prev, ResultP cur, HashMap<Integer, ResultP> result, ArrayList<Integer> order, boolean rotation, HashSet<Long> wasEdge){
		HashSet<Integer> used = new HashSet<Integer>();
		
		System.err.println("start loop TEST" + prev.id);
//		System.err.println(prev.id + " " + cur.id);
		Flat2D curF = new Flat2D(-1);
		used.add(prev.id);
		System.err.println("test " + prev.id);
		order.add(prev.id);
		order.add(cur.id);
		curF.addPoint(0, prev);
		
		while(!used.contains(cur.id)){
			curF.addPoint(0, cur);
			System.err.println("test " + cur.id);
			used.add(cur.id);
			int nextId = (rotation) ? cur.getNextId(prev) : cur.getPrevId(prev);
			if(wasEdge.contains(getHash(cur.id, nextId)) && wasEdge.contains(getHash(nextId, cur.id)))
				return Double.MAX_VALUE;
			System.err.println("ne = " + nextId);
			if(wasEdge.contains(getHash(cur.id, nextId)) && wasEdge.contains(getHash(nextId, cur.id))) throw new RuntimeException();
			prev = cur;
			cur = result.get(nextId);
			order.add(cur.id);
		}
		
		
		System.err.println("ord = " + order);
		return  Flat2D.getArea(curF);
	}
	
	static class PointOnRange implements Comparable<PointOnRange>{
		int id;
		HashSet<Integer> convexFlatIds;
		
		double t;
		
		public PointOnRange(int id, double t) {
			this.id = id;
			this.t = t;
		}
		void addAllFlats(HashSet<Integer> add){
			convexFlatIds.addAll(add);
		}
		void addFlat(int add){
			convexFlatIds.add(add);
		}
		@Override
		public int compareTo(PointOnRange e) {
			if(abs(t - e.t) > EPS) return t > e.t ? 1 : -1;
			return e.id - id;
		}
		@Override
		public String toString() {
			return String.format("%.2f ", t);
		}
	}
	
	static class ResultP extends MyPoint2D{
		int id;
		HashSet<Integer> convexFlatIds;
		TreeSet<NeighbourPoint> neigh;
		
		public ResultP(int id, MyPoint2D p) {
			super(p);
			this.id = id;
			neigh = new TreeSet<NeighbourPoint>();
		}
		void addAllFlats(HashSet<Integer> add){
			convexFlatIds.addAll(add);
		}
		void addFlat(int add){
			convexFlatIds.add(add);
		}
		void addNeight(ResultP b){
			neigh.add(new NeighbourPoint(b, this));
		}
		int getNextId(ResultP prev){
			NeighbourPoint pnp = new NeighbourPoint(prev, this);
			NeighbourPoint ret = neigh.higher(pnp);
			if(ret == null){
//				System.err.println("OKKKKKKKKKKKK1");
				ret = neigh.first();
			}
			return ret.id;
		}
		int getPrevId(ResultP prev){
			NeighbourPoint pnp = new NeighbourPoint(prev, this);
			NeighbourPoint ret = neigh.lower(pnp);
			if(ret == null){
//				System.err.println("OKKKKKKKKKKKK2");
				ret = neigh.last();
			}
			return ret.id;
		}
		
	}
	static class NeighbourPoint implements Comparable<NeighbourPoint>{
		double alpha;
		double dist;
		int id;
		public NeighbourPoint(ResultP a, ResultP main) {
			this.id = a.id;
			alpha = atan2(a.y - main.y, a.x - main.x);
			dist = MyPoint2D.dist(a, main);
		}
		@Override
		public int compareTo(NeighbourPoint e) {
			if(e.id == id) return 0;
			if(abs(alpha - e.alpha) > EPS) 
				return alpha > e.alpha ? 1 : -1;
			if(abs(dist - e.dist) > EPS) 
				return dist > e.dist ? 1 : -1;
			return 0;
		}
		@Override
		public String toString() {
			return "|" + id + "|";
		}
	}
	
	static class Range{
		ArrayList<PointOnRange> points;
		MyPoint2D p0, p1;
		int id0, id1;
		MyPoint2D vec;
		double maxT;
		HashSet<Integer> convexFlatIds;
		public Range() {
			points = new ArrayList<PointOnRange>();
			convexFlatIds = new HashSet<Integer>();
			p0 = new MyPoint2D(0, 0);
			p1 = new MyPoint2D(0, 0);
			vec = new MyPoint2D(0, 0);
		}
		void set(int idA, ProjPoint2D a, int idB, ProjPoint2D b){
			points.clear();
			convexFlatIds.clear();
			if(a.compareTo(b) > 0){
				ProjPoint2D c = a;
				a = b;
				b = c;
				int e = idA;
				idA = idB;
				idB = e;
			}
			points.add(new PointOnRange(idA, 0));
			points.add(new PointOnRange(idB, MyPoint2D.dist(p0, p1)));
			id0 = idA;
			id1 = idB;
			p0.set(a);
			p1.set(b);
			vec.set(b.x - a.x, b.y - a.y);
			MyPoint2D.normolize(vec);
			convexFlatIds.addAll(a.convexFlatIds);
			convexFlatIds.addAll(b.convexFlatIds);
			maxT = MyPoint2D.dist(a, b) + EPS;
		}
		void addPoint(int idP, MyPoint2D p){
			points.add(new PointOnRange(idP, MyPoint2D.dist(p0, p)));
		}
	}
	
	public static ProjPoint2D its(Range r1, Range r2){
		double det = -MyPoint2D.crossProduct(r1.vec.x, r2.vec.x, r1.vec.y, r2.vec.y);
		if(abs(det) < EPS)
			return null;
		double t1  = -MyPoint2D.crossProduct(r2.p0.x - r1.p0.x, r2.vec.x, r2.p0.y - r1.p0.y, r2.vec.y) / det;
		double t2  =  MyPoint2D.crossProduct(r1.vec.x, r2.p0.x - r1.p0.x, r1.vec.y, r2.p0.y - r1.p0.y) / det; 
		if(t1 < -EPS || t1 > r1.maxT + EPS) return null;
		if(t2 < -EPS || t2 > r2.maxT + EPS) return null;
		ProjPoint2D p1 = new ProjPoint2D(lastId++, r1.p0.x + r1.vec.x * t1, r1.p0.y + r1.vec.y * t1);
		ProjPoint2D p2 = new ProjPoint2D(0, r2.p0.x + r2.vec.x * t2, r2.p0.y + r2.vec.y * t2);
//		System.err.println(t1 + " p1 = " + p1 + " | " + r1.maxT);
//		System.err.println(t2 + " p2 = " + p2 + " | " + r2.maxT);
		if(MyPoint2D.dist(p1, p2) > EPS) throw new RuntimeException();
		
		return p1;
	}
	
	public static ProjPoint2D itsINSIDE(Range r1, Range r2){
		double det = -MyPoint2D.crossProduct(r1.vec.x, r2.vec.x, r1.vec.y, r2.vec.y);
		if(abs(det) < EPS)
			return null;
		double t1  = -MyPoint2D.crossProduct(r2.p0.x - r1.p0.x, r2.vec.x, r2.p0.y - r1.p0.y, r2.vec.y) / det;
		double t2  =  MyPoint2D.crossProduct(r1.vec.x, r2.p0.x - r1.p0.x, r1.vec.y, r2.p0.y - r1.p0.y) / det; 
		if(t1 < 1e-3 || t1 > r1.maxT - 1e-3) return null;
		if(t2 < 1e-3 || t2 > r2.maxT - 1e-3) return null;
		ProjPoint2D p1 = new ProjPoint2D(lastId++, r1.p0.x + r1.vec.x * t1, r1.p0.y + r1.vec.y * t1);
		ProjPoint2D p2 = new ProjPoint2D(0, r2.p0.x + r2.vec.x * t2, r2.p0.y + r2.vec.y * t2);
//		System.err.println(t1 + " p1 = " + p1 + " | " + r1.maxT);
//		System.err.println(t2 + " p2 = " + p2 + " | " + r2.maxT);
//		System.err.println(p1);
		p1.convexFlatIds.addAll(r1.convexFlatIds);
		p1.convexFlatIds.addAll(r2.convexFlatIds);
		if(MyPoint2D.dist(p1, p2) > EPS) throw new RuntimeException();
		
		return p1;
	}
}
