/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package common.signals;

import common.Conversions;

/**
 *
 * @author emossin
 */
public class ExcessoTerminadores extends Signal{
    
    
    public byte[] createSamples(float voltLevel) {
        super.createSamples(voltLevel);
        byte[] bitAmostrado = new byte[4];
        float factor = (float)0.5;
        byte samplesArrayIndex = 0;
        voltLevel = getSignalValue(voltLevel);
        if(voltLevel<0){
            factor = factor*-1;
        }
        
        
        //os dois primeiros bits
        for (int is = 0; is < 2; is++) {
            bitAmostrado = Conversions.toByta(voltLevel+factor+(float) Math.random()/10);
            samplesArray[samplesArrayIndex] = bitAmostrado[0];
            samplesArrayIndex++;
            samplesArray[samplesArrayIndex] = bitAmostrado[1];
            samplesArrayIndex++;
            samplesArray[samplesArrayIndex] = bitAmostrado[2];
            samplesArrayIndex++;
            samplesArray[samplesArrayIndex] = bitAmostrado[3];
            samplesArrayIndex++;
        }
        
         
        return samplesArray;
    }
    
}

