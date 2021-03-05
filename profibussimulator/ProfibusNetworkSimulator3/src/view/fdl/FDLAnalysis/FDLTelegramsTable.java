/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FDLTelegramsTable.java
 *
 * Created on 25/03/2010, 16:21:33
 */
package view.fdl.FDLAnalysis;

import MDBAccess.PBCfg.DeviceCfg;
import MDBAccess.PBFile;
import SlaveDialog.DiagAndCfgStationInterface;
import class2MasterComm.CommandServicesInterface;
import common.ConversionsAdvanced;
import common.framestructure.ProfibusFrame;
import common.framestructure.ProfibusServices;
import common.framestructure.SD;
import common.framestructure.Service;
import communication.Server;
import consolesimulator.ProfibusMaster;
import consolesimulator.ProfibusSimulator;
import consolesimulator.ProfibusSimulatorSingleton;
import dataunits.SlaveDiagnostic;
import exceptions.ProfibusFrameException;
import java.util.Vector;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.table.*;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.jfree.ui.ExtensionFileFilter;
import stackDP.DPLine;
import stackDP.DPLineTCP;
import view.fdl.ComponentEventSwitch;
import view.fdl.PopUpFrameTable;
import view.fdl.SlaveObj;
import view.fdl.SlavePanel;
import view.fdl.jPanelSlaveCommProblem;
import view.liveListTree.JPannelSlaveList;
import view.liveListTree.LiveListStatus;
import view.liveListTree.LiveListTreePanel;
import view.oscilloscope.OscilloscopeChartPanel;


/**
 *
 * @author eduardo_mossin
 */
public class FDLTelegramsTable extends javax.swing.JPanel {

    private int tableIndex = 0;
    private boolean gettingPackages = true;
    //lista com todos os pacotes que chegam. Usada para armazenar detalhes dos pacotes profibus
    private Hashtable packageIDHash = new Hashtable();
    LiveListTreePanel liveListTree;
    OscilloscopeChartPanel oscilloscopeChart;
    //SlavePanel slaveAttributesPanel = new SlavePanel(0);
    jPanelSlaveCommProblem slaveCommPanel;// = new jPanelSlaveCommProblem( );
    CustomRenderer customTableRenderer = new CustomRenderer( );
    Vector RowFrameList = new Vector();
    Vector FrameList = new Vector( );
    
    static protected float fTimeTotalizer = 0;
    static public boolean bMirrorToSimulated = true;
    
    int LastRenderRow;

    //Simulator
    private ProfibusSimulator Sim;
    private boolean isSimulatorStarted;

    //Frame
    private ProfibusFrame pbBefore = null;
    private final JPannelSlaveList stationGrid;
    public final TableColumnHider hider;
    DPLineTCP dpTCP = new DPLineTCP();
    private int[] LAS = new int[127];
    private PBFile pbf = null;
    private float fTimeTotalizerSlowest = 0;
    private int uiOnlineStations = 0;
    private int uiDataExchangeStations = 0;
    private int uiRetries = 0;
    private int uiSyncs   = 0;
    private int uiInvalidFCS = 0;
    private DiagAndCfgStationInterface diagitf;

    /** Creates new form FDLTelegramsTable */
    public FDLTelegramsTable() {
        initComponents();
        
        getjTable1().setDefaultRenderer(String.class, customTableRenderer);
        
        getjTable1().setRowSorter(null);
        SelectionListener listener = new SelectionListener(getjTable1());
        getjTable1().getSelectionModel().addListSelectionListener(listener);
        getjTable1().getColumnModel().getSelectionModel().addListSelectionListener(listener);
        
        hider = new TableColumnHider(getjTable1());

        //treePanel.setSize(235, 290);
        //liveListTree = new LiveListTreePanel(new Dimension(treePanel.getWidth(), treePanel.getHeight() ),this);
        stationGrid = new JPannelSlaveList( this );
        //oscilloscopeChart = new OscilloscopeChartPanel(null, new Dimension(168, 170), null, null,1);
        treePanel.add( stationGrid );
        //devlist.setBounds(treePanel.getX(), treePanel.getY(), treePanel.getWidth(), treePanel.getHeight());
        //this.treePanel.add(liveListTree);
        //oscilloscopePanel.add(oscilloscopeChart);
        //setAddrSelectedInLiveListGridEvt(-1);

        ComponentEventSwitch.Register( jTextFieldSlaveAddress, ComponentEventSwitch.TEXT_FIELD_SLAVE_ADDR );
        ComponentEventSwitch.Register( jTabbedPane1, ComponentEventSwitch.JTABLE_SLAVE );
        
        jTable1.addMouseListener(new PopClickListener());
        
        ToolTipManager.sharedInstance().setDismissDelay(60000);
    }
    
    public void refreshIcons() {
        liveListTree.refreshIcons();
    }


    /*
     * Este método será sempre chamado quando uma mudanca de selecao na tree for feita
     */
    
    public void setAddrSelectedInLiveListGridEvt(int address){
    
        if( (pbf != null) && (Sim != null) )
        {
            diagitf = new DiagAndCfgStationInterface(address);

            if( (address >= 0) && (address <= 126) ) {

                fillDiagCheckBoxes( diagitf.getSlaveDiagBits() );
                jTextFieldConfiguration.setText( diagitf.getConfig() );
                jTextFieldIdentNumber.setText( diagitf.getIdent( ) );

                SlaveObj obj = new SlaveObj();
                obj.setAddress(address);
                obj.setStatus(LiveListStatus.ONLINE);

               // slaveAttributesPanel = new SlavePanel(address);
                //slaveAttributesPanel.setSlaveObject(obj);

                slaveCommPanel = new jPanelSlaveCommProblem();
                slaveCommPanel.setSlaveObject( obj );

               // slaveAttributesPanel.setSize(slavePanelContainer.getSize());
               // slavePanelContainer.add(slaveAttributesPanel);

                slavePanelContainer.repaint();
                slavePanelContainer.revalidate();
            }
        }
    }
    

