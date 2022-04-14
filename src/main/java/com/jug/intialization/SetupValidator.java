package com.jug.intialization;

import com.jug.MoMA;
import gurobi.GRBEnv;
import gurobi.GRBException;

import javax.swing.*;

public class SetupValidator {
    public static boolean checkGurobiInstallation(boolean runningHeadless, boolean running_as_Fiji_plugin) {
        final String jlp = System.getProperty( "java.library.path" );
        try {
            new GRBEnv( "MoMA_gurobi.log" );
        } catch ( final GRBException e ) {
            final String msgs = "Initial Gurobi test threw exception... check your Gruobi setup!\n\nJava library path: " + jlp;
            if ( runningHeadless ) {
                System.out.println( msgs );
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        msgs,
                        "Gurobi Error?",
                        JOptionPane.ERROR_MESSAGE );
            }
            e.printStackTrace();
            if (!running_as_Fiji_plugin) {
                System.exit( 98 );
            } else {
                return true;
            }
        } catch ( final UnsatisfiedLinkError ulr ) {
            final String msgs = "Could not initialize Gurobi.\n" + "You might not have installed Gurobi properly or you miss a valid license.\n" + "Please visit 'www.gurobi.com' for further information.\n\n" + ulr.getMessage() + "\nJava library path: " + jlp;
            if ( runningHeadless ) {
                System.out.println( msgs );
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        msgs,
                        "Gurobi Error?",
                        JOptionPane.ERROR_MESSAGE );
                ulr.printStackTrace();
            }
            System.out.println( "\n>>>>> Java library path: " + jlp + "\n" );
            if (!running_as_Fiji_plugin) {
                System.exit( 99 );
            } else {
                return true;
            }
        }
        return false;
    }
}
