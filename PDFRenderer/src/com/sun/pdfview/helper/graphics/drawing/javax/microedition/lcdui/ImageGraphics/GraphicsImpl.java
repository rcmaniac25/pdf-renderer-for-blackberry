/*
 * File: GraphicsImpl.java
 * Version: 1.0
 * Initial Creation: Jun 25, 2011 11:48:00 PM
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
package com.sun.pdfview.helper.graphics.drawing.javax.microedition.lcdui.ImageGraphics;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;

import net.rim.device.api.system.Bitmap;

import com.sun.pdfview.helper.AffineTransform;
import com.sun.pdfview.helper.PDFGraphics;
import com.sun.pdfview.helper.graphics.BasicStroke;
import com.sun.pdfview.helper.graphics.Composite;
import com.sun.pdfview.helper.graphics.Geometry;
import com.sun.pdfview.helper.graphics.Paint;

/**
 * PDFgraphics implementation of Image.
 */
public final class GraphicsImpl extends PDFGraphics
{
	private PDFGraphics gDest;
	private Graphics g;
	
	private Image destination;
	
	protected void onFinished()
	{
		PDFGraphics.finishGraphics(gDest);
	}
	
	protected void setDrawingDevice(Object device)
	{
		if(device == null)
		{
			throw new NullPointerException();
		}
		this.destination = (Image)device;
		
		this.g = this.destination.getGraphics();
		
		this.gDest = PDFGraphics.createGraphics(this.g);
	}
	
	public void clear(int x, int y, int width, int height)
	{
		gDest.clear(x, y, width, height);
	}
	
	public void draw(Geometry s)
	{
		gDest.draw(s);
	}
	
	public boolean drawImage(Bitmap img, AffineTransform xform)
	{
		return gDest.drawImage(img, xform);
	}
	
	public void fill(Geometry s)
	{
		gDest.fill(s);
	}
	
	public Geometry getClip()
	{
		return gDest.getClip();
	}
	
	public AffineTransform getTransform()
	{
		return gDest.getTransform();
	}
	
	public void setBackgroundColor(int c)
	{
		gDest.setBackgroundColor(c);
	}
	
	protected void setClip(Geometry s, boolean direct)
	{
		if(direct)
		{
			gDest.setClip(s);
		}
		else
		{
			gDest.clip(s);
		}
	}
	
	public void setColor(int c)
	{
		gDest.setColor(c);
	}
	
	public void setComposite(Composite comp)
	{
		gDest.setComposite(comp);
	}
	
	public void setPaint(Paint paint)
	{
		gDest.setPaint(paint);
	}
	
	public void setRenderingHint(int hintKey, int hintValue)
	{
		gDest.setRenderingHint(hintKey, hintValue);
	}
	
	public void setStroke(BasicStroke s)
	{
		gDest.setStroke(s);
	}
	
	protected void setTransform(AffineTransform Tx, boolean direct)
	{
		if(direct)
		{
			gDest.setTransform(Tx);
		}
		else
		{
			gDest.transform(Tx);
		}
	}
	
	public void translate(int x, int y)
	{
		gDest.translate(x, y);
	}
}
