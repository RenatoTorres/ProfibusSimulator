/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package common.framestructure;

import common.ConversionsAdvanced;

/**
 *
 * @author Renato Veiga Torres
 */
public class ProfibusServices {

    public static ProfibusFrame SendRequestFDLStatus(int srcaddress, int dstaddress)
    {
        return (new ProfibusFrame(dstaddress, srcaddress, Service.PB_REQ_FDL_STATUS));
    }
    
    public static ProfibusFrame SendResponseFDLStatus(int srcaddress, int dstaddress )
    {
        return new ProfibusFrame(srcaddress, dstaddress, Service.PB_RES_FDL_STATUS);
    }

    public static ProfibusFrame SendServiceRequest(int srcaddress, int dstaddress, Service serv )
    {
        return (new ProfibusFrame(srcaddress, dstaddress, serv.getService()));
    }

    public static ProfibusFrame SendSCResponse( )
    {
        return ( new ProfibusFrame(new SD(SD.SC)) );
    }

    public static ProfibusFrame SendTokenPass(int address, int address0) {
         return (new ProfibusFrame(address, address0, Service.PB_REQ_TOKEN_PASS ));
    }
}
