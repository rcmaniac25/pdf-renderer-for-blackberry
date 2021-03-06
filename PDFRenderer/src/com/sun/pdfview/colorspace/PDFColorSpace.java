/*
 * File: PDFColorSpace.java
 * Version: 1.5
 * Initial Creation: May 12, 2010 7:09:37 PM
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
package com.sun.pdfview.colorspace;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFPaint;
import com.sun.pdfview.PDFParseException;
import com.sun.pdfview.ResourceManager;
import com.sun.pdfview.function.PDFFunction;
import com.sun.pdfview.helper.ColorSpace;
import com.sun.pdfview.helper.PDFUtil;
import com.sun.pdfview.helper.graphics.color.ICC_ColorSpace;
import com.sun.pdfview.helper.graphics.color.ICC_Profile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.util.Hashtable;;

/**
 * A color space that can convert a set of color components into
 * PDFPaint.
 * @author Mike Wessler
 */
public class PDFColorSpace
{
	/** the name of the device-dependent gray color space */
    public static final int COLORSPACE_GRAY = 0;
    
    /** the name of the device-dependent RGB color space */
    public static final int COLORSPACE_RGB = 1;
    
    /** the name of the device-dependent CMYK color space */
    public static final int COLORSPACE_CMYK = 2;
    
    /** the name of the pattern color space */
    public static final int COLORSPACE_PATTERN = 3;
    
    /** the device-dependent color spaces */
    private static final long PDF_CS_GRAY_ID = 0x4A042B32B235E300L;
    private static final long PDF_CS_RGB_ID = 0x4CB4C8A6537D5C18L;
    private static final long PDF_CS_CMYK_ID = 0x8ED55C5DB58272C0L;
    private static final long PDF_CS_YCCK_ID = 0x63454268D512235CL;
    //private static PDFColorSpace graySpace;
    private static PDFColorSpace rgbSpace;
    private static PDFColorSpace cmykSpace;
    private static PDFColorSpace ycckSpace;
    
    /** the pattern space */
    private static final long PDF_PATTERY_SPACE_ID = 0x22CCE51F780E6E80L;
    private static PDFColorSpace patternSpace;
    
    /** graySpace and the gamma correction for it. */
    private static PDFColorSpace graySpace;
    
    static
    {
    	/*
    	graySpace = (PDFColorSpace)ResourceManager.singletonStorageGet(PDF_CS_GRAY_ID);
    	if(graySpace == null)
    	{
    		graySpace = new PDFColorSpace(ColorSpace.getInstance(ColorSpace.CS_GRAY));
    		ResourceManager.singletonStorageSet(PDF_CS_GRAY_ID, graySpace);
    	}
    	*/
    	
    	rgbSpace = (PDFColorSpace)ResourceManager.singletonStorageGet(PDF_CS_RGB_ID);
    	if(rgbSpace == null)
    	{
    		rgbSpace = new PDFColorSpace(ColorSpace.getInstance(ColorSpace.CS_sRGB));
    		ResourceManager.singletonStorageSet(PDF_CS_RGB_ID, rgbSpace);
    	}
    	
    	cmykSpace = (PDFColorSpace)ResourceManager.singletonStorageGet(PDF_CS_CMYK_ID);
    	if(cmykSpace == null)
    	{
    		cmykSpace = new PDFColorSpace(new CMYKColorSpace());
    		ResourceManager.singletonStorageSet(PDF_CS_CMYK_ID, cmykSpace);
    	}
    	
    	ycckSpace = (PDFColorSpace)ResourceManager.singletonStorageGet(PDF_CS_YCCK_ID);
    	if(ycckSpace == null)
    	{
    		ycckSpace = new PDFColorSpace(new YCCKColorSpace());
    		ResourceManager.singletonStorageSet(PDF_CS_YCCK_ID, ycckSpace);
    	}
    	
    	patternSpace = (PDFColorSpace)ResourceManager.singletonStorageGet(PDF_PATTERY_SPACE_ID);
    	if(patternSpace == null)
    	{
    		patternSpace = new PatternSpace();
    		ResourceManager.singletonStorageSet(PDF_PATTERY_SPACE_ID, patternSpace);
    	}
    	
        boolean useSGray = true;
        
        graySpace = (PDFColorSpace)ResourceManager.singletonStorageGet(PDF_CS_GRAY_ID);
        if(graySpace == null)
        {
	        try
	        {
	            graySpace = new PDFColorSpace((!useSGray) ? ColorSpace.getInstance(ColorSpace.CS_GRAY) : new ICC_ColorSpace(ICC_Profile.getInstance(ResourceManager.getResource("colorspace").getStream("sGray.icc"))));
	            ResourceManager.singletonStorageSet(PDF_CS_GRAY_ID, graySpace);
	        }
	        catch (Exception e)
	        {
	            throw new RuntimeException();
	        }
        }
    }
    
    /** the color space */
    ColorSpace cs;
    
    /**
     * create a PDFColorSpace based on a Java ColorSpace
     * @param cs the Java ColorSpace
     */
    public PDFColorSpace(ColorSpace cs)
    {
        this.cs = cs;
    }
    
