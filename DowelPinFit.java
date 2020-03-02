package com.example.helloworld;

import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import com.opencsv.CSVReader;


/**
 * Created by Abew on 2016-11-15.
 */
public class DowelPinFit extends JFrame implements ItemListener,ActionListener{

    JPanel pnl= new JPanel();
    JTextField txt1= new JTextField("Internal Dimension",10);
    JTextField txt2= new JTextField("External Dimension",10);

    JTextArea FIT= new JTextArea(2,20);
    JComboBox Box1= new JComboBox ();
    JComboBox Box2= new JComboBox ();

    float Hole_MMC= 0.0f;
    float Hole_LMC= 0.0f;
    float Pin_MMC= 0.0f;
    float Pin_LMC= 0.0f;

    String Hole_Tol= "E12";
    String Pin_Tol= "E11";

    int Diameter= 3;

    int row_index = 1;

    String[] Fit_Type= {"CLEARANCE","INTERFERENCE","TRANSITION"};

    int fit_index=0;


    public DowelPinFit(){

        super("DOWEL PIN FIT TEST");
        setSize(290,170);
        setResizable(false);
        setDefaultCloseOperation( EXIT_ON_CLOSE);

        try{
            File Document1 = new File("/Users/Abew/introcs/hello/Internal.csv");
            File Document2 = new File("/Users/Abew/introcs/hello/External.csv");

            System.out.println(getClass().getProtectionDomain().getCodeSource().getLocation());
            System.out.println(new File(".").getAbsolutePath());

           System.out.println(getClass().getResource("Internal.csv").getPath());

            CSVReader Internal = new CSVReader(new FileReader(getClass().getResource("Internal.csv").getPath()));
            CSVReader External = new CSVReader(new FileReader(getClass().getResource("External.csv").getPath()));


            List<String[]> Inner = Internal.readAll();
            List<String[]> Outer = External.readAll();

            String[] ID_entry = Inner.get(0);
            String[] OD_entry = Outer.get(0);

            String[] Tol_internal = Arrays.copyOfRange(ID_entry, 2, ID_entry.length - 1);
            String[] Tol_external = Arrays.copyOfRange(OD_entry, 2, OD_entry.length - 1);

            final int[] size= {0,3,6,10,14,18};

            int index=0;

            row_index = 1;

            while (!(Diameter>size[index] && Diameter<=size[index+1]))
            {
                row_index+=2;
                index++;
            }


            DefaultComboBoxModel model1 = new DefaultComboBoxModel( Tol_internal );
            Box1.setModel(model1);
            DefaultComboBoxModel model2 = new DefaultComboBoxModel( Tol_external );
            Box2.setModel(model2);



            JButton calculate= new JButton("CALCULATE");

            Box1.addItemListener(this);
            Box2.addItemListener(this);
            calculate.addActionListener(this);

            JLabel Internal_lbl= new JLabel("Internal");
            JLabel External_lbl= new JLabel("External");


            add(pnl);
            pnl.add(Internal_lbl);
            pnl.add(txt1);
            pnl.add(Box1);
            pnl.add(External_lbl);
            pnl.add(txt2);
            pnl.add(Box2);
            pnl.add(calculate);
            pnl.add(FIT);

            setVisible(true);



        }

        catch(IOException e)
        {
            System.out.println("An error has occured");
        }


    }

    public void actionPerformed (ActionEvent event)
    {
        String Val1 = txt1.getText();
        String Val2= txt2.getText();

        try{
            int Dia1 = Integer.parseInt(Val1);
            int Dia2 = Integer.parseInt(Val2);

            if (Dia1==Dia2) {

                Diameter=Dia1;

                try {
                    File Document1 = new File("/Users/Abew/introcs/hello/Internal.csv");
                    File Document2 = new File("/Users/Abew/introcs/hello/External.csv");


                    final int[] size = {0, 3, 6, 10, 14, 18};

                    int index = 0;

                    row_index = 1;

                    while (!(Diameter > size[index] && Diameter <= size[index + 1])) {
                        row_index += 2;
                        index++;
                    }

                    CSVReader Internal = new CSVReader(new FileReader(Document1));
                    CSVReader External = new CSVReader(new FileReader(Document2));

                    List<String[]> Inner = Internal.readAll();
                    List<String[]> Outer = External.readAll();

                    String[] ID_entry = Inner.get(0);
                    String[] OD_entry = Outer.get(0);
                    

                    Hole_MMC = Float.parseFloat(Inner.get(row_index)[Arrays.asList(ID_entry).indexOf(Hole_Tol)]);
                    Hole_LMC = Float.parseFloat(Inner.get(row_index+1)[Arrays.asList(ID_entry).indexOf(Hole_Tol)]);

                    Pin_MMC = Float.parseFloat(Outer.get(row_index)[Arrays.asList(OD_entry).indexOf(Pin_Tol)]);
                    Pin_LMC = Float.parseFloat(Outer.get(row_index+1)[Arrays.asList(OD_entry).indexOf(Pin_Tol)]);

                    float Clearance_low = Hole_LMC-Pin_MMC;
                    float Clearance_high= Hole_MMC-Pin_LMC;

                    if (Clearance_high<=0)
                    {
                        fit_index=1;
                    }
                    else if (Clearance_low<=0)
                    {
                        fit_index=2;
                    }

                    FIT.setText(Fit_Type[fit_index]+" FIT: ["+String.format("%.3f",Clearance_low)+
                            ","+String.format("%.3f",Clearance_high)+"]\n");


                }

                    catch(IOException e)
                    {
                        System.out.println("An error has occured");
                    }

            }

            else
            {
                FIT.setText("DIAMETER MISMATCH");
            }


        }
        catch(Exception ED)
        {
            FIT.setText("INVALID DIAMETER");
        }

    }

    public void itemStateChanged(ItemEvent event)
    {
        if((event.getItemSelectable()== Box1) && (event.getStateChange()== ItemEvent.SELECTED))
        {
            Hole_Tol=event.getItem().toString();
        }

        if((event.getItemSelectable()== Box2) && (event.getStateChange()== ItemEvent.SELECTED))
        {
            Pin_Tol=event.getItem().toString();
        }
    }

    public static void main ( String[] ags)
    {
        DowelPinFit gui= new DowelPinFit();
    }
}
