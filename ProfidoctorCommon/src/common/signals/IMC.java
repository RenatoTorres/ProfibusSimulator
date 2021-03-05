/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package common.signals;

import common.Conversions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author emossin
 */
public class IMC extends Signal{

    public float getSignalValue() {
        float signalType = 1;
        if (Math.random() > (float)0.5) {
            signalType = -1;
        }else{
            signalType = 1;
        }
        return signalType;
    }
    

    public byte[] createSamples(float voltLevel) {
        byte[] bitAmostrado = new byte[4];
        byte samplesArrayIndex = 0;
        float signalType = getSignalValue(voltLevel);
        

        for (int is = 0; is < NUM_OF_SAMPLES_PER_BIT; is++) {
            bitAmostrado = Conversions.toByta((float) Math.random()*3*getSignalValue());
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


    @Override
    public String toString() {
        String ret = new String();
        StringBuilder retB = new StringBuilder();
        ArrayList<Float> oneBitSamples = new ArrayList<Float>();
        byte messageIndex = 0;
        for (int is = 0; is < NUM_OF_SAMPLES_PER_BIT; is++) {
            
            byte[] bitAmostrado = new byte[4];
            bitAmostrado[0] = samplesArray[messageIndex];
            messageIndex++;
            bitAmostrado[1] = samplesArray[messageIndex];
            messageIndex++;
            bitAmostrado[2] = samplesArray[messageIndex];
            messageIndex++;
            bitAmostrado[3] = samplesArray[messageIndex];
            messageIndex++;
            oneBitSamples.add(Conversions.toFloat(bitAmostrado));
        }
        
        Iterator oneBitSamplesIt = oneBitSamples.iterator();
        while (oneBitSamplesIt.hasNext()){
            retB.append(Float.toString((Float)oneBitSamplesIt.next())+",");
        }
        ret = retB.substring(0,retB.length()-1);
        return ret;
    }
}
