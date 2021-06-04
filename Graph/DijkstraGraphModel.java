package Graph;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.*;
import Exceptions.*;
import Algorithms.*;

public class DijkstraGraphModel{

	public int vertices = 0, edges = 0, totalUnNamedVertices=0;
	public Vertex[] graph;
	public ArrayList<Vertex> graphAL;
	public ArrayList<Edge> edgeListOrig;
	public boolean direct = false;
	private Vertex farthest = null;

	public DijkstraGraphModel(){
		graphAL = new ArrayList<Vertex>();
		edgeListOrig = new ArrayList<Edge>();
	}

	public void addVertex(String name, double time, int xPos, int yPos) throws VertexAlreadyExistsException{//used when adding thru application
		try{
			addV(name, time, xPos, yPos,false);
		}catch(VertexAlreadyExistsException v){throw new VertexAlreadyExistsException(v.getMessage());};
	}

	public void addVertex(String name) throws InvalidGraphException{ //used when reading from a file
		String[] vertexInput = name.split("//");//[0]=name:weight, [1]=poss,posy
		double executionTime = 0.0;
		int xPos = 0, yPos = 0;
		boolean farFound = false;

		//name and weight evaluation
		String[] vertexNameWeight = vertexInput[0].split(":");//[0]=name,[1]=weight
		String vertexName = vertexNameWeight[0];

		if(vertexNameWeight.length>1){
			try{
				executionTime = Double.parseDouble(vertexNameWeight[1]);
			}catch(NumberFormatException n){
				throw new InvalidGraphException("Graph file contains invalid execution time.");
			};				
		}

		if(vertexInput.length==2){ //has default position
			String[] pos = vertexInput[1].split(",");
			if(pos.length>1){
				try{
					xPos = Integer.parseInt(pos[0]);
					yPos = Integer.parseInt(pos[1]);
					if(farthest==null || (Math.sqrt(Math.pow(xPos,2)+Math.pow(yPos,2))>farthest.getRadius()))
						farFound = true;
				}catch(NumberFormatException n){
					throw new InvalidGraphException("Graph file contains invalid vertex position");
				};
			}
		}else if(vertexInput.length<2 && farthest==null){
			farFound = true;
		}
		try{
			addV(vertexName,executionTime,xPos,yPos,farFound);
		}catch(VertexAlreadyExistsException v){
			throw new InvalidGraphException("Graph file contains duplicate vertex");};
	}

	private void addV(String name, double time, int xPos, int yPos, boolean far) throws VertexAlreadyExistsException{ //general function for adding vertex
		if(!name.equals("")){
			if(indexRetriever(name)!=-1)
				throw new VertexAlreadyExistsException(name);
		}
		else if(name.equals("")){
			while(indexRetriever("V"+totalUnNamedVertices)!=-1)
				totalUnNamedVertices++;
		}

		if(name.equals(""))
			graphAL.add(new Vertex(totalUnNamedVertices++, xPos, yPos));
		else
			graphAL.add(new Vertex(xPos, yPos, name, time)); //add exec time soon
		if(far)
			farthest = graphAL.get(graphAL.size()-1);	

		graph = graphAL.toArray(new Vertex[graphAL.size()]);
		vertices++;
	}

	public void addEdge(String v1, String v2, String weight) throws InvalidEdgeException{ //used in adding thru application
		int i1 = indexRetriever(v1);
		int i2 = indexRetriever(v2);
		String w = weight;

		if(weight.equals(""))
			w = String.valueOf(Math.sqrt(Math.pow((graph[i1].getStartX()-graph[i2].getStartX()),2)+Math.pow((graph[i1].getStartY()-graph[i2].getStartY()),2)));

		try{
			addE(v1, v2, w);
		}catch(InvalidEdgeException ie){throw new InvalidEdgeException(ie.getMessage());}; //ignore when adding multiple edge 
	}

	public void addEdge(String edge) throws InvalidGraphException{ //used in adding thru file
		String[] edgeInput = edge.split(":"); //[0]=edge pair, [1]=weight
		String weight = "1.0";

		if(edgeInput.length>2)
			throw new InvalidGraphException("Graph file contains an invalid edge input");//error invalid graph file

		//weight evaluation
		if(edgeInput.length==2)
			weight = edgeInput[1];
			
		//edge pair evaluation
		String[] pair = edgeInput[0].split(",");

		if(pair.length!=2)
			throw new InvalidGraphException("Graph file contains an invalid edge input");//error invalid graph file

		try{
			addE(pair[0], pair[1], weight);
		}catch(InvalidEdgeException ie){
			throw new InvalidGraphException("Graph file contains an invalid edge input");//error invalid graph file
		};
	}

