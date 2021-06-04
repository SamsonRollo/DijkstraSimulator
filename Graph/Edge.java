package Graph;

import java.util.ArrayList;
import java.awt.Point;
import java.awt.Rectangle;

public class Edge implements Comparable<Edge>{

	public int startX, startY, endX, endY;
	public double weight = 1.0;
	public String v1, v2;
	public ArrayList<Point> line;
	private ArrayList<Point> arrow;
	public String active = "inactive";
	public Rectangle rect;
	private boolean isDir = false;

	public Edge(String v1, String v2, int startX, int startY, int endX, int endY, boolean isDir){
		try{
			if(v1.equals(v2))
				createArc(startX, startY, isDir);
			else	
				createLine(startX, startY, endX, endY, isDir);
		}catch(ArithmeticException ae){};
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;	
		this.v1 = v1;
		this.v2 = v2;
		this.isDir = isDir;
	}

	public Edge(String v1, String v2, int startX, int startY, int endX, int endY, double weight, boolean isDir){
		try{
			if(v1.equals(v2))
				createArc(startX, startY, isDir);
			else
				createLine(startX, startY, endX, endY, isDir);
		}catch(ArithmeticException ae){};
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;	
		this.weight = weight;
		this.v1 = v1;
		this.v2 = v2;
		this.isDir = isDir;
	}

	public Edge(String v1, String v2, Point startPoint, Point endPoint, double weight, boolean isDir){
		try{
			if(v1.equals(v2))
				createArc((int)startPoint.getX(), (int)startPoint.getY(), isDir);
			else
				createLine((int)startPoint.getX(), (int)startPoint.getY(), (int)endPoint.getX(), (int)endPoint.getY(), isDir);
		}catch(ArithmeticException ae){};
		this.startX = (int)startPoint.getX();
		this.startY = (int)startPoint.getY();
		this.endX = (int)endPoint.getX();
		this.endY = (int)endPoint.getY();	
		this.weight = weight;
		this.v1 = v1;
		this.v2 = v2;	
		this.isDir = isDir;
	}

	private void createLine(int startX, int startY, int endX, int endY, boolean isDir) throws ArithmeticException{
		ArrayList<Point> newLine = new ArrayList<Point>();
		int start = startX<endX?startX:endX;
		int end = startX>endX?startX:endX;
		double dy = endY - startY;
		double dx = endX - startX;
		double angle = Math.atan2(dy,dx);
		Point intersection = new Point(0,0);
		Point focus = new Point(0,0);

		for(double i=start; i<=end; i+=0.1){
			double y = startY + dy * (i-startX)/dx;
			double r = Math.sqrt(Math.pow(i-endX,2)+Math.pow(y-endY,2));

			if(isDir && r>=15 && r<17)
				intersection.setLocation(i,y);	
			
			if(isDir && r>=19 && r<25)
				focus.setLocation(i,y);

			Point newPoint = new Point(0,0);
			newPoint.setLocation(i,y);
			newLine.add(newPoint);
		}
		if(isDir){
			arrow = new ArrayList<Point>();

			if(angle<1.70 && angle>1.46){ //bottom anomaly
				intersection.setLocation(endX,endY-15);
				arrow.add(new Point(endX-5,endY-20));
				arrow.add(new Point(endX+5, endY-20));
			}else if(angle>-1.70 && angle<-1.46){ //top anomaly
				intersection.setLocation(endX,endY+15);
				arrow.add(new Point(endX-5,endY+20));
				arrow.add(new Point(endX+5, endY+20));
			}else if(angle>=-3.14&& angle<=-1.7){
				arrow.add(new Point((int)focus.getX()-2,(int)focus.getY()+6));
				arrow.add(new Point((int)focus.getX()+2,(int)focus.getY()-6));
			}else if(angle<=3.14 && angle>=1.7){
				arrow.add(new Point((int)focus.getX()+2,(int)focus.getY()+6));
				arrow.add(new Point((int)focus.getX()+2,(int)focus.getY()-6));
			}else if(angle>=0 && angle<=1.46){
				arrow.add(new Point((int)focus.getX()-2,(int)focus.getY()+6));
				arrow.add(new Point((int)focus.getX()-2,(int)focus.getY()-6));
			}else if(angle<0 && angle>=-1.46){
				arrow.add(new Point((int)focus.getX()+2,(int)focus.getY()+6));
				arrow.add(new Point((int)focus.getX()-2,(int)focus.getY()-6));
			}else{
				arrow.add(new Point((int)focus.getX(),(int)focus.getY()+6));
				arrow.add(new Point((int)focus.getX(),(int)focus.getY()-6));
			}
			arrow.add(intersection);
		}

		line = newLine;
	}

	public void updateLine(){
		try{
			if(v1.equals(v2))
				createArc(startX, startY, isDir);
			else	
				createLine(startX, startY, endX, endY, isDir);
		}catch(ArithmeticException ae){};
	}

	public void updateOrientation(boolean isDir){
		if(this.isDir!=isDir){
			this.isDir = isDir;
			updateLine();
		}
	}

	public void setWeight(double weight){
		this.weight = weight;
	}

	private void createArc(int startX, int startY, boolean isDir){
		rect = new Rectangle(startX-10,startY-25,20,30);
	}

	public void setActivity(String active){
		this.active = active;
	}

	public String getActivity(){
		return active;
	}

	public String getv1(){
		return v1;
	}

	public String getv2(){
		return v2;
	}

	public void setv1(String v1){
		this.v1 = v1;
	}

	public void setv2(String v2){
		this.v2 = v2;
	}

	public void setStartPoint(Point point){
		this.startX = (int)point.getX();
		this.startY = (int)point.getY();
	}

	public void setEndPoint(Point point){
		this.endX = (int)point.getX();
		this.endY = (int)point.getY();
	}

	public Point getStartPoint(){
		return new Point(startX, startY);
	}

	public Point getEndPoint(){
		return new Point(endX, endY);
	}

	public int getStartX(){
		return startX;
	}
	public int getStartY(){
		return startY;
	}
	public int getEndX(){
		return endX;
	}
	public int getEndY(){
		return endY;
	}
	public double getWeight(){
		return weight;
	}	

	public String getUniqueID(){
		return v1+","+v2;
	}

	public ArrayList<Point> getLine(){
		return line;
	}

	public Rectangle getRectangle(){
		return rect;
	}

	public ArrayList<Point> getArrow(){
		return arrow;
	}

	@Override
	public int compareTo(Edge e){
		return this.getUniqueID().compareToIgnoreCase(e.getUniqueID());
	}
}
