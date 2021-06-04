package Graph;

import Exceptions.*;
import UI.MainClass;
import java.util.ArrayList;
import java.awt.Point;
import Algorithms.Dijkstra;

public class DijkstraGraphController{

	private DijkstraGraphModel model;
	private MainClass main;
	private Thread dijkstraSearch;

	public DijkstraGraphController(MainClass main){
		this.main = main;
		model = new DijkstraGraphModel();
	}

	public boolean checkVertexIsNear(int x,int y, boolean isCarrying, String selectedElement, int offset){
		return model.checkVertexIsNear(x,y,isCarrying,selectedElement,offset);
	}

	public void saveGraph(String path){
		try{
			model.saveGraph(path+".txt");
		}catch(Exception ex1){new ErrorReport(ex1.getMessage(), "Save Error");};
	}

	public void loadGraph(java.io.File path){
		try{
			model.readGraph(path);		
			main.refreshDraw();		
			main.refreshVisual();
			main.reloadVertices(model.getVertices());
			main.repaintActive();
		}catch(Exception ex){new ErrorReport(ex.getMessage(),"Load Error");};
	}

	public void generateGraph(String vSize, String eSize){
		try{
			model = new DijkstraGraphModel();
			model.generateRandom(vSize, eSize);
			main.resetSetting("Transform to digraph");
			model.setDirected(false);
			main.refreshDraw();
			main.refreshVisual();
		}catch(InvalidGenerationException ige){
			new ErrorReport(ige.getMessage(), "Error Creation");			
		}
	}

	public void setDirected(boolean direct){
		model.setDirected(direct);
		main.refreshDraw();
		main.refreshVisual();
	}

	public ArrayList<Edge> getEdges(){
		return model.getEdges();
	}
	
	public ArrayList<Vertex> getVertices(){
		return model.getVertices();
	}

	public boolean isDirected(){
		return model.isDirected();
	}

	public void reloadVertices(){
		main.reloadVertices(model.getVertices());
	}

	public void setArbitraryPosition(String vertex, Point point){
		model.setArbitraryPosition(vertex, point);
	}

	public void addVertex(String name, double time, int x, int y){
		try{
			model.addVertex(name, time, x, y);
			main.refreshDraw();
		}catch(VertexAlreadyExistsException vaee){new ErrorReport(vaee.getMessage(), "Add Error");};
	}

	public void addEdge(String v1, String v2, String weight){
		try{
			model.addEdge(v1, v2, weight);
			main.refreshDraw();
		}catch(InvalidEdgeException vaee){new ErrorReport(vaee.getMessage(), "Add Error");};
	}

	public void removeVertex(String vertex){
		try{
			model.removeVertex(vertex);
			main.refreshDraw();
		}catch(InvalidVertexException iv){new ErrorReport(iv.getMessage(),"Remove Error");};
	}

	public void removeEdge(String edge){
		try{
			model.removeEdge(edge);
			main.refreshDraw();
		}catch(InvalidEdgeException ie){new ErrorReport(ie.getMessage(), "Remove Error");};
	}

	public void renameVertex(String oldN, String newN){
		try{
			model.renameVertex(oldN, newN);
			main.refreshDraw();
		}catch(Exception e){new ErrorReport(e.getMessage(),"Rename Error");};
	}

	public void changeWeight(String edge, String w){
		try{
			model.changeWeight(edge, w);
			main.refreshDraw();
		}catch(Exception e){new ErrorReport(e.getMessage(), "Change Error");}
	}

	public Point getVertexPoint(String vertex){
		main.getDrawPanel().setCarrying(true);
		return model.getVertices().get(indexRetriever(vertex)).getPoint();
	}

	public String checkVertexOverlap(int x, int y){
		return model.checkVertexOverlap(x,y);
	}

	public void resetActivity(){
		model.resetActivity();
	}

	public int numberOfVertices(){
		return model.numberOfVertices();
	}

	public int indexRetriever(String vertex){
		return model.indexRetriever(vertex);
	}

	public int edgeIndex(String edge){
		return model.edgeIndex(edge);
	}

	public void runDijkstra(UI.VisualPanel vPanel, String start, String end , String mode){
		Dijkstra dijkstra = new Dijkstra(vPanel,this, start, end, mode);
		main.getVisualPanel().disableBottom();
		dijkstraSearch = new Thread(dijkstra);
		dijkstraSearch.start();
	}

	public void resetExec(){
		main.refreshVisual();
	}

	public void stopExec(){
		main.getVisualPanel().doneExec();	
		main.getVisualPanel().stopExecution();
	}

	public Thread getDijkstra(){
		return dijkstraSearch;
	}
}