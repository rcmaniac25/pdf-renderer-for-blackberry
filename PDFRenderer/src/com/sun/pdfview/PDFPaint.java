/*
 * File: PDFPaint.java
 * Version: 1.4
 * Initial Creation: May 12, 2010 9:18:34 PM
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
package com.sun.pdfview;

import com.sun.pdfview.helper.PDFGraphics;
import com.sun.pdfview.helper.XYRectFloat;
import com.sun.pdfview.helper.graphics.Geometry;
import com.sun.pdfview.helper.graphics.Paint;

/**
 * PDFPaint is some kind of shader that knows how to fill a path.
 * At the moment, only a solid color is implemented, but gradients
 * and textures should be possible, too.
 * @author Mike Wessler
 */
public class PDFPaint
{
	private Paint mainPaint;
	
    /**
     * create a new PDFPaint based on a solid color
     */
    protected PDFPaint(Paint p)
    {
        this.mainPaint = p;
    }
    
    /**
     * get the PDFPaint representing a solid color
     */
    public static PDFPaint getColorPaint(int c)
    {
        return getPaint(Paint.getInstance(c));
    }
    
    /**
     * get the PDFPaint representing a generic paint
     */
    public static PDFPaint getPaint(Paint p)
    {
        return new PDFPaint(p);
    }
    
    /**
     * fill a path with the paint, and record the dirty area.
     * @param state the current graphics state
     * @param g the graphics into which to draw
     * @param s the path to fill
     */
    public XYRectFloat fill(PDFRenderer state, PDFGraphics g, Geometry s)
    {
        g.setPaint(mainPaint);
        g.fill(s);
        
        return s.createTransformedShape(g.getTransform()).getBounds2D();
    }
    
    /**
     * get the primary color associated with this PDFPaint.
     */
    public Paint getPaint()
    {
        return mainPaint;
    }
}
