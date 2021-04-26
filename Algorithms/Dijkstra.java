package Algorithms;

import Exceptions.*;
//import Graphs.*;
import java.util.PriorityQueue;
import java.util.Arrays;
import java.util.ArrayList;
import java.lang.StringBuilder;
import Graph.*;
import UI.*;

public class Dijkstra implements Runnable{
	
	private DijkstraGraphController g;
	private Vertex[] vertexSet;
	private VisualPanel vPanel; 
	private String track, start, end;
	private boolean[] visited;
	private double[] distance;
	private boolean all = false;

	public Dijkstra(){}

	public Dijkstra(VisualPanel vPanel, DijkstraGraphController g, String start, String end, String choice){
		this.vPanel = vPanel;
		this.g = g;
		this.start = start;
		this.end = end;
		if(choice.equals("A"))//visit all vertices
			all = true;
		else
			all = false;
	}

	public void run(){
		try{
			try{
				findPath(this.g, this.start, this.end);
			}catch(InvalidVertexException ive){new ErrorReport(ive.getMessage(), "Invalid Vertex");};
		}catch(NegativeEdgeException nee){new ErrorReport(nee.getMessage(), "Negative Edge");};
		vPanel.doneExec();
	}

	public void findPath(DijkstraGraphController g, String start, String end) throws NegativeEdgeException, InvalidVertexException{
		this.vertexSet = g.getVertexSet();

		String check = hasNegativeEdge(g.getEdges());
		if(check!=null)// maybe an input or empty set
			throw new NegativeEdgeException(check);
		if(indexRetriever(start)==-1)
			throw new InvalidVertexException(start,1);
		if((!all && indexRetriever(end)==-1))
			throw new InvalidVertexException(start,1);

		visited = new boolean[vertexSet.length];
		distance = new double[vertexSet.length];
		String[] prev = new String[vertexSet.length];
		Queue<String> queue = new Queue<>();
		int reservedInDegree = 0;
		if(!all)
			reservedInDegree = vertexSet[indexRetriever(end)].getInDegree();

		for(int i = 0; i<vertexSet.length; i++)
			distance[i] = Double.POSITIVE_INFINITY;
		distance[indexRetriever(start)] = 0;
		queue.enqueue(start);

		findPath(visited,distance, prev, queue, end);
		if(!all)
			vertexSet[indexRetriever(end)].setInDegree(reservedInDegree);
		
		String[] tracks = print(distance, prev, start);
		String[][] outputs;

		if(!all){
			outputs = new String[1][3];
			generatePath(tracks);
			int idx = indexRetriever(end);
			outputs[0][0] = vertexSet[idx].getVertexName();
			outputs[0][1] = String.valueOf(distance[idx]);
			outputs[0][2] = tracks[idx]; 
		}
		else{
			outputs = new String[vertexSet.length][3];
			for(int i=0; i<vertexSet.length; i++){
				outputs[i][0] = vertexSet[i].getVertexName();
				outputs[i][1] = String.valueOf(distance[i]);
				outputs[i][2] = tracks[i]; 
			}
		}
		vPanel.reportResult(outputs, start);
	}

	private void findPath(boolean[] visited, double[] distance, String[] prev, Queue<String> queue, String destination){
		int destIndex = indexRetriever(destination);

		while(!queue.isEmpty()){
			algoSleep("while(!queue.");
			String cur = queue.dequeue();
			g.getVertices().get(g.indexRetriever(cur)).setActivity("active");
			bothSleep("String cur ="); // current vertex evaluated

			int indexCur = indexRetriever(cur);
			algoSleep("int indexCur =");
			visited[indexCur] = true;
			algoSleep("visited[indexCur] = true;");

			VNode neighbours = vertexSet[indexCur].getNeighbour();
			algoSleep("VNode neighbours =");

			while(neighbours!=null){
				algoSleep("while(neighbours!");
				double tempDist = distance[indexCur]+neighbours.getWeight();
				algoSleep("double tempDist =");
				int neighcurIndex = indexRetriever(neighbours.getVertexName());
				
				int i = 0;
				int j = 0;

				if(g.isDirected()){
					i = g.edgeIndex(cur+","+neighbours.getVertexName());
					if(i!=-1)
						g.getEdges().get(i).setActivity("active");
				}
				else{
					i = g.edgeIndex(cur+","+neighbours.getVertexName());
					j = g.edgeIndex(neighbours.getVertexName()+","+cur);
					if(i!=-1 && j!=-1){	
						g.getEdges().get(j).setActivity("active"); 
						g.getEdges().get(i).setActivity("active");
					}
				}
				bothSleep("int neighcurIndex ="); //current edge evaluated

				if(!all && neighbours.getVertexName().equals(destination))
					vertexSet[destIndex].removeInDegree();

				if(tempDist<distance[neighcurIndex]){
					algoSleep("if(tempDist<distance[");
					distance[neighcurIndex] = tempDist;
					algoSleep("distance[neighcurIndex] = ");
					prev[neighcurIndex] = cur;
					algoSleep("prev[neighcurIndex] =");
					queue.enqueue(neighbours.getVertexName());
				}
				g.getEdges().get(i).setActivity("done");
				if(!g.isDirected())
					g.getEdges().get(j).setActivity("done");
				bothSleep("queue.enqueue(neighbours.");//vertex evaluation done
				neighbours = neighbours.getVNode();
				algoSleep("neighbours = neighbours.getVNode();");
			}
			g.getVertices().get(g.indexRetriever(cur)).setActivity("done");
			visualSleep();//vertex evaluation done
			if(!all && cur.equals(destination) 
				&& !(vertexSet[destIndex].getInDegree()>0))//stop when destination is done
				break;
		}
	}

