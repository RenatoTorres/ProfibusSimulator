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
public class TerminadorSemAlimentacaoEnergia extends Signal {

    public byte[] createSamples(float voltLevel) {
        byte[] bitAmostrado = new byte[4];
        byte samplesArrayIndex = 0;
        float factor = 1;
        
        if(Math.random() > 0.5f){
            factor = -1;
        }

        for (int is = 0; is < NUM_OF_SAMPLES_PER_BIT; is++) {
            float sample = 0.5f + factor * (float) Math.random() / 10;
            
            bitAmostrado = Conversions.toByta(sample);
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
