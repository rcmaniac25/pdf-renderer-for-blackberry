/*
 * File: XYRectFloat.java
 * Version: 1.0
 * Initial Creation: May 6, 2010 6:05:40 PM
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
package com.sun.pdfview.helper;

/**
 * Simple rectangle like {@link net.rim.device.api.ui.XYRect XYRect} only it store floating point values.
 * @author Vincent Simonetti
 */
public class XYRectFloat
{
	//Only has what is necessary
	
	/** Height of rect. */
	public double height;
	/** Width of rect. */
	public double width;
	/** Horizontal position of top left point. */
	public double x;
	/** Vertical position of top left point. */
	public double y;
	
	/**
	 * Constructs a new XYRectFloat instance and sets all fields to 0.
	 */
	public XYRectFloat()
	{
		this(0, 0, 0, 0);
	}
	
	/**
	 * Constructs a specified rectangle.
	 * @param x Horizontal location of rectangle's origin.
	 * @param y Vertical location of rectangle's origin.
	 * @param width Distance in pixels to the right edge of the rectangle.
	 * @param height Distance in pixels to the bottom edge of the rectangle.
	 */
	public XYRectFloat(double x, double y, double width, double height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Get the right edge of the rectangle.
	 */
	public double X2()
	{
		return x + width;
	}
	
	/**
	 * Get the bottom edge of the rectangle.
	 */
	public double Y2()
	{
		return y + height;
	}
	
	/**
	 * Determines whether the XYRectFloat is empty.
	 * @return true if the XYRectFloat is empty; false otherwise.
	 */
	public boolean isEmpty()
	{
		return this.width == 0 && this.height == 0;
	}
	
	/**
	 * Returns a string representation of the object.
	 * @return A string representation of the object.
	 */
	public String toString()
	{
		StringBuffer buffer = new StringBuffer("{X = ");
		buffer.append(this.x);
		buffer.append(", Y = ");
		buffer.append(this.y);
		buffer.append(", Width = ");
		buffer.append(this.width);
		buffer.append(", Height = ");
		buffer.append(this.height);
		buffer.append('}');
		return buffer.toString();
	}
}
