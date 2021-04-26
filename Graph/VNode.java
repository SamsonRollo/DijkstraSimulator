package Graph;

public class VNode implements Comparable<VNode>{
	VNode next = null;
	String vertexName;
	double weight=0;

	public VNode(){}

	public VNode(String vertexName, VNode next, double weight){
		this.vertexName = vertexName;
		this.next = next;
		this.weight = weight;
	}	

	public VNode(String vertexName, VNode next){
		this.vertexName = vertexName;
		this.next = next;
	}

	public VNode(String vertexName, double weight){
		this.vertexName = vertexName;
		this.weight = weight;
	}

	public VNode(double weight){
		this.weight = weight;
	}

	public void setVNode(VNode neighbour){
		next = neighbour;
	}

	public VNode getVNode(){
		return next;
	}

	public void setVertexName(String vertexName){
		this.vertexName = vertexName;
	}

	public String getVertexName(){
		return vertexName;
	}

	public void setWeight(double weight){
		this.weight = weight;
	}

	public double getWeight(){
		return weight;
	}

	@Override
    public int compareTo(VNode vnode) {
        if(this.getWeight() > vnode.getWeight()) {
            return 1;
        } else if (this.getWeight() < vnode.getWeight()) {
            return -1;
        } else {
            return 0;
        }
    }
}
