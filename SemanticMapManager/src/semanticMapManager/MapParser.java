package semanticMapManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import semanticMapManager.model.Vertex;

public class MapParser {
	
	
	public static HashMap<Integer, Vertex> readMapFile(String path) throws Exception {
		System.out.println("parsing " + path);
		HashMap<Integer, Vertex> result = null;
		File file = new File(path);
		if(file.exists()) {
			boolean ifFinished = false;
			result = new HashMap<Integer, Vertex>();
				
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
				
			while(line != null) {
				Integer vertexName = null;
				Integer vertexType = null;
				Float posX = null;
				Float posY = null;
				Float posZ = null;
				List<Integer> edges = new LinkedList<Integer>();
				
			    if(line.contains("Vertex")) {
					while(!ifFinished) {
						line = reader.readLine();
						if(line == null) break;
						if(line.contains("name")) vertexName = Integer.valueOf(line.split(" ")[1]);
						else if(line.contains("type")) vertexType = Integer.valueOf(line.split(" ")[1]);
						else if(line.contains("pos")) {
							String[] stringVertexes = line.split(" ");
							posX = Float.valueOf(stringVertexes[1]);
							posY = Float.valueOf(stringVertexes[3]);
							posZ = Float.valueOf(stringVertexes[2]);
						}
						else if(line.contains("edge")) {
							Integer edge = Integer.valueOf(line.split(" ")[1]);
							edges.add(edge);
						}
						else if(line.contains("Vertex")) break;
						else throw new Exception("parsing map file error");
					}
				}
				Vertex vertex = new Vertex(Integer.valueOf(vertexName), posX, posY);
				vertex.setEdges(edges);
				result.put(vertexName, vertex);
			}
			reader.close();
		}
		else throw new Exception("parsing fail: no such file");
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		
		HashMap<Integer, Vertex> vertexMap;
		try {
			MapParser converter = new MapParser();
			vertexMap = converter.readMapFile("map_cloud_real.txt");
			Iterator<Integer> keys = vertexMap.keySet().iterator();
			while(keys.hasNext()) {
			    int key = keys.next();
			    Vertex vertex = vertexMap.get(key);
				System.out.println("	fact " + vertex.toString() + ";");
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
