/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package view.liveListTree;

import SlaveDialog.DiagnosticDialog;
import consolesimulator.ProfibusSimulator;
import consolesimulator.ProfibusSimulatorSingleton;
import consolesimulator.ProfibusSlave;
import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author Renato Veiga
 */
public class PopUpMenuLiveList extends JPopupMenu implements ActionListener {
    JMenuItem jMenuItemGoONLINE, jMenuItemGoOFFLINE, jMenuItemChangeDiag, jMenuItemChangeCfg;
    ProfibusSimulator Sim = ProfibusSimulatorSingleton.getInstance();
    int slaveAddr = 0;
    
    public PopUpMenuLiveList(int Addr ){
        slaveAddr = Addr;
        
        jMenuItemGoONLINE = new JMenuItem("Go ONLINE Station " + String.valueOf( slaveAddr ) );
        jMenuItemGoOFFLINE = new JMenuItem("Go OFFLINE Station " + String.valueOf( slaveAddr ) );
        jMenuItemChangeDiag = new JMenuItem("Change Diagnostic for Station " + String.valueOf( slaveAddr )+" ..." );
        jMenuItemChangeCfg = new JMenuItem("Change Configuration for Station " + String.valueOf( slaveAddr )+" ..." );
        
        if( Sim.isSlaveOnline( slaveAddr ) == true ) {
            jMenuItemGoONLINE.setEnabled( false );
            jMenuItemGoOFFLINE.setEnabled( true );
        }
        else {
            jMenuItemGoONLINE.setEnabled( true );
            jMenuItemGoOFFLINE.setEnabled( false );
        }

        add( jMenuItemGoONLINE );
        add( jMenuItemGoOFFLINE );
        add( jMenuItemChangeDiag );
        add( jMenuItemChangeCfg );
        
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        jMenuItemGoONLINE.addActionListener(this);
        jMenuItemGoOFFLINE.addActionListener(this);
        jMenuItemChangeDiag.addActionListener(this);
        jMenuItemChangeCfg.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        
        if( (JMenuItem) e.getSource() == jMenuItemGoONLINE ) {
            Sim.getSlaveCollection().getSlave( slaveAddr ).setOnline(true);
        }
        else if( (JMenuItem) e.getSource() == jMenuItemGoOFFLINE ) {
            Sim.getSlaveCollection().getSlave( slaveAddr ).setOnline(false);
            Sim.getSlaveCollection().getSlave( slaveAddr ).restart();
        }
        else if( (JMenuItem) e.getSource() == jMenuItemChangeDiag ) {
            JFrame jDiag = new DiagnosticDialog( slaveAddr );
            jDiag.setVisible(true);
        }
        else if( (JMenuItem) e.getSource() == jMenuItemChangeCfg ) {
            
        }
    }
    
    
}