	private void addE(String v1,String v2, String weight)throws InvalidEdgeException{ //general edge adder
		int index1 = indexRetriever(v1);
		int index2 = indexRetriever(v2);
		double weightInt = 1.0;

		if(index1==-1 || index2==-1)
			throw new InvalidEdgeException("One or more of the vertex does not exists.");

		try{
			weightInt = Double.parseDouble(weight);
		}catch(NumberFormatException e){throw new InvalidEdgeException("Invalid Weight");};

		boolean eV1 = graph[index1].addNeighbour(v2,weightInt);
	
		if(eV1==false){
			graph[index2].addInDegree(); //adding indegree to v2 since this is directed
			if(!this.direct && !v1.equals(v2)){ // undirected graphs
				graph[index2].addNeighbour(v1,weightInt);
				graph[index1].addInDegree();	
			}
			
			edgeListOrig.add(new Edge(v1, v2, graph[index1].getStartX(), graph[index1].getStartY(), graph[index2].getStartX(), graph[index2].getStartY(), weightInt, direct)); //orig
			edges++;
		}
	}

	public void removeVertex(String vertex) throws InvalidVertexException{
		try{
			removeVertexAux(vertex);
		}catch(InvalidVertexException iv){ throw new InvalidVertexException("Invalid vertex: "+iv.getMessage());};
	}

	private void removeVertexAux(String vertex) throws InvalidVertexException{//fixed needed
		int vertexIndex = indexRetriever(vertex);
		if(vertexIndex==-1)
			throw new InvalidVertexException(vertex+" does not exist.");
			
		VNode neighbours = graph[vertexIndex].getNeighbour();

		while(neighbours!=null){ //remove outdegree edges
			removeEdgeInner(vertex, neighbours.getVertexName()); //v, n
			neighbours = neighbours.getVNode();
		}		

		for(Vertex v : graph){ // remove indegree  edges
			VNode temV = v.getNeighbour();

			while(temV!=null){ 
				if(temV.getVertexName().equals(vertex))
					removeEdgeInner(v.getVertexName(), temV.getVertexName()); //v, n
				temV = temV.getVNode();
			}
		}

		graph[vertexIndex] = null;
		shiftDown(vertexIndex);
		vertices--;
	}

	private void shiftDown(int index){
		//update mirror edges using the orig edges
		graphAL.remove(index);
		graph = graphAL.toArray(new Vertex[graphAL.size()]);
	}

	public void removeEdge(String edge)throws InvalidEdgeException{
		try{
			removeEdgeAux(edge);
		}catch(InvalidEdgeException ie){throw new InvalidEdgeException(ie.getMessage());};
	}

	private void removeEdgeAux(String edge) throws InvalidEdgeException{

		String[] vertexPair = edge.split(",");

		if(vertexPair.length!=2)
			throw new InvalidEdgeException("Input not paired properly.");

		String v1 = vertexPair[0], v2 = vertexPair[1];

		int v1I = indexRetriever(v1);
		int v2I = indexRetriever(v2);

		if(v1I==-1 || v2I==-1)
			throw new InvalidEdgeException("One or more of the vertex does not exists.");
			
		removeEdgeInner(v1,v2);
 	}

 	private void removeEdgeInner(String v1, String v2){
		int vertexIndex = indexRetriever(v1);
		VNode auxVNode = graph[vertexIndex].getNeighbour();
		VNode prev = null;

		while(auxVNode!= null){
			if(v2.equals(auxVNode.getVertexName())){
				for(int i = 0; i<edgeListOrig.size(); i++){
					if(edgeListOrig.get(i).getUniqueID().equals(v1+","+v2)){
						edgeListOrig.remove(i);
						break;
					}
				}
				graph[indexRetriever(v2)].removeInDegree();
				edges--;

				if(prev==null){//first element on the list
					graph[vertexIndex].setNeighbour(auxVNode.getVNode());
					return;
				}
				else{
					prev.setVNode(auxVNode.getVNode());
					return;
				}
			}
			prev = auxVNode;
			auxVNode = auxVNode.getVNode();
		}
	}

