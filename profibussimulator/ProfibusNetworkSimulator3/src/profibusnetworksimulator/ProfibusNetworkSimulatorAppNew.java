/*
 * ProfibusNetworkSimulatorApp.java
 */

package profibusnetworksimulator;

import org.jdesktop.application.SingleFrameApplication;
import view.fdl.FDLInternalFrame;

/**
 * The main class of the application.
 */
public class ProfibusNetworkSimulatorAppNew extends SingleFrameApplication {

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        FDLInternalFrame frame = new FDLInternalFrame();
        frame.setVisible(true);
    }

    @Override
    protected void startup() {
        
    }
}
