//#preprocessor

/*
 * File: PDFDocCharsetEncoder.java
 * Version: 1.0
 * Initial Creation: May 10, 2010 4:09:10 PM
 *
 * Copyright 2008 Pirion Systems Pty Ltd, 139 Warry St,
 * Fortitude Valley, Queensland, Australia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.sun.pdfview;

//#ifndef BlackBerrySDK4.5.0 | BlackBerrySDK4.6.0 | BlackBerrySDK4.6.1 | BlackBerrySDK4.7.0 | BlackBerrySDK4.7.1
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
//#else
import com.sun.pdfview.helper.nio.ByteBuffer;
import com.sun.pdfview.helper.nio.ShortBuffer;
//#endif
import java.util.Hashtable;

import com.sun.pdfview.helper.CharsetEncoder;

/**
 * Encodes into a PDFDocEncoding representation. Note that only 256 characters
 * (if that) are represented in the PDFDocEncoding, so users should be
 * prepared to deal with unmappable character exceptions.
 *
 * @see "PDF Reference version 1.7, Appendix D"
 *
 * @author Luke Kirby
 */
public class PDFDocCharsetEncoder extends CharsetEncoder
{
	/**
     * Identify whether a particular character preserves the same byte value upon encoding in PDFDocEncoding
     * @param ch the character
     * @return whether the character is identity encoded
     */
    public static boolean isIdentityEncoding(char ch)
    {
        return ch >= 0 && ch <= 255 && IDENT_PDF_DOC_ENCODING_MAP[ch];
    }
    
    /**
     * For each character that exists in PDFDocEncoding, identifies whether
     * the byte value in UTF-16BE is the same as it is in PDFDocEncoding
     */
    final static boolean[] IDENT_PDF_DOC_ENCODING_MAP = new boolean[256];
    
    /**
     * For non-identity encoded characters, maps from the character to
     * the byte value in PDFDocEncoding. If an entry for a non-identity
     * coded character is absent from this map, that character is unmappable
     * in the PDFDocEncoding.
     */
    final static Hashtable EXTENDED_TO_PDF_DOC_ENCODING_MAP = new Hashtable();
    static
    {
    	int len = PDFStringUtil.PDF_DOC_ENCODING_MAP.length;
        for (int i = 0; i < len; ++i)
        {
            final short c = (short)PDFStringUtil.PDF_DOC_ENCODING_MAP[i];
            final boolean identical = (c == i);
            IDENT_PDF_DOC_ENCODING_MAP[i] = identical;
            if (!identical)
            {
                EXTENDED_TO_PDF_DOC_ENCODING_MAP.put(new Short(c), new Byte((byte)i));
            }
        }
    }
    
    public PDFDocCharsetEncoder()
    {
        super(null, 1, 1);
    }
	
	protected int encodeLoop(ShortBuffer in, ByteBuffer out)
	{
		while (in.remaining() > 0)
		{
            if (out.remaining() < 1)
            {
                return CharsetEncoder.RESULT_OVERFLOW;
            }
            final short c = in.get();
            if (c >= 0 && c < 256 && IDENT_PDF_DOC_ENCODING_MAP[c])
            {
                out.put((byte) c);
            }
            else
            {
                final Byte mapped = (Byte)EXTENDED_TO_PDF_DOC_ENCODING_MAP.get(new Short(c));
                if (mapped != null)
                {
                    out.put(mapped.byteValue());
                }
                else
                {
                    return CharsetEncoder.resultUnmappableForLength(1);
                }
            }
        }
        return CharsetEncoder.RESULT_UNDERFLOW;
	}
	
	public boolean isLegalReplacement(byte[] repl)
	{
        // avoid referencing the non-existent character set
        return true;
    }
}
