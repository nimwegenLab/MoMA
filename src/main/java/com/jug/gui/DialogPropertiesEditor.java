package com.jug.gui;

import com.jug.config.ConfigurationManager;
import com.jug.logging.LoggingHelper;
import com.jug.util.PseudoDic;
import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheet;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * @author jug
 */
public class DialogPropertiesEditor extends JDialog implements ActionListener {

	private static final long serialVersionUID = -5529104109524798394L;
	private final PropEditedListener propEditListener = new PropEditedListener();
    private static Component parent = null;
    private final PropFactory propFactory;
    private PropertySheetPanel sheet;
    private ConfigurationManager configurationManager;
    private PseudoDic dic;

    public class PropEditedListener implements PropertyChangeListener {

		@Override
		public void propertyChange( final PropertyChangeEvent evt ) {
            Property sourceProperty = (Property) evt.getSource();
			final String sourceName = sourceProperty.getName();

			try {
                switch (sourceName) {
                    case "GUROBI_TIME_LIMIT":
                        configurationManager.GUROBI_TIME_LIMIT =
                                Double.parseDouble(evt.getNewValue().toString());
                        props.setProperty(
                                "GUROBI_TIME_LIMIT",
                                "" + configurationManager.GUROBI_TIME_LIMIT);
                        break;
                    case "SEGMENTATION_MODEL_PATH": {
                        String newPath = sourceProperty.getValue().toString();
                        if(newPath!=configurationManager.SEGMENTATION_MODEL_PATH) {
                            File f = new File(newPath);
                            if(!f.exists() || f.isDirectory()) {
                                JOptionPane.showMessageDialog(
                                        parent,
                                        "Specified file does not exist. Falling back to previous path.",
                                        "Model file not found.",
                                        JOptionPane.ERROR_MESSAGE);
                                sourceProperty.setValue(configurationManager.SEGMENTATION_MODEL_PATH);
                                break;
                            }

                            showPropertyEditedNeedsRerunDialog("Continue?",
                                    "Changing this value will rerun segmentation and optimization.\nYou will loose all manual edits performed so far!",
                                    () -> newPath != configurationManager.SEGMENTATION_MODEL_PATH,
                                    () -> sourceProperty.setValue(configurationManager.SEGMENTATION_MODEL_PATH),
                                    () -> {
                                        configurationManager.SEGMENTATION_MODEL_PATH = newPath;
                                        dic.getFilePaths().setModelFilePath(configurationManager.SEGMENTATION_MODEL_PATH);
                                        props.setProperty(
                                                "SEGMENTATION_MODEL_PATH",
                                                "" + configurationManager.SEGMENTATION_MODEL_PATH);
                                        final Thread t = new Thread(() -> {
                                            ((MoMAGui) parent).restartFromGLSegmentation();
                                            ((MoMAGui) parent).restartTracking();
                                        });
                                        t.start();
                            });
                        }
                        break;
                    }
                    case "ASSIGNMENT_COST_CUTOFF": {
                        int newValue = Integer.parseInt(evt.getNewValue().toString());
                        showPropertyEditedNeedsRerunDialog("Continue?",
                                "Changing this value will restart the optimization.\nYou will loose all manual edits performed so far!",
                                () -> newValue != configurationManager.ASSIGNMENT_COST_CUTOFF,
                                () -> sourceProperty.setValue(configurationManager.ASSIGNMENT_COST_CUTOFF),
                                () -> {
                                    configurationManager.ASSIGNMENT_COST_CUTOFF = newValue;
                                    props.setProperty(
                                            "ASSIGNMENT_COST_CUTOFF",
                                            "" + configurationManager.ASSIGNMENT_COST_CUTOFF);
                                    ((MoMAGui) parent).restartTrackingAsync();
                                });
                        break;
                    }
                    case "LYSIS_ASSIGNMENT_COST": {
                        int newValue = Integer.parseInt(evt.getNewValue().toString());
                        showPropertyEditedNeedsRerunDialog("Continue?",
                                "Changing this value will restart the optimization.\nYou will loose all manual edits performed so far!",
                                () -> newValue != configurationManager.LYSIS_ASSIGNMENT_COST,
                                () -> sourceProperty.setValue(configurationManager.LYSIS_ASSIGNMENT_COST),
                                () -> {
                                    configurationManager.LYSIS_ASSIGNMENT_COST = newValue;
                                    props.setProperty(
                                            "LYSIS_ASSIGNMENT_COST",
                                            "" + configurationManager.LYSIS_ASSIGNMENT_COST);
                                    ((MoMAGui) parent).restartTrackingAsync();
                                });
                        break;
                    }
                    case "SIZE_MINIMUM_FOR_ROOT_COMPONENTS": {
                        int newValue = Integer.parseInt(evt.getNewValue().toString());
                        showPropertyEditedNeedsRerunDialog("Continue?",
                                "Changing this value will restart the optimization.\nYou will loose all manual edits performed so far!",
                                () -> newValue != configurationManager.SIZE_MINIMUM_FOR_ROOT_COMPONENTS,
                                () -> sourceProperty.setValue(configurationManager.SIZE_MINIMUM_FOR_ROOT_COMPONENTS),
                                () -> {
                                    configurationManager.SIZE_MINIMUM_FOR_ROOT_COMPONENTS = newValue;
                                    props.setProperty(
                                            "SIZE_MINIMUM_FOR_ROOT_COMPONENTS",
                                            "" + configurationManager.SIZE_MINIMUM_FOR_ROOT_COMPONENTS);
                                    final Thread t = new Thread(() -> {
                                        ((MoMAGui) parent).restartFromGLSegmentation();
                                        ((MoMAGui) parent).restartTracking();
                                    });
                                    t.start();
                                });
                        break;
                    }
                    case "SIZE_MINIMUM_FOR_LEAF_COMPONENTS": {
                        int newValue = Integer.parseInt(evt.getNewValue().toString());
                        showPropertyEditedNeedsRerunDialog("Continue?",
                                "Changing this value will restart the optimization.\nYou will loose all manual edits performed so far!",
                                () -> newValue != configurationManager.SIZE_MINIMUM_FOR_LEAF_COMPONENTS,
                                () -> sourceProperty.setValue(configurationManager.SIZE_MINIMUM_FOR_LEAF_COMPONENTS),
                                () -> {
                                    configurationManager.SIZE_MINIMUM_FOR_LEAF_COMPONENTS = newValue;
                                    props.setProperty(
                                            "SIZE_MINIMUM_FOR_LEAF_COMPONENTS",
                                            "" + configurationManager.SIZE_MINIMUM_FOR_LEAF_COMPONENTS);
                                    final Thread t = new Thread(() -> {
                                        ((MoMAGui) parent).restartFromGLSegmentation();
                                        ((MoMAGui) parent).restartTracking();
                                    });
                                    t.start();
                                });
                        break;
                    }
                    case "GL_OFFSET_TOP": {
                        int newValue = Integer.parseInt(evt.getNewValue().toString());
                        showPropertyEditedNeedsRerunDialog("Continue?",
                                "Changing this value will restart the optimization.\nYou will loose all manual edits performed so far!",
                                () -> newValue != configurationManager.GL_OFFSET_TOP,
                                () -> sourceProperty.setValue(configurationManager.GL_OFFSET_TOP),
                                () -> {
                                    configurationManager.GL_OFFSET_TOP = newValue;
                                    props.setProperty(
                                            "GL_OFFSET_TOP",
                                            "" + configurationManager.GL_OFFSET_TOP);
                                    ((MoMAGui) parent).restartTrackingAsync();
                                });
                        break;
                    }
                    case "CELL_DETECTION_ROI_OFFSET_TOP": {
                        int newValue = Integer.parseInt(evt.getNewValue().toString());
                        showPropertyEditedNeedsRerunDialog("Continue?",
                                "Changing this value will restart the optimization.\nYou will loose all manual edits performed so far!",
                                () -> newValue != configurationManager.CELL_DETECTION_ROI_OFFSET_TOP,
                                () -> sourceProperty.setValue(configurationManager.CELL_DETECTION_ROI_OFFSET_TOP),
                                () -> {
                                    configurationManager.CELL_DETECTION_ROI_OFFSET_TOP = newValue;
                                    props.setProperty(
                                            "CELL_DETECTION_ROI_OFFSET_TOP",
                                            "" + configurationManager.CELL_DETECTION_ROI_OFFSET_TOP);
                                    final Thread t = new Thread(() -> {
                                        ((MoMAGui) parent).restartFromGLSegmentation();
                                        ((MoMAGui) parent).restartTracking();
                                    });
                                    t.start();
                                });
                        break;
                    }
                    case "THRESHOLD_FOR_COMPONENT_MERGING": {
                        float newValue = Float.parseFloat(evt.getNewValue().toString());
                        showPropertyEditedNeedsRerunDialog("Continue?",
                                "Changing this value will restart the optimization.\nYou will loose all manual edits performed so far!",
                                () -> newValue != configurationManager.THRESHOLD_FOR_COMPONENT_MERGING,
                                () -> sourceProperty.setValue(configurationManager.THRESHOLD_FOR_COMPONENT_MERGING),
                                () -> {
                                    configurationManager.THRESHOLD_FOR_COMPONENT_MERGING = newValue;
                                    props.setProperty(
                                            "GL_OFFSET_TOP",
                                            "" + configurationManager.THRESHOLD_FOR_COMPONENT_MERGING);
                                    dic.getWatershedMaskGenerator().setThresholdForComponentMerging(configurationManager.THRESHOLD_FOR_COMPONENT_MERGING);
                                    final Thread t = new Thread(() -> {
                                        ((MoMAGui) parent).restartFromGLSegmentation();
                                        ((MoMAGui) parent).restartTracking();
                                    });
                                    t.start();
                                });
                        break;
                    }
                    case "THRESHOLD_FOR_COMPONENT_GENERATION": {
                        float newValue = Float.parseFloat(evt.getNewValue().toString());
                        showPropertyEditedNeedsRerunDialog("Continue?",
                                "Changing this value will restart the optimization.\nYou will loose all manual edits performed so far!",
                                () -> newValue != configurationManager.THRESHOLD_FOR_COMPONENT_GENERATION,
                                () -> sourceProperty.setValue(configurationManager.THRESHOLD_FOR_COMPONENT_GENERATION),
                                () -> {
                                    configurationManager.THRESHOLD_FOR_COMPONENT_GENERATION = newValue;
                                    props.setProperty(
                                            "GL_OFFSET_TOP",
                                            "" + configurationManager.THRESHOLD_FOR_COMPONENT_GENERATION);
                                    dic.getWatershedMaskGenerator().setThreshold(configurationManager.THRESHOLD_FOR_COMPONENT_GENERATION);
                                    final Thread t = new Thread(() -> {
                                        ((MoMAGui) parent).restartFromGLSegmentation();
                                        ((MoMAGui) parent).restartTracking();
                                    });
                                    t.start();
                                });
                        break;
                    }
                    case "THRESHOLD_FOR_COMPONENT_SPLITTING": {
                        float newValue = Float.parseFloat(evt.getNewValue().toString());
                        showPropertyEditedNeedsRerunDialog("Continue?",
                                "Changing this value will restart the optimization.\nYou will loose all manual edits performed so far!",
                                () -> newValue != configurationManager.THRESHOLD_FOR_COMPONENT_SPLITTING,
                                () -> sourceProperty.setValue(configurationManager.THRESHOLD_FOR_COMPONENT_SPLITTING),
                                () -> {
                                    configurationManager.THRESHOLD_FOR_COMPONENT_SPLITTING = newValue;
                                    props.setProperty(
                                            "GL_OFFSET_TOP",
                                            "" + configurationManager.THRESHOLD_FOR_COMPONENT_SPLITTING);
                                    dic.getWatershedMaskGenerator().setThreshold(configurationManager.THRESHOLD_FOR_COMPONENT_SPLITTING);
                                    final Thread t = new Thread(() -> {
                                        ((MoMAGui) parent).restartFromGLSegmentation();
                                        ((MoMAGui) parent).restartTracking();
                                    });
                                    t.start();
                                });
                        break;
                    }
                    case "MAXIMUM_GROWTH_RATE": {
                        double newValue = Double.parseDouble(evt.getNewValue().toString());
                        showPropertyEditedNeedsRerunDialog("Continue?",
                                "Changing this value will restart the optimization.\nYou will loose all manual edits performed so far!",
                                () -> newValue != configurationManager.MAXIMUM_GROWTH_RATE,
                                () -> sourceProperty.setValue(configurationManager.MAXIMUM_GROWTH_RATE),
                                () -> {
                                    configurationManager.MAXIMUM_GROWTH_RATE = newValue;
                                    ((MoMAGui) parent).restartTrackingAsync();
                                });
                        break;
                    }
                    case "INTENSITY_FIT_ITERATIONS": {
                        configurationManager.INTENSITY_FIT_ITERATIONS =
                                Integer.parseInt(evt.getNewValue().toString());
                        props.setProperty(
                                "INTENSITY_FIT_ITERATIONS",
                                "" + configurationManager.INTENSITY_FIT_ITERATIONS);
                        break;
                    }
                    case "INTENSITY_FIT_PRECISION": {
                        configurationManager.INTENSITY_FIT_PRECISION =
                                Double.parseDouble(evt.getNewValue().toString());
                        props.setProperty(
                                "INTENSITY_FIT_PRECISION",
                                "" + configurationManager.INTENSITY_FIT_PRECISION);
                        break;
                    }
                    case "INTENSITY_FIT_INITIAL_WIDTH": {
                        configurationManager.INTENSITY_FIT_INITIAL_WIDTH =
                                Double.parseDouble(evt.getNewValue().toString());
                        props.setProperty(
                                "INTENSITY_FIT_INITIAL_WIDTH",
                                "" + configurationManager.INTENSITY_FIT_INITIAL_WIDTH);
                        break;
                    }
                    case "INTENSITY_FIT_RANGE_IN_PIXELS": {
                        configurationManager.INTENSITY_FIT_RANGE_IN_PIXELS =
                                Integer.parseInt(evt.getNewValue().toString());
                        props.setProperty(
                                "INTENSITY_FIT_RANGE_IN_PIXELS",
                                "" + configurationManager.INTENSITY_FIT_RANGE_IN_PIXELS);
                        break;
                    }
                    default:
                        JOptionPane.showMessageDialog(
                                parent,
                                "Value not changed - NOT YET IMPLEMENTED!",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        break;
                }
			} catch ( final NumberFormatException e ) {
				JOptionPane.showMessageDialog(
                        parent,
						"Illegal value entered -- value not changed!",
						"Error",
						JOptionPane.ERROR_MESSAGE );
			}
		}

	}

