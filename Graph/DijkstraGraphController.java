package Graph;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.awt.Point;
import java.io.*;
import Exceptions.*;
import Algorithms.*;

public class DijkstraGraphController implements Graph{

	public int vertices = 0, edges = 0, totalUnNamedVertices=0;
	public ArrayList<Vertex> graphAL;
	public Vertex[] graph;
	public ArrayList<Edge> edgeList;
	public boolean direct = false;
	public boolean weight = false;

	public DijkstraGraphController(String direct, String weight){
		graphAL = new ArrayList<Vertex>();
		edgeList = new ArrayList<Edge>();
		if(direct.equals("D"))
			this.direct = true;
		if(weight.equals("W"))
			this.weight = true;
	}

	public void addVertex(String name, double time, int xPos, int yPos){//used when adding thru application
		try{
			addV(name, time, xPos, yPos);
		}catch(VertexAlreadyExistsException v){new ErrorReport(v.getMessage(),"Add Error");};
	}

	public void addVertex(String name){ //used when reading from a file
		String[] verticesName = name.split("\n");
		double executionTime = 0.0;
		String vertexName = null;
		int xPos = 0, yPos = 0;

		for(int i=0; i<verticesName.length; i++){
			vertexName = verticesName[i];
			if(verticesName[i].contains(":")){
				String[] vertexComponent = verticesName[i].split(":");// asume 4 elements, name:exec:xPos:yPos
				vertexName = vertexComponent[0];
				try{
					executionTime = Double.parseDouble(vertexComponent[1]);
				}catch(NumberFormatException n){System.out.println("Illegal execution time input at vertex "+vertexName);continue;};
				try{
					xPos = Integer.parseInt(vertexComponent[2]);
					yPos = Integer.parseInt(vertexComponent[3]);
				}catch(NumberFormatException n){System.out.println("Illegal coordinate input at vertex "+vertexName);continue;};
			}	
			try{
				addV(vertexName,executionTime,xPos,yPos);
			}catch(VertexAlreadyExistsException v){System.out.println("Vertex from file already exists on list");};
		}
	}

	private void addV(String name, double time, int xPos, int yPos) throws VertexAlreadyExistsException{ //general function for adding vertex
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
		
		graph = graphAL.toArray(new Vertex[graphAL.size()]);
		vertices++;
	}

	public void addEdge(String v1, String v2, String weight){ //used in adding thru application
		int i1 = indexRetriever(v1);
		int i2 = indexRetriever(v2);
		String w = weight;

		if(this.weight && weight.equals(""))
			w = String.valueOf(Math.sqrt(Math.pow((graph[i1].getStartX()-graph[i2].getStartX()),2)+Math.pow((graph[i1].getStartY()-graph[i2].getStartY()),2)));
		if(!this.weight)
			w = "1";
		try{
			try{
				addE(v1, v2, w);
			}catch(InvalidEdgeException ie){new ErrorReport(ie.getMessage(), "Add Error");}; //ignore when adding multiple edge 
		}catch(NumberFormatException ne){new ErrorReport("Invalid weight!", "Add Error");};
	}

	public void addEdge(String edge){ //used in adding thru file
		String[] edgesName = edge.split("\n");
		String[] edgeSet;
		String weight;

		for(int i=0; i<edgesName.length; i++){
			edgeSet = edgesName[i].split(",");

			if(edgeSet.length<2){
				System.out.println("Invalid set of edges: "+Arrays.toString(edgeSet));
				continue;
			}

			try{
				if(edgeSet[2]==null)
					weight = "1";
				else
					weight = edgeSet[2];
			}catch(Exception f){weight = "1";};

			try{
				try{
					addE(edgeSet[0], edgeSet[1], weight); //modify later that will copy from
				}catch(InvalidEdgeException ie){System.out.println("Edge from file is invalid! :"+Arrays.toString(edgeSet));};
			}catch(NumberFormatException ne){System.out.println("invalid weight at "+Arrays.toString(edgeSet));};
		}
	}

	private void addE(String v1,String v2, String weight)throws InvalidEdgeException{ //general edge adder
		int index1 = indexRetriever(v1);
		int index2 = indexRetriever(v2);
		double weightInt;

		if(index1==-1 || index2==-1)
			throw new InvalidEdgeException("One or more of the vertex does not exists.");

		try{
			weightInt = Double.parseDouble(weight);
		}catch(NumberFormatException e){throw new NumberFormatException();};

		boolean eV1 = graph[index1].addNeighbour(v2,weightInt);
	
		if(eV1==false){
			graph[index2].addInDegree(); //adding indegree to v2 since this is directed
			if(!this.direct && !v1.equals(v2)){ // undirected graphs
				graph[index2].addNeighbour(v1,weightInt);
				graph[index1].addInDegree();	
			}
			if(!this.weight){
				if(!this.direct && !v1.equals(v2))
					edgeList.add(new Edge(v2, v1, graph[index2].getStartX(), graph[index2].getStartY(), graph[index1].getStartX(), graph[index1].getStartY(), direct));
				edgeList.add(new Edge(v1, v2, graph[index1].getStartX(), graph[index1].getStartY(), graph[index2].getStartX(), graph[index2].getStartY(), direct));
			}
			else{
				if(!this.direct && !v1.equals(v2))
					edgeList.add(new Edge(v2, v1, graph[index2].getStartX(), graph[index2].getStartY(), graph[index1].getStartX(), graph[index1].getStartY(), weightInt, direct));
				edgeList.add(new Edge(v1, v2, graph[index1].getStartX(), graph[index1].getStartY(), graph[index2].getStartX(), graph[index2].getStartY(), weightInt, direct));
			}
			edges++;
		}
	}

