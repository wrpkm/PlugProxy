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

/**
 *  Simple structure class for holding parameters.
 */
public final class Parameters
{
    /** the remote host **/
    public String host;
    /** the remote host's port where data is forwarded to **/
    public int remotePort;
    /** the local host's port where connections are established **/
    public int localPort;
    /** verbose mode flag **/
    public boolean verbose;
    /** sniff mode flag **/
    public boolean sniff;
    /** hex sniff mode flag **/
    public boolean hex;

    public boolean outDelay;
    public int outDelayFrequency;
    public long outDelayTime;

    public boolean inDelay;
    public int inDelayFrequency;
    public long inDelayTime;

    public String logFilename;
    public boolean logAppend;
}