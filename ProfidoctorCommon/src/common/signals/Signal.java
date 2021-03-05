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
public class Signal {

    public final static int NUM_OF_SAMPLES_PER_BIT = 20;
    public final static float LOW_SIGNAL_DEFAULT = (float)-2.8;
    byte samplesArray[] = new byte[NUM_OF_SAMPLES_PER_BIT * 4];
    boolean isHigh = true;

    public void setSignalType(boolean isHigh) {
        this.isHigh = isHigh;
    }

    public float getSignalValue(float voltLevel) {
        float signalType = voltLevel;
        if (!isHigh) {
            signalType = voltLevel/-2;
        }else{
            signalType = voltLevel/2;
        }
        return signalType;
    }

    public byte[] createSamples(float voltLevel) {
        byte[] bitAmostrado = new byte[4];
        byte samplesArrayIndex = 0;
        float signalType = getSignalValue(voltLevel);

        for (int is = 0; is < NUM_OF_SAMPLES_PER_BIT; is++) {
            bitAmostrado = Conversions.toByta(signalType + ((float) Math.random() / 10));
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
