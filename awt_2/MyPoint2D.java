import static java.lang.Math.*;

import java.util.Locale;

public class MyPoint2D{
	double x, y;
	public MyPoint2D(double x, double y) {
		this.x = x; this.y = y;
	}
	public MyPoint2D(MyPoint2D p) {
		this.x = p.x; this.y = p.y;
	}
	void set(double x, double y) {
		this.x = x; this.y = y;
	}
	void set(MyPoint2D p) {
		this.x = p.x; this.y = p.y;
	}
	
	
	double getNorm(){
		return sqrt(sqr(x) + sqr(y));
	}
	
	/*** static ***/
	static void rotate(MyPoint2D p, double phi){
		double cos = cos(phi);
		double sin = sin(phi);
		p.set(p.x * cos - p.y * sin, p.x * sin + p.y * cos);
	}
	static void sub(MyPoint2D p, MyPoint2D sub){
		p.x -= sub.x; p.y -= sub.y;
	}
	static void add(MyPoint2D p, MyPoint2D sub){
		p.x += sub.x; p.y += sub.y;
	}
	static double dotProduct(MyPoint2D a, MyPoint2D b){
		return a.x * b.x + a.y * b.y;
	}
	static double crossProduct(MyPoint2D a, MyPoint2D b){
		return a.x * b.y - a.y * b.x;
	}
	static double crossProduct(MyPoint2D a, MyPoint2D b, MyPoint2D c){
		return crossProduct(b.x - a.x, b.y - a.y, c.x - a.x, c.y - a.y);
	}
	static double crossProduct(double ax, double ay, double bx, double by){
		return ax * by - ay * bx;
	}
	
	
	static void normolize(MyPoint2D p){
		double k = 1 / p.getNorm();
		p.x *= k;
		p.y *= k;
	}
	
	/*** others ***/
	static double sqr(double x){
		return x * x;
	}
	@Override
	public String toString() {
		return String.format(Locale.US, "%.2f %.2f", x, y);
	}
	static double dist(MyPoint2D a, MyPoint2D b){
		return sqrt(sqr(a.x - b.x) + sqr(a.y - b.y));
	}
}
