/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package common.signals;

import dataunits.SlaveDiagnostic;

/**
 *
 * @author emossin
 */
public class TestSignals {

    public static void main(String[] args) {
       
        CabeamentoLongo cl = new CabeamentoLongo();
        CurtoCircuitoAB cab = new CurtoCircuitoAB();
        ExcessoTerminadores et = new ExcessoTerminadores();
        FaltaTerminadoresOuCaboQuebrado ftcq = new FaltaTerminadoresOuCaboQuebrado();
        IMC imc = new IMC();
        //Sinal com rede funcionando normalmente
        Signal frame = new Signal();
        TerminadorSemAlimentacaoEnergia tsae = new TerminadorSemAlimentacaoEnergia();

        //loop para criar alguns sinais de cada tipo
        for (int i = 1; i < 40; i++) {
            //quando o i for par, ele cria os sinais com nível de tensão maior que 0!!!
            if (i%2 != 0) {
                frame.setSignalType(true);
                cab.setSignalType(true);
                cl.setSignalType(true);
                ftcq.setSignalType(true);
                et.setSignalType(true);
                imc.setSignalType(true);
                tsae.setSignalType(true);
            } else {
                frame.setSignalType(false);
                cab.setSignalType(false);
                cl.setSignalType(false);
                ftcq.setSignalType(false);
                et.setSignalType(false);
                imc.setSignalType(false);
                tsae.setSignalType(false);
            }

            //criação dos samples de cada classes.
            frame.createSamples((float) 3);
            ftcq.createSamples((float) 5.4);
            cl.createSamples((float) 5.4);
            et.createSamples((float) 5.4);
            imc.createSamples((float) 5.4);
            tsae.createSamples(0);
            cab.createSamples((float) 5.4);

            //mostrando os samples criados na saída. Útil para colar no excel e ver
            //o gráfico que é formado.
            System.out.println(frame.toString());
            //System.out.println(ftcq.toString());
            //System.out.println(cl.toString());
            //System.out.println(cc.toString());
            //System.out.println(et.toString());
            //System.out.println(imc.toString());
            //System.out.println(tsae.toString());
            //System.out.println(cab.toString());
        }

        
        /*
        boolean isSamples = false;

        //gerando amostras RNA ************************
        for (int i = 1; i < 20; i++) {
            frame.createSamples((float) 4.5 + (float) Math.random() * 2);
            if (isSamples) {
                System.out.println("-1, " + frame.toString());
            } else {
                System.out.println("0,0,0,0,0,1");
            }
        }

        for (int i = 1; i < 20; i++) {
            ftcq.createSamples((float) 5.4);
            if (isSamples) {
                System.out.println("-1, " + ftcq.toString());
            } else {
                System.out.println("0,0,0,0,1,0");
            }
        }

        for (int i = 1; i < 20; i++) {
            cl.createSamples((float) 5.4);
            if (isSamples) {
                System.out.println("-1, " + cl.toString());
            } else {
                System.out.println("0,0,0,1,0,0");
            }
        }


        for (int i = 1; i < 20; i++) {
            et.createSamples((float) 5.4);
            if (isSamples) {
                System.out.println("-1, " + et.toString());
            } else {
                System.out.println("0,0,1,0,0,0");
            }
        }

        for (int i = 1; i < 20; i++) {
            tsae.createSamples(0);
            if (isSamples) {
                System.out.println("-1, " + tsae.toString());
            } else {
                System.out.println("0,1,0,0,0,0");
            }
        }

        for (int i = 1; i < 20; i++) {
            cab.createSamples((float) 5.4);
            if (isSamples) {
                System.out.println("-1, " + cab.toString());
            } else {
                System.out.println("1,0,0,0,0,0");
            }
        }

        /*
        for (int i = 1; i < 20; i++) {
        imc.createSamples((float) 5.4);
        System.out.println("-1," + imc.toString());
        }*/
    }
}
