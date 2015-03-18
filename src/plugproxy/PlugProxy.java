package plugproxy;

/**
 *   (c) Copyright 2001,2002 - Christopher A. Longo
 *   =======================================
 *
 *   PlugProxy is free software; you can redistribute this file and/or modify it
 *   under the terms of the GNU General Public License as published by the Free
 *   Software Foundation; either version 2 of the License, or (at your option)
 *   any later version.
 */

import plugproxy.util.CommandLineParser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  <p>A plug proxy implementation.  Allows TCP/IP data to be passed from the
 *  local system:port to a remote system:port transparently.  Quite useful when
 *  dealing with applet issues where the remote client must talk to the server
 *  from which it was loaded.</p>
 *
 *  <p>Works with most protocols HTTP/HTTPS/POP/SMTP/Telnet etc... Protocols
 *  which rely on multiple ports may difficult to configure.</p>
 *
 *  <p>This is a command line version, being it will probably be run in the
 *  background.  It would be quite easy to write a pretty Swing GUI version
 *  using the ProxyProxyListener class.</p>

 *  <p>History:<br>
 *  2.00 06/27/2002<br>
 *  <ol>
 *  <li>Added support for a GUI</li>
 *  <li>Added lots of cleanup code for spawned threads</li>
 *  <li>Lots and lots of code simplification</li>
 *  <li>Drastically changed and simplified the command line parameters.</li>
 *  <li>Added -h parameter for hex sniffing</li>
 *  <li>Cleaned up code</li>
 *  </ol></p>

 *  <p>History:<br>
 *  1.03 01/09/2001<br>
 *  <ol>
 *  <li>Implemented support for sniff protocol dumping.</li>
 *  </ol></p>
 *
 *  <p>History:<br>
 *  1.02 06/12/2000<br>
 *  <ol>
 *  <li>Improved verbose mode.</li>
 *  <li>Improved usage text.</li>
 *  <li>Minor source/comment changes.</li>
 *  </ol></p>
 *
 *  @author Christopher A. Longo (cal@cloud9.net)
 *  @author Kevin A. Burton (burton@apache.org | burton@openprivacy.org)
 *  @version 2.00 06.21.2002
 **/

public class PlugProxy
{
    /** Program version number **/
    public static final String VERSION = "2.00";

    /**
     *  Constructor.  Parses the command line and starts up the proxy.
     *  @param args A string array containing the command line arguments.
     *  @exception java.io.IOException if a network error occurs.
     */
    public PlugProxy(String[] args)
        throws IOException
    {
        final Parameters params = parseCommandLine(args);

        PlugProxyListener ppl = new PlugProxyListener(params);

        if(params.verbose || params.sniff)
        {
            ppl.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    int modifiers = e.getModifiers();

                    if((params.verbose && (modifiers & PlugProxyListener.EVENT_INFO) != 0) ||
                        (params.sniff && (modifiers & PlugProxyListener.EVENT_DATA) != 0))
                    {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                        System.out.print((modifiers & PlugProxyListener.STREAM_IN) != 0 ? "<" : ">");
                        System.out.print("[" + sdf.format(new Date()) + "] ");

                        if(params.sniff)
                            System.out.print("\n");

                        System.out.println(e.getActionCommand());
                    }
                }
            });

            if(params.verbose) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                System.out.println("[" + sdf.format(new Date()) + "]" + " Ready: " + ppl);
            }
        }
    }

    /**
     *  Verifies the command line arguments.  If any arguments are missing or invalid
     *  the usage will be printed and the program exits.
     *  @param args An array of Strings containing the command line parameters.
     **/
    protected Parameters parseCommandLine(String[] args)
    {
        if(args.length < 3)
            printUsageAndExit();

        Parameters params = new Parameters();
        CommandLineParser cmp = new CommandLineParser(args);

        params.host = args[0];

        try
        {
            params.localPort = Integer.parseInt(args[2]);
            params.remotePort = Integer.parseInt(args[1]);
        }
        catch(NumberFormatException ignore) {}

        if(cmp.containsParameter("v"))
            params.verbose = true;

        if(cmp.containsParameter("s"))
            params.sniff = true;

        if(cmp.containsParameter("h"))
            params.hex = true;

        if(cmp.containsParameter("id")) {
            params.inDelay = true;

            params.inDelayFrequency = Integer.parseInt(cmp.getParameter("if"));
            params.inDelayTime = Long.parseLong(cmp.getParameter("it"));
        }

        if(cmp.containsParameter("od")) {
            params.outDelay = true;

            params.outDelayFrequency = Integer.parseInt(cmp.getParameter("of"));
            params.outDelayTime = Long.parseLong(cmp.getParameter("ot"));
        }

        if (cmp.containsParameter("lg")) {
        	params.logFilename = cmp.getParameter("lg");
        }

        if (cmp.containsParameter("a"))
        	params.logAppend = true;

        if(cmp.containsParameter("?") || cmp.containsParameter("help") ||
               params.host == null || params.localPort == 0 || params.remotePort == 0)
        {
            printUsageAndExit();
        }

        return params;
    }

    /**
     *  Prints the command line usage and exits the program.
     **/
    protected void printUsageAndExit()
    {
        PrintWriter out = new PrintWriter(System.out);

        out.print("PlugProxy version: " + VERSION + "\n\n");
        out.print("Usage: PlugProxy [-g] host rport lport [-v] [-s] [-h]\n");
        out.print("\nWhere options are:\n\n");
        out.print("\t-g");
        out.print("\trun the graphical user interface\n");
        out.print("\thost");
        out.print("\tset the host system\n");
        out.print("\trport");
        out.print("\tset the remote (forwarding) port\n");
        out.print("\tlport");
        out.print("\tset the local (listener) port\n");
        out.print("\t-v");
        out.print("\tset verbose mode\n");
        out.print("\t-s");
        out.print("\tsniff (display) the data passed through the proxy.\n");
        out.print("\t-h ");
        out.print("\tuse hexdump format when sniffing (ignored if -s not set)\n");
        out.print("\t-id ");
        out.print("\tEnabled Request Delay requires -if and -it\n");
        out.print("\t-if <f>");
        out.print("\tRequest Delay Frequency every <f>\n");
        out.print("\t-it <m>");
        out.print("\tRequest Delay Interval in <m> milliseconds\n");
        out.print("\t-od ");
        out.print("\tEnabled Response Delay requires -of and -ot\n");
        out.print("\t-of <f>");
        out.print("\tResponse Delay Frequency every <f>\n");
        out.print("\t-ot <m>");
        out.print("\tResponse Delay Interval in <m> milliseconds\n");
        out.print("\t-lg <f>");
        out.print("\tWrite proxy activity to file <f>\n");
        out.print("\t-a ");
        out.print("\tAppend proxy activity to existing log\n");
        out.print("\n");
        out.print("Example: \"PlugProxy www.javasoft.com 80 8080\"\n");
        out.print("Forwards data from port 8080 on the localhost to port 80 on www.javasoft.com");

        out.flush();
        out.close();

        System.exit(0);
    }

    /**
     *  Program entry point.  Creates the PlugProxy object and listens for connections.
     *  @param args A string array containing the command line arguments.
     **/
    public static void main(String[] args)
            throws IOException
    {
        if(args.length > 0 && args[0].equals("-g"))
        {
            String[] guiArgs = new String[args.length - 1];
            System.arraycopy(args, 1, guiArgs, 0, args.length - 1);
            new PlugProxyGui(guiArgs);
        }
        else new PlugProxy(args);
    }
}