    public void setStatus(int address, byte status) {
        liveListTree.setStatus(address, status);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelMasterTimeCfg = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabelMasterAddr = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabelBaudRate = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabelSlotTime = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabelMinTSDR = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabelMaxTSDR = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabelQuietTime = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabelSetupTime = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabelConfigurationFileName = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabelTTR = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabelGAP = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabelRetries = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabelHSA = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabelWatchDogTime = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jButtonChangeConfig = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableCfg = new javax.swing.JTable();
        jLabelBytesIN = new javax.swing.JLabel();
        jLabelBytesOUT = new javax.swing.JLabel();
        jCheckBoxMirrorToSimulated = new javax.swing.JCheckBox();
        jPanelSimulationControl = new javax.swing.JPanel();
        jPanelControl = new javax.swing.JPanel();
        StepSimButton = new javax.swing.JButton();
        RunSimButton = new javax.swing.JToggleButton();
        jLabelSimulatorStatus = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        ClearTableButton = new javax.swing.JButton();
        jButtonColumnHider = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        StartSimServerButton = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jPanelSimulationStatistics = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jButtonResetStatistics = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabelDataExchange = new javax.swing.JLabel();
        jLabelDataExchangeNum = new javax.swing.JLabel();
        jLabelSysReaction = new javax.swing.JLabel();
        jLabelSystemReactionTime = new javax.swing.JLabel();
        jLabelSysReactionSlowest = new javax.swing.JLabel();
        jLabelSystemReactionTimeSlowest = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        jLabelRetries1 = new javax.swing.JLabel();
        jLabelRetriesNum = new javax.swing.JLabel();
        jLabelSync = new javax.swing.JLabel();
        jLabelSyncNum = new javax.swing.JLabel();
        jLabelInvalidFCS = new javax.swing.JLabel();
        jLabelInvalidFCSNum = new javax.swing.JLabel();
        jPanel16 = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabelTRR = new javax.swing.JLabel();
        jLabelTTR2 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabelDiffTTR = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabelTRRANDWatchdog = new javax.swing.JLabel();
        slavePanelContainer = new javax.swing.JPanel();
        jPanel25 = new javax.swing.JPanel();
        jPanel24 = new javax.swing.JPanel();
        jButtonForceNewDiag = new javax.swing.JButton();
        jButtonRestoreStation = new javax.swing.JButton();
        jPanel17 = new javax.swing.JPanel();
        jLabelIdent = new javax.swing.JLabel();
        jTextFieldConfiguration = new javax.swing.JTextField();
        jLabelIdent1 = new javax.swing.JLabel();
        jTextFieldIdentNumber = new javax.swing.JTextField();
        jPanel23 = new javax.swing.JPanel();
        jCheckBox16 = new javax.swing.JCheckBox();
        jLabelMaster = new javax.swing.JLabel();
        jPanel22 = new javax.swing.JPanel();
        jCheckBox13 = new javax.swing.JCheckBox();
        jCheckBox14 = new javax.swing.JCheckBox();
        jCheckBox15 = new javax.swing.JCheckBox();
        jPanel21 = new javax.swing.JPanel();
        jCheckBox10 = new javax.swing.JCheckBox();
        jCheckBox11 = new javax.swing.JCheckBox();
        jCheckBox12 = new javax.swing.JCheckBox();
        jPanel20 = new javax.swing.JPanel();
        jCheckBox7 = new javax.swing.JCheckBox();
        jCheckBox8 = new javax.swing.JCheckBox();
        jCheckBox9 = new javax.swing.JCheckBox();
        jPanel19 = new javax.swing.JPanel();
        jCheckBox4 = new javax.swing.JCheckBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        jCheckBox6 = new javax.swing.JCheckBox();
        jPanel18 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jPanelSlaveCommSignal = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jSliderVoltage = new javax.swing.JSlider();
        jVoltageLabel = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jComboBoxProblemType = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jCheckBoxReadableSignal = new javax.swing.JCheckBox();
        jButtonApplyCommProblem = new javax.swing.JButton();
        jPanelClass2Master = new javax.swing.JPanel();
        jPanelMasterC2Commands = new javax.swing.JPanel();
        jButtonGetDiag = new javax.swing.JButton();
        jButtonGetCfg = new javax.swing.JButton();
        jButtonReadInOutDataIn = new javax.swing.JButton();
        jButtonReadInOutDataOut = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jTextFieldSlaveAddress = new javax.swing.JTextField();
        jButton9 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jButtonInitComm = new javax.swing.JButton();
        jComboUSBBox = new javax.swing.JComboBox();
        jButtonRefreshUSBList = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldMasterAddress = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jTextFieldSlotTimeMs = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jButtonMasterStatus = new javax.swing.JButton();
        jLabelMasterStatus = new javax.swing.JLabel();
        jButtonGetLiveList = new javax.swing.JButton();
        jLabelLiveListTotalStations = new javax.swing.JLabel();
        jButtonClearTables = new javax.swing.JButton();
        jSplitPaneOutput = new javax.swing.JSplitPane();
        jPanelTable = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanelInfo = new javax.swing.JPanel();
        jPanelLegend = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        treePanel = new javax.swing.JPanel();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(profibusnetworksimulator.ProfibusNetworkSimulatorAppNew.class).getContext().getResourceMap(FDLTelegramsTable.class);
        setBackground(resourceMap.getColor("Form.background")); // NOI18N
        setBorder(javax.swing.BorderFactory.createEtchedBorder());
        setAutoscrolls(true);
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(1000, 400));

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanelMasterTimeCfg.setName("jPanelMasterTimeCfg"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setName("jPanel4"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jLabelMasterAddr.setText(resourceMap.getString("jLabelMasterAddr.text")); // NOI18N
        jLabelMasterAddr.setName("jLabelMasterAddr"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabelBaudRate.setText(resourceMap.getString("jLabelBaudRate.text")); // NOI18N
        jLabelBaudRate.setName("jLabelBaudRate"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabelSlotTime.setText(resourceMap.getString("jLabelSlotTime.text")); // NOI18N
        jLabelSlotTime.setName("jLabelSlotTime"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabelMinTSDR.setText(resourceMap.getString("jLabelMinTSDR.text")); // NOI18N
        jLabelMinTSDR.setName("jLabelMinTSDR"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabelMaxTSDR.setText(resourceMap.getString("jLabelMaxTSDR.text")); // NOI18N
        jLabelMaxTSDR.setName("jLabelMaxTSDR"); // NOI18N

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jLabelQuietTime.setText(resourceMap.getString("jLabelQuietTime.text")); // NOI18N
        jLabelQuietTime.setName("jLabelQuietTime"); // NOI18N

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        jLabelSetupTime.setText(resourceMap.getString("jLabelSetupTime.text")); // NOI18N
        jLabelSetupTime.setName("jLabelSetupTime"); // NOI18N

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        jLabelConfigurationFileName.setFont(resourceMap.getFont("jLabelConfigurationFileName.font")); // NOI18N
        jLabelConfigurationFileName.setText(resourceMap.getString("jLabelConfigurationFileName.text")); // NOI18N
        jLabelConfigurationFileName.setName("jLabelConfigurationFileName"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 57, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 57, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel6))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jLabelMasterAddr, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabelBaudRate, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabelSlotTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(18, 18, 18)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel4Layout.createSequentialGroup()
                                .add(jLabel11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 57, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(jLabelSetupTime, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE))
                            .add(jPanel4Layout.createSequentialGroup()
                                .add(jLabel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 57, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(jLabelQuietTime, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE))
                            .add(jPanel4Layout.createSequentialGroup()
                                .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 57, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(jLabelMinTSDR, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE))
                            .add(jPanel4Layout.createSequentialGroup()
                                .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 57, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(jLabelMaxTSDR, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE))))
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jLabel12)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabelConfigurationFileName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel12)
                    .add(jLabelConfigurationFileName))
                .add(8, 8, 8)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel5)
                            .add(jLabelMinTSDR))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel7)
                            .add(jLabelMaxTSDR))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel9)
                            .add(jLabelQuietTime))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel11)
                            .add(jLabelSetupTime)))
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jLabelMasterAddr)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jLabelBaudRate)
                        .add(12, 12, 12)
                        .add(jLabelSlotTime))
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jLabel6)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jLabel1)
                        .add(12, 12, 12)
                        .add(jLabel3)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel6.setName("jPanel6"); // NOI18N

        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        jLabelTTR.setText(resourceMap.getString("jLabelTTR.text")); // NOI18N
        jLabelTTR.setName("jLabelTTR"); // NOI18N

        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        jLabelGAP.setText(resourceMap.getString("jLabelGAP.text")); // NOI18N
        jLabelGAP.setName("jLabelGAP"); // NOI18N

        jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        jLabelRetries.setText(resourceMap.getString("jLabelRetries.text")); // NOI18N
        jLabelRetries.setName("jLabelRetries"); // NOI18N

        jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N

        jLabelHSA.setText(resourceMap.getString("jLabelHSA.text")); // NOI18N
        jLabelHSA.setName("jLabelHSA"); // NOI18N

        jLabel21.setText(resourceMap.getString("jLabel21.text")); // NOI18N
        jLabel21.setName("jLabel21"); // NOI18N

        jLabelWatchDogTime.setText(resourceMap.getString("jLabelWatchDogTime.text")); // NOI18N
        jLabelWatchDogTime.setName("jLabelWatchDogTime"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel6Layout.createSequentialGroup()
                        .add(jLabel13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 57, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabelTTR, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE))
                    .add(jPanel6Layout.createSequentialGroup()
                        .add(jLabel15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 57, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabelGAP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE))
                    .add(jPanel6Layout.createSequentialGroup()
                        .add(jLabel17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 57, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabelRetries, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE))
                    .add(jPanel6Layout.createSequentialGroup()
                        .add(jLabel19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 57, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabelHSA, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(61, 61, 61))
                    .add(jPanel6Layout.createSequentialGroup()
                        .add(jLabel21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 57, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabelWatchDogTime, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                        .add(61, 61, 61)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel13)
                    .add(jLabelTTR))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel15)
                    .add(jLabelGAP))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel17)
                    .add(jLabelRetries))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel19)
                    .add(jLabelHSA))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel21)
                    .add(jLabelWatchDogTime))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        jPanel13.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel13.setName("jPanel13"); // NOI18N

        jButtonChangeConfig.setText(resourceMap.getString("jButtonChangeConfig.text")); // NOI18N
        jButtonChangeConfig.setName("jButtonChangeConfig"); // NOI18N
        jButtonChangeConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonChangeConfigActionPerformed(evt);
            }
        });

        jPanel9.setName("jPanel9"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTableCfg.setAutoCreateRowSorter(true);
        jTableCfg.setFont(resourceMap.getFont("jTableCfg.font")); // NOI18N
        jTableCfg.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Addr", "Tag", "Bytes IN", "Bytes OUT", "Cfg Identifier"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableCfg.setName("jTableCfg"); // NOI18N
        jScrollPane1.setViewportView(jTableCfg);

        org.jdesktop.layout.GroupLayout jPanel9Layout = new org.jdesktop.layout.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 513, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
        );

        jLabelBytesIN.setText(resourceMap.getString("jLabelBytesIN.text")); // NOI18N
        jLabelBytesIN.setName("jLabelBytesIN"); // NOI18N

        jLabelBytesOUT.setText(resourceMap.getString("jLabelBytesOUT.text")); // NOI18N
        jLabelBytesOUT.setName("jLabelBytesOUT"); // NOI18N

        jCheckBoxMirrorToSimulated.setSelected(true);
        jCheckBoxMirrorToSimulated.setText(resourceMap.getString("jCheckBoxMirrorToSimulated.text")); // NOI18N
        jCheckBoxMirrorToSimulated.setName("jCheckBoxMirrorToSimulated"); // NOI18N
        jCheckBoxMirrorToSimulated.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxMirrorToSimulatedStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel13Layout = new org.jdesktop.layout.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel13Layout.createSequentialGroup()
                .add(jPanel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel13Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel13Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabelBytesOUT, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jButtonChangeConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                        .add(jLabelBytesIN))
                    .add(jCheckBoxMirrorToSimulated))
                .addContainerGap(41, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel13Layout.createSequentialGroup()
                .add(jPanel13Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel13Layout.createSequentialGroup()
                        .add(jLabelBytesIN)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 11, Short.MAX_VALUE)
                        .add(jLabelBytesOUT)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonChangeConfig)
                        .add(5, 5, 5)
                        .add(jCheckBoxMirrorToSimulated))
                    .add(jPanel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(11, 11, 11))
        );

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 176, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel13, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel13, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout jPanelMasterTimeCfgLayout = new org.jdesktop.layout.GroupLayout(jPanelMasterTimeCfg);
        jPanelMasterTimeCfg.setLayout(jPanelMasterTimeCfgLayout);
        jPanelMasterTimeCfgLayout.setHorizontalGroup(
            jPanelMasterTimeCfgLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelMasterTimeCfgLayout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelMasterTimeCfgLayout.setVerticalGroup(
            jPanelMasterTimeCfgLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanelMasterTimeCfg.TabConstraints.tabTitle"), jPanelMasterTimeCfg); // NOI18N

        jPanelSimulationControl.setName("jPanelSimulationControl"); // NOI18N

        jPanelControl.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("jPanelControl.border.title"))); // NOI18N
        jPanelControl.setName("jPanelControl"); // NOI18N

        StepSimButton.setText(resourceMap.getString("StepSimButton.text")); // NOI18N
        StepSimButton.setName("StepSimButton"); // NOI18N
        StepSimButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StepSimButtonActionPerformed(evt);
            }
        });

        RunSimButton.setText(resourceMap.getString("RunSimButton.text")); // NOI18N
        RunSimButton.setName("RunSimButton"); // NOI18N
        RunSimButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RunSimButtonActionPerformed(evt);
            }
        });

        jLabelSimulatorStatus.setForeground(resourceMap.getColor("jLabelSimulatorStatus.foreground")); // NOI18N
        jLabelSimulatorStatus.setText(resourceMap.getString("jLabelSimulatorStatus.text")); // NOI18N
        jLabelSimulatorStatus.setName("jLabelSimulatorStatus"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanelControlLayout = new org.jdesktop.layout.GroupLayout(jPanelControl);
        jPanelControl.setLayout(jPanelControlLayout);
        jPanelControlLayout.setHorizontalGroup(
            jPanelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelControlLayout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(RunSimButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(StepSimButton)
                .addContainerGap())
            .add(jPanelControlLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabelSimulatorStatus)
                .addContainerGap(25, Short.MAX_VALUE))
        );
        jPanelControlLayout.setVerticalGroup(
            jPanelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelControlLayout.createSequentialGroup()
                .add(jPanelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(RunSimButton)
                    .add(StepSimButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jLabelSimulatorStatus)
                .addContainerGap(37, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        ClearTableButton.setText(resourceMap.getString("ClearTableButton.text")); // NOI18N
        ClearTableButton.setName("ClearTableButton"); // NOI18N
        ClearTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearTableButtonActionPerformed(evt);
            }
        });

        jButtonColumnHider.setText(resourceMap.getString("jButtonColumnHider.text")); // NOI18N
        jButtonColumnHider.setName("jButtonColumnHider"); // NOI18N
        jButtonColumnHider.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColumnHiderActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jButtonColumnHider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, ClearTableButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(73, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jButtonColumnHider)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ClearTableButton)
                .addContainerGap(33, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("jPanel8.border.title"))); // NOI18N
        jPanel8.setName("jPanel8"); // NOI18N

        StartSimServerButton.setText(resourceMap.getString("StartSimServerButton.text")); // NOI18N
        StartSimServerButton.setName("StartSimServerButton"); // NOI18N
        StartSimServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StartSimServerButtonActionPerformed(evt);
            }
        });

        jLabel8.setForeground(resourceMap.getColor("jLabel8.foreground")); // NOI18N
        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(StartSimServerButton)
                    .add(jLabel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .add(StartSimServerButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel8)
                .addContainerGap(42, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanelSimulationControlLayout = new org.jdesktop.layout.GroupLayout(jPanelSimulationControl);
        jPanelSimulationControl.setLayout(jPanelSimulationControlLayout);
        jPanelSimulationControlLayout.setHorizontalGroup(
            jPanelSimulationControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelSimulationControlLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelControl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(719, Short.MAX_VALUE))
        );
        jPanelSimulationControlLayout.setVerticalGroup(
            jPanelSimulationControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelSimulationControlLayout.createSequentialGroup()
                .add(jPanelSimulationControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanelControl, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanelSimulationControl.TabConstraints.tabTitle"), jPanelSimulationControl); // NOI18N

        jPanelSimulationStatistics.setName("jPanelSimulationStatistics"); // NOI18N

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("jPanel12.border.title"))); // NOI18N
        jPanel12.setName("jPanel12"); // NOI18N

        jButtonResetStatistics.setText(resourceMap.getString("jButtonResetStatistics.text")); // NOI18N
        jButtonResetStatistics.setName("jButtonResetStatistics"); // NOI18N
        jButtonResetStatistics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResetStatisticsActionPerformed(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel5.setName("jPanel5"); // NOI18N

        jLabelDataExchange.setForeground(resourceMap.getColor("jLabelDataExchange.foreground")); // NOI18N
        jLabelDataExchange.setText(resourceMap.getString("jLabelDataExchange.text")); // NOI18N
        jLabelDataExchange.setName("jLabelDataExchange"); // NOI18N

        jLabelDataExchangeNum.setForeground(resourceMap.getColor("jLabelDataExchangeNum.foreground")); // NOI18N
        jLabelDataExchangeNum.setText(resourceMap.getString("jLabelDataExchangeNum.text")); // NOI18N
        jLabelDataExchangeNum.setName("jLabelDataExchangeNum"); // NOI18N

        jLabelSysReaction.setForeground(resourceMap.getColor("jLabelSysReaction.foreground")); // NOI18N
        jLabelSysReaction.setText(resourceMap.getString("jLabelSysReaction.text")); // NOI18N
        jLabelSysReaction.setName("jLabelSysReaction"); // NOI18N

        jLabelSystemReactionTime.setForeground(resourceMap.getColor("jLabelSystemReactionTime.foreground")); // NOI18N
        jLabelSystemReactionTime.setText(resourceMap.getString("jLabelSystemReactionTime.text")); // NOI18N
        jLabelSystemReactionTime.setName("jLabelSystemReactionTime"); // NOI18N

        jLabelSysReactionSlowest.setForeground(resourceMap.getColor("jLabelSysReactionSlowest.foreground")); // NOI18N
        jLabelSysReactionSlowest.setText(resourceMap.getString("jLabelSysReactionSlowest.text")); // NOI18N
        jLabelSysReactionSlowest.setName("jLabelSysReactionSlowest"); // NOI18N

        jLabelSystemReactionTimeSlowest.setForeground(resourceMap.getColor("jLabelSystemReactionTimeSlowest.foreground")); // NOI18N
        jLabelSystemReactionTimeSlowest.setText(resourceMap.getString("jLabelSystemReactionTimeSlowest.text")); // NOI18N
        jLabelSystemReactionTimeSlowest.setName("jLabelSystemReactionTimeSlowest"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(jLabelSysReactionSlowest)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabelSystemReactionTimeSlowest, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE))
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(jLabelDataExchange, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 82, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabelDataExchangeNum, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 83, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(jLabelSysReaction)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabelSystemReactionTime, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)))
                .add(73, 73, 73))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelDataExchange)
                    .add(jLabelDataExchangeNum))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelSysReaction)
                    .add(jLabelSystemReactionTime))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelSysReactionSlowest)
                    .add(jLabelSystemReactionTimeSlowest))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel15.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel15.setName("jPanel15"); // NOI18N

        jLabelRetries1.setForeground(resourceMap.getColor("jLabelRetries1.foreground")); // NOI18N
        jLabelRetries1.setText(resourceMap.getString("jLabelRetries1.text")); // NOI18N
        jLabelRetries1.setName("jLabelRetries1"); // NOI18N

        jLabelRetriesNum.setForeground(resourceMap.getColor("jLabelRetriesNum.foreground")); // NOI18N
        jLabelRetriesNum.setText(resourceMap.getString("jLabelRetriesNum.text")); // NOI18N
        jLabelRetriesNum.setName("jLabelRetriesNum"); // NOI18N

        jLabelSync.setForeground(resourceMap.getColor("jLabelSync.foreground")); // NOI18N
        jLabelSync.setText(resourceMap.getString("jLabelSync.text")); // NOI18N
        jLabelSync.setName("jLabelSync"); // NOI18N

        jLabelSyncNum.setForeground(resourceMap.getColor("jLabelSyncNum.foreground")); // NOI18N
        jLabelSyncNum.setText(resourceMap.getString("jLabelSyncNum.text")); // NOI18N
        jLabelSyncNum.setName("jLabelSyncNum"); // NOI18N

        jLabelInvalidFCS.setForeground(resourceMap.getColor("jLabelInvalidFCS.foreground")); // NOI18N
        jLabelInvalidFCS.setText(resourceMap.getString("jLabelInvalidFCS.text")); // NOI18N
        jLabelInvalidFCS.setName("jLabelInvalidFCS"); // NOI18N

        jLabelInvalidFCSNum.setForeground(resourceMap.getColor("jLabelInvalidFCSNum.foreground")); // NOI18N
        jLabelInvalidFCSNum.setText(resourceMap.getString("jLabelInvalidFCSNum.text")); // NOI18N
        jLabelInvalidFCSNum.setName("jLabelInvalidFCSNum"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel15Layout = new org.jdesktop.layout.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel15Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel15Layout.createSequentialGroup()
                        .add(jPanel15Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel15Layout.createSequentialGroup()
                                .add(jLabelSync, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 42, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel15Layout.createSequentialGroup()
                                .add(jLabelRetries1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 82, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .add(jPanel15Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel15Layout.createSequentialGroup()
                                .add(jLabelRetriesNum)
                                .add(72, 72, 72))
                            .add(jPanel15Layout.createSequentialGroup()
                                .add(jLabelSyncNum, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
                                .add(64, 64, 64))))
                    .add(jPanel15Layout.createSequentialGroup()
                        .add(jLabelInvalidFCS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 82, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabelInvalidFCSNum)
                        .addContainerGap(72, Short.MAX_VALUE))))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel15Layout.createSequentialGroup()
                .add(jPanel15Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelRetries1)
                    .add(jLabelRetriesNum))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel15Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelSync)
                    .add(jLabelSyncNum))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel15Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelInvalidFCS)
                    .add(jLabelInvalidFCSNum))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel16.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel16.setName("jPanel16"); // NOI18N

        jLabel29.setText(resourceMap.getString("jLabel29.text")); // NOI18N
        jLabel29.setName("jLabel29"); // NOI18N

        jLabel30.setForeground(resourceMap.getColor("jLabel30.foreground")); // NOI18N
        jLabel30.setText(resourceMap.getString("jLabel30.text")); // NOI18N
        jLabel30.setName("jLabel30"); // NOI18N

        jLabelTRR.setForeground(resourceMap.getColor("jLabelTRR.foreground")); // NOI18N
        jLabelTRR.setText(resourceMap.getString("jLabelTRR.text")); // NOI18N
        jLabelTRR.setName("jLabelTRR"); // NOI18N

        jLabelTTR2.setText(resourceMap.getString("jLabelTTR2.text")); // NOI18N
        jLabelTTR2.setName("jLabelTTR2"); // NOI18N

        jLabel33.setForeground(resourceMap.getColor("jLabel33.foreground")); // NOI18N
        jLabel33.setText(resourceMap.getString("jLabel33.text")); // NOI18N
        jLabel33.setName("jLabel33"); // NOI18N

        jLabelDiffTTR.setForeground(resourceMap.getColor("jLabelDiffTTR.foreground")); // NOI18N
        jLabelDiffTTR.setText(resourceMap.getString("jLabelDiffTTR.text")); // NOI18N
        jLabelDiffTTR.setName("jLabelDiffTTR"); // NOI18N

        jLabel34.setForeground(resourceMap.getColor("jLabel34.foreground")); // NOI18N
        jLabel34.setText(resourceMap.getString("jLabel34.text")); // NOI18N
        jLabel34.setName("jLabel34"); // NOI18N

        jLabelTRRANDWatchdog.setForeground(resourceMap.getColor("jLabelTRRANDWatchdog.foreground")); // NOI18N
        jLabelTRRANDWatchdog.setText(resourceMap.getString("jLabelTRRANDWatchdog.text")); // NOI18N
        jLabelTRRANDWatchdog.setName("jLabelTRRANDWatchdog"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel16Layout = new org.jdesktop.layout.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel16Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel16Layout.createSequentialGroup()
                        .add(jLabel29)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabelTTR2))
                    .add(jPanel16Layout.createSequentialGroup()
                        .add(jLabel30)
                        .add(18, 18, 18)
                        .add(jLabelTRR))
                    .add(jPanel16Layout.createSequentialGroup()
                        .add(jLabel33)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabelDiffTTR))
                    .add(jPanel16Layout.createSequentialGroup()
                        .add(jLabel34)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabelTRRANDWatchdog)))
                .addContainerGap(166, Short.MAX_VALUE))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel16Layout.createSequentialGroup()
                .add(jPanel16Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel29)
                    .add(jLabelTTR2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel16Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel30)
                    .add(jLabelTRR))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel16Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel33)
                    .add(jLabelDiffTTR))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel16Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel34)
                    .add(jLabelTRRANDWatchdog))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanel12Layout = new org.jdesktop.layout.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButtonResetStatistics)
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel12Layout.createSequentialGroup()
                .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jPanel15, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(12, 12, 12))
            .add(jPanel16, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
            .add(jPanel12Layout.createSequentialGroup()
                .add(jButtonResetStatistics)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout jPanelSimulationStatisticsLayout = new org.jdesktop.layout.GroupLayout(jPanelSimulationStatistics);
        jPanelSimulationStatistics.setLayout(jPanelSimulationStatisticsLayout);
        jPanelSimulationStatisticsLayout.setHorizontalGroup(
            jPanelSimulationStatisticsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelSimulationStatisticsLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(123, Short.MAX_VALUE))
        );
        jPanelSimulationStatisticsLayout.setVerticalGroup(
            jPanelSimulationStatisticsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelSimulationStatisticsLayout.createSequentialGroup()
                .add(jPanel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanelSimulationStatistics.TabConstraints.tabTitle"), jPanelSimulationStatistics); // NOI18N

        slavePanelContainer.setName("slavePanelContainer"); // NOI18N

        jPanel25.setName("jPanel25"); // NOI18N

        jPanel24.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel24.setName("jPanel24"); // NOI18N

        jButtonForceNewDiag.setText(resourceMap.getString("jButtonForceNewDiag.text")); // NOI18N
        jButtonForceNewDiag.setName("jButtonForceNewDiag"); // NOI18N
        jButtonForceNewDiag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonForceNewDiagActionPerformed(evt);
            }
        });

        jButtonRestoreStation.setText(resourceMap.getString("jButtonRestoreStation.text")); // NOI18N
        jButtonRestoreStation.setName("jButtonRestoreStation"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel24Layout = new org.jdesktop.layout.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel24Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jButtonRestoreStation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jButtonForceNewDiag, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 137, Short.MAX_VALUE))
                .addContainerGap(11, Short.MAX_VALUE))
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .add(jButtonForceNewDiag)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButtonRestoreStation)
                .addContainerGap(7, Short.MAX_VALUE))
        );

        jPanel17.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel17.setName("jPanel17"); // NOI18N

        jLabelIdent.setText(resourceMap.getString("jLabelIdent.text")); // NOI18N
        jLabelIdent.setName("jLabelIdent"); // NOI18N

        jTextFieldConfiguration.setText(resourceMap.getString("jTextFieldConfiguration.text")); // NOI18N
        jTextFieldConfiguration.setName("jTextFieldConfiguration"); // NOI18N

        jLabelIdent1.setText(resourceMap.getString("jLabelIdent1.text")); // NOI18N
        jLabelIdent1.setName("jLabelIdent1"); // NOI18N

        jTextFieldIdentNumber.setName("jTextFieldIdentNumber"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel17Layout = new org.jdesktop.layout.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel17Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel17Layout.createSequentialGroup()
                        .add(jLabelIdent1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextFieldIdentNumber, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
                    .add(jPanel17Layout.createSequentialGroup()
                        .add(jLabelIdent)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextFieldConfiguration, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel17Layout.createSequentialGroup()
                .add(jPanel17Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelIdent)
                    .add(jTextFieldConfiguration, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel17Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelIdent1)
                    .add(jTextFieldIdentNumber, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(29, 29, 29))
        );

        jPanel23.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel23.setName("jPanel23"); // NOI18N

        jCheckBox16.setText(resourceMap.getString("jCheckBox16.text")); // NOI18N
        jCheckBox16.setName("jCheckBox16"); // NOI18N

        jLabelMaster.setText(resourceMap.getString("jLabelMaster.text")); // NOI18N
        jLabelMaster.setName("jLabelMaster"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel23Layout = new org.jdesktop.layout.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel23Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelMaster)
                    .add(jCheckBox16))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .add(jCheckBox16)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelMaster)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        jPanel22.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel22.setName("jPanel22"); // NOI18N

        jCheckBox13.setText(resourceMap.getString("jCheckBox13.text")); // NOI18N
        jCheckBox13.setName("jCheckBox13"); // NOI18N

        jCheckBox14.setText(resourceMap.getString("jCheckBox14.text")); // NOI18N
        jCheckBox14.setName("jCheckBox14"); // NOI18N

        jCheckBox15.setText(resourceMap.getString("jCheckBox15.text")); // NOI18N
        jCheckBox15.setName("jCheckBox15"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel22Layout = new org.jdesktop.layout.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel22Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBox13)
                    .add(jCheckBox14)
                    .add(jCheckBox15))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .add(jCheckBox13)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBox14)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBox15)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel21.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel21.setName("jPanel21"); // NOI18N

        jCheckBox10.setText(resourceMap.getString("jCheckBox10.text")); // NOI18N
        jCheckBox10.setName("jCheckBox10"); // NOI18N

        jCheckBox11.setText(resourceMap.getString("jCheckBox11.text")); // NOI18N
        jCheckBox11.setName("jCheckBox11"); // NOI18N

        jCheckBox12.setText(resourceMap.getString("jCheckBox12.text")); // NOI18N
        jCheckBox12.setName("jCheckBox12"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel21Layout = new org.jdesktop.layout.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel21Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel21Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBox10)
                    .add(jCheckBox11)
                    .add(jCheckBox12))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel21Layout.createSequentialGroup()
                .add(jCheckBox10)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBox11)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBox12))
        );

        jPanel20.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel20.setName("jPanel20"); // NOI18N

        jCheckBox7.setText(resourceMap.getString("jCheckBox7.text")); // NOI18N
        jCheckBox7.setName("jCheckBox7"); // NOI18N

        jCheckBox8.setText(resourceMap.getString("jCheckBox8.text")); // NOI18N
        jCheckBox8.setName("jCheckBox8"); // NOI18N

        jCheckBox9.setText(resourceMap.getString("jCheckBox9.text")); // NOI18N
        jCheckBox9.setName("jCheckBox9"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel20Layout = new org.jdesktop.layout.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel20Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBox8)
                    .add(jCheckBox9)
                    .add(jCheckBox7))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel20Layout.createSequentialGroup()
                .add(jCheckBox7)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBox8)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBox9))
        );

        jPanel19.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel19.setName("jPanel19"); // NOI18N

        jCheckBox4.setText(resourceMap.getString("jCheckBox4.text")); // NOI18N
        jCheckBox4.setName("jCheckBox4"); // NOI18N

        jCheckBox5.setText(resourceMap.getString("jCheckBox5.text")); // NOI18N
        jCheckBox5.setName("jCheckBox5"); // NOI18N

        jCheckBox6.setText(resourceMap.getString("jCheckBox6.text")); // NOI18N
        jCheckBox6.setName("jCheckBox6"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel19Layout = new org.jdesktop.layout.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel19Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBox4)
                    .add(jCheckBox5)
                    .add(jCheckBox6))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel19Layout.createSequentialGroup()
                .add(jCheckBox4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBox5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBox6)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel18.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel18.setName("jPanel18"); // NOI18N

        jCheckBox1.setText(resourceMap.getString("jCheckBox1.text")); // NOI18N
        jCheckBox1.setName("jCheckBox1"); // NOI18N

        jCheckBox2.setText(resourceMap.getString("jCheckBox2.text")); // NOI18N
        jCheckBox2.setName("jCheckBox2"); // NOI18N

        jCheckBox3.setText(resourceMap.getString("jCheckBox3.text")); // NOI18N
        jCheckBox3.setName("jCheckBox3"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel18Layout = new org.jdesktop.layout.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel18Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBox1)
                    .add(jCheckBox2)
                    .add(jCheckBox3))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .add(jCheckBox1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBox2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBox3))
        );

        org.jdesktop.layout.GroupLayout jPanel25Layout = new org.jdesktop.layout.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel25Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(57, Short.MAX_VALUE))
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel25Layout.createSequentialGroup()
                .add(jPanel25Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel25Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel21, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel20, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(jPanel22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel25Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel24, 0, 79, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel17, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)))
                .addContainerGap(37, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout slavePanelContainerLayout = new org.jdesktop.layout.GroupLayout(slavePanelContainer);
        slavePanelContainer.setLayout(slavePanelContainerLayout);
        slavePanelContainerLayout.setHorizontalGroup(
            slavePanelContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel25, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        slavePanelContainerLayout.setVerticalGroup(
            slavePanelContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel25, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(resourceMap.getString("slavePanelContainer.TabConstraints.tabTitle"), slavePanelContainer); // NOI18N

        jPanelSlaveCommSignal.setName("jPanelSlaveCommSignal"); // NOI18N

        jPanel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel10.setName("jPanel10"); // NOI18N

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel11.border.title"))); // NOI18N
        jPanel11.setName("jPanel11"); // NOI18N

        jSliderVoltage.setMaximum(10);
        jSliderVoltage.setValue(7);
        jSliderVoltage.setName("jSliderVoltage"); // NOI18N
        jSliderVoltage.setValueIsAdjusting(true);
        jSliderVoltage.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderVoltageStateChanged(evt);
            }
        });

        jVoltageLabel.setText(resourceMap.getString("jVoltageLabel.text")); // NOI18N
        jVoltageLabel.setName("jVoltageLabel"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel11Layout = new org.jdesktop.layout.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .add(jSliderVoltage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 139, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jVoltageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jVoltageLabel)
                    .add(jSliderVoltage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(59, 59, 59))
        );

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel14.border.title"))); // NOI18N
        jPanel14.setName("jPanel14"); // NOI18N

        jComboBoxProblemType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Problem", "Terminator Missing", "Terminator Excess", "A-B Short Circuit", "Cable Break", "Cable Long", "Terminator Power Supply failure (*)", "Spur-lines (*)", "Corrupted Signal" }));
        jComboBoxProblemType.setName("jComboBoxProblemType"); // NOI18N
        jComboBoxProblemType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxProblemTypeActionPerformed(evt);
            }
        });

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel14Layout = new org.jdesktop.layout.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel14Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jComboBoxProblemType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 371, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 117, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel14Layout.createSequentialGroup()
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jComboBoxProblemType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jCheckBoxReadableSignal.setSelected(true);
        jCheckBoxReadableSignal.setText(resourceMap.getString("jCheckBoxReadableSignal.text")); // NOI18N
        jCheckBoxReadableSignal.setName("jCheckBoxReadableSignal"); // NOI18N
        jCheckBoxReadableSignal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxReadableSignalActionPerformed(evt);
            }
        });

        jButtonApplyCommProblem.setText(resourceMap.getString("jButtonApplyCommProblem.text")); // NOI18N
        jButtonApplyCommProblem.setName("jButtonApplyCommProblem"); // NOI18N
        jButtonApplyCommProblem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonApplyCommProblemActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel10Layout = new org.jdesktop.layout.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBoxReadableSignal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 118, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonApplyCommProblem, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 120, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(570, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel10Layout.createSequentialGroup()
                .add(jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel10Layout.createSequentialGroup()
                        .add(jCheckBoxReadableSignal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jButtonApplyCommProblem)
                        .add(27, 27, 27))
                    .add(jPanel11, 0, 86, Short.MAX_VALUE)
                    .add(jPanel14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout jPanelSlaveCommSignalLayout = new org.jdesktop.layout.GroupLayout(jPanelSlaveCommSignal);
        jPanelSlaveCommSignal.setLayout(jPanelSlaveCommSignalLayout);
        jPanelSlaveCommSignalLayout.setHorizontalGroup(
            jPanelSlaveCommSignalLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelSlaveCommSignalLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelSlaveCommSignalLayout.setVerticalGroup(
            jPanelSlaveCommSignalLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelSlaveCommSignalLayout.createSequentialGroup()
                .add(5, 5, 5)
                .add(jPanel10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanelSlaveCommSignal.TabConstraints.tabTitle"), jPanelSlaveCommSignal); // NOI18N

        jPanelClass2Master.setEnabled(false);
        jPanelClass2Master.setName("jPanelClass2Master"); // NOI18N

        jPanelMasterC2Commands.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("jPanelMasterC2Commands.border.title"))); // NOI18N
        jPanelMasterC2Commands.setName("jPanelMasterC2Commands"); // NOI18N

        jButtonGetDiag.setText(resourceMap.getString("jButtonGetDiag.text")); // NOI18N
        jButtonGetDiag.setName("jButtonGetDiag"); // NOI18N
        jButtonGetDiag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGetDiagActionPerformed(evt);
            }
        });

        jButtonGetCfg.setText(resourceMap.getString("jButtonGetCfg.text")); // NOI18N
        jButtonGetCfg.setName("jButtonGetCfg"); // NOI18N
        jButtonGetCfg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGetCfgActionPerformed(evt);
            }
        });

        jButtonReadInOutDataIn.setText(resourceMap.getString("jButtonReadInOutDataIn.text")); // NOI18N
        jButtonReadInOutDataIn.setName("jButtonReadInOutDataIn"); // NOI18N
        jButtonReadInOutDataIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonReadInOutDataInActionPerformed(evt);
            }
        });

        jButtonReadInOutDataOut.setText(resourceMap.getString("jButtonReadInOutDataOut.text")); // NOI18N
        jButtonReadInOutDataOut.setName("jButtonReadInOutDataOut"); // NOI18N
        jButtonReadInOutDataOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonReadInOutDataOutActionPerformed(evt);
            }
        });

        jButton8.setText(resourceMap.getString("jButton8.text")); // NOI18N
        jButton8.setName("jButton8"); // NOI18N

        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        jTextFieldSlaveAddress.setText(resourceMap.getString("jTextFieldSlaveAddress.text")); // NOI18N
        jTextFieldSlaveAddress.setName("jTextFieldSlaveAddress"); // NOI18N
        jTextFieldSlaveAddress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldSlaveAddressActionPerformed(evt);
            }
        });

        jButton9.setText(resourceMap.getString("jButton9.text")); // NOI18N
        jButton9.setName("jButton9"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanelMasterC2CommandsLayout = new org.jdesktop.layout.GroupLayout(jPanelMasterC2Commands);
        jPanelMasterC2Commands.setLayout(jPanelMasterC2CommandsLayout);
        jPanelMasterC2CommandsLayout.setHorizontalGroup(
            jPanelMasterC2CommandsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelMasterC2CommandsLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelMasterC2CommandsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jTextFieldSlaveAddress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)
                    .add(jLabel16))
                .add(18, 18, 18)
                .add(jPanelMasterC2CommandsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanelMasterC2CommandsLayout.createSequentialGroup()
                        .add(jButton8)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(jPanelMasterC2CommandsLayout.createSequentialGroup()
                        .add(jPanelMasterC2CommandsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jButtonGetDiag, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jButtonGetCfg, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .add(6, 6, 6)
                        .add(jPanelMasterC2CommandsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jButtonReadInOutDataOut, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                            .add(jButtonReadInOutDataIn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)))))
        );
        jPanelMasterC2CommandsLayout.setVerticalGroup(
            jPanelMasterC2CommandsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelMasterC2CommandsLayout.createSequentialGroup()
                .add(jPanelMasterC2CommandsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanelMasterC2CommandsLayout.createSequentialGroup()
                        .add(jButtonReadInOutDataIn)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonReadInOutDataOut))
                    .add(jPanelMasterC2CommandsLayout.createSequentialGroup()
                        .add(jButtonGetDiag)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonGetCfg))
                    .add(jPanelMasterC2CommandsLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel16)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jTextFieldSlaveAddress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(2, 2, 2)
                .add(jPanelMasterC2CommandsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton8)
                    .add(jButton9))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanel2ComponentShown(evt);
            }
        });
        jPanel2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jPanel2FocusGained(evt);
            }
        });

        jButtonInitComm.setBackground(resourceMap.getColor("jButtonInitComm.background")); // NOI18N
        jButtonInitComm.setForeground(resourceMap.getColor("jButtonInitComm.foreground")); // NOI18N
        jButtonInitComm.setText(resourceMap.getString("jButtonInitComm.text")); // NOI18N
        jButtonInitComm.setName("jButtonInitComm"); // NOI18N
        jButtonInitComm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInitCommActionPerformed(evt);
            }
        });

        jComboUSBBox.setName("jComboUSBBox"); // NOI18N
        jComboUSBBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboUSBBoxActionPerformed(evt);
            }
        });

        jButtonRefreshUSBList.setText(resourceMap.getString("jButtonRefreshUSBList.text")); // NOI18N
        jButtonRefreshUSBList.setName("jButtonRefreshUSBList"); // NOI18N
        jButtonRefreshUSBList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRefreshUSBListActionPerformed(evt);
            }
        });

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jTextFieldMasterAddress.setText(resourceMap.getString("jTextFieldMasterAddress.text")); // NOI18N
        jTextFieldMasterAddress.setName("jTextFieldMasterAddress"); // NOI18N

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jTextFieldSlotTimeMs.setText(resourceMap.getString("jTextFieldSlotTimeMs.text")); // NOI18N
        jTextFieldSlotTimeMs.setName("jTextFieldSlotTimeMs"); // NOI18N
        jTextFieldSlotTimeMs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldSlotTimeMsActionPerformed(evt);
            }
        });

        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jButtonInitComm)
                        .add(47, 47, 47))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                        .add(jComboUSBBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonRefreshUSBList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextFieldMasterAddress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jLabel10)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextFieldSlotTimeMs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel14)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboUSBBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonRefreshUSBList))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jTextFieldMasterAddress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel10)
                    .add(jTextFieldSlotTimeMs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel14))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButtonInitComm)
                .addContainerGap())
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("jPanel7.border.title"))); // NOI18N
        jPanel7.setName("jPanel7"); // NOI18N

        jButtonMasterStatus.setText(resourceMap.getString("jButtonMasterStatus.text")); // NOI18N
        jButtonMasterStatus.setName("jButtonMasterStatus"); // NOI18N
        jButtonMasterStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMasterStatusActionPerformed(evt);
            }
        });

        jLabelMasterStatus.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelMasterStatus.setText(resourceMap.getString("jLabelMasterStatus.text")); // NOI18N
        jLabelMasterStatus.setName("jLabelMasterStatus"); // NOI18N
        jLabelMasterStatus.setOpaque(true);

        jButtonGetLiveList.setText(resourceMap.getString("jButtonGetLiveList.text")); // NOI18N
        jButtonGetLiveList.setName("jButtonGetLiveList"); // NOI18N
        jButtonGetLiveList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGetLiveListActionPerformed(evt);
            }
        });

        jLabelLiveListTotalStations.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelLiveListTotalStations.setText(resourceMap.getString("jLabelLiveListTotalStations.text")); // NOI18N
        jLabelLiveListTotalStations.setName("jLabelLiveListTotalStations"); // NOI18N

        jButtonClearTables.setText(resourceMap.getString("jButtonClearTables.text")); // NOI18N
        jButtonClearTables.setName("jButtonClearTables"); // NOI18N
        jButtonClearTables.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearTablesActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jButtonMasterStatus)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel7Layout.createSequentialGroup()
                        .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jPanel7Layout.createSequentialGroup()
                                .add(jButtonGetLiveList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 157, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabelLiveListTotalStations, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButtonClearTables, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jLabelMasterStatus, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE))
                        .add(145, 145, 145))))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .add(jButtonMasterStatus)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelMasterStatus)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonGetLiveList)
                    .add(jLabelLiveListTotalStations)
                    .add(jButtonClearTables))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout jPanelClass2MasterLayout = new org.jdesktop.layout.GroupLayout(jPanelClass2Master);
        jPanelClass2Master.setLayout(jPanelClass2MasterLayout);
        jPanelClass2MasterLayout.setHorizontalGroup(
            jPanelClass2MasterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelClass2MasterLayout.createSequentialGroup()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 274, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 402, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelMasterC2Commands, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(216, Short.MAX_VALUE))
        );
        jPanelClass2MasterLayout.setVerticalGroup(
            jPanelClass2MasterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelClass2MasterLayout.createSequentialGroup()
                .add(jPanelClass2MasterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanelMasterC2Commands, 0, 109, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanelClass2Master.TabConstraints.tabTitle"), jPanelClass2Master); // NOI18N

        jSplitPaneOutput.setDividerLocation(250);
        jSplitPaneOutput.setName("jSplitPaneOutput"); // NOI18N

        jPanelTable.setName("jPanelTable"); // NOI18N

        jScrollPane2.setBackground(resourceMap.getColor("jScrollPane2.background")); // NOI18N
        jScrollPane2.setForeground(resourceMap.getColor("jScrollPane2.foreground")); // NOI18N
        jScrollPane2.setName("jScrollPane2"); // NOI18N
        jScrollPane2.getViewport().setBackground(Color.WHITE);

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setBackground(resourceMap.getColor("jTable1.background")); // NOI18N
        jTable1.setFont(resourceMap.getFont("jTable1.font")); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Frame Nr", "Address", "Msg Type", "Service", "Req/Res", "SAPS", "Data_Unit_Len", "Data", "Delta Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setToolTipText(resourceMap.getString("jTable1.toolTipText")); // NOI18N
        jTable1.setCellSelectionEnabled(true);
        jTable1.setName("jTable1"); // NOI18N
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jTable1.setShowHorizontalLines(false);
        jTable1.setShowVerticalLines(false);
        ((DefaultTableModel) jTable1.getModel()).setRowCount(0);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jTable1MouseReleased(evt);
            }
        });
        jScrollPane2.setViewportView(jTable1);
        jTable1.getAccessibleContext().setAccessibleDescription(resourceMap.getString("jTable1.AccessibleContext.accessibleDescription")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanelTableLayout = new org.jdesktop.layout.GroupLayout(jPanelTable);
        jPanelTable.setLayout(jPanelTableLayout);
        jPanelTableLayout.setHorizontalGroup(
            jPanelTableLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1003, Short.MAX_VALUE)
        );
        jPanelTableLayout.setVerticalGroup(
            jPanelTableLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE)
        );

        jSplitPaneOutput.setRightComponent(jPanelTable);

        jPanelInfo.setName("jPanelInfo"); // NOI18N

        jPanelLegend.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelLegend.setName("jPanelLegend"); // NOI18N

        jLabel20.setBackground(resourceMap.getColor("jLabel20.background")); // NOI18N
        jLabel20.setForeground(resourceMap.getColor("jLabel20.foreground")); // NOI18N
        jLabel20.setText(resourceMap.getString("jLabel20.text")); // NOI18N
        jLabel20.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel20.setName("jLabel20"); // NOI18N
        jLabel20.setOpaque(true);

        jLabel22.setText(resourceMap.getString("jLabel22.text")); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N

        jLabel23.setBackground(resourceMap.getColor("jLabel23.background")); // NOI18N
        jLabel23.setForeground(resourceMap.getColor("jLabel23.foreground")); // NOI18N
        jLabel23.setText(resourceMap.getString("jLabel23.text")); // NOI18N
        jLabel23.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel23.setName("jLabel23"); // NOI18N
        jLabel23.setOpaque(true);

        jLabel24.setText(resourceMap.getString("jLabel24.text")); // NOI18N
        jLabel24.setName("jLabel24"); // NOI18N

        jLabel25.setBackground(resourceMap.getColor("jLabel25.background")); // NOI18N
        jLabel25.setForeground(resourceMap.getColor("jLabel25.foreground")); // NOI18N
        jLabel25.setText(resourceMap.getString("jLabel25.text")); // NOI18N
        jLabel25.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel25.setName("jLabel25"); // NOI18N
        jLabel25.setOpaque(true);

        jLabel26.setText(resourceMap.getString("jLabel26.text")); // NOI18N
        jLabel26.setName("jLabel26"); // NOI18N

        jLabel27.setBackground(resourceMap.getColor("jLabel27.background")); // NOI18N
        jLabel27.setText(resourceMap.getString("jLabel27.text")); // NOI18N
        jLabel27.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel27.setName("jLabel27"); // NOI18N
        jLabel27.setOpaque(true);

        jLabel28.setText(resourceMap.getString("jLabel28.text")); // NOI18N
        jLabel28.setName("jLabel28"); // NOI18N

        jLabel31.setBackground(resourceMap.getColor("jLabel31.background")); // NOI18N
        jLabel31.setText(resourceMap.getString("jLabel31.text")); // NOI18N
        jLabel31.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel31.setName("jLabel31"); // NOI18N
        jLabel31.setOpaque(true);

        jLabel32.setText(resourceMap.getString("jLabel32.text")); // NOI18N
        jLabel32.setName("jLabel32"); // NOI18N

        jLabel35.setBackground(resourceMap.getColor("jLabel35.background")); // NOI18N
        jLabel35.setForeground(resourceMap.getColor("jLabel35.foreground")); // NOI18N
        jLabel35.setText(resourceMap.getString("jLabel35.text")); // NOI18N
        jLabel35.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel35.setName("jLabel35"); // NOI18N
        jLabel35.setOpaque(true);

        jLabel36.setText(resourceMap.getString("jLabel36.text")); // NOI18N
        jLabel36.setName("jLabel36"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanelLegendLayout = new org.jdesktop.layout.GroupLayout(jPanelLegend);
        jPanelLegend.setLayout(jPanelLegendLayout);
        jPanelLegendLayout.setHorizontalGroup(
            jPanelLegendLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelLegendLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelLegendLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanelLegendLayout.createSequentialGroup()
                        .add(jLabel20)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel22))
                    .add(jPanelLegendLayout.createSequentialGroup()
                        .add(jLabel23)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel24))
                    .add(jPanelLegendLayout.createSequentialGroup()
                        .add(jLabel25)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel26))
                    .add(jPanelLegendLayout.createSequentialGroup()
                        .add(jLabel35)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel36))
                    .add(jPanelLegendLayout.createSequentialGroup()
                        .add(jLabel27)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel28))
                    .add(jPanelLegendLayout.createSequentialGroup()
                        .add(jLabel31)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel32)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelLegendLayout.setVerticalGroup(
            jPanelLegendLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelLegendLayout.createSequentialGroup()
                .add(jPanelLegendLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel20)
                    .add(jLabel22))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelLegendLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel24)
                    .add(jLabel23))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelLegendLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel26)
                    .add(jLabel25))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanelLegendLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel36)
                    .add(jLabel35))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelLegendLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel28)
                    .add(jLabel27))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelLegendLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel32)
                    .add(jLabel31))
                .add(12, 12, 12))
        );

        treePanel.setBackground(resourceMap.getColor("treePanel.background")); // NOI18N
        treePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("treePanel.border.title"))); // NOI18N
        treePanel.setMinimumSize(new java.awt.Dimension(170, 472));
        treePanel.setName("treePanel"); // NOI18N
        treePanel.setPreferredSize(new java.awt.Dimension(170, 472));
        treePanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout jPanelInfoLayout = new org.jdesktop.layout.GroupLayout(jPanelInfo);
        jPanelInfo.setLayout(jPanelInfoLayout);
        jPanelInfoLayout.setHorizontalGroup(
            jPanelInfoLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelInfoLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelInfoLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanelLegend, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, treePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelInfoLayout.setVerticalGroup(
            jPanelInfoLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelInfoLayout.createSequentialGroup()
                .addContainerGap()
                .add(treePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 279, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelLegend, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPaneOutput.setLeftComponent(jPanelInfo);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1259, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSplitPaneOutput, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1259, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSplitPaneOutput, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void StepSimButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StepSimButtonActionPerformed
        try {
            StepSimulationButtonPress( );
        } catch (ProfibusFrameException ex) {
            Logger.getLogger(FDLTelegramsTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_StepSimButtonActionPerformed

    private void RunSimButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RunSimButtonActionPerformed
        RunSimulationButtonPress( );
    }//GEN-LAST:event_RunSimButtonActionPerformed

    private void ClearTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearTableButtonActionPerformed
       if( isSimulatorStarted == true )
        {
            //Stop simulation to clear
            RunSimulationButtonPress( );
        }
        
        ClearFrameTable();
    }//GEN-LAST:event_ClearTableButtonActionPerformed

    protected void ClearFrameTable( )
    {
        DefaultTableModel model = (DefaultTableModel)jTable1.getModel(); 
        int rows = model.getRowCount(); 
        for(int i = rows - 1; i >=0; i--)
        {
           model.removeRow(i); 
        }    
    }
    
    private void StartSimServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartSimServerButtonActionPerformed
        StartServerButtonPress( );
    }//GEN-LAST:event_StartSimServerButtonActionPerformed

private void jTable1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseReleased

}//GEN-LAST:event_jTable1MouseReleased

private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        String frameDescription = new String();
        
        Point p = evt.getPoint(); 
        int row = jTable1.rowAtPoint(p);
        
        int RowNumber   = (Integer) ((DefaultTableModel) getjTable1().getModel()).getValueAt(row, 0);
        String frameString = (String) ((DefaultTableModel) getjTable1().getModel()).getValueAt(row, 7);
        String elapsedTime = (String) ((DefaultTableModel) getjTable1().getModel()).getValueAt(row, 8);
        
        ProfibusFrame pbf = null;
        try {
            pbf = new ProfibusFrame( frameString );
        } catch (ProfibusFrameException ex) {
            Logger.getLogger(FDLTelegramsTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        frameDescription += "<HTML><FONT size=\"4\" face=\"Courier New\" color=\"black\">";
        frameDescription += "<B><U>Frame Description</B></U><BR>";
        frameDescription += "<B>Frame Number...:</B> " + String.valueOf(RowNumber) + "<BR>";
        frameDescription += "<B>Frame Structure:</B> " + pbf.getFrameType().toString() + "<BR>";
        frameDescription += "<B>Frame Type.....:</B> " + ( pbf.IsReq() == true ? "Request" : "Response") + "<BR>";
        frameDescription += "<BR>";
        frameDescription += "<B>Source Address.....:</B> " + String.valueOf( pbf.getSourceAddr() ) + "<BR>";
        frameDescription += "<B>Destination Address:</B> " + String.valueOf( pbf.getDestAddr() )   + "<BR>";
        frameDescription += "<BR>";
        frameDescription += "<B>Service:</B> " + pbf.getFrameServiceString() + "<BR>";
        
        if( pbf.getDecodedDataUnit() != null )
        {
            frameDescription += "<B>Description:</B><BR>";
            frameDescription += "<FONT size=\"3\" face=\"Courier New\" color=\"black\">";
            frameDescription += pbf.getDecodedDataUnit().toHTML();
            frameDescription += "</FONT>";
        }
        
        if( pbf.getData_Unit() != null )
        {
            frameDescription += "<BR>";
            frameDescription += "<B>Data Unit</B> (Len: "+String.valueOf( pbf.getData_Unit().length) + ")";
            frameDescription += "<BR>";
            frameDescription += ConversionsAdvanced.toStringFromStream( pbf.getData_Unit() );
            
        }
        
        if( pbf.getRawData().length < 20 )
        {
            frameDescription += "<BR>";
            frameDescription += "<B>RawData</B> (Len: "+String.valueOf( pbf.getRawData().length) + ")";
            frameDescription += "<BR>";
            frameDescription += ConversionsAdvanced.toStringFromStream( pbf.getRawData() );
        }

        frameDescription += "<BR>";
        frameDescription += "<B>TimeStamp..:</B> " + pbf.getTimeStamp().toString() + "<BR>";
        frameDescription += "<B>ElapsedTime:</B> " + elapsedTime;
        frameDescription += "<BR></FONT>";
    
        jTable1.setToolTipText(String.valueOf(frameDescription));    
        
        if (evt.isControlDown() == true)
        {
            //Generate and show graph
            //String sfile = GenerateSamples( pbf );
            //ShowSamplesGraph( sfile );
        }
}//GEN-LAST:event_jTable1MouseClicked

private void jButtonColumnHiderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColumnHiderActionPerformed
    JFrameColumnSel j1 = new JFrameColumnSel(this);
    j1.setVisible(true);
    //hider.hide("Frame Nr");
}//GEN-LAST:event_jButtonColumnHiderActionPerformed

private void jTextFieldSlotTimeMsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldSlotTimeMsActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jTextFieldSlotTimeMsActionPerformed

private void jTextFieldSlaveAddressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldSlaveAddressActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jTextFieldSlaveAddressActionPerformed

private void jButtonInitCommActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInitCommActionPerformed
try {
        //Start FDL Machine
        String COMPORT        = (String) ( jComboUSBBox.getSelectedItem() );
        String SerialServerIP = (String) ( "localhost" );
        String BaudRate       = "9600";
        String MasterAddr     = jTextFieldMasterAddress.getText();
        float fSlotTime       = Float.valueOf( jTextFieldSlotTimeMs.getText() ) / 1000;

        String SlotTime       = String.valueOf(fSlotTime);


        if( dpTCP.isConnected() == true )
        {
            System.out.println( "Trying to Disconnect!" );
            dpTCP.disconnect();
            if( !dpTCP.isConnected() ) {
                jButtonInitComm.setForeground(Color.red);
                jButtonInitComm.setBackground(Color.red);
                jButtonInitComm.setText("Connect USB Master (Disconnected)");
            }
        }
        else
        {
            System.out.println( "Trying to connect!" );
            dpTCP.connect( SerialServerIP, 30000, COMPORT, BaudRate, SlotTime, MasterAddr );
            Thread.sleep(200);                
            if( dpTCP.isConnected() ) {
                jButtonInitComm.setForeground(Color.blue);
                jButtonInitComm.setBackground(Color.blue);
                jButtonInitComm.setText("Disconnect USB Master (Connected)");
            }
        }


        InitializeLAS();

    } catch (Exception ex) {
        Logger.getLogger(FDLTelegramsTable.class.getName()).log(Level.SEVERE, null, ex);
    }
}//GEN-LAST:event_jButtonInitCommActionPerformed

