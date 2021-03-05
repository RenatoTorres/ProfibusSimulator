/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package exceptions;

/**
 *
 * @author emossin
 */
public class ProfibusFrameException extends Exception{
    
    private int wrongFieldValue;
    
    public ProfibusFrameException (int wrongFieldValueParam, String msg){
        super(msg);
        wrongFieldValue = wrongFieldValueParam;
    }
    
    
    public int getWrongFieldValue(){
        return wrongFieldValue;
    }
}
