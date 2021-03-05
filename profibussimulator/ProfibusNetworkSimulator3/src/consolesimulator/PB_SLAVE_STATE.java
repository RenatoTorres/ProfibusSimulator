/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package consolesimulator;

/**
 *
 * @author Veiga
 */
public class PB_SLAVE_STATE {
    public static final byte SSM_GETDIAG_REQUEST         = (byte) 0x01;
    public static final byte SSM_GETCONFIG_REQUEST       = (byte) 0x02;
    public static final byte SSM_SETADDR_REQUEST         = (byte) 0x03;
    public static final byte SSM_SETPRM_REQUEST          = (byte) 0x04;
    public static final byte SSM_CHECKCFG_REQUEST        = (byte) 0x05;
    public static final byte SSM_RDINPUTS_REQUEST        = (byte) 0x06;
    public static final byte SSM_RDOUTPUTS_REQUEST       = (byte) 0x07;
    public static final byte SSM_LEAVEMASTER_REQUEST     = (byte) 0x08;
    public static final byte SSM_DATAEXCHANGE_REQUEST    = (byte) 0x09;
}
