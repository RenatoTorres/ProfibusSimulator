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
public class FaltaTerminadoresOuCaboQuebrado extends Signal{
    
    public byte[] createSamples(float voltLevel) {
        byte[] bitAmostrado = new byte[4];
        byte samplesArrayIndex = 0;
        float factorLow = (float)-0.5;
        float factorHigh = (float)-0.78;
        float factorVeryHigh = (float)-1;
        
        voltLevel = getSignalValue(voltLevel);
        if(voltLevel>0){
            factorLow = factorLow*-1;
            factorHigh = factorHigh*-1;
            factorVeryHigh = factorVeryHigh*-1;
        }
        
        //dividi-se o sinal em 4 partes. Se temos 20 amostras, cada parte possui 5 amostras.
        int eachPart = NUM_OF_SAMPLES_PER_BIT/4;
        
        //parte 1
        for (int is = 0; is < eachPart; is++) {
            bitAmostrado = Conversions.toByta(voltLevel/2-factorLow+(float) Math.random()/10);
            samplesArray[samplesArrayIndex] = bitAmostrado[0];
            samplesArrayIndex++;
            samplesArray[samplesArrayIndex] = bitAmostrado[1];
            samplesArrayIndex++;
            samplesArray[samplesArrayIndex] = bitAmostrado[2];
            samplesArrayIndex++;
            samplesArray[samplesArrayIndex] = bitAmostrado[3];
            samplesArrayIndex++;
        }
        
                //parte 2
        for (int is = eachPart; is < 2*eachPart; is++) {
            bitAmostrado = Conversions.toByta(voltLevel/2+factorVeryHigh+(float) Math.random()/10);
            samplesArray[samplesArrayIndex] = bitAmostrado[0];
            samplesArrayIndex++;
            samplesArray[samplesArrayIndex] = bitAmostrado[1];
            samplesArrayIndex++;
            samplesArray[samplesArrayIndex] = bitAmostrado[2];
            samplesArrayIndex++;
            samplesArray[samplesArrayIndex] = bitAmostrado[3];
            samplesArrayIndex++;
        }
        
                //parte 3 e 4
        for (int is = 2*eachPart; is < 4*eachPart; is++) {
            bitAmostrado = Conversions.toByta(voltLevel/2+factorHigh+(float) Math.random()/10);
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
    
   public float getSignalValue(float voltLevelParam) {
        float voltLevel = voltLevelParam;
        if (!isHigh) {
            voltLevel = voltLevel*-1;
        }
        return voltLevel;
    }

}

