package semanticMapManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import kr.ac.uos.ai.agentCommunicationFramework.BrokerType;
import kr.ac.uos.ai.agentCommunicationFramework.agent.AgentExecutor;
import kr.ac.uos.ai.agentCommunicationFramework.model.GLFactory;
import kr.ac.uos.ai.agentCommunicationFramework.model.generailzedList.GeneralizedList;
import kr.ac.uos.ai.agentCommunicationFramework.model.parser.ParseException;
import kr.ac.uos.ai.arbi.agent.ArbiAgent;
import kr.ac.uos.ai.arbi.agent.ArbiAgentExecutor;
import semanticMapManager.logger.MyLogger;
import semanticMapManager.logger.SemanticMapManagerLogger;
import semanticMapManager.utility.GLMessageManager;
import semanticMapManager.utility.JAMUtilityManager;
import semanticMapManager.utility.RecievedMessage;
import semanticMapManager.utility.TaskGenerator;
import semanticMapManager.utility.VertexCalcurator;
import uos.ai.jam.Interpreter;
import uos.ai.jam.JAM;

public class SemanticMapManager extends ArbiAgent {
	private Interpreter interpreter;
	private GLMessageManager msgManager;
	private MultiAgentCommunicator agentCommunicator;
	private VertexCalcurator calcurator;
	private TaskGenerator taskGenerator;
	private SemanticMapManagerLogger logger;
	private MyLogger taskLogger;
	private MyLogger contextLogger;
	
	private BlockingQueue<RecievedMessage> messageQueue;
	public static String MY_mcARBI_AGENT_ADRRESS = "agent://www.mcarbi.com/SemanticMapManager";
	private MyDataSource dataSource;
	
	public SemanticMapManager(String channelHost, String brokerAddress, int brokerPort) {

		messageQueue = new LinkedBlockingQueue<RecievedMessage>();
		dataSource = new MyDataSource(messageQueue);
		calcurator = new VertexCalcurator();
		
		taskGenerator = new TaskGenerator();
		taskLogger = new MyLogger("task");
		contextLogger = new MyLogger("context");
		
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
		
		msgManager.assertFact("TaskGenerator", taskGenerator);
		
		
		msgManager.assertFact("Channel", "base", agentCommunicator.getBaseChannel());
		//aplViewer.init();
	
		Thread t = new Thread() {
			public void run() {
				interpreter.run();
			}
		};

//		logger = new SemanticMapManagerLogger(this, interpreter);
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
		if (i <= 10) {
			result = "agent://www.mcarbi.com/Local1";
		} else if (i > 10 && i <= 20) {
			result = "agent://www.mcarbi.com/Local2";
		}
		return result;
	}
	
	public String getRobotContextReceiver(String robotID) {
		String result = null;
//		System.out.println("robotID");
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
	
	public String getChannelName(String managerID) {
		String result = null;
		if (managerID.equals("agent://www.mcarbi.com/Local1")) {
			result = "warehouseManagement1";
		} else if (managerID.equals("agent://www.mcarbi.com/Local2")) {
			result = "warehouseManagement2";
		} else {
			System.out.println("what???");
			System.out.println(managerID);
		}
		return result;
	}
	
	public void assertToLTM(String content) {
//		dataSource.assertFact(content);
	}
	public void updateToLTM(String content) {
//		dataSource.updateFact(content);
	}
	public void subscribeToLTM(String rule) {
		dataSource.subscribe(rule);
	}
	
	public void logContext(String log) {
		contextLogger.log(log);
	}
	public void logTask(String log) {
		taskLogger.log(log);
	}
	
	public static void main(String[] args) {
		String brokerAddress = "";
		String channelHost = "";
		int port = 0;
		if(args.length == 0) {
			brokerAddress = "127.0.0.1";
//			brokerAddress = "172.16.165.77";
//			brokerAddress = "192.168.100.11";
			port = 61315;
//			channelHost = "172.16.165.158";
		} else {
			brokerAddress = args[1];
		}
		SemanticMapManager agent = new SemanticMapManager(brokerAddress, brokerAddress, port);
//		SemanticMapManager agent = new SemanticMapManager("127.0.0.1", brokerAddress, port);
		
	}

	
}
