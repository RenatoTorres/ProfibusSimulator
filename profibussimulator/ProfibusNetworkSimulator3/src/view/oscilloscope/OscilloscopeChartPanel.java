/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package view.oscilloscope;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author eduardo_mossin
 */
public class OscilloscopeChartPanel extends JPanel {

    ChartPanel chartPanel = null;
    XYSeries series = new XYSeries("DP Signal");
    XYSeriesCollection data = new XYSeriesCollection(series);
    float i = 0.0f;
    JFreeChart chart = null;
    ArrayList<Byte> filteredDevices = new ArrayList<Byte>();
    ArrayList<ArrayList> signalArrayList = new ArrayList<ArrayList>();
    byte graphicPloted = 0;
    int bufferSize;
    boolean plotting = true;

    public OscilloscopeChartPanel(final String title, Dimension dim, String xTitle, String yTitle, int bufferSize) {
        super();
        this.bufferSize = bufferSize;
        this.setBackground(Color.WHITE);
        setVisible(true);
        chart = ChartFactory.createXYLineChart(
                title,
                xTitle,
                yTitle,
                data,
                PlotOrientation.VERTICAL,
                false,
                true,
                false);
        chart.getPlot().setBackgroundPaint(Color.BLACK);
        ((XYPlot) chart.getPlot()).getRenderer().setSeriesPaint(0, Color.YELLOW);
        chartPanel = new ChartPanel(chart);
        this.setSize(dim);
        chartPanel.setPreferredSize(dim);
        chartPanel.setVisible(true);
        this.add(chartPanel);
    }

    public void setFilteredDevices(ArrayList filteredDevices) {
        this.filteredDevices = filteredDevices;
    }

    public void setFilteredDevices(Byte filteredDevice) {
        filteredDevices = new ArrayList<Byte>();
        filteredDevices.add(filteredDevice);
    }

    public void setPlotting(boolean plotting) {
        this.plotting = plotting;
    }

    public void setProfidoctorPackage(ArrayList bitsSampled) {
        if (plotting) {
            Iterator bitsSampledIt = bitsSampled.iterator();
            while (bitsSampledIt.hasNext()) {
                ArrayList oneBitSamples = (ArrayList) bitsSampledIt.next();
                setDataSerie(oneBitSamples);
            }

        }
    }

    private void setDataSerie(ArrayList signalArray) {
        signalArrayList.add(signalArray);
        if (signalArrayList.size() == bufferSize) {
            Iterator signalArrayListIt = signalArrayList.iterator();
            int oneBitSize = signalArray.size();
            int shift = oneBitSize * bufferSize;
            graphicPloted++;
            if (graphicPloted > 2) {
                series.delete(0, shift);
            }

            while (signalArrayListIt.hasNext()) {
                Iterator signalArrayit = ((ArrayList) signalArrayListIt.next()).iterator();
                while (signalArrayit.hasNext()) {
                    series.add(i, (Float) signalArrayit.next());
                    i++;
                }
            }
            signalArrayList.clear();
        }
        chart.fireChartChanged();
    }
}
