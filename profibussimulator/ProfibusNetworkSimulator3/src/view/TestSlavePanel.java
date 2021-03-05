/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package view;

import java.awt.Frame;

/**
 *
 * @author Veiga
 */
public class TestSlavePanel {

    public static void main(String[] args) {
        Frame f = new Frame("Test Slave Panel");
        SlavesPanel c = new SlavesPanel();
        c.startIcons();
        f.add(c);
        f.setVisible(true);
    }
}
