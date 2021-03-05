/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package view;

import java.net.URL;
import javax.swing.ImageIcon;

/**
 *
 * @author eduardo_mossin
 */
public class SimulationSlavePanel  extends SlavePanel{

    @Override
    public void changeStatus() {

        if(isIsOn()){
            setIsOn(false);
        }else{
            setIsOn(true);
        }

        setIcon();
    }


    

}
