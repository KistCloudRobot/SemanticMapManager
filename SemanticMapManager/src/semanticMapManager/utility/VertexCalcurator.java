package semanticMapManager.utility;

import java.util.Map;

import kr.ac.uos.ai.agentCommunicationFramework.model.GLFactory;
import kr.ac.uos.ai.agentCommunicationFramework.model.generailzedList.GeneralizedList;
import kr.ac.uos.ai.agentCommunicationFramework.model.parser.ParseException;
import semanticMapManager.MapParser;
import semanticMapManager.model.Vertex;

public class VertexCalcurator {
	private Float threashHold;
	private Map<Integer, Vertex> vertexMap;
	
	public VertexCalcurator() {
		threashHold = (float) 0.5;
		try {
			vertexMap = MapParser.readMapFile("./map_cloud_real.txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private float calculateDistance(float x, float y, float vx, float vy) {
		double disX = Math.pow((vx - x), 2);
		double disY = Math.pow((vy - y), 2);
		double distance = Math.sqrt(disX + disY);
		return (float)distance;
	}
	
	public String convertPositionToVertex(String robotID, float x, float y, String path) {
		String result = "";
		float minDistance = Float.MAX_VALUE;
		float nextMinDistance = Float.MAX_VALUE;
		Vertex nearestVertex = null;
		Vertex secondNearestVertex = null;
//		System.out.println("input (robotPosition "+robotID +" " + x + " " + y  + ") timestamp : " + System.currentTimeMillis());

	    GeneralizedList pathGL = null;
		try {
			pathGL = GLFactory.newGLFromGLString(path);
		} catch (ParseException e) {
			e.printStackTrace();
			System.out.println("wrong path : " + path);
		}
	    if (pathGL.getExpressionsSize() == 0) {
		    for (Vertex vertex : vertexMap.values()) {
		       float distance = calculateDistance(vertex.getX(), vertex.getY(), x, y);
		       if (distance < minDistance) {
		    	   minDistance = distance;
		    	   nearestVertex = vertex;
		       }
		    } 
		    for(int edge : nearestVertex.getEdges()) {
		    	Vertex v = vertexMap.get(edge);
		    	float distance = calculateDistance(v.getX(), v.getY(), x, y);
		    	if (distance < nextMinDistance) {
		    		nextMinDistance = distance;
		    		secondNearestVertex = v;
		    	}
		    }
		} else {
			for (int i = 0; i < pathGL.getExpressionsSize(); i++) {
				Vertex vertex = vertexMap.get(pathGL.getExpression(i).asValue().intValue());
				float distance = calculateDistance(vertex.getX(), vertex.getY(), x, y);
				if (distance < minDistance) {
					nextMinDistance = minDistance;
					minDistance = distance;
					secondNearestVertex = nearestVertex;
					nearestVertex = vertex;
				} else if (distance < nextMinDistance) {
					nextMinDistance = distance;
					secondNearestVertex = vertex;
				}
			}
		}
		
		if (minDistance <= threashHold) {
			result = "(robotAt \"" + robotID + "\" " + nearestVertex.getVertexName() + " " + nearestVertex.getVertexName() + ")"; 
		} else {
			result = "(robotAt \"" + robotID + "\" " + nearestVertex.getVertexName() + " " + secondNearestVertex.getVertexName() + ")";
		}
//		System.out.println("output " + result + " timestamp : " + System.currentTimeMillis());
		return result;
	}
	
}
