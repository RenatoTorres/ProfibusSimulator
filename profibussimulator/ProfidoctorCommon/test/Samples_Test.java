
import common.framestructure.ProfibusFrame;
import common.framestructure.ProfibusServices;
import java.io.File;
import org.LiveGraph.LiveGraph;
import org.LiveGraph.dataFile.write.DataStreamWriter;
import org.LiveGraph.dataFile.write.DataStreamWriterFactory;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author renato_veiga
 */
public class Samples_Test 
{
    public static final String DEMO_DIR = System.getProperty("user.dir");
    //public static ProfibusFrame pb = ProfibusServices.SendServiceRequest(1, 34, new Service(Service.PB_REQ_GET_DIAG));
    public static ProfibusFrame pb = ProfibusServices.SendResponseFDLStatus(1, 10);
    public static float[][] fsamples;
    public static final String FILE_NAME = "FRAME_SAMPLES";
    
    public static void SetDataSamples( )
    {
        //Remove older files
        File f = new File(FILE_NAME + ".lgdat");
        if( f.exists() == true )
            f.delete();
        
        //Remove older files
        File g = new File(FILE_NAME + ".png");
        if( g.exists() == true )
            g.delete();
        
        
        // Setup a data writer object:
        DataStreamWriter out = DataStreamWriterFactory.createDataWriter(DEMO_DIR, FILE_NAME);

        System.out.println( DEMO_DIR );

        // Set a values separator:
        out.setSeparator(";");

        // Add a file description line:
        out.writeFileInfo("Frame Samples for: " + pb.toString() );

        // Set-up the data series:
        out.addDataSeries( "Time"  );
        out.addDataSeries( "Value" );

        for (int b = 0; b < fsamples.length; b++)
        {
            out.setDataValue( fsamples[b][0] );
            out.setDataValue( fsamples[b][1] );

            // Write dataset to disk:
            out.writeDataSet();

            // Check for IOErrors:      
            if (out.hadIOException()) {
                out.getIOException().printStackTrace();
                out.resetIOException();
            }
        }
        // Finish:
        out.close();
        System.out.println("Frame graph file finished!");
    }
    
    public static void main(String[] unusedArgs) {
        final int BAUD_RATE = 6000000;
        fsamples = pb.BuildSignalDataSet(BAUD_RATE, 50);        
        SetDataSamples( );
        
        File f = new File(FILE_NAME + ".png");

        LiveGraph app = LiveGraph.application();
        app.execStandalone(new String[] {"-dfs", DEMO_DIR+"\\"+"default.lgdfs","-gs",DEMO_DIR+"\\default.lggs", "-dss", DEMO_DIR+"\\default.lgdss" });
        org.LiveGraph.LiveGraph.application().guiManager().setDisplayDataFileSettingsWindows(false);
        org.LiveGraph.LiveGraph.application().guiManager().setDisplayMessageWindows(false);
        org.LiveGraph.LiveGraph.application().guiManager().setDisplaySeriesSettingsWindows(false);
        org.LiveGraph.LiveGraph.application().guiManager().setDisplayGraphSettingsWindows(false);
        org.LiveGraph.LiveGraph.application().getGraphSettings().setVGridSize(11*(1/Double.valueOf(BAUD_RATE))*1000);
        org.LiveGraph.LiveGraph.application().getGraphExporter().doExportGraph(1400, 400, "image/png", f);
    }
    
}
