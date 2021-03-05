/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package view.liveListTree;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import view.fdl.FDLAnalysis.FDLTelegramsTable;

/**
 *
 * @author emossin
 */
public class LiveListTreePanel extends JPanel {

    private String test;
    protected ArrayList<DefaultMutableTreeNode> nodesList = new ArrayList<DefaultMutableTreeNode>();
    public static final int NUM_OFF_SLAVES = 127;
    public static final int FIRST_SLAVE_INDEX = 0;
    public static final int LAST_SLAVE_INDEX = 126;
    JTree tree;
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Live List");
    FDLTelegramsTable parentFrame;

    /** Creates new form SlavesPanel */
    public LiveListTreePanel(Dimension size, FDLTelegramsTable parentFrame) {
        this.parentFrame = parentFrame;
        startIcons();
        setSize(size);
        tree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent evt) {
                doMouseClicked(evt);
            }
        });
        //initComponents();
    }

    void doMouseClicked(TreeSelectionEvent me) {
        parentFrame.setAddrSelectedInLiveListGridEvt(getSelectedDevice());
    }

    public int getSelectedDevice() {
        int[] rows = tree.getSelectionRows();
        if (rows == null) {
            return -1;
        } else if (rows.length == 0) {
            return -1;
        } else {
            return rows[0] - 1;
        }
    }

    public void startIcons() {
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        gridBagConstraints.anchor = GridBagConstraints.CENTER;


        for (int l = 0; l < NUM_OFF_SLAVES; l++) {
            LiveListNode slaveNode = new LiveListNode(l);
            root.add(slaveNode);
        }




        setLayout(new GridBagLayout());
        tree = new JTree(root);
        tree.setCellRenderer(new CustomIconRenderer());
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().add(tree);
        this.add(scrollPane, gridBagConstraints);
    }

    public void refreshIcons() {
        tree.repaint();


    }

    public void setStatus(int address, byte status) {
        LiveListNode slaveNode = (LiveListNode) tree.getModel().getChild(root, address);
        slaveNode.changeStatus(status);
    }

    /**
     * @return the test
     */
    public String getTest() {
        return test;
    }

    /**
     * @param test the test to set
     */
    public void setTest(String test) {
        this.test = test;
    }
    // Variables declaration - do not modify
    // End of variables declaration
}

class CustomIconRenderer extends DefaultTreeCellRenderer {

    public CustomIconRenderer() {
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
            Object value, boolean sel, boolean expanded, boolean leaf,
            int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel,
                expanded, leaf, row, hasFocus);

        if (value instanceof LiveListNode) {
            Object nodeObj = ((LiveListNode) value).getUserObject();
            setIcon(((LiveListNode) nodeObj).getIcon());
        } else {
            URL url = ClassLoader.getSystemResource(LiveListStatus.rootIconPath + "liveList16.png");
            ImageIcon icon = new ImageIcon(url);

            Object nodeObj = ((DefaultMutableTreeNode) value).getUserObject();
            setIcon(icon);
        }
        return this;
    }
}