	public void renameVertex(String oldName, String newName)throws Exception{
		try{
			try{
				renameVertexInner(oldName, newName);
			}catch(InvalidVertexException ive){throw new Exception(ive.getMessage());};
		}catch(VertexAlreadyExistsException vae){throw new Exception(vae.getMessage());};
	}

	private void renameVertexInner(String oldName, String newName) throws VertexAlreadyExistsException, InvalidVertexException{
			if(oldName.equals(newName))
				return;

			if(newName.equals(""))
				throw new InvalidVertexException("Invalid Vertex name!");

			if(indexRetriever(newName)!=-1)
				throw new VertexAlreadyExistsException(newName);

			graphAL.get(indexRetriever(oldName)).setVertexName(newName);

			for(Vertex v : graphAL){
				VNode node = v.getNeighbour();

				while(node!=null){
					if(node.getVertexName().equals(oldName))
						node.setVertexName(newName);
					node = node.getVNode();
				}
			}
			Edge e;
			for(int i = 0; i< edgeListOrig.size(); i++){
				e = edgeListOrig.get(i);
				if((e.getv1()).equals(oldName))
					e.setv1(newName);
				if((e.getv2()).equals(oldName))
					e.setv2(newName);
			}
			graph = graphAL.toArray(new Vertex[graphAL.size()]);
	}

	public void changeWeight(String edgeName, String weight)throws Exception{
		try{
			changeWeightInner(edgeName, weight);
		}catch(InvalidEdgeException iee){throw new Exception(iee.getMessage());};
	}

	private void changeWeightInner(String edgeName, String weight) throws InvalidEdgeException{
		double weightInt = 1.0;
		try{
			weightInt = Double.parseDouble(weight);
		}catch(NumberFormatException nfe){
			throw new InvalidEdgeException("Invalid edge weight!");
		};

		int idx = edgeIndex(edgeName);
		edgeListOrig.get(idx).setWeight(weightInt);
		String[] edgePair = edgeName.split(",");
		changeEdgeWeightInNode(edgePair[0], edgePair[1], weightInt);
		if(!isDirected())
			changeEdgeWeightInNode(edgePair[1], edgePair[0], weightInt);

		//editing the inner nodes changes the state of the outer graph
		graph = graphAL.toArray(new Vertex[graphAL.size()]);
	}

	private void changeEdgeWeightInNode(String head, String tail, double weightInt){
		int idx = indexRetriever(head);
		VNode node = graphAL.get(idx).getNeighbour();
		while(node!=null){
			if(node.getVertexName().equals(tail)){
				node.setWeight(weightInt);
				break;
			}
			node = node.getVNode();
		}
	}

	public void setArbitraryPosition(String vertex, Point point){
		int idx = indexRetriever(vertex);
		graphAL.get(idx).setPoint(point);
		graph[idx].setPoint(point);
		Edge e;

		for(int i = 0; i<edgeListOrig.size(); i++){
			e = edgeListOrig.get(i); 
			if(e.getv1().equals(vertex)){
				e.setStartPoint(point);
				e.updateLine();
			}
			if(e.getv2().equals(vertex)){
				e.setEndPoint(point);
				e.updateLine();
			}
		}
	}

	public void printNeighbours(String vertex){
		try{
			printNeighboursAux(vertex);
		}catch(InvalidVertexException iv){System.out.println(iv);};
	}

	private void printNeighboursAux(String vertex) throws InvalidVertexException{
		int vertexIndex = indexRetriever(vertex);

		if(vertexIndex==-1)
			throw new InvalidVertexException("Vertex does not exists.");

		System.out.print("Vertex "+vertex+" has neighbours ");
		VNode auxVNode = graph[vertexIndex].getNeighbour();

		while(auxVNode!=null){
			System.out.print(auxVNode.getVertexName()+" ");
			auxVNode = auxVNode.getVNode();
		}
		System.out.println("");
	}

	public int isAdjacent(String edge){
		int ret=-1;
		try{
			ret = isAdjacentAux(edge);
		}catch(InvalidEdgeException ie){};
		
		return ret;
	}
 