    private void showPropertyEditedNeedsRerunDialog(String title, String message, Supplier<Boolean> condition, Runnable abortCallback, Runnable acceptCallback) {
        if (condition.get()) {
            final int choice =
                    JOptionPane.showConfirmDialog(
                            parent,
                            message,
                            title,
                            JOptionPane.YES_NO_OPTION);
            LoggingHelper.logUiAction(choice);
            if (choice != JOptionPane.OK_OPTION) {
                abortCallback.run();
            } else {
                acceptCallback.run();
            }
        }
    }

    private class PropFactory {

        Property buildFor(final String key, final Object value) {
			final DefaultProperty property = new DefaultProperty();
			property.setDisplayName( key );
			property.setName( key );
			property.setValue( value.toString() );
			property.setType( String.class );
			property.addPropertyChangeListener( propEditListener );

            String GRB = "GUROBI Properties";
            String SEG = "Segmentation";
            String TRACK = "Tracking";
            String EXPORT = "Export Properties";

            switch (key) {
                case "GL_WIDTH_IN_PIXELS":
                case "SEGMENTATION_MODEL_PATH":
                case "CELL_DETECTION_ROI_OFFSET_TOP":
                case "THRESHOLD_FOR_COMPONENT_MERGING":
                case "THRESHOLD_FOR_COMPONENT_GENERATION":
                case "THRESHOLD_FOR_COMPONENT_SPLITTING":
                    property.setCategory(SEG);
                    property.setShortDescription(key);
                    property.setEditable(true);
                    break;
                case "GL_OFFSET_TOP":
                case "ASSIGNMENT_COST_CUTOFF":
                case "LYSIS_ASSIGNMENT_COST":
                case "MAXIMUM_GROWTH_RATE":
                    property.setCategory(TRACK);
                    property.setShortDescription(key);
                    property.setEditable(true);
                case "DEFAULT_PATH":
                    property.setShortDescription(key);
                    break;
                case "GUROBI_TIME_LIMIT":
                case "GUROBI_MAX_OPTIMALITY_GAP":
                    property.setCategory(GRB);
                    property.setShortDescription(key);
                    break;
                case "INTENSITY_FIT_ITERATIONS":
                case "INTENSITY_FIT_PRECISION":
                case "INTENSITY_FIT_INITIAL_WIDTH":
                case "INTENSITY_FIT_RANGE_IN_PIXELS":
                    property.setCategory(EXPORT);
                    property.setShortDescription(key);
                    break;
                default:
                    // ALL OTHERS ARE ADDED HERE
                    property.setShortDescription(key);
                    property.setEditable(false);
                    break;
            }
			return property;
		}
	}