private void InitializeLAS()
{
    for(int i = 0; i < LAS.length; i++) LAS[ i ] =4;
}

private void jPanel2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jPanel2FocusGained
    
}//GEN-LAST:event_jPanel2FocusGained

private void jComboUSBBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboUSBBoxActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jComboUSBBoxActionPerformed

private void jPanel2ComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel2ComponentShown
    
}//GEN-LAST:event_jPanel2ComponentShown

private void jButtonRefreshUSBListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRefreshUSBListActionPerformed
    RefreshUSBList();
}//GEN-LAST:event_jButtonRefreshUSBListActionPerformed

private void jButtonMasterStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMasterStatusActionPerformed
    
    int MasterCondition = dpTCP.MASTER_OFFLINE;
    String sreturn = new String();
    
    if (dpTCP.isConnected() == true)
        MasterCondition = CommandServicesInterface.getMasterStatus(dpTCP, Integer.parseInt( jTextFieldMasterAddress.getText() ) );
    
    if( MasterCondition == dpTCP.MASTER_OFFLINE || (dpTCP.isConnected() == false) ) { 
        sreturn = "Status: Master is OFFLINE";
        jLabelMasterStatus.setForeground(Color.blue);
        jLabelMasterStatus.setBackground(Color.red);
    }
    else if( MasterCondition == dpTCP.MASTER_NOT_READY_TO_ENTER_IN_LOGICAL_TOKEN_RING ) {
        sreturn = "Status: Master is Not Ready to enter in Logical Token Ring";
        jLabelMasterStatus.setForeground(Color.red);
        jLabelMasterStatus.setBackground(Color.yellow);
    }
    else if( MasterCondition == dpTCP.MASTER_READY_TO_ENTER_IN_LOGICAL_TOKEN_RING ) {
        sreturn = "Status: Master is Ready to enter in Logical Token Ring";
        jLabelMasterStatus.setForeground(Color.blue);
        jLabelMasterStatus.setBackground(Color.yellow);
    }
    else if( MasterCondition == dpTCP.MASTER_ALREADY_IN_LOGICAL_TOKEN_RING ) {
        sreturn = "Status: Master is Already in Logical Token Ring";                        
        jLabelMasterStatus.setForeground(Color.black);
        jLabelMasterStatus.setBackground(Color.green);
    }

    jLabelMasterStatus.setText( sreturn );
    
}//GEN-LAST:event_jButtonMasterStatusActionPerformed

