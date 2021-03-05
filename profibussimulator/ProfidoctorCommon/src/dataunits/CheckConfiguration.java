/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dataunits;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 *
 * @author renato_veiga
 */
public class CheckConfiguration extends GeneralDataUnit {
    ArrayList Identifiers;
    private byte[] Data_Unit;
    int input_size;
    int output_size;

    public byte[] getData_Unit() {
        return Data_Unit;
    }

    class Identifier
    {
        int Input_Output_Length;
        boolean isSpecial;
        boolean isInput;
        boolean isOutput;
        boolean isWordFormat;
        boolean isWholeConsistency;
    }

    class SpecialFormatIdentifier extends Identifier {
        int Input_Length;
        int Output_Length;
        boolean isInputWordFormat;
        boolean isOutputWordFormat;
        boolean isInputWholeConsistency;
        boolean isOutputWholeConsistency;
        int ManDataLength;
        byte ManufSpecificData[];
    }

    public CheckConfiguration( byte[] DU ) {
        int DULength = (DU.length < 246) ? DU.length : 1;
        Data_Unit = new byte[DULength];
        System.arraycopy(DU, 0,Data_Unit,0, DULength );
        Decode( Data_Unit );
    }

    private void Decode(byte[] Data_Unit) {
        int i = 0;
        
        if( Data_Unit.length != 0 )
        {
            Identifiers = new ArrayList();
        }

        while(i < Data_Unit.length)
        {
            byte current_byte = Data_Unit[i];

            if( (current_byte & 0x30) != 0x00 ) {
                //Simple Format Identifier
                Identifier Id = new Identifier();
                Id.isSpecial = false;
                Id.Input_Output_Length = ((int) (Data_Unit[i] & 0x0F)) + 1;
                Id.isInput  = ((current_byte & 0x10) != 0);
                Id.isOutput = ((current_byte & 0x20) != 0);
                Id.isWordFormat = ((current_byte & 0x40) != 0);
                Id.isWholeConsistency = ((current_byte & 0x80) != 0);
                
                if( Id.isInput == true )
                {
                    input_size += ((Id.isWordFormat == true) ? 2 : 1)*Id.Input_Output_Length;
                }
                
                if( Id.isOutput == true )
                {
                     output_size += ((Id.isWordFormat == true) ? 2 : 1)*Id.Input_Output_Length;
                }
            }
            else {
                //Special Format Identifier
                SpecialFormatIdentifier Id = new SpecialFormatIdentifier();
                Id.isSpecial = true;
                
                Id.ManDataLength = ((int) (Data_Unit[i] & 0x0F));
                
                if( (Id.ManDataLength == 0) || (Id.ManDataLength == 15) )
                    Id.ManDataLength = 0;
                else
                    Id.ManufSpecificData = new byte[Id.ManDataLength];
                
                Id.isInput  = ((current_byte & 0x40) != 0) ? true : false;
                Id.isOutput = ((current_byte & 0x80) != 0) ? true : false;

                if( (Id.isOutput == true) && (Data_Unit.length > (i+1)) ) {
                    byte idbyte = Data_Unit[++i];
                    Id.Output_Length = (idbyte & 0x1F) + 1;
                    Id.isOutputWordFormat = ((idbyte & 0x40) != 0) ? true : false;
                    Id.isOutputWholeConsistency = ((idbyte & 0x80) != 0) ? true : false;
                    
                    output_size += ((Id.isWordFormat == true) ? 2 : 1)*Id.Output_Length;
                    
                }

                if( Id.isInput == true && (Data_Unit.length > (i+1)) ) {
                    byte idbyte = Data_Unit[++i];
                    Id.Input_Length = (idbyte & 0x1F) + 1;
                    Id.isInputWordFormat = ((idbyte & 0x40) != 0) ? true : false;
                    Id.isInputWholeConsistency = ((idbyte & 0x80) != 0) ? true : false;
                
                    input_size += ((Id.isWordFormat == true) ? 2 : 1)*Id.Input_Length;
                    
                }

                for(int j=i, k=0; k < Id.ManDataLength; k++, j++, i++) {
                    if( (Data_Unit.length > j) && (Id.ManufSpecificData.length > k) )
                        Id.ManufSpecificData[k] = Data_Unit[j];
                }

                Identifiers.add(Id);
            }
            i++;
        }
    }
    
    public int getCfgInputSize( )
    {
        return input_size;
    }

    public int getCfgOutputSize( )
    {
        return output_size;
    }
    
    public byte[] getBytes( )
    {
        byte tmp[] = new byte[244];
        ListIterator LI = Identifiers.listIterator();

        while( LI.hasNext() )
        {
            Identifier Id = (Identifier) LI.next();

            if(Id.isSpecial == true)
            {

            }
            else
            {

            }
        }

        return tmp;
    }


}
