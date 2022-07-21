package com.jug.gurobi;

import com.jug.MoMA;
import com.jug.fijiplugins.MotherMachineAnalyserPlugin;
import fiji.util.gui.GenericDialogPlus;
import gurobi.GRBEnv;
import gurobi.GRBException;
import ij.IJ;
import ij.gui.GenericDialog;
import ij.gui.MultiLineLabel;
import ij.plugin.BrowserLauncher;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.*;

/**
 * Author: HongKee Moon (moon@mpi-cbg.de), Robert Haase(rhaase@mpi-cbg.de) Scientific Computing Facility
 * Organization: MPI-CBG Dresden
 * Date: October 2016
 */
public class GurobiInstaller {
    public static boolean checkInstallation()
    {
        if (!MoMA.runningAsFijiPlugin) {
            return true;
        }

        final Class<?> clazz = MotherMachineAnalyserPlugin.class;
        final String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        final String pluginsDir = url.substring(0, url.length() - clazz.getName().length() - 6);

        boolean actuallyCopiedGurobiFiles = false;

        try
        {
            actuallyCopiedGurobiFiles = NativeLibrary.copyLibraries( new URL(pluginsDir) );
            System.out.println("copyLibraries succeeded? " + actuallyCopiedGurobiFiles);
        }
        catch ( URISyntaxException e )
        {
            IJ.log( "Native library allocation for Fiji failed." );
            e.printStackTrace();
        }
        catch ( MalformedURLException e )
        {
            IJ.log( "The given class URL is wrong." );
            e.printStackTrace();
        }

        final String gurobiLicFilePath = System.getProperty( "user.home" ) + File.separator + "gurobi.lic";
        final File gurobiLicFile = new File( gurobiLicFilePath );

        if(!gurobiLicFile.exists())
        {
            GenericDialogPlus gurobiDialog = new GenericDialogPlus("Getting Gurobi License File");
            gurobiDialog.addMessage( "There was no gurobi license file found in your users home directory." );
            gurobiDialog.addMessage( "Please acquire a license at " );
            addHyperLink(gurobiDialog, "http://www.gurobi.com/downloads/licenses/license-center", "http://www.gurobi.com/downloads/licenses/license-center" );
            gurobiDialog.addMessage( "Afterwards, please copy and paste the string starting with \"grbgetkey\":" );
            gurobiDialog.addStringField( "", "", 45 );
            gurobiDialog.showDialog();

            if(gurobiDialog.wasCanceled())
                return false;

            final String grbkeygetString = gurobiDialog.getNextString();

            Exec.runGrbgetkey( grbkeygetString.split( " " ) );
        }

        if (actuallyCopiedGurobiFiles) {
            boolean restartNeccessary = false;

            try {
                new GRBEnv( "MoMA_gurobi.log" );
            } catch ( final GRBException | UnsatisfiedLinkError e ) {
                restartNeccessary = true;
            }

            if (restartNeccessary) {
                JOptionPane.showMessageDialog(
                        null,
                        "Installation of a module (Gurobi Optimizer) is requesting for a restart. Please restart ImageJ/FIJI.",
                        "Gurobi Installation",
                        JOptionPane.ERROR_MESSAGE );
            }
        }

        return gurobiLicFile.exists();
    }



    private static void addHyperLink(final GenericDialog gd, final String msg, final String url )
    {
        gd.addMessage( msg + "\n", new Font( Font.SANS_SERIF, Font.ITALIC + Font.BOLD, 12 ) );
        MultiLineLabel text =  (MultiLineLabel) gd.getMessage();
        addHyperLinkListener( text, url );
    }

    private static void addHyperLinkListener(final MultiLineLabel text, final String myURL )
    {
        if ( text != null && myURL != null )
        {
            text.addMouseListener( new MouseAdapter()
            {
                @Override
                public void mouseClicked( final MouseEvent e )
                {
                    try
                    {
                        BrowserLauncher.openURL( myURL );
                    }
                    catch ( Exception ex )
                    {
                        IJ.log( "" + ex);
                    }
                }

                @Override
                public void mouseEntered( final MouseEvent e )
                {
                    text.setForeground( Color.BLUE );
                    text.setCursor( new java.awt.Cursor( java.awt.Cursor.HAND_CURSOR ) );
                }

                @Override
                public void mouseExited( final MouseEvent e )
                {
                    text.setForeground( Color.BLACK );
                    text.setCursor( new java.awt.Cursor( java.awt.Cursor.DEFAULT_CURSOR ) );
                }
            });
        }
    }

}
