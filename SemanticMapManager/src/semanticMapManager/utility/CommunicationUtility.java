package semanticMapManager.utility;

import kr.ac.uos.ai.arbi.agent.ArbiAgent;
import kr.ac.uos.ai.arbi.ltm.DataSource;

public class CommunicationUtility {
	private ArbiAgent agent;
	private DataSource ds;
	
	public CommunicationUtility(ArbiAgent agent, DataSource ds) {
		this.agent = agent;
		this.ds = ds;
		
	}

	public void assertToLTM(String data) {
		ds.assertFact(data);
	}
	
	public void retractFromLTM(String data) {
		String result = ds.retractFact(data);
	}
	public void inform(String receiver, String content) {
		agent.send(receiver, content);
	}	
	
	public void unsubscribe(String receiver, String content) {
		System.out.println("unsubscribe : " + receiver + " " + content);
		agent.unsubscribe(receiver, content);
	}

	public String sendQuery(String receiver, String content) {
		//System.out.println("query : " + receiver + " " + content);
		String result = "";
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		result = agent.query(receiver, content);

		//System.out.println("query result :" + result);
		return result;

	}

	public String request(String receiver, String content) {
		//System.out.println("Request : " + receiver + " " + content);
		String result = "";
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		result = agent.request(receiver, content);
		//System.out.println("result : " + result);

		return result;
	}

	public void test() {
		System.out.println("testPrint");

	}
	public void subscribe(String receiver,String content){
		agent.subscribe(receiver, content);
	}
	public String subscribeToLTM(String content){
		String result = ds.subscribe(content);
		return result;
	}
	
	
	public void updateToLTM(String content) {
		//System.out.println("==== updateFact To LTM : " + content + " ====");
		ds.updateFact(content);
	}
	
	public String toString(){
		return "CommunicationUtility";
	}
}
