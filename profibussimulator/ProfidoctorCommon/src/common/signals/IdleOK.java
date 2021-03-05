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
public class IdleOK extends Signal{
   
    public byte[] createSamples(float voltLevel) {
        byte[] bitAmostrado = new byte[4];
        byte samplesArrayIndex = 0;
        voltLevel = voltLevel - 0.15f;

        for (int is = 0; is < NUM_OF_SAMPLES_PER_BIT; is++) {
            double randiator = Math.random();
            if(randiator>0.1 && randiator<0.3){
                bitAmostrado = Conversions.toByta(voltLevel/2 + (float) Math.random()/10);
            }
            if(randiator<0.9 && randiator>0.7){
                bitAmostrado = Conversions.toByta(voltLevel/2 - (float) Math.random()/10);
            }
            
            if(randiator>=0.3 && randiator<=0.7){
                bitAmostrado = Conversions.toByta(voltLevel/2+ (float) Math.random()/20);
            }
            
            if(randiator <=0.1){
                bitAmostrado = Conversions.toByta(voltLevel/2 - (float) Math.random()/4);
            }
            
            if(randiator >=0.9){
                bitAmostrado = Conversions.toByta(voltLevel/2 + (float) Math.random()/2);
            }
            
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

    public static void main(String[] args) {
        IdleOK i = new IdleOK();
     i.plotToMatlab();
    }
    
        
       public void samplesToRNA() {
        createSamples(1.8f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(1.84f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(1.92f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(1.96f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(2f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(2.04f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(2.08f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(2.12f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(2.16f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(2.13f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(1.87f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(1.92f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(1.91f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(1.99f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(1.98f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(2.01f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(2.02f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(2.03f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(1.93f);
        System.out.println(toString().substring(0,toString().length()-1));
        createSamples(1.94f);
        System.out.println(toString().substring(0,toString().length()-1));
    }
       
    public void plotToMatlab(){
        createSamples(1.8f);
        System.out.println("x1 = ["+ toString() + "];");
        createSamples(1.85f);
        System.out.println("x2 = ["+ toString() + "];");
        createSamples(1.9f);
        System.out.println("x3 = ["+ toString() + "];");
        createSamples(2f);
        System.out.println("x4 = ["+ toString() + "];");
        createSamples(2.05f);
        System.out.println("x5 = ["+ toString() + "];");
        createSamples(2.1f);
        System.out.println("x6 = ["+ toString() + "];");
        createSamples(1.98f);
        System.out.println("x7 = ["+ toString() + "];");
        createSamples(1.95f);
        System.out.println("x8 = ["+ toString() + "];");
        createSamples(2.03f);
        System.out.println("x9 = ["+ toString() + "];");
        createSamples(2.06f);
        System.out.println("x10 = ["+ toString() + "];");
        System.out.println("v=[var(x1),var(x2),var(x3),var(x4),var(x5),var(x6),var(x7),var(x8),var(x9),var(x10)]");
     System.out.println("v=[mean(x1),mean(x2),mean(x3),mean(x4),mean(x5),mean(x6),mean(x7),mean(x8),mean(x9),mean(x10)]");
        System.out.println("plot(x1, 'b');");
        System.out.println("hold");
        System.out.println("plot(x2, 'b');");
        System.out.println("plot(x3, 'b');");
        System.out.println("plot(x4, 'b');");
        System.out.println("plot(x5, 'b');");
        System.out.println("plot(x6, 'b');");
        System.out.println("plot(x7, 'b');");
        System.out.println("plot(x8, 'b');");
        System.out.println("plot(x9, 'b');");
        System.out.println("plot(x10, 'b');");
        System.out.println("plot(x0, 'r');");
    }
    
        
}

