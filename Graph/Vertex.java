package Graph;

import java.awt.Point;

public class Vertex{
	
	VNode neighbours = null;
	double executionTime = 0.0; //time within the vertex
	public String name = "V";
	public int inDegree = 0;
	public int xPos = 0;
	public int yPos = 0;
	public int size = 30;
	public String active = "inactive";

	public Vertex(){}

	public Vertex(int vertexId, int xPos, int yPos){
		this.xPos = xPos;
		this.yPos = yPos;
		name += String.valueOf(vertexId);
	}

	public Vertex(int xPos, int yPos){
		this.xPos = xPos;
		this.yPos = yPos;
		name += String.valueOf(xPos)+String.valueOf(yPos);
	}

	public Vertex(int xPos, int yPos, String name){
		this.xPos = xPos;
		this.yPos = yPos;
		this.name = name;
	}

	public Vertex(int xPos, int yPos, String name, double time){
		this.xPos = xPos;
		this.yPos = yPos;
		this.name = name;
		this.executionTime = time;
	}

	public void setPos(int xPos, int yPos){
		this.xPos = xPos;
		this.yPos = yPos;
	}

	public void setSize(int size){
		this.size = size;
	}

	public void setActivity(String active){
		this.active = active;
	}

	public String getActivity(){
		return active;
	}

	public int getStartX(){
		return xPos;
	}

	public int getStartY(){
		return yPos;
	}

	public int getEndX(){
		return xPos+size;
	}

	public int getEndY(){
		return yPos+size;
	}

	public Point getPoint(){
		return new Point(xPos, yPos);
	}

	public void setPoint(Point point){
		this.xPos = (int)point.getX();
		this.yPos = (int)point.getY();
	}

	public void setVertexName(String name){
		this.name = name;
	}

	public void setNeighbour(VNode neighbours){
		this.neighbours = neighbours;
	}

	public void setExecTime(double d){
		executionTime = d;
	}

	public void setInDegree(int degrees){
		this.inDegree = degrees;
	}

	public void addInDegree(){
		inDegree++;
	}

	public void removeInDegree(){
		if(inDegree>0)
			inDegree--;
	}

	public int getInDegree(){
		return inDegree;
	}

	public String getVertexName(){
		return name;
	}

	public double getExecTime(){
		return executionTime;
	}

	public VNode getNeighbour(){
		return neighbours;
	}

	public boolean addNeighbour(String neighbourName, double weight){ 
		//check if neighbor is valid and already exists
		if(neighbours==null){
			neighbours = new VNode(neighbourName, null, weight);
			return false;
		}

		VNode auxVNode = neighbours;

		while(auxVNode!=null){
			if(auxVNode.getVertexName()==neighbourName)
				return true;
			auxVNode = auxVNode.getVNode();
		}
		
		VNode newVNode = new VNode(neighbourName, null, weight);
		arrangeNeighbours(newVNode);
		return false;
	}

	private void arrangeNeighbours(VNode newVNode){//remake fast sort algo
		VNode auxVNode = neighbours;
		VNode prev = null;

		if(newVNode.getWeight()==0){
			newVNode.setVNode(neighbours);
			neighbours = newVNode;
			return;
		}

		while(auxVNode!=null){
			if(newVNode.getWeight()<auxVNode.getWeight()){
				if(prev==null){
					newVNode.setVNode(neighbours);
					neighbours = newVNode;
					return;
				}
				newVNode.setVNode(auxVNode);
				prev.setVNode(newVNode);
				return;
			}
			prev = auxVNode;
			auxVNode = auxVNode.getVNode();
		}
		prev.setVNode(newVNode); //capture
	}
}
