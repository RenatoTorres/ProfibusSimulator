/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package view.fdl;

import java.awt.Dimension;
import javax.swing.JFrame;
import view.fdl.FDLAnalysis.FDLTelegramsTable;

/**
 *
 * @author eduardo_mossin
 */
public class FDLInternalFrame extends JFrame {

    FDLTelegramsTable panel = new FDLTelegramsTable();

    public FDLInternalFrame() {
        this.setSize(new Dimension(1640, 875));
        panel.setPreferredSize(new Dimension(425, 550));
        this.add(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //insere-se um icone na interface
        //URL url = ClassLoader.getSystemResource("profidoctorapp/resources/fdlWhite.png");
        //ImageIcon icon = new ImageIcon(url);
        //this.setFrameIcon(icon);
        
        this.setTitle("Profidoctor Simulator");
        panel.requestFocus();
    }



}
