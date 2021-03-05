/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package view.liveListTree;

import java.net.URL;
import javax.swing.ImageIcon;

/**
 *
 * @author emossin
 */
public class LiveListStatus {

    public static final byte MASTER = 0;
    public static final byte OFFLINE = 1;
    public static final byte ONLINE = 2;
    public static final byte PROBLEM = 3;
    public static String rootIconPath = "view/resources/";
    private byte status = OFFLINE;

    public ImageIcon getSmallIcon() {
        URL url = ClassLoader.getSystemResource(rootIconPath + "deviceON16.GIF");
        if (isMaster()) {
            url = ClassLoader.getSystemResource(rootIconPath + "deviceMaster16.GIF");
        } else if (isOFF()) {
            url = ClassLoader.getSystemResource(rootIconPath + "deviceOFF16.GIF");
        } else if (hasProblem()) {
            url = ClassLoader.getSystemResource(rootIconPath + "deviceERROR16.GIF");
        } else if (isON()) {
            url = ClassLoader.getSystemResource(rootIconPath + "deviceON16.GIF");
        } else {
            url = ClassLoader.getSystemResource(rootIconPath + "deviceOFF16.GIF");
        }
        ImageIcon icon = new ImageIcon(url);
        return icon;
    }

    public ImageIcon getLargeIcon() {
        URL url = ClassLoader.getSystemResource(rootIconPath + "deviceON.GIF");
        if (isMaster()) {
            url = ClassLoader.getSystemResource(rootIconPath + "deviceMaster.GIF");
        } else if (isOFF()) {
            url = ClassLoader.getSystemResource(rootIconPath + "deviceOFF.GIF");
        } else if (hasProblem()) {
            url = ClassLoader.getSystemResource(rootIconPath + "deviceERROR.GIF");
        } else if (isON()) {
            url = ClassLoader.getSystemResource(rootIconPath + "deviceON.GIF");
        } else {
            url = ClassLoader.getSystemResource(rootIconPath + "deviceOFF.GIF");
        }
        ImageIcon icon = new ImageIcon(url);
        return icon;
    }

    /**
     * @return the hasError
     */
    public boolean hasProblem() {
        boolean ret = false;
        if (status == PROBLEM) {
            ret = true;
        }
        return ret;
    }

    public boolean isMaster() {
        boolean ret = false;
        if (status == MASTER) {
            ret = true;
        }
        return ret;
    }

    public boolean isON() {
        boolean ret = false;
        if (status == ONLINE) {
            ret = true;
        }
        return ret;

    }

    public boolean isOFF() {
        boolean ret = false;
        if (status == OFFLINE) {
            ret = true;
        }
        return ret;

    }

    public void setStatus(byte status){
        this.status = status;

    }
}
