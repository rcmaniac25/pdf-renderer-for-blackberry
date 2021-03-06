/*
 * File: PDFShapeCmd.java
 * Version: 1.3
 * Initial Creation: May 14, 2010 12:50:17 PM
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

import com.sun.pdfview.helper.AffineTransform;
import com.sun.pdfview.helper.PDFUtil;
import com.sun.pdfview.helper.XYRectFloat;
import com.sun.pdfview.helper.graphics.BasicStroke;
import com.sun.pdfview.helper.graphics.Geometry;

/**
 * Encapsulates a path.  Also contains extra fields and logic to check
 * for consecutive abutting anti-aliased regions.  We stroke the shared
 * line between these regions again with a 1-pixel wide line so that
 * the background doesn't show through between them.
 *
 * @author Mike Wessler
 */
public class PDFShapeCmd extends PDFCmd
{
	/** stroke the outline of the path with the stroke paint */
    public static final int STROKE = 1;
    /** fill the path with the fill paint */
    public static final int FILL = 2;
    /** perform both stroke and fill */
    public static final int BOTH = 3;
    /** set the clip region to the path */
    public static final int CLIP = 4;
    /** base path */
    private Geometry gp;
    /** the style */
    private int style;
    /** the bounding box of the path */
    private XYRectFloat bounds;
    /** the stroke style for the anti-antialias stroke */
    BasicStroke againstroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    
    /**
     * create a new PDFShapeCmd and check it against the previous one
     * to find any shared edges.
     * @param gp the path
     * @param style the style: an OR of STROKE, FILL, or CLIP.  As a
     * convenience, BOTH = STROKE | FILL.
     */
    public PDFShapeCmd(Geometry gp, int style)
    {
        this.gp = new Geometry(gp);
        this.style = style;
        bounds = gp.getBounds2D();
    }
    
    /**
     * perform the stroke and record the dirty region
     */
    public XYRectFloat execute(PDFRenderer state)
    {
    	XYRectFloat rect = null;
        
        if ((style & FILL) != 0)
        {
            rect = state.fill(gp);
            
            Geometry strokeagain = checkOverlap(state);
            if (strokeagain != null)
            {
                state.draw(strokeagain, againstroke);
            }
            
            if (gp != null)
            {
                state.setLastShape(gp);
            }
        }
        if ((style & STROKE) != 0)
        {
            XYRectFloat strokeRect = state.stroke(gp);
            if (rect == null)
            {
                rect = strokeRect;
            }
            else
            {
            	PDFUtil.union(strokeRect, rect, rect);
            }
        }
        if ((style & CLIP) != 0)
        {
            state.clip(gp);
        }
        
        return rect;
    }
    
    /**
     * Check for overlap with the previous shape to make anti-aliased shapes
     * that are near each other look good
     */
    private Geometry checkOverlap(PDFRenderer state)
    {
        if (style == FILL && gp != null && state.getLastShape() != null)
        {
            float mypoints[] = new float[16];
            float prevpoints[] = new float[16];
            
            int mycount = getPoints(gp, mypoints);
            int prevcount = getPoints(state.getLastShape(), prevpoints);
            
            // now check mypoints against prevpoints for opposite pairs:
            if (mypoints != null && prevpoints != null)
            {
                for (int i = 0; i < prevcount; i += 4)
                {
                    for (int j = 0; j < mycount; j += 4)
                    {
                        if ((Math.abs(mypoints[j + 2] - prevpoints[i]) < 0.01 && Math.abs(mypoints[j + 3] - prevpoints[i + 1]) < 0.01 && 
                        		Math.abs(mypoints[j] - prevpoints[i + 2]) < 0.01 && Math.abs(mypoints[j + 1] - prevpoints[i + 3]) < 0.01))
                        {
                        	Geometry strokeagain = new Geometry();
                            strokeagain.moveTo(mypoints[j], mypoints[j + 1]);
                            strokeagain.lineTo(mypoints[j + 2], mypoints[j + 3]);
                            return strokeagain;
                        }
                    }
                }
            }
        }
        
        // no issues
        return null;
    }
    
    /**
     * Get an array of 16 points from a path
     * @return the number of points we actually got
     */
    private int getPoints(Geometry path, float[] mypoints)
    {
        int count = 0;
        float x = 0;
        float y = 0;
        float startx = 0;
        float starty = 0;
        float[] coords = new float[6];
        
        Geometry.Enumeration pi = path.getPathEnumerator(new AffineTransform());
        while (!pi.isDone())
        {
            if (count >= mypoints.length)
            {
                mypoints = null;
                break;
            }
            
            int pathtype = pi.currentSegment(coords);
            switch (pathtype)
            {
                case Geometry.Enumeration.SEG_MOVETO:
                    startx = x = coords[0];
                    starty = y = coords[1];
                    break;
                case Geometry.Enumeration.SEG_LINETO:
                    mypoints[count++] = x;
                    mypoints[count++] = y;
                    x = mypoints[count++] = coords[0];
                    y = mypoints[count++] = coords[1];
                    break;
                case Geometry.Enumeration.SEG_QUADTO:
                    x = coords[2];
                    y = coords[3];
                    break;
                case Geometry.Enumeration.SEG_CUBICTO:
                    x = mypoints[4];
                    y = mypoints[5];
                    break;
                case Geometry.Enumeration.SEG_CLOSE:
                    mypoints[count++] = x;
                    mypoints[count++] = y;
                    x = mypoints[count++] = startx;
                    y = mypoints[count++] = starty;
                    break;
            }
            
            pi.next();
        }
        
        return count;
    }
    
    /** Get detailed information about this shape
     */
    public String getDetails()
    {
        StringBuffer sb = new StringBuffer();
        
        XYRectFloat b = gp.getBounds2D();
        sb.append("ShapeCommand at: " + b.x + ", " + b.y + "\n");
        sb.append("Size: " + b.width + " x " + b.height + "\n");
        
        sb.append("Mode: ");
        if ((style & FILL) != 0)
        {
            sb.append("FILL ");
        }
        if ((style & STROKE) != 0)
        {
            sb.append("STROKE ");
        }
        if ((style & CLIP) != 0)
        {
            sb.append("CLIP");
        }
        
        return sb.toString();
    }
}
