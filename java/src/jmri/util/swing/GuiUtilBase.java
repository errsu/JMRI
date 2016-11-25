package jmri.util.swing;

import java.io.IOException;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import jmri.util.FileUtil;
import jmri.util.jdom.LocaleSelector;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utility methods for working with GUI items
 *
 * @author Bob Jacobsen Copyright 2010
 */
public class GuiUtilBase {

    static Action actionFromNode(Element child, WindowInterface wi, Object context) {
        String name = null;
        Icon icon = null;

        HashMap<String, String> parameters = new HashMap<>();
        if (child == null) {
            log.warn("Action from node called without child");
            return createEmptyMenuItem(null, "<none>");
        }
        name = LocaleSelector.getAttribute(child, "name");
        if ((name == null) || (name.equals(""))) {
            if (child.getChild("name") != null) {
                name = child.getChild("name").getText();
            }
        }

        if (child.getChild("icon") != null) {
            icon = new ImageIcon(FileUtil.findURL(child.getChild("icon").getText()));
        }
        //This bit does not size very well, but it works for now.
        if (child.getChild("option") != null) {
            child.getChildren("option").stream().forEach((item) -> {
                String setting = ((Element) item).getAttribute("setting").getValue();
                String setMethod = ((Element) item).getText();
                parameters.put(setMethod, setting);
            });
        }

        if (child.getChild("adapter") != null) {
            String classname = child.getChild("adapter").getText();
            JmriAbstractAction a = null;
            try {
                Class<?> c = Class.forName(classname);
                for (java.lang.reflect.Constructor<?> ct : c.getConstructors()) {
                    // look for one with right arguments
                    if (icon == null) {
                        Class<?>[] parms = ct.getParameterTypes();
                        if (parms.length != 2) {
                            continue;
                        }
                        if (parms[0] != String.class) {
                            continue;
                        }
                        if (parms[1] != WindowInterface.class) {
                            continue;
                        }
                        // found it!
                        a = (JmriAbstractAction) ct.newInstance(new Object[]{name, wi});
                        a.setName(name);
                        a.setContext(context);
                        setParameters(a, parameters);
                        return a;
                    } else {
                        Class<?>[] parms = ct.getParameterTypes();
                        if (parms.length != 3) {
                            continue;
                        }
                        if (parms[0] != String.class) {
                            continue;
                        }
                        if (parms[1] != Icon.class) {
                            continue;
                        }
                        if (parms[2] != WindowInterface.class) {
                            continue;
                        }
                        // found it!
                        a = (JmriAbstractAction) ct.newInstance(new Object[]{name, icon, wi});
                        a.setName(name);
                        a.setContext(context);
                        setParameters(a, parameters);
                        return a;
                    }
                }
                log.warn("Did not find suitable ctor for " + classname + (icon != null ? " with" : " without") + " icon");
                return createEmptyMenuItem(icon, name);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                log.warn("failed to load GUI adapter class: " + classname + " due to: " + e);
                return createEmptyMenuItem(icon, name);
            }
        } else if (child.getChild("panel") != null) {
            try {
                JmriNamedPaneAction act;
                if (icon == null) {
                    act = new JmriNamedPaneAction(name, wi, child.getChild("panel").getText());
                } else {
                    act = new JmriNamedPaneAction(name, icon, wi, child.getChild("panel").getText());
                }
                act.setContext(context);
                setParameters(act, parameters);
                return act;
            } catch (Exception ex) {
                log.warn("could not load toolbar adapter class: " + child.getChild("panel").getText()
                        + " due to " + ex);
                return createEmptyMenuItem(icon, name);
            }
        } else if (child.getChild("help") != null) {
            String reference = child.getChild("help").getText();
            return jmri.util.HelpUtil.getHelpAction(name, icon, reference);
        } else if (child.getChild("current") != null) {
            String method[] = {child.getChild("current").getText()};
            return createActionInCallingWindow(context, method, name, icon);
            //Relates to the instance that has called it 
        } else { // make from icon or text without associated function
            return createEmptyMenuItem(icon, name);
        }
    }

    /**
     * Create an action against the object that invoked the creation of the
     * GUIBase, a string array is used so that in the future further options can
     * be specified to be passed.
     *
     * @param obj  the object to create an action for
     * @param args arguments to passed remoteCalls method of obj
     * @param name name of the action
     * @param icon icon for the action
     * @return the action for obj or an empty action with name and icon
     */
    static Action createActionInCallingWindow(Object obj, final String args[], String name, Icon icon) {
        java.lang.reflect.Method method = null;
        try {
            method = obj.getClass().getDeclaredMethod("remoteCalls", String[].class);
        } catch (java.lang.NullPointerException e) {
            log.error("Null object passed");
            return createEmptyMenuItem(icon, name);
        } catch (SecurityException e) {
            log.error("security exception unable to find remoteCalls for " + obj.getClass().getName());
            createEmptyMenuItem(icon, name);
        } catch (NoSuchMethodException e) {
            log.error("No such method remoteCalls for " + obj.getClass().getName());
            return createEmptyMenuItem(icon, name);
        }

        CallingAbstractAction act = new CallingAbstractAction(name, icon);

        act.setMethod(method);
        act.setArgs(args);
        act.setObject(obj);
        act.setEnabled(true);
        return act;
    }

    static class CallingAbstractAction extends javax.swing.AbstractAction {

        public CallingAbstractAction(String name, Icon icon) {
            super(name, icon);
        }

        java.lang.reflect.Method method;
        Object obj;
        Object args;

        public void setArgs(Object args[]) {
            //args = stringArgs.getClass();
            this.args = args;
        }

        public void setMethod(java.lang.reflect.Method method) {
            this.method = method;
        }

        public void setObject(Object obj) {
            this.obj = obj;
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            try {
                method.invoke(obj, args);
            } catch (IllegalArgumentException ex) {
                System.out.println("IllegalArgument " + ex);
            } catch (IllegalAccessException ex) {
                System.out.println("IllegalAccess " + ex);
            } catch (java.lang.reflect.InvocationTargetException ex) {
                System.out.println("InvocationTarget " + ex.toString());
            }
        }
    }

    static Action createEmptyMenuItem(Icon icon, String name) {
        if (icon != null) {
            AbstractAction act = new AbstractAction(name, icon) {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                }

                @Override
                public String toString() {
                    return (String) getValue(javax.swing.Action.NAME);
                }
            };
            act.setEnabled(false);
            return act;
        } else { // then name must be present
            AbstractAction act = new AbstractAction(name) {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                }

                @Override
                public String toString() {
                    return (String) getValue(javax.swing.Action.NAME);
                }
            };
            act.setEnabled(false);
            return act;
        }
    }

    static void setParameters(JmriAbstractAction act, HashMap<String, String> parameters) {
        parameters.entrySet().stream().forEach((map) -> {
            act.setParameter(map.getKey(), map.getValue());
        });
    }

    /**
     * Get root element from XML file, handling errors locally.
     *
     * @param name the name of the XML file
     * @return the root element or null
     */
    static protected Element rootFromName(String name) {
        try {
            return new jmri.jmrit.XmlFile() {
            }.rootFromName(name);
        } catch (JDOMException | IOException e) {
            log.error("Could not parse file \"" + name + "\" due to: " + e);
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(GuiUtilBase.class.getName());
}
