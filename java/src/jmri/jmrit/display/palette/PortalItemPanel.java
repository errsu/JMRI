package jmri.jmrit.display.palette;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JPanel;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.display.controlPanelEditor.PortalIcon;
import jmri.util.JmriJFrame;

/**
*  ItemPanel for for PortalIcons.  Since this class has been introduced after users may have
*  customized the defaultPanelIcons, the default family, "Standard" is added by overriding
*  the initIconFamiliesPanel method.
*   
* @author Pete Cressman  Copyright (c) 2013
*/
public /*abstract*/ class PortalItemPanel extends FamilyItemPanel {

    /**
    * Constructor types with multiple families and multiple icon families
    */
    public PortalItemPanel(JmriJFrame parentFrame, String type, String family, Editor editor) {
        super(parentFrame, type, family, editor);
    }

    /**
    * Init for creation
    * _bottom1Panel and _bottom2Panel alternate visibility in bottomPanel depending on
    * whether icon families exist.  They are made first because they are referenced in
    * initIconFamiliesPanel()
    * subclasses will insert other panels
    */
    public void init() {
       	if (!_initialized) {
       		Thread.yield();
       		_update = false;
       		_supressDragging = true;
       		makeBottomPanel();
       		add(makeChangeDefaultIconsPanel());
       	}
    }
    
    private JPanel makeChangeDefaultIconsPanel() {
    	JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton setDefaultsButton = new JButton(Bundle.getMessage("setDefaultIcons"));
        setDefaultsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	setDefaults();
                }
        });
        setDefaultsButton.setToolTipText(Bundle.getMessage("ToolTipSetDefaultIcons"));
        panel.add(setDefaultsButton);
    	return panel;
    }
    
    private void setDefaults() {
    	HashMap <String, NamedIcon> map = getIconMap();
    	((ControlPanelEditor)_editor).setDefaultPortalIcons(jmri.jmrit.display.PositionableIcon.cloneMap(map, null));
    }

    static Logger log = LoggerFactory.getLogger(PortalItemPanel.class.getName());
}