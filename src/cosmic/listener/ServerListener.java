package cosmic.listener;

import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;

import cosmic.core.ServerMain;
import cosmic.message.*;

public class ServerListener implements MessageListener<HostedConnection>{
	
	ServerMain server;
	
	public ServerListener(ServerMain srv){
		server = srv;
	}
	
	public void messageReceived(HostedConnection source, Message msg) {
		if (msg instanceof BasicMessage) {
			BasicMessage message = (BasicMessage) msg;
			
			if (message.str.equals("ready")){
				server.ready += 1;
			}
		}
		
		if (msg instanceof CreateMessage) {
			CreateMessage message = (CreateMessage) msg;
			server.gameState.createUnit(message.player_id, message.unit, message.pos);
			server.relayMessage(source, message);
		}
		
		if (msg instanceof MoveMessage) {
			MoveMessage message = (MoveMessage) msg;
			server.gameState.moveUnit(message.player_id, message.unit, message.dest);
			server.relayMessage(source, message);
		}
		
		if (msg instanceof TargetMessage) {
			TargetMessage message = (TargetMessage) msg;
			server.gameState.setTarget(message.unit, message.unit_player, message.target, message.target_player);
			server.relayMessage(source, message);
		}
	}
		
}
