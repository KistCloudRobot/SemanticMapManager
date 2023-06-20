package semanticMapManager;

import java.util.concurrent.BlockingQueue;

import kr.ac.uos.ai.arbi.ltm.DataSource;
import semanticMapManager.utility.RecievedMessage;

public class MyDataSource extends DataSource{


	private BlockingQueue<RecievedMessage> messageQueue;
	
	public MyDataSource(BlockingQueue<RecievedMessage> queue) {
		this.messageQueue = queue;
	}
	
	
	@Override
	public void onNotify(String content) {
		RecievedMessage message = new RecievedMessage("LTM", content);
		messageQueue.add(message);
	}
}
