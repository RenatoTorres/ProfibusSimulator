/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package view.fdl;

import common.framestructure.ProfibusFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author Renato Veiga
 */
public class PopUpFrameTable extends JPopupMenu {
    JMenuItem anItem1;
    JMenuItem anItem2;
    public PopUpFrameTable(ProfibusFrame frm){
        anItem1 = new JMenuItem("Details for Station " + String.valueOf( frm.getSourceAddr() ) );
        
        add(anItem1);
        add(anItem2);
    }
}