private void jButtonGetLiveListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGetLiveListActionPerformed
        
    //stationGrid.removeAll(  );        

    //stationGrid.repaint(  );
    //stationGrid.invalidate(  );

    jButtonGetLiveList.setEnabled(false);

    int[] LAS = new int[127];
    byte [] ll = null;
    int i=0;
    int t=0;

    for(i = 0; i < LAS.length; i++) LAS[ i ] =4;

    if (dpTCP.isConnected() == true)
        ll = CommandServicesInterface.getLiveList(dpTCP, Integer.parseInt( jTextFieldMasterAddress.getText() ) );

    if( ll != null ) {
        for( i=0; i < ll.length; i++ )
        {
            LAS[ i ] = ll[ i ];

            if( ll[i] != 4 )
            {
                if( ll[i] == 0 )
                {
                    stationGrid.SetOperationalStation( i );
                    t+=1;
                }
                else if( ll[i] == 3 )
                {
                    stationGrid.SetMasterStation( i );
                    t+=1;                        
                }
            }
            else
            {
                stationGrid.SetOfflineStation( i );
            }
        }
    }
    stationGrid.repaint(  );
    jLabelLiveListTotalStations.setText( String.valueOf(t)+ " stations" );
    jButtonGetLiveList.setEnabled(true);
}//GEN-LAST:event_jButtonGetLiveListActionPerformed

