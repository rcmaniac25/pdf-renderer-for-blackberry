//#preprocessor

/*
 * File: CryptFilterDecrypter.java
 * Version: 1.0
 * Initial Creation: May 6, 2010 10:16:32 PM
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
package com.sun.pdfview.decrypt;

//#ifndef BlackBerrySDK4.5.0 | BlackBerrySDK4.6.0 | BlackBerrySDK4.6.1 | BlackBerrySDK4.7.0 | BlackBerrySDK4.7.1
import java.nio.ByteBuffer;
//#else
import com.sun.pdfview.helper.nio.ByteBuffer;
//#endif
import java.util.Enumeration;
import java.util.Hashtable;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParseException;
import com.sun.pdfview.helper.PDFUtil;

/**
 * Implements Version 4 standard decryption, whereby the Encrypt dictionary
 * contains a list of named 'crypt filters', each of which is the equivalent
 * of a {@link PDFDecrypter}. In addition to this list of crypt filters,
 * the name of the filter to use for streams and the default filter to use
 * for strings is specified. Requests to decode a stream with a named
 * decrypter (typically Identity) instead of the default decrypter
 * are honoured. 
 *
 * @author Luke Kirby
 */
public class CryptFilterDecrypter implements PDFDecrypter
{
	/** Maps from crypt filter names to their corresponding decrypters */
    private Hashtable decrypters;
    /** The default decrypter for stream content */
    private PDFDecrypter defaultStreamDecrypter;
    /** The default decrypter for string content */
    private PDFDecrypter defaultStringDecrypter;

    /**
     * Class constructor
     * @param decrypters a map of crypt filter names to their corresponding
     *  decrypters. Must already contain the Identity filter.
     * @param defaultStreamCryptName the crypt filter name of the default
     *  stream decrypter
     * @param defaultStringCryptName the crypt filter name of the default
     * string decrypter
     * @throws PDFParseException if one of the named defaults is not
     *  present in decrypters
     */
    public CryptFilterDecrypter(Hashtable decrypters, String defaultStreamCryptName, String defaultStringCryptName) throws PDFParseException
    {
        this.decrypters = decrypters;
        PDFUtil.assert(this.decrypters.containsKey("Identity"), "this.decrypters.containsKey(\"Identity\")", "Crypt Filter map does not contain required Identity filter");
        defaultStreamDecrypter = (PDFDecrypter)this.decrypters.get(defaultStreamCryptName);
        if (defaultStreamDecrypter == null)
        {
            throw new PDFParseException(com.sun.pdfview.ResourceManager.getResource(com.sun.pdfview.ResourceManager.LOCALIZATION).getFormattedString(com.sun.pdfview.i18n.ResourcesResource.DECRYPT_FILTER_UNK_CRYPT_STREAM, new Object[]{defaultStreamCryptName}));
        }
        defaultStringDecrypter = (PDFDecrypter)this.decrypters.get(defaultStringCryptName);
        if (defaultStringDecrypter == null)
        {
            throw new PDFParseException(com.sun.pdfview.ResourceManager.getResource(com.sun.pdfview.ResourceManager.LOCALIZATION).getFormattedString(com.sun.pdfview.i18n.ResourcesResource.DECRYPT_FILTER_UNK_CRYPT_STRING, new Object[]{defaultStringCryptName}));
        }
    }

    public ByteBuffer decryptBuffer(String cryptFilterName, PDFObject streamObj, ByteBuffer streamBuf) throws PDFParseException
    {
        final PDFDecrypter decrypter;
        if (cryptFilterName == null)
        {
            decrypter = defaultStreamDecrypter;
        }
        else
        {
            decrypter = (PDFDecrypter)decrypters.get(cryptFilterName);
            if (decrypter == null)
            {
                throw new PDFParseException(com.sun.pdfview.ResourceManager.getResource(com.sun.pdfview.ResourceManager.LOCALIZATION).getFormattedString(com.sun.pdfview.i18n.ResourcesResource.DECRYPT_FILTER_UNK_CRYPTFILTER, new Object[]{cryptFilterName}));
            }
        }
        return decrypter.decryptBuffer(
                // elide the filter name to prevent V2 decrypters from
                // complaining about a crypt filter name
                null,
                // if there's a specific crypt filter being used then objNum
                // and objGen shouldn't contribute to the key, so we
                // should make sure that no streamObj makes its way through
                cryptFilterName != null ? null : streamObj,
                streamBuf);
    }

    public String decryptString(int objNum, int objGen, String inputBasicString) throws PDFParseException
    {
        return defaultStringDecrypter.decryptString(objNum, objGen, inputBasicString);
    }

    public boolean isEncryptionPresent()
    {
    	for(Enumeration en = decrypters.elements(); en.hasMoreElements();)
        {
    		final PDFDecrypter decrypter = (PDFDecrypter)en.nextElement();
            if (decrypter.isEncryptionPresent())
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean isEncryptionPresent(String cryptFilterName)
    {
        PDFDecrypter decrypter = (PDFDecrypter)decrypters.get(cryptFilterName);
        return decrypter != null && decrypter.isEncryptionPresent(cryptFilterName);
    }

    public boolean isOwnerAuthorised()
    {
    	for(Enumeration en = decrypters.elements(); en.hasMoreElements();)
        {
    		final PDFDecrypter decrypter = (PDFDecrypter)en.nextElement();
            if (decrypter.isOwnerAuthorised())
            {
                return true;
            }
        }
        return false;
    }
}