	private int isAdjacentAux(String edge) throws InvalidEdgeException{

		String[] vertexPair = edge.split(",");

		if(vertexPair.length!=2)
			throw new InvalidEdgeException("Input not paired properly.");

		String v1 = vertexPair[0], v2 = vertexPair[1];

		int v1I = indexRetriever(v1);
		int v2I = indexRetriever(v2);

		if(v1I==-1 || v2I==-1)
			throw new InvalidEdgeException("One or more of the vertex does not exists.");

		VNode auxVNode = graph[v1I].getNeighbour();

		while(auxVNode!=null){
			int v3I = indexRetriever(auxVNode.getVertexName());
			if(v3I==v2I){
				return 1;
			}
			auxVNode = auxVNode.getVNode();
		}
		return 0;
	}

	public void positionRandomizer(Vertex v){
		int x=0, y=0;
		int far = graphAL.size()*50;
		if(farthest.getRadius()!=0)
			far = far>farthest.getRadius()?far:farthest.getRadius()+60;
		if(v.getStartX()==0 && v.getStartY()==0){
			do{
				java.util.Random rand = new java.util.Random();
				int angle = (int)rand.nextInt(91); //0 to 90 degree angle
				int radius = (int)rand.nextInt((farthest.getRadius()+far));

				x = Math.abs((int)(radius*Math.cos(angle)));
				y = Math.abs((int)(radius*Math.sin(angle)));
			}while(checkVertexIsNear(x,y,false, v.getVertexName(),15) || vertexHasOverlap(x,y));

			setArbitraryPosition(v.getVertexName(),new Point(x,y));
		}
	}

	public boolean vertexHasOverlap(int x, int y){
		if(checkVertexOverlap(x,y)==null)
			return false;
		return true;
	}

	public String checkVertexOverlap(int x, int y){
		for(Vertex v: getVertices()){
			Rectangle checkV = new Rectangle(v.getStartX()-15,v.getStartY()-15,30,30);
			if(checkV.contains(new Point(x,y)))
				return v.getVertexName();
		}
		return null;
	}

	public boolean checkVertexIsNear(int x,int y, boolean isCarrying, String selectedElement, int offset){
		if(x-20<0 || y-30<0) //beyond top and left
			return true;

		Rectangle newV = new Rectangle(x-15-offset,y-15-offset,30+2*offset,60+2*offset);
		for(Vertex v: getVertices()){
			if(isCarrying && v.getVertexName().equals(selectedElement))
				continue;

			Rectangle checkV = new Rectangle(v.getStartX()-15,v.getStartY()-15,30,30);
			if(checkV.intersects(newV))
				return true;
		}
		return false;
	}

	public boolean isConnected(String edge){
		try{
			return isConnectedAux(edge);
		}catch(InvalidEdgeException ie){};
		return false;
	}
	
	private boolean isConnectedAux(String edge) throws InvalidEdgeException{ 
		String[] vertexPair = edge.split(",");

		if(vertexPair.length!=2)
			throw new InvalidEdgeException("Input not paired properly.");

		int v1I = indexRetriever(vertexPair[0]);
		int v2I = indexRetriever(vertexPair[1]);

		if(v1I==-1 || v2I==-1)
			return false;

		return isConnected(v1I,v2I);
	}

	public String nameRetriever(int index){
		return graph[index].getVertexName();
	}

	public int indexRetriever(String name){
		for(int i=0; i<graphAL.size(); i++){
			if(name.equals(graphAL.get(i).getVertexName()))
				return i;
		}
		return -1;
	}

	public void resetActivity(){
		for(Vertex v: graphAL)
			v.setActivity("inactive");
		for(int i = 0;i<edgeListOrig.size(); i++)
			edgeListOrig.get(i).setActivity("inactive");
	}

	public int edgeIndex(String name){
		for(Edge e: edgeListOrig)
			if((e.getUniqueID()).equals(name))
				return edgeListOrig.indexOf(e);

		return -1;
	}

	private boolean isConnected(int v1I, int v2I){
		boolean[] isVisited = new boolean[vertices];
		Stack<Integer> stack = new Stack<>();
		stack.push(v1I);

		while(!stack.isEmpty()){
			VNode neighbours = graph[stack.peek()].getNeighbour();
			isVisited[stack.pop()]=true;

			while(neighbours!=null){
				int testIndex = indexRetriever(neighbours.getVertexName());

				if(testIndex==v2I)
					return true;
				if(!isVisited[testIndex])
					stack.push(testIndex);
				neighbours = neighbours.getVNode();
			}
		}
		return false;
	}

