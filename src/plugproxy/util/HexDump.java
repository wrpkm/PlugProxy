package plugproxy.util;

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
 *  Simple class for dumping a buffer in hexdump format.
 */
public final class HexDump
{
    /**
     *  Returns a string containing a hexdump of the supplied buffer.
     *  @param buffer The buffer to base the hexdump on.
     *  @param size The size of the supplied buffer.
     *  @return A string containing a hexdump of the supplied buffer.
     */
    public static String toHex(byte[] buffer, int size)
    {
        StringBuffer out = new StringBuffer(size * 80);
        int j = 0;

        for(int i = 0; i < size; i += 16)
        {
            for(j = 0; j < 16; j++)
            {
                if(i + j < size)
                {
                    out.append(convert(buffer[i + j]));
                    out.append((j == 7 && (i + j != size - 1)) ? '-' : ' ');
                }
                else out.append("   ");
            }

            out.append("  ");

            for(j = 0; j < 16; j++)
            {
                char ch = (char)buffer[i + j];

                if(i + j < size)
                    out.append((ch > 32 && ch < 128) ? ch : '.');
                else
                    out.append(' ');
            }

            out.append("\n");
        }

        return out.toString();
    }

    /**
     *  Convert a byte into a hex digit string.
     *  @param b The byte to convert.
     *  @return A string representing the supplied byte in 2 character hex format.
     */
    private static String convert(byte b)
    {
        int value = b & ~0xffffff00;

        String s = Integer.toHexString(value).toUpperCase();

        if(value <= 0x0f)
            s = '0' + s;

        return s;
    }
}