package semanticMapManager.model;

import java.util.List;

public class Vertex {

	private int vertexName;
	private Float x;
	private Float y;
	private List<Integer> edges;
	
	public Vertex(int vertex, Float x, Float y) {
		this.vertexName = vertex;
		this.x = x;
		this.y = y;
	}
	
	public int getVertexName() {
		return vertexName;
	}
	public Float getX() {
		return x;
	}
	public Float getY() {
		return y;
	}
	
	public void setEdges(List<Integer> edges) {
		this.edges = edges;
	}
	
	public List<Integer> getEdges() {
		return edges;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Vertex(" + vertexName + ", " + x + ", " + y +")";
	}
}