	private void generatePath(String[] tracks){
		g.resetActivity();
		int i = indexRetriever(end);
		String track = tracks[i];
		String[] path = track.split("-");

		g.getVertices().get(g.indexRetriever(path[path.length-1])).setActivity("active");

		for(int j = path.length-2; j>=0; j--){
			boolean foundAt1 = true;

			if(j==0)
				g.getVertices().get(g.indexRetriever(path[0])).setActivity("active");
			else
				g.getVertices().get(g.indexRetriever(path[j])).setActivity("done"); //vertexc

			int idx = g.edgeIndex(path[j+1]+","+path[j]);
			if(idx==-1 && g.getGraph().isDirected())
				idx = g.edgeIndex(path[j]+","+path[j+1]);

			g.getEdges().get(idx).setActivity("done"); //edge
			if(!all && !g.getGraph().isDirected()){
				if(g.getGraph().isDirected() && foundAt1)
					g.getEdges().get(g.edgeIndex(path[j]+","+path[j+1])).setActivity("done"); 
			}
		}
	}

	private String[] print(double[] vertices, String[] prev, String tSource){
		String[] tracks = new String[vertices.length];
		//System.out.println("Vertex\t\tDistance\tPath");
		
		for(int i=0; i<vertices.length; i++){
			if(vertices[i]==Double.POSITIVE_INFINITY)
				track = "";
			else
				track = nameRetriever(i);
			backtrack(prev, tSource, prev[i]);
			//System.out.println(nameRetriever(i)+"\t\t"+vertices[i]+"\t\t"+track);
			tracks[i] = track; 
		}
		
		for(int i = 0; i<tracks.length; i++){
			String[] revTrack = tracks[i].split("-");
			String s = revTrack[revTrack.length-1];
			for(int j = revTrack.length-2; j>=0; j--)
				s +="-"+revTrack[j];
			tracks[i]=s;
		}

		return tracks;
	}

	private void backtrack(String[] prev, String tSource, String source){
		if(source==null)
			return;
		track+="-";
		if(source.equals(tSource)){
			track+=source;
			return;
		}
		else{
			track+=source;
			backtrack(prev, tSource, prev[indexRetriever(source)]);
		}
	}

	private String checkNegativeVertex(){
		String out = "null";
		for(int i = 0; i<vertexSet.length; i++){
			VNode aux = vertexSet[i].getNeighbour();

			while(aux!=null){
				if(aux.getWeight()<0)
					return nameRetriever(i)+" and "+aux.getVertexName();
				aux = aux.getVNode();
			}
		}
		return out;
	}

	private String hasNegativeEdge(ArrayList<Edge> edges){
		for(Edge e : edges)
			if(e.getWeight()<0)
				return e.getUniqueID();
		return null;
	}

	private int indexRetriever(String name){
		for(int i=0; i<vertexSet.length; i++){
			if(name.equals(vertexSet[i].getVertexName()))
				return i;
		}
		return -1;
	}

	private String nameRetriever(int index){
		return vertexSet[index].getVertexName();
	}

	private void visualSleep(){
		try{
			vPanel.refresh();
			Thread.sleep(vPanel.getSleepTime());	
		}catch(InterruptedException ie){};
	}

	private void algoSleep(String pos){
		try{
			vPanel.refreshAlgoScreen(pos, getDistances());
			Thread.sleep(vPanel.getSleepTime());
		}catch(InterruptedException ie){};
	}

	private void bothSleep(String pos){
		try{
			vPanel.refresh();
			vPanel.refreshAlgoScreen(pos, getDistances());
			Thread.sleep(vPanel.getSleepTime());
		}catch(InterruptedException ie){};
	}

	public boolean[] getVisited(){
		return this.visited;
	}

	public double[] getDistances(){
		return this.distance;
	}
}
