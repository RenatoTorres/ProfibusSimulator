/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package view.fdl;

import javax.swing.ImageIcon;
import view.liveListTree.LiveListStatus;

/**
 *
 * @author emossin
 */
public class SlaveObj {


    private int address = -1;
    private LiveListStatus status = new LiveListStatus();

    public SlaveObj(){
        status.setStatus(LiveListStatus.OFFLINE);
    }

    public void setAddress (int address){
        this.address = address;
    }

    public int getAddress(){
        return address;
    }


    public void setStatus (int statusVal){
        status.setStatus((byte)statusVal);
    }

    public ImageIcon getIcon(){
        return status.getLargeIcon();
    }

}
