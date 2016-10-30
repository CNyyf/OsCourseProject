package osmanagement;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import osmanagement.FCB;

public class MyDefaultTreeCellRenderer extends DefaultTreeCellRenderer  
{
	private static final long serialVersionUID = 1L;
	@Override  
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus){ 
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);  
  
        setText(value.toString());  
       
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        FCB fcb = (FCB)node.getUserObject();
        int type = fcb.getType();
        //System.out.println("!" + type + FCB.getPath(node) + fcb.name); 
        if(type == FCB.FILE)  
        {
        	this.setIcon(leafIcon);
        	//System.out.println("leafIcon" + FCB.getPath(node) + fcb.name);
        }
        if(type == FCB.FOLDER){
        	this.setIcon(closedIcon);
        }
  
        return this;  
    }  
}
