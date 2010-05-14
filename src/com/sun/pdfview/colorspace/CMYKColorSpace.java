/*
 * File: CMYKColorSpace.java
 * Version: 1.2
 * Initial Creation: May 13, 2010 5:18:36 PM
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

import com.sun.pdfview.helper.ColorSpace;

/**
 * A ColorSpace for the CMYK color space.
 *
 * @author Mike Wessler
 */
public class CMYKColorSpace extends ColorSpace
{
	/**
     * create a new CMYK color space:  a ColorSpace with 4 components
     */
    public CMYKColorSpace()
    {
    	super(TYPE_CMYK, 4);
    }
    
    /**
     * Convert from CIEXYZ to RGB.  NOT IMPLEMENTED
     */    
    public float[] fromCIEXYZ(float[] colorvalue)
    {
    	return new float[3];
    }
    
    /**
     * Convert from RGB to CMYK.
     * @param rgbvalue the red, green, and blue values (0-1)
     * @return the CMYK values (0-1)
     */
    public float[] fromRGB(float[] rgbvalue)
    {
		float[] color = new float[4];
		float c = 1 - rgbvalue[0];
		float m = 1 - rgbvalue[1];
		float y = 1 - rgbvalue[2];
		float k = Math.min(c, Math.min(m, y));
		float ik = 1 - k;
		if (ik == 0)
		{
		    c = 1;
		    m = 1;
		    y = 1;
		}
		else
		{
		    c = (c - k) / ik;
		    m = (m - k) / ik;
		    y = (y - k) / ik;
		}
		color[0] = c;
		color[1] = m;
		color[2] = y;
		color[3] = k;
		return color;
    }

    /**
     * the number of components
     */
    public int getNumComponents()
    {
    	return 4;
    }
    
    /**
     * the name of this color space
     */
    public String getName(int idx)
    {
    	return "CMYK";
    }
    
    /**
     * the type of this color space (TYPE_CMYK)
     */
    public int getType()
    {
    	return TYPE_CMYK;
    }
    
    /**
     * Convert from CMYK to CIEXYZ.  NOT IMPLEMENTED
     */
    public float[] toCIEXYZ(float[] colorvalue)
    {
    	return new float[3];
    }
    
    /**
     * Convert from CMYK to RGB.
     * @param colorvalue the CMYK values (0-1)
     * @return the RGB values (0-1)
     */
    public float[] toRGB(float[] colorvalue)
    {
		if (colorvalue.length == 4)
		{
		    float[] color = new float[3];
		    float k = colorvalue[3];
		    float ik = 1 - k;
		    color[0] = (1 - (colorvalue[0] * ik + k));
		    color[1] = (1 - (colorvalue[1] * ik + k));
		    color[2] = (1 - (colorvalue[2] * ik + k));
		    return color;
		}
		else
		{
		    return new float[3];
		}
    }
}
