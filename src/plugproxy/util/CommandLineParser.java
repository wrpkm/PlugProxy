package plugproxy.util;

/**
 *   (c) Copyright 2001 - Christopher A. Longo
 *   =======================================
 *
 *   PlugProxy is free software; you can redistribute this file and/or modify it
 *   under the terms of the GNU General Public License as published by the Free
 *   Software Foundation; either version 2 of the License, or (at your option)
 *   any later version.
 */

import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 *  A simple command line parser.
 *
 *  @author Christopher A. Longo (cal@cloud9.net)
 *  @version 1.03 01.09.2001
 **/
public class CommandLineParser
{
    private Hashtable map;

    /**
     *  Constructor.  Take an array of String and hashes out the command line
     *  options from it.
     *  @param args - An array of Strings containing the command line arguments.
     **/
    public CommandLineParser(String[] args)
    {
        map = new Hashtable();
        parseArgs(args);
    }

    /**
     *  Returns a parameter passed on the command line.
     *  @param param The key for the parameter.
     *  @return A String containing the value of the command line parameter.
     **/
    public String getParameter(String param)
    {
        return (String) map.get(param);
    }

    /**
     *  Tests if a parameter was supplied on the command line.
     *  Good for testing switch parameters.
     *  @param name The key for the parameter.
     *  @return true if the parameter was supplied on the command
     *  line.
     **/
    public boolean containsParameter(String name)
    {
        return map.containsKey(name);
    }

    /**
     *  Returns a String representation of this object.
     *  @return a String representation of this object.
     **/
    public String toString()
    {
        return map.toString();
    }


    /**
     *  Parses the command line.  Will look for name:value pairs seperated by the
     *  characters ':' or '='.  Switches start with the characters '-' or '/'.
     *  @param args - An array of Strings containing the command line arguments.
     **/
    protected void parseArgs(String[] args)
    {
        for(int i = 0; i < args.length; i++)
        {
        	if (args[i].startsWith("-")) {
        		String name = null;
        		String value = null;

        		if (i < (args.length -1)  && !args[i + 1].startsWith("-"))
        		{
        		  name = args[i].replaceFirst("-", "");
        		  value = args[i + 1];
                  map.put(name, (value == null) ? "true" : value);
                  i++;
        		}
        		else
        		{
          		  name = args[i].replaceFirst("-", "");
                  map.put(name, "true");
        		}
        	}

//            StringTokenizer st = new StringTokenizer(args[i], "-/:= ");
//
//            while(st.hasMoreTokens())
//            {
//                String name = null;
//                String value = null;
//
//                try
//                {
//                    name = st.nextToken();
//                    value = st.nextToken();
//                }
//                catch(NoSuchElementException ignore)
//                {
//                }
//
//                map.put(name, (value == null) ? "true" : value);
//            }
        }
    }

    /**
     *  For testing purposes.
     **/
    public static void main(String[] args)
    {
        System.out.println(new CommandLineParser(args));
    }
}