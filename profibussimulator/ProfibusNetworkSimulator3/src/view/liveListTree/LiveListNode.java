/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SlavePanel.java
 *
 * Created on 19/05/2010, 16:16:16
 */
package view.liveListTree;

import java.awt.Component;

import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author eduardo_mossin
 */
public class LiveListNode extends DefaultMutableTreeNode {

    int address = 0;
    LiveListStatus liveStatus = new LiveListStatus();

    /** Creates new form SlavePanel */
    public LiveListNode(int address) {
        super("address: " + new Integer(address).toString(),false);
        this.address = address;
    }

    public ImageIcon getIcon() {
        return liveStatus.getSmallIcon();
    }

    public void changeStatus(byte status){
        liveStatus.setStatus(status);
    }

    @Override
    public Object getUserObject() {
        return this;
    }

    
    

}