    /**
     * Get a color space by name
     *
     * @param name the name of one of the device-dependent color spaces
     */
    public static PDFColorSpace getColorSpace(int name)
    {
        switch (name) {
        case COLORSPACE_GRAY:
            return graySpace;
            
        case COLORSPACE_RGB:
            return rgbSpace;
            
        case COLORSPACE_CMYK:
            return cmykSpace;
            
        case COLORSPACE_PATTERN:
            return patternSpace;
            
        default:
            throw new IllegalArgumentException(com.sun.pdfview.ResourceManager.getResource(com.sun.pdfview.ResourceManager.LOCALIZATION).getFormattedString(com.sun.pdfview.i18n.ResourcesResource.COLORSPACE_UNK_COLORSPACE_NAME, new Object[]{new Integer(name)}));
        }
    }
    
    /**
     * Get a color space specified in a PDFObject
     *
     * @param csobj the PDFObject with the colorspace information
     */
    public static PDFColorSpace getColorSpace(PDFObject csobj, Hashtable resources) throws IOException
    {
        String name;
        
        PDFObject colorSpaces = null;
        
        if (resources != null)
        {
            colorSpaces = (PDFObject)resources.get("ColorSpace");
        }
        
        if (csobj.getType() == PDFObject.NAME)
        {
            name = csobj.getStringValue();
            
            if (name.equals("DeviceGray") || name.equals("G"))
            {
                return getColorSpace(COLORSPACE_GRAY);
            }
            else if (name.equals("DeviceRGB") || name.equals("RGB"))
            {
                return getColorSpace(COLORSPACE_RGB);
            }
            else if (name.equals("DeviceCMYK") || name.equals("CMYK"))
            {
                return getColorSpace(COLORSPACE_CMYK);
            }
            else if (name.equals("Pattern"))
            {
                return getColorSpace(COLORSPACE_PATTERN);
            }
            else if (colorSpaces != null)
            {
                csobj = (PDFObject)colorSpaces.getDictRef(name);
            }
        }
        
        if (csobj == null)
        {
            return null;
        }
        else if (csobj.getCache() != null)
        {
            return (PDFColorSpace)csobj.getCache();
        }
        
        PDFColorSpace value = null;
        
        // csobj is [/name <<dict>>]
        PDFObject[] ary = csobj.getArray();
        name = ary[0].getStringValue();
        
        if (name.equals("CalGray"))
        {
            value = new PDFColorSpace(new CalGrayColor(ary[1]));
        }
        else if (name.equals("CalRGB"))
        {
            value = new PDFColorSpace(new CalRGBColor(ary[1]));
        }
        else if (name.equals("Lab"))
        {
            value = new PDFColorSpace(new LabColor(ary[1]));
        }
        else if (name.equals("ICCBased"))
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(ary[1].getStream());
            ICC_Profile profile = ICC_Profile.getInstance(bais);
            value = new PDFColorSpace(new ICC_ColorSpace(profile));
            bais.close();
        }
        else if (name.equals("Separation") || name.equals("DeviceN"))
        {
            PDFColorSpace alternate = getColorSpace(ary[2], resources);
            PDFFunction function = PDFFunction.getFunction(ary[3]);
            
            value = new AlternateColorSpace(alternate, function);
        }
        else if (name.equals("Indexed") || name.equals("I"))
        {
            /**
             * 4.5.5 [/Indexed baseColor hival lookup]
             */
            PDFColorSpace refspace = getColorSpace(ary[1], resources);
            
            // number of indices= ary[2], data is in ary[3];
            int count = ary[2].getIntValue();
            value = new IndexedColor(refspace, count, ary[3]);
        }
        else if (name.equals("Pattern"))
        {
            if (ary.length == 1)
            {
                return getColorSpace(COLORSPACE_PATTERN);
            }
            
            PDFColorSpace base = getColorSpace(ary[1], resources);
            
            return new PatternSpace(base);
        }
        else
        {
            throw new PDFParseException(com.sun.pdfview.ResourceManager.getResource(com.sun.pdfview.ResourceManager.LOCALIZATION).getFormattedString(com.sun.pdfview.i18n.ResourcesResource.COLORSPACE_UNK_COLORSPACE_NAME_WITH_ARR, new Object[]{name, ary[1].toString()}));
        }
        
        csobj.setCache(value);
        
        return value;
    }
    
    /**
     * get the number of components expected in the getPaint command
     */
    public int getNumComponents()
    {
        return cs.getNumComponents();
    }
    
    /**
     * get the PDFPaint representing the color described by the
     * given color components
     * @param components the color components corresponding to the given
     * colorspace
     * @return a PDFPaint object representing the closest Color to the
     * given components.
     */
    public PDFPaint getPaint(float[] components)
    {
        float[] rgb = cs.toRGB(components);
        
        return PDFPaint.getColorPaint(PDFUtil.createColor(rgb[0], rgb[1], rgb[2]));
    }
    
    /**
     * get the original Java ColorSpace.
     */
    public ColorSpace getColorSpace()
    {
        return cs;
    }
}
