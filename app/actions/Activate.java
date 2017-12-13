package actions;

import java.util.List;

import models.Globals;
import models.Node;

public class Activate extends RegalAction {
	/**
	 * Activate only this single node. Child objects will remain.
	 * 
	 * @param n a node to delete
	 * @return a message
	 */
	private String activate(Node n) {
		StringBuffer message = new StringBuffer();
		Globals.fedora.activateNode(n.getPid());
		updateIndex(n.getPid());
		return message.toString() + "\n" + n.getPid() + " activated!";
	}

	/**
	 * Each node in the list will be activated. Child objects will remain
	 * 
	 * @param nodes a list of nodes to delete.
	 * @return a message
	 */
	public String activate(List<Node> nodes) {
		StringBuffer message = new StringBuffer();
		for (Node n : nodes) {
			message.append(activate(n) + " \n");
		}
		return message.toString();
	}
}
