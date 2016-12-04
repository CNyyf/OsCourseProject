package osmanagement.file;

import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;

public class FolderFCB extends FCB {
    ArrayList<FCB> members = new ArrayList<FCB>();
	public FolderFCB(String n) {
		super(n);
	}
	DefaultMutableTreeNode cCreateFCB(String n, int t) {
    	String name = getName(this.theNode, n);
    	FCB fcb = createFCB(name, t);
    	DefaultMutableTreeNode node = createNode(fcb);
    	this.theNode.add(node);
    	members.add(fcb);
    	return node;
	}
    boolean cDeleteFCB() {
		while(theNode.getChildCount() != 0){
			//System.out.println(theNode.getChildCount());
			FCB child = (FCB) ((DefaultMutableTreeNode)theNode.getFirstChild()).getUserObject();
			if(child.mainDeleteFCB() == false) {
				return false;
			}
		}
    	return true;
    }
    int cMoveFCB(DefaultMutableTreeNode node) {
    	if(node.getParent() == theNode){
    		return 1;
    	}
    	FCB fcb = (FCB)node.getUserObject();
    	String name = fcb.name;
    	fcb.name = getName(theNode, name);
    	node.removeFromParent();
    	fcb.removeFromParent();
    	theNode.add(node);
    	members.add(fcb);
    	return 1;
    }
    int cCopyFCB(DefaultMutableTreeNode node) {
    	if(node == null){
    		return 0;
    	}
    	return ((FCB)node.getUserObject()).mainDeepCopyInto(theNode);
    }
    int cDeepCopyInto(DefaultMutableTreeNode parent, DefaultMutableTreeNode newNode) {
		for(int i = 0; i != theNode.getChildCount(); i++){
			if(((FCB)((DefaultMutableTreeNode) theNode.getChildAt(i)).getUserObject()).mainDeepCopyInto(newNode) == -1){
				return -1;
			}
		}
    	return 1;
    }
    public int getType(){
    	return FOLDER;
    }
    ArrayList<FCB> members(){
    	return members;
    }
}
