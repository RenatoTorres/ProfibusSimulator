/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stackDP;

/**
 *
 * @author renato_veiga
 */
public class Timer {
    
    boolean bisrunning = false;
    private int timerValue;

    public void startTimer( )
    {
        resetTimer( );
    }
    
    public void resetTimer( )
    {
        bisrunning = true;
    }
    
    public boolean isRunning( )
    {
        return bisrunning;
    }
            
    public boolean isEnd() 
    {
        if( isRunning() == false )
            startTimer();
        
        return true;
    }

    void setTimerValue(int value) {
        timerValue = value;
    }

    void setTimerValueAndStart(int value) {
        timerValue = value;
        startTimer();
    }
    
    
}
