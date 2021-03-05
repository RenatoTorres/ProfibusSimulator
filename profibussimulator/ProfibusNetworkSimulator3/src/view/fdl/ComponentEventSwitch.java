/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package view.fdl;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import view.liveListTree.JPannelSlaveList;

/**
 *
 * @author Renato Veiga
 */
public class ComponentEventSwitch {
    
    static JTextField jTextFieldSlaveAddress = null;
    static JPannelSlaveList jTableLiveList = null;
    static JTabbedPane jTableSlave = null;
       
    static public final int TABLE_LIVE_LIST       = 0;
    static public final int TEXT_FIELD_SLAVE_ADDR = 1;
    static public final int JTABLE_SLAVE = 2;
    
    static public void Register( Object b, int ID )
    {
        if( b instanceof JPannelSlaveList )
            jTableLiveList = (JPannelSlaveList) b;
        else if( b instanceof JTextField)
            jTextFieldSlaveAddress = (JTextField) b;
        
        if( ID == 2 )
            jTableSlave = (JTabbedPane) b;
        
   }
    
    static public void Fire( )
    {
        if( (jTextFieldSlaveAddress != null) && (jTableLiveList != null) )
            jTextFieldSlaveAddress.setText( String.valueOf( jTableLiveList.getCurrentAddr() ) );
        
        if( jTableSlave != null )
        {
            jTableSlave.setTitleAt( 3, "Station properties for addr: " + String.valueOf( jTableLiveList.getCurrentAddr() ));
            jTableSlave.setTitleAt( 4, "Station communication signal for addr: " + String.valueOf( jTableLiveList.getCurrentAddr() ));
        }
    }
    
}
