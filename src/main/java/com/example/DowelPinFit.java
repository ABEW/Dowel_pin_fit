package com.example;

import java.util.*;
import java.io.*;
import javax.swing.*;

import java.awt.event.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

/**
 * Created by Abew on 2016-11-15. Updated on 2025-03-11
 */
public class DowelPinFit extends JFrame implements ItemListener, ActionListener {

    JPanel pnl = new JPanel();
    
    JTextField txt1 = new JTextField("Internal Dimension", 10);
    JTextField txt2 = new JTextField("External Dimension", 10);

    JTextArea textArea = new JTextArea(2, 20);
    JComboBox<String> box1 = new JComboBox<>();
    JComboBox<String> box2 = new JComboBox<>();

    float holeMmc = 0.0f;
    float holeLmc = 0.0f;
    float pinMmc = 0.0f;
    float pinLmc = 0.0f;

    String holeTol = "E12";
    String pinTol = "E11";

    float diameter = 3;

    int rowIndex = 1;

    String[] fitType = { "CLEARANCE", "INTERFERENCE", "TRANSITION" };

    int fitIndex = 0;

    Map<String, List<String[]>> csvData;

    public DowelPinFit() {

        super("DOWEL PIN FIT TEST");
        setSize(300, 180);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.csvData = loadReferenceArrays();

        List<String[]> inner = csvData.get("internal");
        List<String[]> outer = csvData.get("external");

        String[] idEntry = inner.get(0);
        String[] odEntry = outer.get(0);

        String[] Tol_internal = Arrays.copyOfRange(idEntry, 2, idEntry.length - 1);
        String[] Tol_external = Arrays.copyOfRange(odEntry, 2, odEntry.length - 1);

        final int[] size = { 0, 3, 6, 10, 14, 18 };

        int index = 0;

        rowIndex = 1;

        while (!(diameter > size[index] && diameter <= size[index + 1])) {
            rowIndex += 2;
            index++;
        }

        DefaultComboBoxModel<String> model1 = new DefaultComboBoxModel<>(Tol_internal);
        box1.setModel(model1);
        DefaultComboBoxModel<String> model2 = new DefaultComboBoxModel<>(Tol_external);
        box2.setModel(model2);

        JButton calculate = new JButton("CALCULATE");

        box1.addItemListener(this);
        box2.addItemListener(this);
        calculate.addActionListener(this);

        JLabel internalLabel = new JLabel("Hole");
        JLabel externalLabel = new JLabel("Pin");

        internalLabel.setBounds(10,0,40,30);
        txt1.setBounds(50,0,135,30);
        box1.setBounds(200,0,100,30);

        externalLabel.setBounds(10,35,40,30);
        txt2.setBounds(50,35,135,30);
        box2.setBounds(200,35,100,30);

        calculate.setBounds(100,70,100,30);
        textArea.setBounds(50,105,200,30);

        add(pnl);
        pnl.setLayout(null);
        pnl.add(internalLabel);
        pnl.add(txt1);
        pnl.add(box1);
        pnl.add(externalLabel);
        pnl.add(txt2);
        pnl.add(box2);
        pnl.add(calculate);
        pnl.add(textArea);

        setVisible(true);

    }

    public Map<String, List<String[]>> loadReferenceArrays() {

        Map<String, List<String[]>> tableData = new HashMap<>();

        try (
                // Load Internal.csv using InputStreamReader
                InputStream internalInputStream = getClass().getClassLoader().getResourceAsStream("Internal.csv");
                // Load External.csv using InputStreamReader
                InputStream externalInputStream = getClass().getClassLoader().getResourceAsStream("External.csv");

                // Check if internal file is available and read
                CSVReader internal = (internalInputStream != null)
                        ? new CSVReader(new InputStreamReader(internalInputStream))
                        : null;
                CSVReader external = (externalInputStream != null)
                        ? new CSVReader(new InputStreamReader(externalInputStream))
                        : null;) {
            if (internal == null) {
                System.out.println("Internal reference not found");
            }

            if (external == null) {
                System.out.println("External reference not found");
            }

            List<String[]> inner = internal.readAll();
            List<String[]> outer = external.readAll();

            tableData.put("internal", inner);
            tableData.put("external", outer);

        } catch (CsvException e) {
            System.out.println("Unable to read the csv files " + e.getMessage());
        } catch (IOException | NullPointerException e) {
            System.out.println("An error has occured " + e.getMessage());
        }

        return tableData;

    }

    public void actionPerformed(ActionEvent event) {
        String val1 = txt1.getText();
        String val2 = txt2.getText();

        try {
            float Dia1 =  Float.parseFloat(val1); //Integer.parseInt(Val1);
            float Dia2 = Float.parseFloat(val2); //Integer.parseInt(Val2);

            if (Dia1 == Dia2) {

                diameter = Dia1;

                final int[] size = { 0, 3, 6, 10, 14, 18 };

                int index = 0;

                rowIndex = 1;

                while (!(diameter > size[index] && diameter <= size[index + 1])) {
                    rowIndex += 2;
                    index++;
                }

                List<String[]> inner = this.csvData.get("internal");
                List<String[]> outer = this.csvData.get("external");

                String[] idEntry = inner.get(0);
                String[] odEntry = outer.get(0);

                holeMmc = Float.parseFloat(inner.get(rowIndex)[Arrays.asList(idEntry).indexOf(holeTol)]);
                holeLmc = Float.parseFloat(inner.get(rowIndex + 1)[Arrays.asList(idEntry).indexOf(holeTol)]);

                pinMmc = Float.parseFloat(outer.get(rowIndex)[Arrays.asList(odEntry).indexOf(pinTol)]);
                pinLmc = Float.parseFloat(outer.get(rowIndex + 1)[Arrays.asList(odEntry).indexOf(pinTol)]);

                float clearanceLow = holeLmc - pinMmc;
                float clearanceHigh = holeMmc - pinLmc;

                if (clearanceHigh <= 0) {
                    fitIndex = 1;
                } else if (clearanceLow <= 0) {
                    fitIndex = 2;
                }

                textArea.setText(fitType[fitIndex] + " FIT: [" + String.format("%.3f", clearanceLow) + ","
                        + String.format("%.3f", clearanceHigh) + "]\n");

            }

            else {
                textArea.setText("DIAMETER MISMATCH");
            }

        } catch (Exception ED) {
            textArea.setText("INVALID DIAMETER");
        }

    }

    public void itemStateChanged(ItemEvent event) {
        if ((event.getItemSelectable() == box1) && (event.getStateChange() == ItemEvent.SELECTED)) {
            holeTol = event.getItem().toString();
        }

        if ((event.getItemSelectable() == box2) && (event.getStateChange() == ItemEvent.SELECTED)) {
            pinTol = event.getItem().toString();
        }
    }

    public static void main(String[] ags) {
        new DowelPinFit();
    }
}