	public void removeVertex(String vertex){
		try{
			removeVertexAux(vertex);
		}catch(InvalidVertexException iv){new ErrorReport("Invalid vertex: "+iv.getMessage(),"Remove Error");};
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
		graphAL.remove(index);
		graph = graphAL.toArray(new Vertex[graphAL.size()]);
	}

	public void removeEdge(String edge){
		try{
			removeEdgeAux(edge);
		}catch(InvalidEdgeException ie){new ErrorReport(ie.getMessage(), "Remove Error");};
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
		if(!direct)
			removeEdgeInner(v2,v1);
 	}

 	private void removeEdgeInner(String v1, String v2){
		int vertexIndex = indexRetriever(v1);
		VNode auxVNode = graph[vertexIndex].getNeighbour();
		VNode prev = null;

		while(auxVNode!= null){
			if(v2.equals(auxVNode.getVertexName())){
				for(int i = 0; i<edgeList.size(); i++){
					if(edgeList.get(i).getUniqueID().equals(v1+","+v2)){
						edgeList.remove(i);
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

	public boolean renameVertex(String oldName, String newName){
		boolean success = true;
		try{
			try{
				renameVertexInner(oldName, newName);
			}catch(InvalidVertexException ive){
				success=false;
				new ErrorReport(ive.getMessage(), "Rename Error");
			};
		}catch(VertexAlreadyExistsException vae){
			success=false;
			new ErrorReport(vae.getMessage(), "Rename Error");
		};

		return success;
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
			for(Edge e : edgeList){
				if((e.getv1()).equals(oldName))
					e.setv1(newName);
				if((e.getv2()).equals(oldName))
					e.setv2(newName);
			}
			graph = graphAL.toArray(new Vertex[graphAL.size()]);
	}

	public boolean changeWeight(String edgeName, String weight){
		boolean success = true;
		if(isWeighted()){
			try{
				changeWeightInner(edgeName, weight);
			}catch(InvalidEdgeException iee){
				success =false;
				new ErrorReport(iee.getMessage(), "Change Error");
			};
		}
		return success;
	}

	private void changeWeightInner(String edgeName, String weight) throws InvalidEdgeException{
		double weightInt = 1.0;
		try{
			weightInt = Double.parseDouble(weight);
		}catch(NumberFormatException nfe){
			throw new InvalidEdgeException("Invalid edge weight!");
		};

		int idx = edgeIndex(edgeName);
		edgeList.get(idx).setWeight(weightInt);
		String[] edgePair = edgeName.split(",");
		if(!isDirected()){
			idx = edgeIndex(edgePair[1]+","+edgePair[0]);
			edgeList.get(idx).setWeight(weightInt);
		}

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

		for(Edge e: edgeList){
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
		}catch(InvalidEdgeException ie){ie.printStackTrace();};
		
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

	public boolean isConnected(String edge){
		try{
			return isConnectedAux(edge);
		}catch(InvalidEdgeException ie){ie.printStackTrace();};
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
			if(name.equals(graph[i].getVertexName()))
				return i;
		}
		return -1;
	}

	public void resetActivity(){
		for(Vertex v: graphAL)
			v.setActivity("inactive");
		for(Edge e: edgeList)
			e.setActivity("inactive");
	}

	public int edgeIndex(String name){
		for(Edge e: edgeList)
			if((e.getUniqueID()).equals(name))
				return edgeList.indexOf(e);

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

	public boolean isWeighted(){
		return weight;
	}

	public void saveGraph(String path){
		String weight, direct;
		if(this.direct)
			direct = "D";
		else
			direct = "U";
		if(this.weight)
			weight = "W";
		else
			weight = "U";

		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path)));
			bw.write("//"+direct+weight); //change later to get from the user setting
			bw.newLine();
			for(Vertex v : graphAL){
				bw.write(v.getVertexName()+":"+String.valueOf(v.getExecTime())+":"+v.getStartX()+":"+v.getStartY());
				bw.newLine();
			}
			bw.write("EDGES");
			for(Edge e : edgeList){
				bw.newLine();
				bw.write(e.getv1()+","+e.getv2()+","+String.valueOf(e.getWeight()));
			}
			bw.close();
		}catch(Exception e){new ErrorReport("Error with file writing!","Write Error");};
	}

	public void readGraph(File file){
		//add recovery when failed reading file at catch bliock
		graphAL = new ArrayList<Vertex>();
		edgeList = new ArrayList<Edge>();
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			boolean v = false;
			String line;

			while((line = reader.readLine())!=null){
				if(line.startsWith("//")){
					if(String.valueOf(line.charAt(2)).equals("D"))
						this.direct = true;
					else
						this.direct = false;
					if(String.valueOf(line.charAt(3)).equals("W"))
						this.weight = true;
					else
						this.weight = false;
					continue;
				}
				if(line.startsWith("EDGES")){
					v = true;
					continue;
				}
				if(!v)
					addVertex(line);
				else
					addEdge(line);
			}
		}catch(Exception f){new ErrorReport("Error with file accessing or reading!","Read Error");};
	}

	public int numberOfVertices(){
		return vertices;
	}

	public int numberOfEdges(){
		return edges;
	}
 
	public DijkstraGraphController getGraph(){
		return this;
	}

	public Vertex[] getVertexSet(){ //use clone soon
		return graph;
	}

	public ArrayList<Vertex> getVertices(){
		return graphAL;
	}

	public ArrayList<Edge> getEdges(){
		return edgeList;
	}
}
