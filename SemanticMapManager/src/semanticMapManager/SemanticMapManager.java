package semanticMapManager;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import kr.ac.uos.ai.agentCommunicationFramework.BrokerType;
import kr.ac.uos.ai.agentCommunicationFramework.agent.Agent;
import kr.ac.uos.ai.agentCommunicationFramework.agent.AgentExecutor;
import kr.ac.uos.ai.agentCommunicationFramework.agent.ChannelFactory;
import kr.ac.uos.ai.agentCommunicationFramework.agent.communication.Channel;
import kr.ac.uos.ai.agentCommunicationFramework.model.GLFactory;
import kr.ac.uos.ai.agentCommunicationFramework.model.generailzedList.GeneralizedList;
import kr.ac.uos.ai.agentCommunicationFramework.model.parser.ParseException;
import kr.ac.uos.ai.arbi.agent.ArbiAgent;
import kr.ac.uos.ai.arbi.agent.ArbiAgentExecutor;
import kr.ac.uos.ai.arbi.ltm.DataSource;
import semanticMapManager.model.Vertex;
import semanticMapManager.utility.GLMessageManager;
import semanticMapManager.utility.JAMUtilityManager;
import semanticMapManager.utility.RecievedMessage;
import uos.ai.jam.Interpreter;
import uos.ai.jam.JAM;

public class SemanticMapManager extends ArbiAgent {
	private Interpreter interpreter;
	private GLMessageManager msgManager;
	private MultiAgentCommunicator agentCommunicator;

	private BlockingQueue<RecievedMessage> messageQueue;
	public static String MY_mcARBI_AGENT_ADRRESS = "agent://www.mcarbi.com/SemanticMapManager";
	private Map<Integer, Vertex> vertexMap;
	private Float threashHold;
	private DataSource dataSource;
	
	public SemanticMapManager(String channelHost, String brokerAddress, int brokerPort) {
		threashHold = (float) 0.5;
		messageQueue = new LinkedBlockingQueue<RecievedMessage>();
		dataSource = new DataSource();
		
		try {
			vertexMap = MapParser.readMapFile("./map_cloud_real.txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		interpreter = JAM.parse(new String[] { "./plan/boot.jam" });
		agentCommunicator = new MultiAgentCommunicator(messageQueue);
		msgManager = new GLMessageManager(interpreter);
		
		AgentExecutor.execute(channelHost, MY_mcARBI_AGENT_ADRRESS, agentCommunicator, BrokerType.ZEROMQ);
		ArbiAgentExecutor.execute(brokerAddress, brokerPort, "agent://www.arbi.com/TaskManager", this, kr.ac.uos.ai.arbi.BrokerType.ACTIVEMQ);
		dataSource.connect(brokerAddress, brokerPort, "ds://www.agent.com/TaskManager", kr.ac.uos.ai.arbi.BrokerType.ACTIVEMQ);
		
		
		init();
	}

	public void updateToLTM(String content) {
		dataSource.updateFact(content);
	}
	public static void main(String[] args) {
		String brokerAddress = "";
		String channelHost = "";
		int port = 0;
		if(args.length == 0) {
//			brokerAddress = "172.16.165.164";
			brokerAddress = "192.168.100.11";
			port = 61315;
//			channelHost = "172.16.165.158";
		} else {
			brokerAddress = args[1];
		}
		SemanticMapManager agent = new SemanticMapManager(brokerAddress, brokerAddress, port);
	}

	public void sleepAwhile(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void init() {
		msgManager.assertFact("GLUtility", msgManager);
		msgManager.assertFact("ExtraUtility", new JAMUtilityManager(interpreter));
		msgManager.assertFact("SemanticMapManager", this);
		msgManager.assertFact("AgentCommunication", agentCommunicator);
		
		
		msgManager.assertFact("Channel", "base", agentCommunicator.getBaseChannel());
		//aplViewer.init();
	
		Thread t = new Thread() {
			public void run() {
				interpreter.run();
			}
		};
		
		t.start();
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
		System.out.println("input (robotPosition "+robotID +" " + x + " " + y  + " timestamp : " + System.currentTimeMillis());

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
		System.out.println("output " + result + " timestamp : " + System.currentTimeMillis());
		return result;
	}
	
	public boolean dequeueMessage() {

		if (messageQueue.isEmpty())
			return false;
		else {
			try {
				RecievedMessage message = messageQueue.take();
				GeneralizedList gl = null;
				String data = message.getMessage();
				String sender = message.getSender();

				gl = GLFactory.newGLFromGLString(data);

//				System.out.println("message dequeued : " + gl.toString());

				if (gl.getName().equals("context")){
					msgManager.assertGL(data);
				} else {
					//System.out.println("assert context : " + data);
					msgManager.assertGL(data);
				}

			} catch (InterruptedException | ParseException e) {
				e.printStackTrace();
			}

			return true;
		}
	}
	
}
