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
import semanticMapManager.logger.SemanticMapManagerLogger;
import semanticMapManager.model.Vertex;
import semanticMapManager.utility.GLMessageManager;
import semanticMapManager.utility.JAMUtilityManager;
import semanticMapManager.utility.RecievedMessage;
import semanticMapManager.utility.VertexCalcurator;
import uos.ai.jam.Interpreter;
import uos.ai.jam.JAM;

public class SemanticMapManager extends ArbiAgent {
	private Interpreter interpreter;
	private GLMessageManager msgManager;
	private MultiAgentCommunicator agentCommunicator;
	private VertexCalcurator calcurator;
	private SemanticMapManagerLogger logger;
	
	private BlockingQueue<RecievedMessage> messageQueue;
	public static String MY_mcARBI_AGENT_ADRRESS = "agent://www.mcarbi.com/SemanticMapManager";
//	private Map<Integer, Vertex> vertexMap;
//	private Float threashHold;
	private MyDataSource dataSource;
	
	public SemanticMapManager(String channelHost, String brokerAddress, int brokerPort) {

		messageQueue = new LinkedBlockingQueue<RecievedMessage>();
		dataSource = new MyDataSource(messageQueue);
		calcurator = new VertexCalcurator();
		
		interpreter = JAM.parse(new String[] { "./plan/boot.jam" });
		agentCommunicator = new MultiAgentCommunicator(messageQueue);
		msgManager = new GLMessageManager(interpreter);

		
		AgentExecutor.execute(channelHost, MY_mcARBI_AGENT_ADRRESS, agentCommunicator, BrokerType.ACTIVEMQ);
		ArbiAgentExecutor.execute(brokerAddress, brokerPort, "agent://www.arbi.com/TaskManager", this, kr.ac.uos.ai.arbi.BrokerType.ACTIVEMQ);
		dataSource.connect(brokerAddress, brokerPort, "ds://www.agent.com/TaskManager", kr.ac.uos.ai.arbi.BrokerType.ACTIVEMQ);
		
		
		init();
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

		logger = new SemanticMapManagerLogger(this, interpreter);
		t.start();
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

	public void sleepAwhile(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String convertPositionToVertex(String location, String robotID, float x, float y, String path) {
		return calcurator.convertPositionToVertex(location, robotID, x, y, path);
	}
	
	public String getObjectContextReceiver(String objectID) {
		String result = null;
		
		String num = objectID.substring(objectID.length() - 2);
//		System.out.println(num);
		int i = Integer.parseInt(num);
		if (i <= 20) {
			result = "agent://www.mcarbi.com/Local1";
		} else if (i > 20 && i <= 40) {
			result = "agent://www.mcarbi.com/Local2";
		}
		return result;
	}
	
	public String getRobotContextReceiver(String robotID) {
		String result = null;
		
		String num = robotID.substring(robotID.length() - 1);
//		System.out.println(num);
		int i = Integer.parseInt(num);
		
		if (i <= 4) {
			result = "agent://www.mcarbi.com/Local1";
		} else if (i >= 5 && i <= 7) {
			result = "agent://www.mcarbi.com/Local2";
		}
		return result;
	}
	
	public void assertToLTM(String content) {
		dataSource.assertFact(content);
	}
	public void updateToLTM(String content) {
		dataSource.updateFact(content);
	}
	public void subscribeToLTM(String rule) {
		dataSource.subscribe(rule);
	}
	
	public static void main(String[] args) {
		String brokerAddress = "";
		String channelHost = "";
		int port = 0;
		if(args.length == 0) {
			brokerAddress = "172.16.165.185";
//			brokerAddress = "192.168.100.11";
			port = 61315;
//			channelHost = "172.16.165.158";
		} else {
			brokerAddress = args[1];
		}
		SemanticMapManager agent = new SemanticMapManager(brokerAddress, brokerAddress, port);
		
	}

	
}