	private JButton bClose;
	private final Properties props;

	public DialogPropertiesEditor(final MoMAGui parent, final Properties props, ConfigurationManager configurationManager, PseudoDic dic) {
		super( SwingUtilities.windowForComponent( parent ), "MoMA Properties Editor" );
        this.configurationManager = configurationManager;
        this.dic = dic;
        propFactory = new PropFactory();
		this.parent = parent;
		this.dialogInit();
		this.setModal( true );

		final int width = 800;
		final int height = 400;

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final int screenWidth = ( int ) screenSize.getWidth();
		final int screenHeight = ( int ) screenSize.getHeight();
		this.setBounds( ( screenWidth - width ) / 2, ( screenHeight - height ) / 2, width, height );

		this.props = props;

		buildGui();
		setKeySetup();
	}

	private void buildGui() {
		this.rootPane.setLayout( new BorderLayout() );

		sheet = new PropertySheetPanel();
		sheet.setMode( PropertySheet.VIEW_AS_CATEGORIES );
		sheet.setDescriptionVisible( false );
		sheet.setSortingCategories( false );
		sheet.setSortingProperties( false );
		sheet.setRestoreToggleStates( false );
		for ( final String key : this.props.stringPropertyNames() ) {
			sheet.addProperty( propFactory.buildFor( key, props.getProperty( key ) ) );
		}
//		sheet.setEditorFactory( PropertyEditorRegistry.Instance );

		bClose = new JButton( "Close" );
		bClose.addActionListener( this );
		this.rootPane.setDefaultButton( bClose );
		final JPanel horizontalHelper = new JPanel( new FlowLayout( FlowLayout.CENTER, 5, 0 ) );
		horizontalHelper.add( bClose );

		this.rootPane.add( sheet, BorderLayout.CENTER );
		this.rootPane.add( horizontalHelper, BorderLayout.SOUTH );
	}

	private void setKeySetup() {
		this.rootPane.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( "ESCAPE" ), "closeAction" );

		this.rootPane.getActionMap().put( "closeAction", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent e ) {
				setVisible( false );
				dispose();
			}
		} );

	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed( final ActionEvent e ) {
		if ( e.getSource().equals( bClose ) ) {
			this.setVisible( false );
			this.dispose();
		}
	}
}
