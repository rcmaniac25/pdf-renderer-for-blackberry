/*
 * File: PDFCMap.java
 * Version: 1.3
 * Initial Creation: May 15, 2010 11:28:11 AM
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
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
package com.sun.pdfview.font;

import java.io.IOException;
import java.util.Hashtable;

import com.sun.pdfview.PDFObject;

/**
 * A CMap maps from a character in a composite font to a font/glyph number
 * pair in a CID font.
 *
 * @author  jkaplan
 */
public abstract class PDFCMap
{
	/**
     * A cache of known CMaps by name
     */
	private static final long CACHE_ID = 0x6C0B12C2A3CDAF6AL;
    private static Hashtable cache;
    
    /** Creates a new instance of CMap */
    protected PDFCMap() {}
    
    /**
     * Get a CMap, given a PDF object containing one of the following:
     *  a string name of a known CMap
     *  a stream containing a CMap definition
     */
    public static PDFCMap getCMap(PDFObject map) throws IOException
    {
        if (map.getType() == PDFObject.NAME)
        {
            return getCMap(map.getStringValue());
        }
        else if (map.getType() == PDFObject.STREAM)
        {
            return parseCMap(map);
        }
        else
        {
            throw new IOException(com.sun.pdfview.ResourceManager.getResource(com.sun.pdfview.ResourceManager.LOCALIZATION).getString(com.sun.pdfview.i18n.ResourcesResource.FONT_CMAP_NOT_NAME_OR_STREAM));
        }
    }
    
    /**
     * Get a CMap, given a string name
     */
    public static PDFCMap getCMap(String mapName) throws IOException
    {
        if (cache == null)
        {
        	cache = (Hashtable)com.sun.pdfview.ResourceManager.singletonStorageGet(CACHE_ID);
        	if (cache == null)
        	{
        		populateCache();
        		com.sun.pdfview.ResourceManager.singletonStorageSet(CACHE_ID, cache);
        	}
        }
        
        if (!cache.containsKey(mapName))
        {
            throw new IOException(com.sun.pdfview.ResourceManager.getResource(com.sun.pdfview.ResourceManager.LOCALIZATION).getFormattedString(com.sun.pdfview.i18n.ResourcesResource.FONT_CMAP_UNK_CMAP, new Object[]{mapName}));
        }
        
        return (PDFCMap)cache.get(mapName);
    }
    
    /**
     * Populate the cache with well-known types
     */
    protected static void populateCache()
    {
        cache = new Hashtable();
        
        // add the Identity-H map
        cache.put("Identity-H", new PDFCMap()
        {
            public char map(char src)
            {
                return src;
            }
        });
    }
    
    /**
     * Parse a CMap from a CMap stream
     */
    protected static PDFCMap parseCMap(PDFObject map) throws IOException
    {
        throw new IOException(com.sun.pdfview.ResourceManager.getResource(com.sun.pdfview.ResourceManager.LOCALIZATION).getString(com.sun.pdfview.i18n.ResourcesResource.FONT_CMAP_UNSUPPORTED_PARSE));
    }
    
    /**
     * Map a given source character to a destination character
     */
    public abstract char map(char src);
    
    /**
     * Get the font number assoicated with a given source character
     */
    public int getFontID(char src)
    {
        return 0;
    }
}
