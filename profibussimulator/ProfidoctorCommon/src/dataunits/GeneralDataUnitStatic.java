/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataunits;

import common.framestructure.ProfibusFrame;
import common.framestructure.Service;

/**
 *
 * @author Renato Veiga
 */
public class GeneralDataUnitStatic {
    
   static public GeneralDataUnit DecodeDataUnit(ProfibusFrame pf)
    {
        if( pf.getData_Unit() != null)
        {
            switch(pf.getFrameService())
            {
                case Service.PB_REQ_GET_CFG:
                case Service.PB_RES_CHK_CFG:
                    return (new CheckConfiguration(pf.getData_Unit()));

                case Service.PB_SET_SLAVE_ADDR:
                    return (new SetAddress(pf.getData_Unit()));

                case Service.PB_REQ_SET_PRM:
                    return (new SetParameter(pf.getData_Unit()));

                case Service.PB_RES_DATA_EXCHANGE:
                    return (new DataExchange(pf.getData_Unit()));

                case Service.PB_RES_GET_DIAG:
                    return (new SlaveDiagnostic(pf.getData_Unit()));

                case Service.PB_RES_RD_INPUTS:
                    return (new DataExchange(pf.getData_Unit()));

                case Service.PB_RES_RD_OUTPUTS:
                    return (new DataExchange(pf.getData_Unit()));

                default:
                    return (new GeneralDataUnit());
            }
        }
        else
        {
            return null;
        }
    }    
}
