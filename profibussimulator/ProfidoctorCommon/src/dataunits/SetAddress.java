/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataunits;

/**
 *
 * @author Renato Veiga
 */
public class SetAddress  extends GeneralDataUnit {
    private int new_addr;
    private byte IdentNumber_high;
    private byte IdentNumber_low;
    private byte further_change_allowed = 0;
    private byte[] user_data = null;
    private byte[] Data_Unit = new byte[4];
    
    public SetAddress( byte[] DU) 
    {
        if( DU.length > 0 )
        {
            
        }
    }
    
    public SetAddress(  ) 
    {
        
    }

    /**
     * @return the new_addr
     */
    public int getNew_addr() {
        return new_addr;
    }

    /**
     * @param new_addr the new_addr to set
     */
    public void setNew_addr(int new_addr) {
        this.new_addr = new_addr;
        Encode();
    }

    /**
     * @return the IdentNumber
     */
    public int getIdentNumber() {
        return (IdentNumber_high < 0 ? 256+IdentNumber_high : IdentNumber_high)*256 + (IdentNumber_low < 0 ? 256+IdentNumber_low : IdentNumber_low);
    }

    /**
     * @return the IdentNumber_high
     */
    public byte getIdentNumber_high() {
        return IdentNumber_high;
    }

    /**
     * @param IdentNumber_high the IdentNumber_high to set
     */
    public void setIdentNumber_high(byte IdentNumber_high) {
        this.IdentNumber_high = IdentNumber_high;
        Encode();
    }

    /**
     * @return the IdentNumber_low
     */
    public byte getIdentNumber_low() {
        return IdentNumber_low;
    }

    /**
     * @param IdentNumber_low the IdentNumber_low to set
     */
    public void setIdentNumber_low(byte IdentNumber_low) {
        this.IdentNumber_low = IdentNumber_low;
        Encode();
    }

    /**
     * @return the further_change_allowed
     */
    public byte getFurther_change_allowed() {
        return further_change_allowed;
    }

    /**
     * @param further_change_allowed the further_change_allowed to set
     */
    public void setFurther_change_allowed(byte further_change_allowed) {
        this.further_change_allowed = further_change_allowed;
        Encode();
    }

    /**
     * @return the bfurther_change_allowed
     */
    public boolean isBfurther_change_allowed() {
        return (further_change_allowed == 0 ? true : false);
    }

    /**
     * @param bfurther_change_allowed the bfurther_change_allowed to set
     */
    public void setBfurther_change_allowed(boolean bfurther_change_allowed) {
        if( bfurther_change_allowed == true )
        {
            further_change_allowed = 0;
        }
        else
        {
            further_change_allowed = 1;
        }
        Encode();
    }

    /**
     * @return the user_data
     */
    public byte[] getUser_data() {
        return user_data;
    }

    /**
     * @param user_data the user_data to set
     */
    public void setUser_data(byte[] user_data) {
        this.user_data = user_data;
        Encode();
    }

    private void Encode() {
        getData_Unit()[0] = (byte) new_addr;
        getData_Unit()[1] = IdentNumber_high;
        getData_Unit()[2] = IdentNumber_low;
        getData_Unit()[3] = further_change_allowed;
        
        if(user_data != null )
        {
            setData_Unit(new byte[user_data.length + 4]);
            getData_Unit()[0] = (byte) new_addr;
            getData_Unit()[1] = IdentNumber_high;
            getData_Unit()[2] = IdentNumber_low;
            getData_Unit()[3] = further_change_allowed;
            
            int k=4;
            for(int i=0; i < user_data.length; i++, k++ )
                getData_Unit()[k] = user_data[i];
        }
    }

    /**
     * @return the Data_Unit
     */
    public byte[] getData_Unit() {
        return Data_Unit;
    }

    /**
     * @param Data_Unit the Data_Unit to set
     */
    public void setData_Unit(byte[] Data_Unit) {
        this.Data_Unit = Data_Unit;
    }
    
    public String toHTML( )
    {
        return "";
    }
    
    
    
}
