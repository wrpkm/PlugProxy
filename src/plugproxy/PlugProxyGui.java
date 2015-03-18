package plugproxy;

import plugproxy.util.CommandLineParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *   (c) Copyright 2001,2002 - Brad Wellington
 *   =======================================
 *
 *   PlugProxyGui is free software; you can redistribute this file and/or modify it
 *   under the terms of the GNU General Public License as published by the Free
 *   Software Foundation; either version 2 of the License, or (at your option)
 *   any later version.
 *
 *   PlugProxyGui is a UI that wraps around the PlugProxy software.  It provided
 *   as a convience tool.
 */
public class PlugProxyGui extends JFrame implements DocumentListener
{
    private JButton startButton;
    private JButton clearButton;

    private JTextField hostUI;
    private JTextField hostPortUI;
    private JTextField localPortUI;
    private JCheckBox  inDelayUI;
    private JTextField inDelayFrequencyUI;
    private JTextField inDelayTimeUI;
    private JCheckBox  outDelayUI;
    private JTextField outDelayFrequencyUI;
    private JTextField outDelayTimeUI;
    private JCheckBox logAppendUI;
    private JTextField logFilenameUI;
    private JTextArea incomingUI;
    private JTextArea outgoingUI;
    private JCheckBox hexModeUI;
    private JLabel statusLabel;
    private JCheckBox quietModeUI;
    private JCheckBox lineWrapUI;

    private boolean isRunning = false;
    private PlugProxyListener ppl;
    private ActionListener listener;

    private Parameters params = new Parameters();

