package Graph;

public interface Graph{
	public void addVertex(String name);
	public void addEdge(String edge);
	public void removeVertex(String vertex);
	public void removeEdge(String edge);
	public void printNeighbours(String vertex);
	public int isAdjacent(String edge);
	public boolean isConnected(String edge);
	public int numberOfVertices();
	public int numberOfEdges();
}
