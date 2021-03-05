/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package common.signals;

import common.Conversions;
import java.util.ArrayList;

/**
 *
 * @author emossin
 */
public class CabeamentoLongo extends Signal {

     public byte[] createSamples(float voltLevel) {
        super.createSamples(voltLevel);
        byte[] bitAmostrado = new byte[4];
        float factor = (float)1;
        
        //guarda todos os novos bit values e depois no looping converte pra byte array
        ArrayList bitVaules = new ArrayList(11);
        
        byte samplesArrayIndex = 9*4;
        voltLevel = getSignalValue(voltLevel);
        if(voltLevel>0){
            factor = factor*-1;
        }
        
        if(voltLevel<0){
        //valores aproximados a serem adicionados
        //-2,43 | -2,25 | -1,8 | -1,1 | -0,7 | -0,1 | 1,2 | 2 | 2,5 | 2,6582434 | 2,6906602
        bitVaules.add(0, new Float(voltLevel+ (factor * 0.3) +(float) Math.random()/10)); 
        bitVaules.add(1, new Float(voltLevel+ (factor * 0.5)+(float) Math.random()/10));
        bitVaules.add(2, new Float(voltLevel+ (factor * 0.9)+(float) Math.random()/10));
        bitVaules.add(3, new Float(voltLevel+ (factor * 1.6)+(float) Math.random()/10));
        bitVaules.add(4, new Float(voltLevel+ (factor * 2)+(float) Math.random()/10));
        bitVaules.add(5, new Float(voltLevel+ (factor * 2.6)+(float) Math.random()/10));
        bitVaules.add(6, new Float(voltLevel+ (factor * 3.9)+(float) Math.random()/10));
        bitVaules.add(7, new Float(voltLevel+ (factor * 4.7)+(float) Math.random()/10));
        bitVaules.add(8, new Float(voltLevel+ (factor * 5.2)+(float) Math.random()/10));
        bitVaules.add(9, new Float(voltLevel+ (factor * 5.3)+(float) Math.random()/10));
        bitVaules.add(10, new Float(voltLevel+ (factor * 5.35)+(float) Math.random()/10));
        }else{
                    //valores aproximados a serem adicionados
        //1,8 | 0,7 | 0,1 | -0,5 | -1 | -1,6 | -2 | -2,4 | -2,5349383 | -2,6205432 | -2,6690047
        bitVaules.add(0, new Float(voltLevel+ (factor * 0.9) +(float) Math.random()/10)); 
        bitVaules.add(1, new Float(voltLevel+ (factor * 2)+(float) Math.random()/10));
        bitVaules.add(2, new Float(voltLevel+ (factor * 2.6)+(float) Math.random()/10));
        bitVaules.add(3, new Float(voltLevel+ (factor * 3.3)+(float) Math.random()/10));
        bitVaules.add(4, new Float(voltLevel+ (factor * 3.7)+(float) Math.random()/10));
        bitVaules.add(5, new Float(voltLevel+ (factor * 4.3)+(float) Math.random()/10));
        bitVaules.add(6, new Float(voltLevel+ (factor * 4.7)+(float) Math.random()/10));
        bitVaules.add(7, new Float(voltLevel+ (factor * 5)+(float) Math.random()/10));
        bitVaules.add(8, new Float(voltLevel+ (factor * 5.1)+(float) Math.random()/10));
        bitVaules.add(9, new Float(voltLevel+ (factor * 5.2)+(float) Math.random()/10));
        bitVaules.add(10, new Float(voltLevel+ (factor * 5.35)+(float) Math.random()/10));

        }
        
        //os 8 ultimos bits
        for (int is = 0; is <11; is++) {
            bitAmostrado = Conversions.toByta((Float)bitVaules.get(is));
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