	public boolean isDirected(){
		return direct;
	}

	public void saveGraph(String path) throws Exception{
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path)));
			for(Vertex v : graphAL){
				bw.write(v.getVertexName()+":"+String.valueOf(v.getExecTime())+"//"+v.getStartX()+","+v.getStartY());
				bw.newLine();
			}
			bw.write("EDGES");
			for(Edge e : edgeListOrig){
				bw.newLine();
				bw.write(e.getv1()+","+e.getv2()+":"+String.valueOf(e.getWeight()));
			}
			bw.close();
		}catch(Exception e){throw new Exception("Error with file writing!");};
	}

	public void readGraph(File file) throws Exception{
		graphAL = new ArrayList<Vertex>();
		edgeListOrig = new ArrayList<Edge>();

		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			boolean v = false;
			String line;

			while((line = reader.readLine())!=null){
				if(line.startsWith("EDGES")){
					v = true;
					continue;
				}
				try{
					if(!v)
						addVertex(line);
					else
						addEdge(line);
				}catch(InvalidGraphException ige){
					graphAL = new ArrayList<Vertex>();
					edgeListOrig = new ArrayList<Edge>();
					throw new Exception(ige.getMessage());
				};
			}
			for(Vertex vertex :graphAL){
				positionRandomizer(vertex);
			}
		}catch(Exception f){throw new Exception("Error with file accessing or reading!");};
	}

	public void setDirected(boolean direct){
		if(this.direct!=direct){
			this.direct = direct;
			for(Edge e: edgeListOrig)
				e.updateOrientation(direct);
		}
	}

	public void generateRandom(String vSize, String eSize) throws InvalidGenerationException{
		int vCount =0, eCount = 0;
		try{
			vCount = Integer.parseInt(vSize);
			eCount = Integer.parseInt(eSize);
		}catch(NumberFormatException e){
			throw new InvalidGenerationException("Invalid input counts");
		};

		if(vCount>50 || vCount<1 || eCount<0)
			throw new InvalidGenerationException("Invalid input counts");

		ArrayList<Vertex> tempV = new ArrayList<Vertex>();

		for(int i=0; i<vCount; i++){
			tempV.add(new Vertex(0,0,"V"+i));
			vertices++;
		}

		ArrayList<Vertex[]> vPair = new ArrayList<Vertex[]>();
	
		for(int i=0; i<vCount; i++){
			for(int j=0; j<vCount; j++){
				if(!tempV.get(i).getVertexName().equals(tempV.get(j).getVertexName())){	//no self loop allowed	
					Vertex[] pair = new Vertex[2];
					pair[0] = tempV.get(i);
					pair[1] = tempV.get(j);
					vPair.add(pair);
				}
			}
		}
		graphAL = tempV;
		graph = graphAL.toArray(new Vertex[graphAL.size()]);
		farthest = graphAL.get(0);
	
		int k=0;
		while(k < eCount || k>=vPair.size()){
			boolean match = false;
			java.util.Random rand = new java.util.Random();
			int index = rand.nextInt(vPair.size());

			for(Edge e : edgeListOrig){
				if(e.getUniqueID().equals(vPair.get(index)[0].getVertexName()+","+vPair.get(index)[1].getVertexName())){
					match = true;
					break;
				}
			}

			if(!match){
				try{
					addEdge(vPair.get(index)[0].getVertexName(),vPair.get(index)[1].getVertexName(),"1.0");
					k++;
				}catch(InvalidEdgeException iee){throw new InvalidGenerationException(iee.getMessage());};
			}
		}
		
		for(Vertex v : graphAL)
			positionRandomizer(v);
	}

	public int numberOfVertices(){
		return vertices;
	}

	public int numberOfEdges(){
		return edges;
	}
 
	public DijkstraGraphModel getGraph(){
		return this;
	}

	public Vertex[] getVertexSet(){ //use clone soon
		return graph;
	}

	public ArrayList<Vertex> getVertices(){
		return graphAL;
	}

	public ArrayList<Edge> getEdges(){
		return edgeListOrig; 
	}
}
