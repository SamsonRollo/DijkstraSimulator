package Algorithms;

import Exceptions.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.lang.StringBuilder;
import Graph.*;
import UI.VisualPanel;

public class Dijkstra implements Runnable{
	
	private DijkstraGraphController controller;
	private ArrayList<Vertex> vertexSet;
	private VisualPanel vPanel; 
	private String track, start, end;
	private double[] distance;
	private boolean all = false;

	public Dijkstra(){}

	public Dijkstra(VisualPanel vPanel, DijkstraGraphController controller, String start, String end, String choice){
		this.vPanel = vPanel;
		this.controller = controller;
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
				findPath(this.controller, this.start, this.end);
			}catch(InvalidVertexException ive){new ErrorReport(ive.getMessage(), "Invalid Vertex");};
		}catch(NegativeEdgeException nee){new ErrorReport(nee.getMessage(), "Negative Edge");};
		vPanel.doneExec();
	}

	public void findPath(DijkstraGraphController controller, String start, String end) throws NegativeEdgeException, InvalidVertexException{
		this.vertexSet = controller.getVertices();
		int length = vertexSet.size();
		String check = hasNegativeEdge(controller.getEdges());

		if(check!=null)// maybe an input or empty set
			throw new NegativeEdgeException(check);
		if(indexRetriever(start)==-1)
			throw new InvalidVertexException(start,1);
		if((!all && indexRetriever(end)==-1))
			throw new InvalidVertexException(start,1);

		distance = new double[length];

		String[] prev = new String[length];
		VertexMinQueue<String> queue = new VertexMinQueue<String>(controller, distance);
		int reservedInDegree = 0;
		
		if(!all)
			reservedInDegree = vertexSet.get(indexRetriever(end)).getInDegree();

		for(int i = 0; i<length; i++)
			distance[i] = Double.POSITIVE_INFINITY;

		bothSleep("Initialize"); //sleep
		algoSleep("Create an"); //sleep
		
		distance[indexRetriever(start)] = 0;
		queue.enqueue(start);
		algoSleep("Insert source");

		findPath(distance, prev, queue, end);
		if(!all)
			vertexSet.get(indexRetriever(end)).setInDegree(reservedInDegree);
		
		String[] tracks = print(distance, prev, start);
		String[][] outputs;

		if(!all){
			outputs = new String[1][3];
			generatePath(tracks);
			int idx = indexRetriever(end);
			outputs[0][0] = vertexSet.get(idx).getVertexName();
			outputs[0][1] = String.valueOf(distance[idx]);
			outputs[0][2] = tracks[idx]; 
		}
		else{
			outputs = new String[length][3];
			for(int i=0; i<length; i++){
				outputs[i][0] = vertexSet.get(i).getVertexName();
				outputs[i][1] = String.valueOf(distance[i]);
				outputs[i][2] = tracks[i]; 
			}
		}
		vPanel.reportResult(outputs, start);
	}

	private void findPath(double[] distance, String[] prev, VertexMinQueue<String> queue, String destination){
		int destIndex = indexRetriever(destination);

		while(!queue.isEmpty()){
			algoSleep("While Q");
			String cur = queue.dequeue();
			int indexCur = indexRetriever(cur);
			VNode neighbours = vertexSet.get(indexCur).getNeighbour();
			controller.getVertices().get(indexCur).setActivity("active");
			bothSleep("Retrieve vertex;");

			while(neighbours!=null){
				algoSleep("Visit every");
				double tempDist = distance[indexCur]+neighbours.getWeight();
				int neighcurIndex = indexRetriever(neighbours.getVertexName());
				int i = controller.edgeIndex(cur+","+neighbours.getVertexName());

				if(!controller.isDirected() || (controller.isDirected() && i!=-1)){
					if(i==-1)
						i = controller.edgeIndex(neighbours.getVertexName()+","+cur);
					if(i!=-1)
						controller.getEdges().get(i).setActivity("active");
					bothSleep("Visit every"); //current edge evaluated

					if(!all && neighbours.getVertexName().equals(destination))
						vertexSet.get(destIndex).removeInDegree();

					if(tempDist<distance[neighcurIndex]){
						algoSleep("if distance of v");
						distance[neighcurIndex] = tempDist;
						prev[neighcurIndex] = cur;
						algoSleep("Update distance");
						queue.enqueue(neighbours.getVertexName());
						algoSleep("Add vertex");
					}
					if(i!=-1)
						controller.getEdges().get(i).setActivity("done");
					visualSleep();//vertex evaluation done
				}
				neighbours = neighbours.getVNode();
			}
			controller.getVertices().get(controller.indexRetriever(cur)).setActivity("done");
			visualSleep();//vertex evaluation done
			if(!all && cur.equals(destination) 
				&& !(vertexSet.get(destIndex).getInDegree()>0))//stop when destination is done
				break;
		}
	}

	private void generatePath(String[] tracks){
		controller.resetActivity();
		int i = indexRetriever(end);
		String track = tracks[i];
		String[] path = track.split("-");

		controller.getVertices().get(controller.indexRetriever(path[path.length-1])).setActivity("active");

		for(int j = path.length-2; j>=0; j--){
			boolean foundAt1 = true;

			if(j==0)
				controller.getVertices().get(controller.indexRetriever(path[0])).setActivity("active");
			else
				controller.getVertices().get(controller.indexRetriever(path[j])).setActivity("done"); //vertexc

			int idx = controller.edgeIndex(path[j+1]+","+path[j]);
			if(idx==-1)
				idx = controller.edgeIndex(path[j]+","+path[j+1]);

			controller.getEdges().get(idx).setActivity("done"); //edge
			if(!all)
				if(controller.isDirected() && foundAt1)
					controller.getEdges().get(controller.edgeIndex(path[j]+","+path[j+1])).setActivity("done"); 
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
		for(int i = 0; i<vertexSet.size(); i++){
			VNode aux = vertexSet.get(i).getNeighbour();

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
		for(int i=0; i<vertexSet.size(); i++){
			if(name.equals(vertexSet.get(i).getVertexName()))
				return i;
		}
		return -1;
	}

	private String nameRetriever(int index){
		return vertexSet.get(index).getVertexName();
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

	public double[] getDistances(){
		return this.distance;
	}
}