    /**
     * A default contructor.
     *
     * @param args - The command line arguments
     */
    public PlugProxyGui(String[] args)
    {
        super("PlugProxy");

        setProperties();
        createUI();
        layoutUI();

        setIconImage(loadImage("plug.gif"));

        parseCommandLine(args);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(-1);
            }
        });

        Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((ss.width - this.getSize().width) / 2, (ss.height - this.getSize().height) / 4);

        doDocEvent();
        show();
    }

    /**
     * For document events.
     * @param e
     */
    public void insertUpdate(DocumentEvent e)
    {
        doDocEvent();
    }

    /**
     * For document events.
     * @param e
     */
    public void removeUpdate(DocumentEvent e)
    {
        doDocEvent();
    }

    /**
     * For document events.
     * @param e
     */
    public void changedUpdate(DocumentEvent e)
    {
        doDocEvent();
    }

    private void createUI()
    {
        startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if(!isRunning)
                    startListener();
                else
                    stopListener();
            }
        });

        clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                incomingUI.setText("");
                outgoingUI.setText("");
            }
        });

        hostUI = new JTextField();
        hostUI.setPreferredSize(new Dimension(120, (int) hostUI.getPreferredSize().getHeight()));
        hostUI.getDocument().addDocumentListener(this);

        hostPortUI = new JTextField();
        hostPortUI.setPreferredSize(new Dimension(75, (int) hostPortUI.getPreferredSize().getHeight()));
        hostPortUI.getDocument().addDocumentListener(this);

        localPortUI = new JTextField();
        localPortUI.setPreferredSize(new Dimension(75, (int) localPortUI.getPreferredSize().getHeight()));
        localPortUI.getDocument().addDocumentListener(this);

        inDelayUI = new JCheckBox("Incoming");
        inDelayUI.setSelected(false);
        inDelayUI.setOpaque(false);

        inDelayUI.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                params.inDelay = !inDelayUI.isSelected();

                if (inDelayUI.isSelected())
                {
                	inDelayFrequencyUI.setEnabled(true);
                	inDelayTimeUI.setEnabled(true);
                }
                else
                {
                	inDelayFrequencyUI.setEnabled(false);
                	inDelayTimeUI.setEnabled(false);
                }
            }
        });

        inDelayFrequencyUI = new JTextField();
        inDelayFrequencyUI.setPreferredSize(new Dimension(75, (int) inDelayFrequencyUI.getPreferredSize().getHeight()));
        inDelayFrequencyUI.getDocument().addDocumentListener(this);
        inDelayFrequencyUI.setEnabled(false);

        inDelayTimeUI = new JTextField();
        inDelayTimeUI.setPreferredSize(new Dimension(75, (int) inDelayTimeUI.getPreferredSize().getHeight()));
        inDelayTimeUI.getDocument().addDocumentListener(this);
        inDelayTimeUI.setEnabled(false);

        outDelayUI = new JCheckBox("Outgoing");
        outDelayUI.setSelected(false);
        outDelayUI.setOpaque(false);

        outDelayUI.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                params.inDelay = !outDelayUI.isSelected();

                if (outDelayUI.isSelected())
                {
                	outDelayFrequencyUI.setEnabled(true);
                	outDelayTimeUI.setEnabled(true);
                }
                else
                {
                	outDelayFrequencyUI.setEnabled(false);
                	outDelayTimeUI.setEnabled(false);
                }
            }
        });

        outDelayFrequencyUI = new JTextField();
        outDelayFrequencyUI.setPreferredSize(new Dimension(75, (int) outDelayFrequencyUI.getPreferredSize().getHeight()));
        outDelayFrequencyUI.getDocument().addDocumentListener(this);
        outDelayFrequencyUI.setEnabled(false);

        outDelayTimeUI = new JTextField();
        outDelayTimeUI.setPreferredSize(new Dimension(75, (int) outDelayTimeUI.getPreferredSize().getHeight()));
        outDelayTimeUI.getDocument().addDocumentListener(this);
        outDelayTimeUI.setEnabled(false);

        logFilenameUI = new JTextField();
        logFilenameUI.setPreferredSize(new Dimension(200, (int) logFilenameUI.getPreferredSize().getHeight()));
        logFilenameUI.getDocument().addDocumentListener(this);

        logAppendUI = new JCheckBox("Append Log");
        logAppendUI.setSelected(false);
        logAppendUI.setOpaque(false);

        logAppendUI.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                params.logAppend = logAppendUI.isSelected();
            }
        });

        incomingUI = new JTextArea();
        incomingUI.setFont(new Font("Monospaced", Font.BOLD, 12));
        incomingUI.setEditable(false);

        outgoingUI = new JTextArea();
        outgoingUI.setFont(new Font("Monospaced", Font.BOLD, 12));
        outgoingUI.setEditable(false);

        hexModeUI = new JCheckBox("Hex Mode");
        hexModeUI.setSelected(false);
        hexModeUI.setOpaque(false);

        hexModeUI.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                params.hex = hexModeUI.isSelected();
            }
        });

        quietModeUI = new JCheckBox("Quiet Mode");
        quietModeUI.setSelected(false);
        quietModeUI.setOpaque(false);

        quietModeUI.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                params.sniff = !quietModeUI.isSelected();
            }
        });

        lineWrapUI = new JCheckBox("Line Wrap");
        lineWrapUI.setSelected(false);
        lineWrapUI.setOpaque(false);

        lineWrapUI.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                incomingUI.setLineWrap(lineWrapUI.isSelected());
                outgoingUI.setLineWrap(lineWrapUI.isSelected());
            }
        });

        statusLabel = new JLabel();
    }

    private void layoutUI()
    {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        p.add(BorderLayout.NORTH, makeTopBar());
        p.add(BorderLayout.CENTER, makeMessagePanel());
        p.add(BorderLayout.SOUTH, makeBottomBar());

        setContentPane(p);
        pack();
        setSize(600, 500);
    }

    private JSplitPane makeTopBar()
    {
    	JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    	sp.setDividerSize(0);

    	JSplitPane sp1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    	sp1.setDividerSize(0);

        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel("Host "));
        p.add(hostUI);
        p.add(new JLabel("Remote Port"));
        p.add(hostPortUI);
        p.add(new JLabel("Local Port "));
        p.add(localPortUI);
        sp1.add(p);

        p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel("Log Filename "));
        p.add(logFilenameUI);
        p.add(logAppendUI);
        sp1.add(p);

    	JSplitPane sp2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    	sp2.setDividerSize(0);

        p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(inDelayUI);
        p.add(new JLabel("Delay Frequency "));
        p.add(inDelayFrequencyUI);
        p.add(new JLabel("Delay Time (ms) "));
        p.add(inDelayTimeUI);
        sp2.add(p);

        p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(outDelayUI);
        p.add(new JLabel("Delay Frequency "));
        p.add(outDelayFrequencyUI);
        p.add(new JLabel("Delay Time (ms) "));
        p.add(outDelayTimeUI);
        sp2.add(p);
        //p.add(hexModeUI);

        sp.add(sp1);
        sp.add(sp2);

        return sp;
    }

    private JSplitPane makeMessagePanel()
    {
        JSplitPane p = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        p.setDividerSize(5);
        p.setBorder(new EmptyBorder(new Insets(1, 1, 1, 1)));
//        p.setDividerLocation((int)((p.getSize().height - p.getInsets().top - p.getDividerSize() ) / 2));
        p.add(makeOutgoingPanel());
        p.add(makeIncomingPanel());

        return p;
    }

    private JPanel makeBottomBar()
    {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(quietModeUI);
        leftPanel.add(lineWrapUI);
        leftPanel.add(hexModeUI);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.add(statusLabel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(clearButton);
        rightPanel.add(startButton);

        p.add(BorderLayout.WEST, leftPanel);
        p.add(BorderLayout.CENTER, centerPanel);
        p.add(BorderLayout.EAST, rightPanel);

        return p;
    }

    private JPanel makeOutgoingPanel()
    {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        TitledBorder border = new TitledBorder("Request");
        border.setTitleColor(new Color(65, 65, 65));
        p.setBorder(border);

        JScrollPane s = new JScrollPane(outgoingUI);

        //Added in to support the JDK 1.3
        s.getViewport().setBackground(Color.white);
        s.setAutoscrolls(true);

        p.add(s, BorderLayout.CENTER);

        p.setMinimumSize(new Dimension(310, 100));
        p.setPreferredSize(new Dimension(310, 200));

        return p;
    }

    private JPanel makeIncomingPanel()
    {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        TitledBorder border = new TitledBorder("Response");
        border.setTitleColor(new Color(65, 65, 65));
        p.setBorder(border);

        JScrollPane s = new JScrollPane(incomingUI);

        //Added in to support the JDK 1.3
        s.getViewport().setBackground(Color.white);
        s.setAutoscrolls(true);

        p.add(s, BorderLayout.CENTER);

        p.setMinimumSize(new Dimension(310, 100));
        p.setPreferredSize(new Dimension(310, 200));

        return p;
    }

    private void setProperties()
    {
        // drag and drop
        UIManager.put("DragAndDrop.selectionBackground", new Color(64, 32, 255));
        UIManager.put("ToolTip.foreground", Color.black);
        UIManager.put("ToolTip.background", new Color(255, 255, 220));
        UIManager.put("ToolTip.border", new EtchedBorder(EtchedBorder.RAISED));

        Color c = new Color(230, 230, 230);

        UIManager.put("Desktop.background", new Color(120, 150, 170));
        UIManager.put("Panel.background", c);
        UIManager.put("Button.background", c);
        UIManager.put("ToggleButton.background", c);
        UIManager.put("Label.background", c);
        UIManager.put("ComboBox.disabledBackground", c);
        UIManager.put("TabbedPane.background", new Color(140, 140, 140));
        UIManager.put("TabbedPane.foreground", new Color(65, 65, 65));
        UIManager.put("TabbedPane.selected", new Color(240, 240, 255));

        UIManager.put("ComboBox.background", c);
        UIManager.put("Label.foreground", new Color(65, 65, 65));
        UIManager.put("CheckBox.foreground", new Color(65, 65, 65));

        ToolTipManager.sharedInstance().setDismissDelay(1000 * 30);
        ToolTipManager.sharedInstance().setReshowDelay(0);

        Font ab = new Font("Verdana", Font.BOLD, 10);
        Font ar = new Font("Verdana", Font.PLAIN, 10);
        Font at = new Font("Verdana", Font.PLAIN, 12);

        // change all UI to use Espy Font
        UIManager.put("Button.font", ar);
        UIManager.put("CheckBox.font", ab);
        UIManager.put("CheckBoxMenuItem.font", ab);
        UIManager.put("ComboBox.font", ar);
        UIManager.put("InternalFrame.font", ab);
        UIManager.put("Label.font", ab);
        UIManager.put("List.font", at);
        UIManager.put("Menu.font", ab);
        UIManager.put("MenuBar.font", ab);
        UIManager.put("MenuItem.font", ab);
        UIManager.put("OptionPane.font", ab);
        UIManager.put("Panel.font", ar);
        UIManager.put("PopupMenu.font", ab);
        UIManager.put("RadioButton.font", ab);
        UIManager.put("RadioButtonMenuItem.font", ab);
        UIManager.put("ScrollPane.font", ab);
        UIManager.put("TabbedPane.font", ab);
        UIManager.put("Table.font", at);
        UIManager.put("TableHeader.font", ar);
        UIManager.put("TextArea.font", at);
        UIManager.put("TextField.font", at);
        UIManager.put("PasswordField.font", at);
        UIManager.put("TextPane.font", at);
        UIManager.put("TitledBorder.font", ab);
        UIManager.put("ToggleButton.font", ab);
        UIManager.put("ToolBar.font", ab);
        UIManager.put("ToolTip.font", ar);
        UIManager.put("Tree.font", at);
        UIManager.put("ViewPort.font", ar);
    }

    private void doDocEvent()
    {
        boolean notEmpty = (hostUI.getText().length() > 0) && (hostPortUI.getText().length() > 0) && (localPortUI.getText().length() > 0)
        		&& ( !inDelayUI.isSelected() || ( (inDelayFrequencyUI.getText().length() > 0) && (inDelayTimeUI.getText().length() > 0) ) )
        		&& ( !outDelayUI.isSelected() || ( (outDelayFrequencyUI.getText().length() > 0) && (outDelayTimeUI.getText().length() > 0) ) );

        boolean portIsNumber = true;

        if(notEmpty)
        {
            try
            {
                Integer.parseInt(hostPortUI.getText());
                Integer.parseInt(localPortUI.getText());
                if (inDelayUI.isSelected()) {
                	Integer.parseInt(inDelayFrequencyUI.getText());
                	Long.parseLong(inDelayTimeUI.getText());
                }
                if (outDelayUI.isSelected()) {
                	Integer.parseInt(outDelayFrequencyUI.getText());
                	Long.parseLong(outDelayTimeUI.getText());
                }
            }
            catch(NumberFormatException e)
            {
                portIsNumber = false;
            }
        }

        boolean valid = (notEmpty && portIsNumber);
        startButton.setEnabled(valid);
    }

    private void startListener()
    {
        params.host = hostUI.getText();
        params.remotePort = Integer.parseInt(hostPortUI.getText());
        params.localPort = Integer.parseInt(localPortUI.getText());
        params.sniff = !quietModeUI.isSelected();
        params.hex = hexModeUI.isSelected();
        params.inDelay = inDelayUI.isSelected();
        if (inDelayUI.isSelected()) {
            params.inDelayFrequency = Integer.parseInt(inDelayFrequencyUI.getText());
            params.inDelayTime = Long.parseLong(inDelayTimeUI.getText());
        }
        else
        {
            params.inDelayFrequency = 0;
            params.inDelayTime = 0;
        }
        params.outDelay = outDelayUI.isSelected();
        if (outDelayUI.isSelected()) {
            params.outDelayFrequency = Integer.parseInt(outDelayFrequencyUI.getText());
            params.outDelayTime = Long.parseLong(outDelayTimeUI.getText());
        }
        else
        {
            params.outDelayFrequency = 0;
            params.outDelayTime = 0;
        }
        params.logFilename = logFilenameUI.getText();
        params.logAppend = logAppendUI.isSelected();

        statusLabel.setText("Listening.....");
        startButton.setText("Stop ");
        hostUI.setEnabled(false);
        hostPortUI.setEnabled(false);
        localPortUI.setEnabled(false);
        inDelayUI.setEnabled(false);
        inDelayFrequencyUI.setEnabled(false);
        inDelayTimeUI.setEnabled(false);
        outDelayUI.setEnabled(false);
        outDelayFrequencyUI.setEnabled(false);
        outDelayTimeUI.setEnabled(false);
        logFilenameUI.setEnabled(false);
        logAppendUI.setEnabled(false);

        isRunning = true;

        try
        {
            new Socket(params.host, params.remotePort).close(); // verify host

            ppl = new PlugProxyListener(params);

            listener = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                	Date now = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    int modifiers = e.getModifiers();

                    if((modifiers & PlugProxyListener.EVENT_DATA) != 0)
                    {
                        if((modifiers & PlugProxyListener.STREAM_IN) != 0)
                        {
                            incomingUI.append("[" + sdf.format(now) + "]" + '\n');
                            incomingUI.append(e.getActionCommand());
                            int i = incomingUI.getText().length();
                            incomingUI.setCaretPosition(i);
                        }
                        else
                        {
                            outgoingUI.append("[" + sdf.format(now) + "]" + '\n');
                            outgoingUI.append(e.getActionCommand());
                            int i = outgoingUI.getText().length();
                            outgoingUI.setCaretPosition(i);
                        }
                    }
                }
            };

            ppl.addActionListener(listener);

        }
        catch(Exception e)
        {
            e.printStackTrace();
            JOptionPane pane = new JOptionPane(e);
            JDialog dialog = pane.createDialog(this, "Error...");
            ((JComponent) dialog.getContentPane().getComponent(0)).setOpaque(false);
            dialog.show();
        }
    }

    private void stopListener()
    {
        statusLabel.setText("");
        startButton.setText("Start");
        isRunning = false;

        if(ppl != null)
        {
            ppl.stop();
            ppl = null;
        }

        hostUI.setEnabled(true);
        hostPortUI.setEnabled(true);
        localPortUI.setEnabled(true);
        inDelayUI.setEnabled(true);
        if (inDelayUI.isSelected())
        {
        	inDelayFrequencyUI.setEnabled(true);
        	inDelayTimeUI.setEnabled(true);
        }
        outDelayUI.setEnabled(true);
        if (outDelayUI.isSelected())
        {
        	outDelayFrequencyUI.setEnabled(true);
        	outDelayTimeUI.setEnabled(true);
        }
        logFilenameUI.setEnabled(true);
        logAppendUI.setEnabled(true);
    }

    /**
     *  Verifies the command line arguments.  If any arguments are missing or invalid
     *  the usage will be printed and the program exits.
     *  @param args An array of Strings containing the command line parameters.
     **/
    protected void parseCommandLine(String[] args)
    {
        if(args.length < 3)
            return;

        CommandLineParser cmp = new CommandLineParser(args);

        hostUI.setText(args[0]);
        hostPortUI.setText(args[1]);
        localPortUI.setText(args[2]);

        if(cmp.containsParameter("id")) {
            inDelayUI.setSelected(true);

            inDelayFrequencyUI.setEnabled(true);
            inDelayFrequencyUI.setText(cmp.getParameter("if"));
            inDelayTimeUI.setEnabled(true);
            inDelayTimeUI.setText(cmp.getParameter("it"));
        }

        if(cmp.containsParameter("od")) {
            outDelayUI.setSelected(true);

            outDelayFrequencyUI.setEnabled(true);
            outDelayFrequencyUI.setText(cmp.getParameter("of"));
            outDelayTimeUI.setEnabled(true);
            outDelayTimeUI.setText(cmp.getParameter("ot"));
        }

        if (cmp.containsParameter("lg")) {
        	logFilenameUI.setText(cmp.getParameter("lg"));
        }

        if (cmp.containsParameter("a"))
        	logAppendUI.setSelected(true);;

        quietModeUI.setSelected(!cmp.containsParameter("s"));
        hexModeUI.setSelected(cmp.containsParameter("h"));
    }

    private Image loadImage(String imageName)
    {
        Image im = null;

        try
        {
            im = Toolkit.getDefaultToolkit().
                    createImage(loadResource(imageName));
        }
        catch(IOException e)
        {
            e.printStackTrace();

        }

        return im;
    }

    /**
     * Loads a resource from the CLASSPATH
     */
    private final byte[] loadResource(String resLocation)
            throws IOException
    {

        Class refClass = null;

        refClass = getClass();


        InputStream is = refClass.getResourceAsStream(resLocation);

        if(is == null)
        {
            throw new IOException("resource not found: " + resLocation);
        }
        else
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int c;

            while((c = is.read()) >= 0)
            {
                baos.write(c);
            }

            return baos.toByteArray();
        }
    }


    /**
     * A default main method.
     *
     * @param args - The same command line args as plug proxy normally takes
     */
    public static void main(String[] args)
    {
        new PlugProxyGui(args);
    }
}

