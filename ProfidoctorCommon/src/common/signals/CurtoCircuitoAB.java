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
public class CurtoCircuitoAB extends Signal{
    public byte[] createSamples(float voltLevel) {
        byte[] bitAmostrado = new byte[4];
        byte samplesArrayIndex = 0;
        float factor = (float)-0.8;
        
        voltLevel = getSignalValue(voltLevel);
        if(voltLevel>0){
            factor = factor*-1;
        }
        
        //dividi-se o sinal em 5 partes. Se temos 20 amostras, cada parte possui 4 amostras.
        int eachPart = NUM_OF_SAMPLES_PER_BIT/5;
        
        //parte 1
        for (int is = 0; is < eachPart; is++) {
            bitAmostrado = Conversions.toByta(voltLevel+(float) Math.random()/8);
            samplesArray[samplesArrayIndex] = bitAmostrado[0];
            samplesArrayIndex++;
            samplesArray[samplesArrayIndex] = bitAmostrado[1];
            samplesArrayIndex++;
            samplesArray[samplesArrayIndex] = bitAmostrado[2];
            samplesArrayIndex++;
            samplesArray[samplesArrayIndex] = bitAmostrado[3];
            samplesArrayIndex++;
        }
        
        
                //parte 2 a 5
        float signalDecrement = 0;
        for (int is = eachPart; is < 5*eachPart; is++) {
            bitAmostrado = Conversions.toByta(voltLevel/2+factor+(float) Math.random()/10 + signalDecrement);
            samplesArray[samplesArrayIndex] = bitAmostrado[0];
            samplesArrayIndex++;
            samplesArray[samplesArrayIndex] = bitAmostrado[1];
            samplesArrayIndex++;
            samplesArray[samplesArrayIndex] = bitAmostrado[2];
            samplesArrayIndex++;
            samplesArray[samplesArrayIndex] = bitAmostrado[3];
            samplesArrayIndex++;
            
            
            if(voltLevel > 0){
                signalDecrement = signalDecrement - (float)0.1;
            }else{
                signalDecrement = signalDecrement + (float)0.1;
            }
        }

        return samplesArray;
    }
    
        public float getSignalValue(float voltLevelParam) {
        float voltLevel = voltLevelParam;
        if (!isHigh) {
            voltLevel = voltLevel*-1;
        }
        return voltLevel;
    }

}

