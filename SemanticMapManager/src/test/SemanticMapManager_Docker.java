package test;

import semanticMapManager.SemanticMapManager;

public class SemanticMapManager_Docker {
	public static void main(String[] args) {
		String channelAddress = System.getenv("BASE_CHANNEL_ADDRESS");
		String brokerAddress = System.getenv("BROKER_ADDRESS");
		String stringPort = System.getenv("BROKER_PORT");
		int port = Integer.parseInt(stringPort);
		SemanticMapManager semanticMapManager = new SemanticMapManager(channelAddress, brokerAddress, port);
	}
}