private void jButtonGetDiagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGetDiagActionPerformed
// TODO add your handling code here:
    ProfibusFrame freq = ProfibusServices.SendServiceRequest(Integer.parseInt(jTextFieldMasterAddress.getText()), Integer.parseInt(jTextFieldSlaveAddress.getText()), new Service(Service.PB_REQ_GET_DIAG) );
    setProfidoctorPackage( freq );
    
    ProfibusFrame fres = CommandServicesInterface.getSlaveDiag( dpTCP, Integer.parseInt(jTextFieldMasterAddress.getText()), Integer.parseInt(jTextFieldSlaveAddress.getText()) );
    setProfidoctorPackage( fres );
}//GEN-LAST:event_jButtonGetDiagActionPerformed

private void jButtonClearTablesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearTablesActionPerformed
    //stationGrid.SetDefaultSlaveColors( );
    //stationGrid.repaint();
    ClearFrameTable();
}//GEN-LAST:event_jButtonClearTablesActionPerformed

private void jButtonGetCfgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGetCfgActionPerformed
    ProfibusFrame freq = ProfibusServices.SendServiceRequest( Integer.parseInt(jTextFieldMasterAddress.getText()), Integer.parseInt(jTextFieldSlaveAddress.getText()), new Service(Service.PB_REQ_GET_CFG) );
    setProfidoctorPackage( freq );
    
    ProfibusFrame fres = CommandServicesInterface.getSlaveCfg( dpTCP, Integer.parseInt(jTextFieldMasterAddress.getText()), Integer.parseInt(jTextFieldSlaveAddress.getText()) );
    setProfidoctorPackage( fres );
}//GEN-LAST:event_jButtonGetCfgActionPerformed

