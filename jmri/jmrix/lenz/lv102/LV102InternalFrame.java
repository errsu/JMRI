// LV102InternalFrame.java

package jmri.jmrix.lenz.lv102;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ResourceBundle;
import jmri.Programmer;
import jmri.ProgListener;
import jmri.ProgrammerException;

/**
 * Internal Frame displaying the LV102 configuration utility
 *
 * This is a configuration utility for the LV102.
 * It allows the user to set the Track Voltage  and E-line status.
 *
 * @author			Paul Bender  Copyright (C) 2005
 * @version			$Revision: 1.4 $
 */
public class LV102InternalFrame extends javax.swing.JInternalFrame {

    private ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.lv102.LV102Bundle");

    private progReplyListener progListener = null;
    private Thread progListenerThread = null;

    final static int waitValue = 1000; // number of ms to wait after a 
				       // programming operation.  This 
				       // should not be more than 15.

   public LV102InternalFrame() {

        // Set up the programmer listener
	
	progListener = new progReplyListener(this);
	progListenerThread = new Thread(progListener);
	progListenerThread.start();

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

	setTitle(rb.getString("LV102Power"));

        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
        pane0.add(new JLabel(rb.getString("LV102Track")));
        pane0.add(voltBox);
        pane0.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane0);

        JPanel pane1 = new JPanel();
        pane1.add(new JLabel(rb.getString("LV102ELine")));
        pane1.add(eLineBox);
        pane1.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane1);

        JPanel pane2 = new JPanel();
        pane2.add(new JLabel(rb.getString("LV102RailCom")));
        pane2.add(railComBox);
        pane2.add(new JLabel(rb.getString("LV102RailComMode")));
        pane2.add(railComModeBox);
        pane2.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane2);

        JPanel pane3 = new JPanel();
	
	// Set the write button label and tool tip
	writeSettingsButton.setText(rb.getString("LV102WriteSettingsButtonLabel"));
	writeSettingsButton.setToolTipText(rb.getString("LV102WriteSettingsButtonToolTip"));
	
        pane3.add(writeSettingsButton);

	// Set the reset to Defaults button label and tool tip
	defaultButton.setText(rb.getString("LV102DefaultButtonLabel"));
	defaultButton.setToolTipText(rb.getString("LV102DefaultButtonToolTip"));

	// Set the reset button label and tool tip
	resetButton.setText(rb.getString("LV102ResetButtonLabel"));
	resetButton.setToolTipText(rb.getString("LV102ResetButtonToolTip"));

        pane3.add(defaultButton);
        pane3.add(resetButton);
        getContentPane().add(pane3);

        // Initilize the Combo Boxes

	/* configure the voltage selection box */
        voltBox.setVisible(true);
        voltBox.setToolTipText(rb.getString("LV102TrackTip"));
        for (int i=0; i<validVoltage.length;i++)
        {
           voltBox.addItem(validVoltage[i]);
        }
	voltBox.setSelectedIndex(23);

 	/* Configure the E-Line Active/Inactive box */
        eLineBox.setVisible(true);
        eLineBox.setToolTipText(rb.getString("LV102ELineTip"));
        for (int i=0; i<validELineStatus.length;i++)
        {
           eLineBox.addItem(validELineStatus[i]);
        }
	eLineBox.setSelectedIndex(3);

	/* Configure the RailCom Active/Inactive box */
        railComBox.setVisible(true);
        railComBox.setToolTipText(rb.getString("LV102RailComTip"));
        for (int i=0; i<validRailComStatus.length;i++)
        {
           railComBox.addItem(validRailComStatus[i]);
        }
	railComBox.setSelectedIndex(2);

	/* Configure the RailCom Mode selection box */
        railComModeBox.setVisible(true);
        railComModeBox.setToolTipText(rb.getString("LV102RailComModeTip"));
        for (int i=0; i<validRailComMode.length;i++)
        {
           railComModeBox.addItem(validRailComMode[i]);
        }
	railComModeBox.setSelectedIndex(2);

	synchronized(CurrentStatus) {
          CurrentStatus.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
          CurrentStatus.setVisible(true);
	  CurrentStatus.setText(" ");
	  if(log.isDebugEnabled()) 
	     log.debug("Current Status: ");
          getContentPane().add(CurrentStatus);
 	}

        // and prep for display
        pack();

        writeSettingsButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	writeLV102Settings();
			writeSettingsButton.setSelected(false);
                }
            }
        );

        // install reset button handler
        resetButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	resetLV102Settings();
			resetButton.setSelected(false);
                }
            }
        );

        // install reset to defaults button handler
        defaultButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	defaultLV102Settings();
			defaultButton.setSelected(false);
                }
            }
        );

	// install a handler to set the status line when the selected item 
	// changes in the e-Line box
	eLineBox.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent a) {
			synchronized(CurrentStatus) {
			   CurrentStatus.setText(rb.getString("LV102StatusChanged"));
	     		   if(log.isDebugEnabled()) 
			      log.debug("Current Status: " + rb.getString("LV102StatusChanged"));
			}
		}
	   }
	);

	// install a handler to set the status line when the selected item 
	// changes in the RailCom box
	railComBox.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent a) {
			synchronized(CurrentStatus) {
			   CurrentStatus.setText(rb.getString("LV102StatusChanged"));
	     		   if(log.isDebugEnabled()) 
			      log.debug("Current Status: " + rb.getString("LV102StatusChanged"));
			}
		}
	   }
	);

	// install a handler to set the status line when the selected item 
	// changes in the RailComMode box
	railComModeBox.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent a) {
			synchronized(CurrentStatus) {
			   CurrentStatus.setText(rb.getString("LV102StatusChanged"));
	     		   if(log.isDebugEnabled()) 
			      log.debug("Current Status: " + rb.getString("LV102StatusChanged"));
			}
		}
	   }
	);

	// install a handler to set the status line when the selected item 
	// changes in the volt box
	voltBox.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent a) {
			synchronized(CurrentStatus) {
			   CurrentStatus.setText(rb.getString("LV102StatusChanged"));
	     		   if(log.isDebugEnabled()) 
			      log.debug("Current Status: " + rb.getString("LV102StatusChanged"));
			}
		}
	   }
	);

	// configure internal frame options

	setClosable(false);  // don't let the user close this frame
	setResizable(false);  // don't let the user resize this frame
        setIconifiable(false); // don't let the user minimize this frame
	setMaximizable(false); // don't let the user maximize this frame

	// make the internal frame visible
	this.setVisible(true);
    }

    boolean read = false;

    JComboBox voltBox = new javax.swing.JComboBox();
    JComboBox eLineBox = new javax.swing.JComboBox();
    JComboBox railComBox = new javax.swing.JComboBox();
    JComboBox railComModeBox = new javax.swing.JComboBox();

    JLabel CurrentStatus = new JLabel(" ");

    JToggleButton writeSettingsButton = new JToggleButton("Write to LV102");
    JToggleButton resetButton = new JToggleButton("Reset to Initial Values");
    JToggleButton defaultButton = new JToggleButton("Reset to Factory Defaults");

    protected String [] validVoltage= new String[]{"11V","11.5V","12V","12.5V","13V","13.5V","14V","14.5V","15V","15.5V","16V (factory default)","16.5V","17V","17.5V","18V","18.5V","19V","19.5V","20V","20.5V","21V","21.5V","22V",""};
    protected int [] validVoltageValues = new int[]{22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,0};

    protected String [] validELineStatus = new String[]{rb.getString("LV102ELineActive"),rb.getString("LV102ELineInactive"),rb.getString("LV102ELineDefault"),""};
    protected int [] validELineStatusValues = new int[]{90,91,99,0};

    protected String [] validRailComStatus = new String[]{rb.getString("LV102RailComActive"),rb.getString("LV102RailComInactive"),""};
    protected int [] validRailComStatusValues = new int[]{93,92,0};

    protected String [] validRailComMode = new String[]{rb.getString("LV102RailCom3BitMode"),rb.getString("LV102RailCom4BitMode"),""};
    protected int [] validRailComModeValues = new int[]{94,95,0};

    //Send Power Station settings
    void writeLV102Settings() {

	// Obtain an ops mode programmer instance
        Programmer opsProg = jmri.InstanceManager.programmerManagerInstance()
                                    .getOpsModeProgrammer(false,00);

	// write the values to the power station.
	writeVoltSetting(opsProg);
	writeELineSetting(opsProg);
	writeRailComSetting(opsProg);
	writeRailComModeSetting(opsProg);

        // we're done now, so we can release the programmer.
        jmri.InstanceManager.programmerManagerInstance()
                    .releaseOpsModeProgrammer(opsProg);
    }

    // Write the voltage setting
    void writeVoltSetting(Programmer opsProg) {
        if((String)voltBox.getSelectedItem()!="" &&
           (String)voltBox.getSelectedItem()!=null) {
        
          if(log.isDebugEnabled()) log.debug("Selected Voltage: " +voltBox.getSelectedItem()); 
	  synchronized(CurrentStatus) {
	     CurrentStatus.setText(rb.getString("LV102StatusProgMode"));
	     CurrentStatus.doLayout();
	     if(log.isDebugEnabled()) 
		log.debug("Current Status: " + rb.getString("LV102StatusProgMode"));
	     /* Pause briefly to give the user a chance to see what is 
                happening */
		try {
			CurrentStatus.wait(waitValue);
		} catch(java.lang.InterruptedException ie1) {
			// Don't do anything with this yet
		}

             /* First, send the ops mode programing command to enter
             programing mode */
 	     try {
                 opsProg.writeCV(7,50,progListener);
             } catch(ProgrammerException e) {
                 // Don't do anything with this yet
             }
	  
	     /* Pause briefly to give the booster a chance to change 
	        into It's programming mode */
		try {
			CurrentStatus.wait(waitValue);
		} catch(java.lang.InterruptedException ie1) {
			// Don't do anything with this yet
		}

	     CurrentStatus.setText(rb.getString("LV102StatusWriteVolt"));
	     CurrentStatus.doLayout();
	     if(log.isDebugEnabled()) 
	        log.debug("Current Status: " + rb.getString("LV102StatusWriteVolt"));

             /* Next, send the ops mode programing command for the voltage 
                we want */
 	     try {
                opsProg.writeCV(7,validVoltageValues[voltBox.getSelectedIndex()],progListener);
             } catch(ProgrammerException e) {
                // Don't do anything with this yet
             }

	     /* Pause briefly to wait for the programmer to send back a 
		reply */ 
	     try {
		CurrentStatus.wait(waitValue);
	     } catch(java.lang.InterruptedException ie1) {
		// Don't do anything with this yet
	     }
          }  // End of synchronized(CurrentStatus) block for voltage setting
        } else { 
		if(log.isDebugEnabled()) log.debug("No Voltage Selected");
	}
    }

    // Write the E-Line setting
    void writeELineSetting(Programmer opsProg) {
        if((String)eLineBox.getSelectedItem()!="" &&
           (String)eLineBox.getSelectedItem()!=null) {

          if(log.isDebugEnabled()) log.debug("E-Line Setting: " +eLineBox.getSelectedItem());
	  synchronized(CurrentStatus) {
             CurrentStatus.setText(rb.getString("LV102StatusProgMode"));
	     CurrentStatus.doLayout();
	     if(log.isDebugEnabled()) 
		log.debug("Current Status: " + rb.getString("LV102StatusProgMode"));
        
	     /* Pause briefly to give the user a chance to see what is 
                happening */
	     try {
		CurrentStatus.wait(waitValue);
	     } catch(java.lang.InterruptedException ie1) {
			// Don't do anything with this yet
	     }

             /* First, send the ops mode programing command to enter
             programing mode */
   	     try {
                opsProg.writeCV(7,50,progListener);
             } catch(ProgrammerException e) {
                // Don't do anything with this yet
             }

	     /* Pause briefly to give the booster a chance to change 
	     into It's programming mode */
	     try {
		CurrentStatus.wait(waitValue);
	     } catch(java.lang.InterruptedException ie2) {
			// Don't do anything with this yet
	     }

 	     CurrentStatus.setText(rb.getString("LV102StatusWriteELine"));
	     CurrentStatus.doLayout();
	     if(log.isDebugEnabled()) 
		log.debug("Current Status: " + rb.getString("LV102StatusWriteELine"));


             /* Next, send the ops mode programing command for the E line 
                Status we want */
    	     try {
                opsProg.writeCV(7,validELineStatusValues[eLineBox.getSelectedIndex()],progListener);
             } catch(ProgrammerException e) {
                // Don't do anything with this yet
             }

	     /* Pause briefly to wait for the programmer to send back a 
		reply */ 
	     try {
		CurrentStatus.wait(waitValue);
	     } catch(java.lang.InterruptedException ie1) {
		// Don't do anything with this yet
	     }
	  } // End of synchronized(CurrentStatus) block for E-line setting
        } else { 
    	        if(log.isDebugEnabled()) log.debug("No E-Line value Selected");
        }

    }

    // Write the RailCom setting
    void writeRailComSetting(Programmer opsProg) {
        if((String)railComBox.getSelectedItem()!="" &&
           (String)railComBox.getSelectedItem()!=null) {

          if(log.isDebugEnabled()) log.debug("RailCom Setting: " + railComBox.getSelectedItem());
	  synchronized(CurrentStatus) {
	     CurrentStatus.setText(rb.getString("LV102StatusProgMode"));
	     CurrentStatus.doLayout();
	     if(log.isDebugEnabled()) 
			 log.debug("Current Status: " + rb.getString("LV102StatusProgMode"));
	  
 	     /* Pause briefly to give the user a chance to see what is 
                happening */
	     try {
		CurrentStatus.wait(waitValue);
	     } catch(java.lang.InterruptedException ie1) {
		// Don't do anything with this yet
	     }

             /* First, send the ops mode programing command to enter
                programing mode */
   	     try {
                opsProg.writeCV(7,50,progListener);
             } catch(ProgrammerException e) {
                // Don't do anything with this yet
             }

	     /* Pause briefly to give the booster a chance to change 
	        into It's programming mode */
	     try {
		CurrentStatus.wait(waitValue);
	     } catch(java.lang.InterruptedException ie3) {
		// Don't do anything with this yet
	     }

	     CurrentStatus.setText(rb.getString("LV102StatusWriteRailCom"));
	     CurrentStatus.doLayout();
	     if(log.isDebugEnabled()) 
		log.debug("Current Status: " + rb.getString("LV102StatusWriteRailCom"));

             /* Next, send the ops mode programing command for the RailComm
                Status we want */
    	     try {
                 opsProg.writeCV(7,validRailComStatusValues[railComBox.getSelectedIndex()],progListener);
             } catch(ProgrammerException e) {
                 // Don't do anything with this yet
             }

	     /* Pause briefly to wait for the programmer to send back a 
		reply */ 
	     try {
		CurrentStatus.wait(waitValue);
	     } catch(java.lang.InterruptedException ie1) {
		// Don't do anything with this yet
             }
	   } // End of synchronized(CurrentStatus) block for RailCom Setting
        } else { 
    	        if(log.isDebugEnabled()) log.debug("No RailCom value Selected");
        }
    }

    // Write the RailCom Mode setting
    void writeRailComModeSetting(Programmer opsProg) {
        if((String)railComModeBox.getSelectedItem()!="" &&
           (String)railComModeBox.getSelectedItem()!=null) {

          if(log.isDebugEnabled()) log.debug("RailCom Setting: " +railComModeBox.getSelectedItem());
	  synchronized(CurrentStatus) {
	     CurrentStatus.setText(rb.getString("LV102StatusProgMode"));
	     CurrentStatus.doLayout();
	     if(log.isDebugEnabled()) 
		log.debug("Current Status: " + rb.getString("LV102StatusProgMode"));
	  
	     /* Pause briefly to give the user a chance to see what is 
                happening */
	     try {
		CurrentStatus.wait(waitValue);
	     } catch(java.lang.InterruptedException ie1) {
		// Don't do anything with this yet
	     }

             /* First, send the ops mode programing command to enter
                programing mode */
   	     try {
                opsProg.writeCV(7,50,progListener);
             } catch(ProgrammerException e) {
                // Don't do anything with this yet
             }

	     /* Pause briefly to give the booster a chance to change 
	        into It's programming mode */
	     try {
		CurrentStatus.wait(waitValue);
	     } catch(java.lang.InterruptedException ie3) {
		// Don't do anything with this yet
	     }

	     CurrentStatus.setText(rb.getString("LV102StatusWriteRailComMode"));
	     CurrentStatus.doLayout();
	     if(log.isDebugEnabled()) 
		log.debug("Current Status: " + rb.getString("LV102StatusWriteRailComMode"));

             /* Next, send the ops mode programing command for the RailCom Mode
                Status we want */
    	     try {
                opsProg.writeCV(7,validRailComModeValues[railComModeBox.getSelectedIndex()],progListener);
             } catch(ProgrammerException e) {
                // Don't do anything with this yet
             }

	     /* Pause briefly to wait for the programmer to send back a 
		reply */ 
	     try {
		CurrentStatus.wait(waitValue);
	     } catch(java.lang.InterruptedException ie1) {
		// Don't do anything with this yet
             }
	  } // End of synchronized(CurrentStatus) block for RailCom Mode
        } else { 
    	        if(log.isDebugEnabled()) log.debug("No RailCom Mode Selected");
        }
    }

    // Set to LV102 default values.  Voltage is 16, E Line is Active, 
    // Railcom is innactive, Railcom Mode is 3 bit cutout.
    void defaultLV102Settings() {
	voltBox.setSelectedIndex(10);
	eLineBox.setSelectedIndex(0);
	railComBox.setSelectedIndex(1);
	railComModeBox.setSelectedIndex(0);
	synchronized(CurrentStatus) {
	   CurrentStatus.setText(rb.getString("LV102StatusInitial"));
	     if(log.isDebugEnabled()) 
			 log.debug("Current Status: " + rb.getString("LV102StatusInitial"));
	}
    }

    // Set to initial values.
    void resetLV102Settings() {
	voltBox.setSelectedIndex(23);
	eLineBox.setSelectedIndex(3);
	railComBox.setSelectedIndex(2);
	railComModeBox.setSelectedIndex(2);
	synchronized(CurrentStatus) {
	   CurrentStatus.setText(rb.getString("LV102StatusOK"));
	   if(log.isDebugEnabled()) 
		log.debug("Current Status: " + rb.getString("LV102StatusOK"));
	}
    }

    public void dispose() {
        // take apart the JInternalFrame
        super.dispose();
    }

    private class progReplyListener implements Runnable, jmri.ProgListener {

    private Object parent = null;

    progReplyListener(Object Parent) {
	parent = Parent;	
    }

    public void run(){
    }

    /**
      *  This class is a programmer listener, so we implement the 
      * programmingOpReply() function
      */
    public void programmingOpReply(int value, int status) {
                if(log.isDebugEnabled()) log.debug("Programming Operation reply recieved, value is " + value + " ,status is " +status);
		if(status==ProgListener.ProgrammerBusy) {
			synchronized(CurrentStatus) {
			   CurrentStatus.setText(rb.getString("LV102StatusBUSY"));
	     		   if(log.isDebugEnabled()) 
			     log.debug("Current Status: " + rb.getString("LV102StatusBUSY"));
 			   CurrentStatus.notify();
			}
	        } else if(status==ProgListener.OK) {
			if(CurrentStatus.getText().equals(rb.getString("LV102StatusProgMode"))) {
			   synchronized(CurrentStatus) {
			      CurrentStatus.setText(rb.getString("LV102StatusReadyProg"));
	     		      if(log.isDebugEnabled()) 
			       log.debug("Current Status: " + rb.getString("LV102StatusReadyProg"));
 			      CurrentStatus.notify();
			   }
			}
			else {
			   synchronized(CurrentStatus) {
			     CurrentStatus.setText(rb.getString("LV102StatusWritten"));
	     		     if(log.isDebugEnabled()) 
				log.debug("Current Status: " + rb.getString("LV102StatusWritten"));
			     CurrentStatus.notify();
			   }
			}
		} else {
			synchronized(CurrentStatus) {
			   CurrentStatus.setText(rb.getString("LV102StatusUnknown"));
	     		   if(log.isDebugEnabled()) 
			      log.debug("Current Status: " + rb.getString("LV102StatusUnknown"));
 			   CurrentStatus.notify();
			}
	        }
        }      
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LV102InternalFrame.class.getName());

}
