package cosmic.listener;

import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;

import cosmic.core.Player;
import cosmic.core.states.PlayerGameState;
import cosmic.message.*;

public class ClientListener implements MessageListener<Client>{

	private Player player;

	public ClientListener(Player p){
		player = p;
	}

	@Override
	public void messageReceived(Client client, Message msg) {
		if (msg instanceof BasicMessage) {
			BasicMessage message = (BasicMessage) msg;
			
			if (message.str.equals("ready")){
				player.gameStart();
			}
			
		}

		if (msg instanceof StartMessage) {
			StartMessage message = (StartMessage) msg;
			player.gameInit(message.map, message.id, message.x, message.y);
		}
		
		if (msg instanceof MoveMessage) {
			MoveMessage message = (MoveMessage) msg;
			PlayerGameState state = (PlayerGameState) player.getState("game");
			state.moveUnit(message.player_id, message.unit, message.dest);
		}
		
		if (msg instanceof CreateMessage) {
			CreateMessage message = (CreateMessage) msg;
			//AppState cur_state = player.getState();
			//System.out.println(cur_state instanceof PlayerGameState);
			PlayerGameState state = (PlayerGameState) player.getState("game");
			state.createEnemyUnit(message.player_id, message.unit, message.pos);
			
		}
		
		if (msg instanceof SyncMessage) {
			SyncMessage message = (SyncMessage) msg;
			PlayerGameState state = (PlayerGameState) player.getState("game");
			state.SyncUnit(message.p_id, message.u_id, message.pos, message.speed);
		}
		
		if (msg instanceof TargetMessage) {
			TargetMessage message = (TargetMessage) msg;
			PlayerGameState state = (PlayerGameState) player.getState("game");
			state.setTarget(message.unit, message.unit_player, message.target, message.target_player);
		}
		
		if (msg instanceof AttackMessage) {
			AttackMessage message = (AttackMessage) msg;
			PlayerGameState state = (PlayerGameState) player.getState("game");
			state.unitAttack(message.unit, message.owner);
		}
		
		if (msg instanceof HitsMessage) {
			HitsMessage message = (HitsMessage) msg;
			PlayerGameState state = (PlayerGameState) player.getState("game");
			state.multiAttack(message.unit, message.targets);
		}
	}

}