private void jButtonReadInOutDataInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonReadInOutDataInActionPerformed
    ProfibusFrame freq = ProfibusServices.SendServiceRequest(Integer.parseInt(jTextFieldMasterAddress.getText()), Integer.parseInt(jTextFieldSlaveAddress.getText()), new Service(Service.PB_REQ_RD_INPUTS) );
    setProfidoctorPackage( freq );
    
    ProfibusFrame fres = CommandServicesInterface.ReadInputs( dpTCP, Integer.parseInt(jTextFieldMasterAddress.getText()), Integer.parseInt(jTextFieldSlaveAddress.getText()) );
    setProfidoctorPackage( fres );
}//GEN-LAST:event_jButtonReadInOutDataInActionPerformed

private void jButtonReadInOutDataOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonReadInOutDataOutActionPerformed
    ProfibusFrame freq = ProfibusServices.SendServiceRequest( Integer.parseInt(jTextFieldMasterAddress.getText()), Integer.parseInt(jTextFieldSlaveAddress.getText()), new Service(Service.PB_REQ_RD_OUTPUTS) );
    setProfidoctorPackage( freq );
    
    ProfibusFrame fres = CommandServicesInterface.ReadOutputs( dpTCP, Integer.parseInt(jTextFieldMasterAddress.getText()), Integer.parseInt(jTextFieldSlaveAddress.getText()) );
    setProfidoctorPackage( fres );
}//GEN-LAST:event_jButtonReadInOutDataOutActionPerformed

private void jButtonChangeConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonChangeConfigActionPerformed
    //Create a file chooser
    final JFileChooser fc = new JFileChooser();
    String completePathAndFile = null;
    String shortfileName = null;
    FileFilter filterPB = new ExtensionFileFilter("Hilscher/Sycon Profibus Configurations (*.pb)", "pb");

    fc.setFileFilter(filterPB);
    int returnVal = fc.showOpenDialog(this);
    
    if (returnVal == JFileChooser.APPROVE_OPTION) {
        completePathAndFile = fc.getSelectedFile().toString();
        shortfileName = fc.getName( fc.getSelectedFile() );
        System.out.println( "Opening: "+ completePathAndFile );
    } else {
        System.out.println( "Open command cancelled by user" );
    }
    
    if( completePathAndFile != null )
    {
        pbf = new PBFile( completePathAndFile );
        pbf.setShortFileName( shortfileName );
    }
    else
        return;
    

    DefaultTableModel model = (DefaultTableModel)jTableCfg.getModel(); 
    int rows = model.getRowCount(); 
    for(int i = rows - 1; i >=0; i--)
    {
       model.removeRow(i); 
    }    
    
    Map<Integer,DeviceCfg> map = pbf.getDeviceMap();
    Iterator<Map.Entry<Integer,DeviceCfg>> entries = map.entrySet().iterator();
    
    int total_in  = 0;
    int total_out = 0;
    
    while( entries.hasNext() ) {
        Map.Entry<Integer,DeviceCfg> entry = entries.next();
        Integer addr = entry.getKey();
        DeviceCfg dev = entry.getValue();
        Vector rowData = new Vector( );
       
        String format = String.format("%%0%dd", 3);
        String sAddr  = String.format(format, addr);
        
        total_in += dev.INLength;
        total_out += dev.OUTLength;
        
        rowData.add( sAddr ); //Address
        rowData.add( dev.name ); //TAG
        rowData.add( dev.INLength ); //Bytes IN
        rowData.add( dev.OUTLength ); //Bytes OUT
        rowData.add( dev.cfg ); //Identifiers

        ( (DefaultTableModel) jTableCfg.getModel() ).addRow( rowData );
    }
    
    TableRowSorter<TableModel> sorter  = new TableRowSorter<TableModel>( jTableCfg.getModel() );
    Comparator<String> comparator = new Comparator<String>() {

            public int compare(String o1, String o2) {
                return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
            }
        };
    
    sorter.setComparator(1, comparator );
    jTableCfg.setRowSorter(sorter);
    sorter.sort();
    
    jTableCfg.repaint();
    
    jLabelBytesIN.setText(String.valueOf(total_in) + " Bytes Read by Master (IN)");
    jLabelBytesOUT.setText(String.valueOf(total_out) + " Bytes Write by Master (OUT)");
    
    FillLabelsWithTimeConfigurations( );
    
    UpdateSimAfterLoadPBFile();
    
}//GEN-LAST:event_jButtonChangeConfigActionPerformed

private void UpdateSimAfterLoadPBFile( )
{
    ClearFrameTable();
    if( Sim != null )
    {
        Sim.notifyFinish( );
        Sim = null;
    }
    FillLabelsWithTimeConfigurations( );
}

private void jButtonResetStatisticsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResetStatisticsActionPerformed
    ResetStatistics( );
}//GEN-LAST:event_jButtonResetStatisticsActionPerformed

private void jComboBoxProblemTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxProblemTypeActionPerformed
        if( Integer.valueOf( jTextFieldSlaveAddress.getText() ) >= 0 )
            Sim.setSlavePhyProblem( Integer.valueOf( jTextFieldSlaveAddress.getText() ), jComboBoxProblemType.getSelectedIndex() );
}//GEN-LAST:event_jComboBoxProblemTypeActionPerformed

private void jSliderVoltageStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderVoltageStateChanged
        int newvalue = jSliderVoltage.getValue();
        String st = new String();

        if( newvalue < 4 ) {
            st = "(Low)";
        }
        else if (newvalue >= 4 && newvalue <= 7) {
            st = "(Normal)";
        }
        else if (newvalue > 7) {
            st = "(Over)";
        }
       
        jVoltageLabel.setText( Integer.toString( newvalue ) + "V " + st );
        
        if( Integer.valueOf( jTextFieldSlaveAddress.getText() ) >= 0 )
            Sim.setSlaveVoltage( Integer.valueOf( jTextFieldSlaveAddress.getText() ), newvalue );
}//GEN-LAST:event_jSliderVoltageStateChanged

//GEN-FIRST:event_jCheckBoxReadableSignalActionPerformed
 
//GEN-LAST:event_jCheckBoxReadableSignalActionPerformed

private void jButtonApplyCommProblemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonApplyCommProblemActionPerformed
    
    if( jCheckBoxReadableSignal.isSelected() == true )
    {
        //Put device in online
        Sim.getSlaveCollection().setOnline( Integer.valueOf( jTextFieldSlaveAddress.getText() )  , true );
    }
    else
    {
        //Put device in offline
        Sim.getSlaveCollection().setOnline( Integer.valueOf( jTextFieldSlaveAddress.getText() ) , false );
    }
    
}//GEN-LAST:event_jButtonApplyCommProblemActionPerformed

private void jButtonForceNewDiagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonForceNewDiagActionPerformed
        diagitf.modifySlaveDiagAndCfg( getCheckBoxDiagBits(), jTextFieldConfiguration.getText(), jTextFieldIdentNumber.getText() );
}//GEN-LAST:event_jButtonForceNewDiagActionPerformed

private void jCheckBoxMirrorToSimulatedStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxMirrorToSimulatedStateChanged
    bMirrorToSimulated = jCheckBoxMirrorToSimulated.isSelected();
}//GEN-LAST:event_jCheckBoxMirrorToSimulatedStateChanged

    public byte[] getCheckBoxDiagBits( )
    {
        byte b[] = new byte[2];
        
        b[0] |= (jCheckBox1.isSelected() == true) ? 0x01 : 0x00;
        b[0] |= (jCheckBox2.isSelected() == true) ? 0x02 : 0x00;
        b[0] |= (jCheckBox3.isSelected() == true) ? 0x04 : 0x00;
        b[0] |= (jCheckBox4.isSelected() == true) ? 0x08 : 0x00;
        b[0] |= (jCheckBox5.isSelected() == true) ? 0x10 : 0x00;
        b[0] |= (jCheckBox6.isSelected() == true) ? 0x20 : 0x00;
        b[0] |= (jCheckBox7.isSelected() == true) ? 0x40 : 0x00;
        b[0] |= (jCheckBox8.isSelected() == true) ? 0x80 : 0x00;

        //Octet 2
        b[1] |= (jCheckBox9.isSelected() == true) ? 0x01 : 0x00;
        b[1] |= (jCheckBox10.isSelected() == true)? 0x02 : 0x00;
        b[1] |= (jCheckBox11.isSelected() == true)? 0x04 : 0x00;
        b[1] |= (jCheckBox12.isSelected() == true)? 0x08 : 0x00;
        b[1] |= (jCheckBox13.isSelected() == true)? 0x10 : 0x00;
        b[1] |= (jCheckBox14.isSelected() == true)? 0x20 : 0x00;
        b[1] |= (jCheckBox15.isSelected() == true)? 0x80 : 0x00;
        
        return b;
    }
    
    public void fillDiagCheckBoxes(byte[] b )
    {
        boolean v[] = new boolean[16];
        
        if( b.length >= 2 )
        {
            //Octet 1
            v[0] = ((b[0] & 0x01) != 0) ? true : false;
            v[1] = ((b[0] & 0x02) != 0) ? true : false;
            v[2] = ((b[0] & 0x04) != 0) ? true : false;
            v[3] = ((b[0] & 0x08) != 0) ? true : false;
            v[4] = ((b[0] & 0x10) != 0) ? true : false;
            v[5] = ((b[0] & 0x20) != 0) ? true : false;
            v[6] = ((b[0] & 0x40) != 0) ? true : false;
            v[7] = ((b[0] & 0x80) != 0) ? true : false;

            //Octet 2
            v[8] = ((b[1] & 0x01) != 0) ? true : false;
            v[9] = ((b[1] & 0x02) != 0) ? true : false;
            v[10]= ((b[1] & 0x04) != 0) ? true : false;
            v[11]= ((b[1] & 0x08) != 0) ? true : false;
            v[12]= ((b[1] & 0x10) != 0) ? true : false;
            v[13]= ((b[1] & 0x20) != 0) ? true : false;
            v[14]= ((b[1] & 0x40) != 0) ? true : false;
            v[15]= ((b[1] & 0x80) != 0) ? true : false;

            fillDiagCheckBoxes( v );
        }
    }
    
    public void fillDiagCheckBoxes(boolean[] b )
    {
        jCheckBox1.setSelected(b[0]);
        jCheckBox2.setSelected(b[1]);
        jCheckBox3.setSelected(b[2]);
        jCheckBox4.setSelected(b[3]);
        jCheckBox5.setSelected(b[4]);
        jCheckBox6.setSelected(b[5]);
        jCheckBox7.setSelected(b[6]);
        jCheckBox8.setSelected(b[7]);
        jCheckBox9.setSelected(b[8]);
        jCheckBox10.setSelected(b[9]);
        jCheckBox11.setSelected(b[10]);
        jCheckBox12.setSelected(b[11]);
        jCheckBox13.setSelected(b[12]);
        jCheckBox14.setSelected(b[13]);
        jCheckBox15.setSelected(b[14]);
        jCheckBox16.setSelected(b[15]);
    }    


    @Override
    public JToolTip createToolTip() {
        JToolTip tip = super.createToolTip();
        tip.setBackground(Color.BLUE);
        tip.setForeground(Color.BLACK);
        tip.setFont(new Font("Courier New", Font.PLAIN, 10));
        return tip;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ClearTableButton;
    private javax.swing.JToggleButton RunSimButton;
    private javax.swing.JButton StartSimServerButton;
    private javax.swing.JButton StepSimButton;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jButtonApplyCommProblem;
    private javax.swing.JButton jButtonChangeConfig;
    private javax.swing.JButton jButtonClearTables;
    private javax.swing.JButton jButtonColumnHider;
    private javax.swing.JButton jButtonForceNewDiag;
    private javax.swing.JButton jButtonGetCfg;
    private javax.swing.JButton jButtonGetDiag;
    private javax.swing.JButton jButtonGetLiveList;
    private javax.swing.JButton jButtonInitComm;
    private javax.swing.JButton jButtonMasterStatus;
    private javax.swing.JButton jButtonReadInOutDataIn;
    private javax.swing.JButton jButtonReadInOutDataOut;
    private javax.swing.JButton jButtonRefreshUSBList;
    private javax.swing.JButton jButtonResetStatistics;
    private javax.swing.JButton jButtonRestoreStation;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox10;
    private javax.swing.JCheckBox jCheckBox11;
    private javax.swing.JCheckBox jCheckBox12;
    private javax.swing.JCheckBox jCheckBox13;
    private javax.swing.JCheckBox jCheckBox14;
    private javax.swing.JCheckBox jCheckBox15;
    private javax.swing.JCheckBox jCheckBox16;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox7;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JCheckBox jCheckBox9;
    private javax.swing.JCheckBox jCheckBoxMirrorToSimulated;
    private javax.swing.JCheckBox jCheckBoxReadableSignal;
    private javax.swing.JComboBox jComboBoxProblemType;
    private javax.swing.JComboBox jComboUSBBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelBaudRate;
    private javax.swing.JLabel jLabelBytesIN;
    private javax.swing.JLabel jLabelBytesOUT;
    private javax.swing.JLabel jLabelConfigurationFileName;
    private javax.swing.JLabel jLabelDataExchange;
    private javax.swing.JLabel jLabelDataExchangeNum;
    private javax.swing.JLabel jLabelDiffTTR;
    private javax.swing.JLabel jLabelGAP;
    private javax.swing.JLabel jLabelHSA;
    private javax.swing.JLabel jLabelIdent;
    private javax.swing.JLabel jLabelIdent1;
    private javax.swing.JLabel jLabelInvalidFCS;
    private javax.swing.JLabel jLabelInvalidFCSNum;
    private javax.swing.JLabel jLabelLiveListTotalStations;
    private javax.swing.JLabel jLabelMaster;
    private javax.swing.JLabel jLabelMasterAddr;
    private javax.swing.JLabel jLabelMasterStatus;
    private javax.swing.JLabel jLabelMaxTSDR;
    private javax.swing.JLabel jLabelMinTSDR;
    private javax.swing.JLabel jLabelQuietTime;
    private javax.swing.JLabel jLabelRetries;
    private javax.swing.JLabel jLabelRetries1;
    private javax.swing.JLabel jLabelRetriesNum;
    private javax.swing.JLabel jLabelSetupTime;
    private javax.swing.JLabel jLabelSimulatorStatus;
    private javax.swing.JLabel jLabelSlotTime;
    private javax.swing.JLabel jLabelSync;
    private javax.swing.JLabel jLabelSyncNum;
    private javax.swing.JLabel jLabelSysReaction;
    private javax.swing.JLabel jLabelSysReactionSlowest;
    private javax.swing.JLabel jLabelSystemReactionTime;
    private javax.swing.JLabel jLabelSystemReactionTimeSlowest;
    private javax.swing.JLabel jLabelTRR;
    private javax.swing.JLabel jLabelTRRANDWatchdog;
    private javax.swing.JLabel jLabelTTR;
    private javax.swing.JLabel jLabelTTR2;
    private javax.swing.JLabel jLabelWatchDogTime;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelClass2Master;
    private javax.swing.JPanel jPanelControl;
    private javax.swing.JPanel jPanelInfo;
    private javax.swing.JPanel jPanelLegend;
    private javax.swing.JPanel jPanelMasterC2Commands;
    private javax.swing.JPanel jPanelMasterTimeCfg;
    private javax.swing.JPanel jPanelSimulationControl;
    private javax.swing.JPanel jPanelSimulationStatistics;
    private javax.swing.JPanel jPanelSlaveCommSignal;
    private javax.swing.JPanel jPanelTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSlider jSliderVoltage;
    private javax.swing.JSplitPane jSplitPaneOutput;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTableCfg;
    private javax.swing.JTextField jTextFieldConfiguration;
    private javax.swing.JTextField jTextFieldIdentNumber;
    private javax.swing.JTextField jTextFieldMasterAddress;
    private javax.swing.JTextField jTextFieldSlaveAddress;
    private javax.swing.JTextField jTextFieldSlotTimeMs;
    private javax.swing.JLabel jVoltageLabel;
    private javax.swing.JPanel slavePanelContainer;
    private javax.swing.JPanel treePanel;
    // End of variables declaration//GEN-END:variables

    public void setProfidoctorPackage(ProfibusFrame pbFrame) {
        //caso o bot�o de stooPlay estiver ligado, recebe o pacote e insere na lista.
        if (gettingPackages) {
            Vector rowData = new Vector();
            Float fdelta;
            
            //Get elapsed Time -> fdelta
            if( pbBefore != null )
                fdelta = pbFrame.getMsFrameTime() - pbBefore.getMsFrameTime();
            else
                fdelta = pbFrame.getMsFrameTime();

            fTimeTotalizer += fdelta;
            
            //Check for retry frame
            if( pbBefore != null )
            {
                if( pbFrame.toString().equals( pbBefore.toString() ) == true )
                    uiRetries += 1;
            }
            
            //Check for Sync
            if( (pbFrame.getFCB() == true) && (pbFrame.getFCV() == false) )
            {
                uiSyncs += 1;
            }
            
           
            //Check for InvalidFCS
            
            
            rowData.add( getTableIndex( ) );
            rowData.add( pbFrame.getSourceAddr( ) + "->" + pbFrame.getDestAddr( ) );
            rowData.add( pbFrame.getFrameServiceString( ) );
            rowData.add( pbFrame.getFrameFCString( ) );
            rowData.add( pbFrame.getDirection( ) );
            rowData.add( pbFrame.getSourceSAP( ) + "->" + pbFrame.getDestSAP( ) );
            
            if( pbFrame.getData_Unit() != null ) 
                rowData.add( pbFrame.getData_Unit().length );
            else  
                rowData.add( 0 );
            
            rowData.add( pbFrame.toString( ) );
            
            pbBefore = pbFrame;
            
            if( pbFrame.getSD() == SD.SD4 ) {
                //Token pass - Update Reaction Time Label and clear fTimeTotalizer
                UpdateReactionTimeLabel( );
            }
            
            rowData.add( String.format("%.3f", fdelta) + " ms");
            
            ( (DefaultTableModel) getjTable1().getModel() ).addRow( rowData );
            
            //Store Frame
            RowFrameList.add( rowData );
            FrameList.add( pbFrame );

            getjScrollPane2( ).getViewport( ).scrollRectToVisible( getjTable1( ).getCellRect( getjTable1( ).getRowCount( ), 0, true ) );
            setTableIndex( getTableIndex() + 1 );

            UpdatePannelSlaveList( pbFrame );
            
            //byte selectedDevice = (byte)(liveListTree.getSelectedDevice()-1);
            //if (selectedDevice != -1) {
            //    oscilloscopeChart.setFilteredDevices(selectedDevice);
//                oscilloscopeChart.setProfidoctorPackage(profidoctorPackage);
            //}
        }
    }

    /**
     * @return the tableIndex
     */
    public int getTableIndex() {
        return tableIndex;
    }

    /**
     * @param tableIndex the tableIndex to set
     */
    public void setTableIndex(int tableIndex) {
        this.tableIndex = tableIndex;
    }

    public String getSelectedFDLMessageType() {
        return "???";
    }

    /**
     * @return the jScrollPane2
     */
    public javax.swing.JScrollPane getjScrollPane2() {
        return jScrollPane2;
    }

    /**
     * @param jScrollPane2 the jScrollPane2 to set
     */
    public void setjScrollPane2(javax.swing.JScrollPane jScrollPane2) {
        this.jScrollPane2 = jScrollPane2;
    }

    /**
     * @return the jTable1
     */
    public javax.swing.JTable getjTable1() {
        return jTable1;
    }

    /**
     * @param jTable1 the jTable1 to set
     */
    public void setjTable1(javax.swing.JTable jTable1) {
        this.jTable1 = jTable1;

    }

    static int gridAddr;
    static ProfibusFrame gridLastFrame;
    private void UpdatePannelSlaveList( ProfibusFrame pbframe ) {
        if( (pbframe.IsReq() == true) || (pbframe.getFrameService() == Service.PB_REQ_TOKEN_PASS) )
        {
            //source add is master
            //dst addr is slave
            gridAddr = pbframe.getDestAddr();
            
            //If request, check if last frame is request either
            if( gridLastFrame != null )
            {
                if( gridLastFrame.IsReq() == true )
                {
                    if( gridLastFrame.getFrameService() != gridLastFrame.getFrameServiceObject().PB_REQ_TOKEN_PASS )
                    {
                        stationGrid.SetSyncLostStation( gridAddr );
                        //stationGrid.SetOfflineStation( gridLastFrame.getDestAddr() );
                    }
                }
            }
            
            if( pbframe.getFrameService() != pbframe.getFrameServiceObject().PB_REQ_GLOBAL_CONTROL )
            {
                if( pbframe.IsToken() == false )
                {
                    byte service = pbframe.getFrameService();
                    if(  IsStationOffline( gridAddr ) == true )
                    {
                        if(  service == pbframe.getFrameServiceObject().PB_REQ_GET_DIAG )
                        {
                            //Sync condition
                            stationGrid.SetSyncLostStation( gridAddr );
                        }
                        else
                        {
                            //Request FDL Status
                            stationGrid.SetOfflineStation( gridAddr );
                        }
                    }
                    else
                    {
                        if( service == pbframe.getFrameServiceObject().PB_REQ_DATA_EXCHANGE_ONLY_INPUTS ) 
                        {
                            //Data Exchange! (PUT GREEN)
                            stationGrid.SetDataExchangeStation( gridAddr );
                        }
                    }
                }
            }
            
            //Set active master station
            stationGrid.SetMasterStation( pbframe.getSourceAddr() );
        }
        else
        {
            //source add is slave
            //dst addr is master
            
            //Filter E5 frame. Could be a Data Exchange!
            byte service;
            
            if( pbframe.IsSC() == false )
            {
                gridAddr = pbframe.getSourceAddr();
                service = pbframe.getFrameService();
            }
            else
            {
                gridAddr = gridLastFrame.getDestAddr();
                service = gridLastFrame.getFrameService();
            }
            
            //Put Operational
            stationGrid.SetOperationalStation( gridAddr ); //(PUT BLUE)
            
            //If GetDiag response - go analysys

            
            if(  service == pbframe.getFrameServiceObject().PB_RES_GET_DIAG )
            {
                SlaveDiagnostic diag = new SlaveDiagnostic( pbframe.getData_Unit() );
                
                if( diag.isCfg_Fault() )
                {
                    //Configuration/Param. Error (PUT PURPLE).                    
                    stationGrid.SetCfgErrorStation( gridAddr );
                }
                else if( diag.isPrm_Fault() )
                {
                    //Configuration/Param. Error (PUT RED).                    
                    stationGrid.SetPrmErrorStation( gridAddr );
                }
                else
                {
                    //Else put only diag color
                    stationGrid.SetDiagStation( gridAddr );
                }
                
            }
            else if( (service == pbframe.getFrameServiceObject().PB_RES_DATA_EXCHANGE) 
                    || (service == gridLastFrame.getFrameServiceObject().PB_REQ_DATA_EXCHANGE) )
            {
                //Data Exchange! (PUT GREEN)
                stationGrid.SetDataExchangeStation( gridAddr );
            }
        }
        gridLastFrame = pbframe;
        stationGrid.repaint();
    }

    private boolean IsStationOffline(int addr) {
        Sim = ProfibusSimulatorSingleton.getInstance();
        
        return Sim.isSlaveOnline( addr );
    }

    private void UpdateReactionTimeLabel() {
        String sSystemReactionTime = String.format("%.3f", fTimeTotalizer) + " ms";
        jLabelSystemReactionTime.setText( sSystemReactionTime );
        
        if( fTimeTotalizer > fTimeTotalizerSlowest )
        {
            fTimeTotalizerSlowest = fTimeTotalizer;
            String sSystemReactionTimeSlowest = String.format("%.3f", fTimeTotalizerSlowest) + " ms";
            jLabelSystemReactionTimeSlowest.setText( sSystemReactionTimeSlowest );
        }
        
        //Update TargetRotation Time
        float fTRR = (fTimeTotalizer/1000)*Sim.SM.Master.getBaudRate(); //BECAUSE fTimeTotalizer is in ms
        float fTTR = Sim.SM.Master.getTTR();
        float fBitTime = Sim.SM.Master.getBitTimeMs();
        
        String sTRR = String.format("%.0f", fTRR ) + " TBit - ";
        sTRR += String.format("%.2f", fTRR*fBitTime ) + " ms";
        jLabelTRR.setText( sTRR );
    
        String sTTR2 = String.format("%.0f", fTTR ) + " TBit - ";
        sTTR2 += String.format("%.2f", fTTR*fBitTime ) + " ms";
        jLabelTTR2.setText( sTTR2 );
        
        float fdiffTTR = (fTTR - fTRR)*fBitTime;
        String sDiffTTR = String.format("%.0f", fdiffTTR ) + " ms (";
        sDiffTTR += String.format("%.2f", (fTRR/fTTR)*100) + "% of occupation of configured)";
        jLabelDiffTTR.setText( sDiffTTR );

        if( fdiffTTR <= 0 ) 
        {
            jLabelDiffTTR.setForeground(Color.red);
        }
        else
        {
            jLabelDiffTTR.setForeground(Color.blue);
        }

        if( fTimeTotalizerSlowest < Sim.SM.Master.getWatchdog()  )
        {
            String sWdgConclusion = "Watchdog OK - Slowest TRR (";
            sWdgConclusion += String.format("%.0f", fTimeTotalizerSlowest ) + " ms) < Watchdog configured (";
            sWdgConclusion += String.format("%.0f", Sim.SM.Master.getWatchdog() ) + " ms)";
            jLabelTRRANDWatchdog.setText( sWdgConclusion );
            jLabelTRRANDWatchdog.setForeground(Color.blue);
        }
        else
        {
            String sWdgConclusion = "Watchdog BAD - Slowest TRR (";
            sWdgConclusion += String.format("%.0f", fTimeTotalizerSlowest ) + " ms) > Watchdog configured (";
            sWdgConclusion += String.format("%.0f", Sim.SM.Master.getWatchdog() ) + " ms)";
            jLabelTRRANDWatchdog.setForeground(Color.red);
        }
        
        ComputeNumOnlineAndDataExchangeStations( );
        
        jLabelRetriesNum.setText( Integer.toString(uiRetries) );
        
        jLabelDataExchangeNum.setText( Integer.toString(uiDataExchangeStations) );
        jLabelSyncNum.setText( Integer.toString(uiSyncs) );
        jLabelInvalidFCSNum.setText( Integer.toString(uiInvalidFCS) );
        
        fTimeTotalizer = 0;
    }

    private void RefreshUSBList() {
        //Populate ComboBox with all serial ports avaiable
        System.out.print("Refreshing USB Ports...");
        jComboUSBBox.removeAllItems();
        String items[] = DPLine.listPorts();
        for(int i=0; i < items.length; i++)
            jComboUSBBox.addItem(items[i]); 
    }

    private void ResetStatistics() {
        fTimeTotalizerSlowest = 0;
        uiOnlineStations = 0;
        uiDataExchangeStations = 0;
        uiRetries = 0;
        uiSyncs = 0;
        uiInvalidFCS = 0;
    }

    private void ComputeNumOnlineAndDataExchangeStations() {
        uiOnlineStations = Sim.SM.Master.getNumOnlineSlaves( );
        uiDataExchangeStations = Sim.SM.Master.getNumDataExchangeSlaves( );
    }

    class SelectionListener implements ListSelectionListener {

        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        SelectionListener(JTable table) {
            
        }

        public void valueChanged(ListSelectionEvent e) {

        }
    }

    private void RunSimulationButtonPress() {
        if( pbf != null )
        {
            if( isSimulatorStarted == false )
            {
                if( Sim == null )
                {
                    System.out.print("Starting Master Simulator...\n");

                    if( pbf == null )
                        Sim = ProfibusSimulatorSingleton.getInstance();
                    else
                        Sim = ProfibusSimulatorSingleton.getInstanceUsingPBFile( pbf );
                    //Sim.setOutputWindow( jTextArea1 );
                    Sim.setOutputTable(this);
                    Sim.start( );

                }
                else
                {
                    System.out.print("Resume Master Simulator...\n");
                    Sim.notifyStart( );
                }
                RunSimButton.setText("Stop");
                jLabelSimulatorStatus.setForeground(Color.blue);
                jLabelSimulatorStatus.setText("Simulator is Running");
                StepSimButton.setEnabled(false);
                //jButtonUpdateSimulation.setEnabled( false );
                isSimulatorStarted = true;
            }
            else
            {
                System.out.print("Stopping Master Simulator...\n");
                RunSimButton.setText("Start");
                jLabelSimulatorStatus.setForeground(Color.red);
                jLabelSimulatorStatus.setText("Simulator is Stopped");
                StepSimButton.setEnabled(true);
                //jButtonUpdateSimulation.setEnabled( true );            
                isSimulatorStarted = false;
                Sim.notifyStop( );
            }

            FillLabelsWithTimeConfigurations( );
        }
    }

    private void StartServerButtonPress() {
        Server srv = new Server( );
        srv.start();
    }

    private void StepSimulationButtonPress() throws ProfibusFrameException {
        if( pbf != null )
        {
            Sim = ProfibusSimulatorSingleton.getInstanceUsingPBFile(pbf);
            //Sim.setOutputWindow( jTextArea1 );
            Sim.setOutputTable(this);
            Sim.BuildNextTelegram();

            FillLabelsWithTimeConfigurations( );
        }
    }
    
    public void FillLabelsWithTimeConfigurations( ) {
        String sLabelBaudRate, sLabelGAP, sLabelHSA, sLabelMaxTSDR, sLabelMinTSDR;
        String sLabelQuietTime, sLabelRetries, sLabelSetupTime, sLabelSlotTime;
        String sLabelTTR, sLabelWatchDog;
        
        
        if( pbf == null )
        {
            ProfibusMaster pm = Sim.SM.Master;

            float fMaxTSDR, fMinTSDR, fQuietTime, fSetupTime, fSlotTime, fTTR;

            fMaxTSDR   = (pm.getMaxTSDR() / pm.getBaudRate()) * 1000;
            fMinTSDR   = (pm.getMinTSDR() / pm.getBaudRate()) * 1000;
            fQuietTime = (pm.getQuietTime() / pm.getBaudRate()) * 1000;
            fSetupTime = (pm.getSetupTime() / pm.getBaudRate()) * 1000;
            fSlotTime  = (pm.getSlotTime() / pm.getBaudRate()) * 1000;
            fTTR       = (pm.getTTR() / pm.getBaudRate()) * 1000;


            sLabelBaudRate  = String.format( "%.0f", pm.getBaudRate() ) + " bps";
            sLabelGAP       = String.format( "%.0f", pm.getGAP() );
            sLabelHSA       = String.format( "%.0f", pm.getHSA() );
            sLabelMaxTSDR   = String.format( "%.0f", pm.getMaxTSDR()) + " Tbit (";
            sLabelMinTSDR   = String.format( "%.0f", pm.getMinTSDR()) + " Tbit (";
            sLabelQuietTime = String.format( "%.0f", pm.getQuietTime())  + " Tbit (";
            sLabelRetries   = String.format( "%.0f", pm.getRetries() );
            sLabelSetupTime = String.format( "%.0f", pm.getSetupTime( )) + " Tbit (";
            sLabelSlotTime  = String.format( "%.0f", pm.getSlotTime() )  + " Tbit (";
            sLabelTTR       = String.format( "%.0f", pm.getTTR() )       + " Tbit (";
            sLabelWatchDog  = String.format( "%.0f", pm.getWatchdog() )  + " ms";

            sLabelMaxTSDR   += String.format( "%.3f", fMaxTSDR ) + " ms)";
            sLabelMinTSDR   +=  String.format( "%.3f", fMinTSDR ) + " ms)";
            sLabelQuietTime +=  String.format( "%.3f", fQuietTime ) + " ms)";
            sLabelSetupTime +=  String.format( "%.3f", fSetupTime ) + " ms)";
            sLabelSlotTime  +=  String.format( "%.3f", fSlotTime ) + " ms)";
            sLabelTTR       +=  String.format( "%.3f", fTTR ) + " ms)";
        }
        else
        {
            float fMaxTSDR, fMinTSDR, fQuietTime, fSetupTime, fSlotTime, fTTR;

            fMaxTSDR   = (pbf.pbcfg.busParameter.MaxTSDR / pbf.pbcfg.busParameter.baudRate)*1000;
            fMinTSDR   = (pbf.pbcfg.busParameter.MinTSDR / pbf.pbcfg.busParameter.baudRate)*1000;
            fQuietTime = (pbf.pbcfg.busParameter.TQui / pbf.pbcfg.busParameter.baudRate)*1000;
            fSetupTime = (pbf.pbcfg.busParameter.TSet / pbf.pbcfg.busParameter.baudRate)*1000;
            fSlotTime  = (pbf.pbcfg.busParameter.SlotTime / pbf.pbcfg.busParameter.baudRate)*1000;
            fTTR       = (pbf.pbcfg.busParameter.TTR / pbf.pbcfg.busParameter.baudRate)*1000;

            sLabelBaudRate  = String.format( "%.0f", pbf.pbcfg.busParameter.baudRate ) + " bps";
            sLabelGAP       = String.format( "%.0f", pbf.pbcfg.busParameter.GAP );
            sLabelHSA       = String.format( "%.0f", pbf.pbcfg.busParameter.HSA );
            sLabelMaxTSDR   = String.format( "%.0f", pbf.pbcfg.busParameter.MaxTSDR ) + " Tbit (";
            sLabelMinTSDR   = String.format( "%.0f", pbf.pbcfg.busParameter.MinTSDR ) + " Tbit (";
            sLabelQuietTime = String.format( "%.0f", pbf.pbcfg.busParameter.TQui )  + " Tbit (";
            sLabelRetries   = String.format( "%.0f", pbf.pbcfg.busParameter.Max_Retry );
            sLabelSetupTime = String.format( "%.0f", pbf.pbcfg.busParameter.TSet ) + " Tbit (";
            sLabelSlotTime  = String.format( "%.0f", pbf.pbcfg.busParameter.SlotTime )  + " Tbit (";
            sLabelTTR       = String.format( "%.0f", pbf.pbcfg.busParameter.TTR )       + " Tbit (";
            sLabelWatchDog  = String.format( "%.0f", pbf.pbcfg.busParameter.Watchdog )  + " ms";

            sLabelMaxTSDR   += String.format( "%.3f", fMaxTSDR ) + " ms)";
            sLabelMinTSDR   +=  String.format( "%.3f", fMinTSDR ) + " ms)";
            sLabelQuietTime +=  String.format( "%.3f", fQuietTime ) + " ms)";
            sLabelSetupTime +=  String.format( "%.3f", fSetupTime ) + " ms)";
            sLabelSlotTime  +=  String.format( "%.3f", fSlotTime ) + " ms)";
            sLabelTTR       +=  String.format( "%.3f", fTTR ) + " ms)";
            
        }
        
        jLabelBaudRate.setText( sLabelBaudRate );
        jLabelGAP.setText( sLabelGAP );
        jLabelHSA.setText( sLabelHSA );
        jLabelMaxTSDR.setText( sLabelMaxTSDR );
        jLabelMinTSDR.setText( sLabelMinTSDR );
        jLabelQuietTime.setText( sLabelQuietTime );
        jLabelRetries.setText( sLabelRetries );
        jLabelSetupTime.setText( sLabelSetupTime );
        jLabelSlotTime.setText( sLabelSlotTime );
        jLabelTTR.setText( sLabelTTR );
        jLabelWatchDogTime.setText( sLabelWatchDog );
        jLabelMasterAddr.setText( String.valueOf( pbf.pbcfg.getMasterAddress() ) );
        
        if( pbf != null )
            jLabelConfigurationFileName.setText( pbf.getShortFileName() );
    }

class CellRendererToolTip extends DefaultTableCellRenderer {  
    // Mantém todos os tooltips com suas linhas.   
    private Map<Long, String> tooltip = new HashMap<Long, String>();  
      
    // Mantém a linha atual que este objeto está renderizando.   
    private int row;  
      
    // Busca qual é a linha atual.   
    public Component getTableCellRendererComponent(JTable table, Object value,  
            boolean isSelected, boolean hasFocus, int row, int column) {  
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);  
          
        this.row = row;  
          
        return c;   
    }  
      
    // Retorna o tooltip baseado no map.   
    public String getToolTipText() {  
        return tooltip.get(new Long(row));  
    }  
      
    // Adiciona um tooltip pela linha.   
    public void addToolTip(int row, String text) {  
        tooltip.put(new Long(row), text);  
    }  
}

    public class TableColumnHider {
        private JTable table;
        private TableColumnModel tcm;
        private Map hiddenColumns;

        public TableColumnHider(JTable table) {
            this.table = table;
            tcm = table.getColumnModel();
            hiddenColumns = new HashMap();
        }
        
        public void setVisible( String columnName, boolean bEnable ) {
            if( bEnable == false ) {
                if( !hiddenColumns.containsKey(columnName) )
                    hide( columnName );
            }
            else {
                if( hiddenColumns.containsKey(columnName) )
                    show( columnName );
            }
        }
        

        public void hide(String columnName) {
            int index = tcm.getColumnIndex(columnName);
            TableColumn column = tcm.getColumn(index);
            hiddenColumns.put(columnName, column);
            hiddenColumns.put(":" + columnName, new Integer(index));
            tcm.removeColumn(column);
        }

        public void show(String columnName) {
            Object o = hiddenColumns.remove(columnName);
            if (o == null) {
                return;
            }
            tcm.addColumn((TableColumn) o);
            o = hiddenColumns.remove(":" + columnName);
            if (o == null) {
                return;
            }
            int column = ((Integer) o).intValue();
            int lastColumn = tcm.getColumnCount() - 1;
            if (column < lastColumn) {
                tcm.moveColumn(lastColumn, column);
            }
        }

        boolean IsColumnVisible(String columnName) {
            return !hiddenColumns.containsKey( columnName );
        }
    }
    
    class CustomRenderer extends DefaultTableCellRenderer 
    {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            TableColumnModel tcm = table.getColumnModel( );
            Component c = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );

            if( ( ((Vector)RowFrameList.get(row)).get(4) ).toString() == "Response"  )
                    c.setForeground( Color.BLUE );
                else
                    c.setForeground( Color.BLACK );
            
            String sService = ( ((Vector)RowFrameList.get(row)).get(2) ).toString();
            
            if( (sService == "Set Parameter Request") || (sService == "Get Diagnostics Request") || (sService == "Check Configuration Request") || (sService == "Slave Diagnostics Response")  )
                    c.setBackground( Color.YELLOW );
                else
                    c.setBackground( Color.WHITE );
            try {
                ProfibusFrame pbt = new ProfibusFrame( (((Vector)RowFrameList.get(row)).get(7) ).toString() );
                
                if( (pbt.getFCB() == true) && (pbt.getFCV() == false) )
                {
                    //Sync Frame
                    c.setBackground( Color.YELLOW );
                }
                else
                {
                    c.setBackground( Color.WHITE );
                }
                
                
            } catch (ProfibusFrameException ex) {
                Logger.getLogger(FDLTelegramsTable.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            return c;
        }
    }
    
    class PopClickListener extends MouseAdapter {
        public void mousePressed(MouseEvent e){
            if (e.isPopupTrigger())
                doPop(e);
        }

        public void mouseReleased(MouseEvent e){
            if (e.isPopupTrigger())
                doPop(e);
        }

        private void doPop(MouseEvent e){

            int row = jTable1.rowAtPoint( e.getPoint() );
            ProfibusFrame pbf = (ProfibusFrame) FrameList.get( row );
            
            PopUpFrameTable menu = new PopUpFrameTable( pbf );
            menu.show(e.getComponent(), e.getX(), e.getY());
            System.out.println("Row Value: " + String.valueOf(row));
        }
    }
    
    private void jCheckBoxReadableSignalActionPerformed(java.awt.event.ActionEvent evt) 
    {
        if( liveListTree.getSelectedDevice() != -1 ) 
        {
            Sim.setReadable( Integer.valueOf( jTextFieldSlaveAddress.getText() ), jCheckBoxReadableSignal.isSelected() );    
        }
        
    }  
}


