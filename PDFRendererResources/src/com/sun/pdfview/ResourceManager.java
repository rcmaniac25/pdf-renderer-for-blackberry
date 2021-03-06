//#preprocessor

/*
 * File: ResourceManager.java
 * Version: 1.0
 * Initial Creation: Jun 30, 2010 2:21:31 PM
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

import java.io.InputStream;
import java.util.Vector;

import net.rim.device.api.system.RuntimeStore;
//#ifndef BlackBerrySDK4.5.0
import net.rim.device.api.util.LongVector;
//#endif

/**
 * Resource manager for PDF Renderer.
 * @author Vincent Simonetti
 */
public final class ResourceManager
{
	private static final long SINGLETON_STORAGE_ID = 0x67C95E44C4049000L;
	private static final long RESOURCE_CACHE_ID = 0x2F3B9A41ADBB0DC2L;
	
	public static String LOCALIZATION = "i18n";
	
	//There are some "extras" that are coded but not used, thus they can be removed if the developer desires. They are there in case anyone wants to make their own extensions.
	
	/**
	 * Get a Resources object that represents resources.
	 * @param name The name of the resource to get. This is a relative namespace of where the resources are located. So if the resource is located in 
	 * "com.sun.pdfview.myresource.res" then this should be "myresource.res".
	 * @return The resource system.
	 */
	public static Resources getResource(String name)
	{
		Vector resourceCache;
		Object obj;
		if((obj = ResourceManager.singletonStorageGet(RESOURCE_CACHE_ID)) != null)
		{
			resourceCache = (Vector)obj;
		}
		else
		{
			resourceCache = new Vector();
			ResourceManager.singletonStorageSet(RESOURCE_CACHE_ID, resourceCache);
		}
		
		int c = resourceCache.size();
		for(int i = 0; i < c; i++)
		{
			Resources res = (Resources)resourceCache.elementAt(i);
			if(res.getName().equals(name.replace('.', '/')))
			{
				//Found it
				return res;
			}
		}
		
		boolean abs = name.startsWith("/");
		
		try
		{
			//See if a specific type exists for this resource
			Class clazz = Class.forName((abs ? name.substring(1) : "com.sun.pdfview." + name) + ".ResourceImpl");
			obj = clazz.newInstance();
			if(obj instanceof Resources)
			{
				resourceCache.addElement(obj);
				return (Resources)obj;
			}
		}
		catch(Exception e)
		{
		}
		
		//Create default resource implementation since no specific or cached version exists
		Resources res = new ResourcesDefaultImpl(name, abs);
		resourceCache.addElement(res);
		return res;
	}
	
	/**
	 * Get a singleton object. This is not the same as just calling RuntimeStore and is managed for memory usage. It will be cleaned up when 
	 * {@link #singltonStorageCleanup()} is called.
	 * @param uid The ID of the object to get.
	 * @return The object (if it was set using {@link #singletonStorageSet(long, Object)}) or null if it doesn't exist or was not set using {@link #singletonStorageSet(long, Object)}.
	 */
	public synchronized static Object singletonStorageGet(long uid)
	{
		RuntimeStore store = RuntimeStore.getRuntimeStore();
		Object obj;
		if((obj = store.get(SINGLETON_STORAGE_ID)) != null)
		{
//#ifndef BlackBerrySDK4.5.0
			LongVector v = (LongVector)obj;
			if(v.contains(uid))
			{
				return store.get(uid);
			}
//#else
			long[] v = (long[])obj;
			int len = v.length;
			boolean hasIndex = false;
			for(int i = 1; i < len; i++)
			{
				if(v[i] == uid)
				{
					hasIndex = true;
					break;
				}
			}
			if(hasIndex)
			{
				return store.get(uid);
			}
//#endif
		}
		return null;
	}
	
	/**
	 * Set a singleton object.
	 * @param uid The ID of the object to set. If this happens to be an object that already exists but was not set using this function then an exception will be thrown.
	 * @param obj The singleton object to set or null if the current object should be removed.
	 * @return The previous object (if it was set using {@link #singletonStorageSet(long, Object)}) or null if it didn't exist or was not set using {@link #singletonStorageSet(long, Object)}.
	 */
	public synchronized static Object singletonStorageSet(long uid, Object obj)
	{
		RuntimeStore store = RuntimeStore.getRuntimeStore();
		Object objS;
//#ifndef BlackBerrySDK4.5.0
		LongVector v;
//#else
		long[] v;
//#endif
		if((objS = store.get(SINGLETON_STORAGE_ID)) != null) //Singleton list exists
		{
//#ifndef BlackBerrySDK4.5.0
			v = (LongVector)objS;
			if(v.contains(uid))
			{
				objS = store.get(uid); //Get previous value
				if(obj != null)
				{
					store.replace(uid, obj); //Replace the current object
				}
				else
				{
					store.remove(uid); //Remove the object
					v.removeElement(uid);
				}
				return objS; //Return previous object
			}
			else if(obj != null) //Does not exist in Singleton list exists, new
			{
				store.put(uid, obj);
				v.addElement(uid);
				return null;
			}
//#else
			v = (long[])objS;
			int len = v.length;
			int index = -1;
			for(int i = 1; i < len; i++)
			{
				if(v[i] == uid)
				{
					index = i;
					break;
				}
			}
			if(index >= 1)
			{
				objS = store.get(uid); //Get previous value
				if(obj != null)
				{
					store.replace(uid, obj); //Replace the current object
				}
				else
				{
					store.remove(uid); //Remove the object
					System.arraycopy(v, index + 1, v, index, (int)((--v[0]) - index));
				}
				return objS; //Return previous object
			}
			else if(obj != null) //Does not exist in Singleton list exists, new
			{
				store.put(uid, obj);
				if(v[0] >= v.length)
				{
					long[] t = new long[v.length * 2];
					System.arraycopy(v, 0, t, 0, v.length);
					v = t;
					store.replace(SINGLETON_STORAGE_ID, v);
				}
				v[(int)(v[0]++)] = uid;
				return null;
			}
//#endif
		}
		if(obj != null) //If the function hasn't returned yet and the object is not null then the Singleton list doesn't exist yet
		{
//#ifndef BlackBerrySDK4.5.0
			v = new LongVector(); //Create the list and add the object
//#else
			v = new long[1 + 4];
			v[0] = 1;
//#endif
			store.put(SINGLETON_STORAGE_ID, v);
			store.put(uid, obj); //Will throw an exception if already there
//#ifndef BlackBerrySDK4.5.0
			v.addElement(uid);
//#else
			v[(int)(v[0]++)] = uid;
//#endif
		}
		return null;
	}
	
	/**
	 * Remove all singleton objects.
	 */
	public synchronized static void singltonStorageCleanup()
	{
		RuntimeStore store = RuntimeStore.getRuntimeStore();
		Object obj;
		if((obj = store.get(SINGLETON_STORAGE_ID)) != null)
		{
//#ifndef BlackBerrySDK4.5.0
			LongVector v = (LongVector)obj;
			store.remove(SINGLETON_STORAGE_ID);
			int len = v.size();
			for(int i = 0; i < len; i++)
			{
				store.remove(v.elementAt(i));
			}
//#else
			long[] v = (long[])obj;
			store.remove(SINGLETON_STORAGE_ID);
			int len = v.length;
			for(int i = 1; i < len; i++)
			{
				store.remove(v[i]);
			}
//#endif
		}
	}
	
	private static class ResourcesDefaultImpl implements Resources
	{
		private static String ORG_PATH = "/com/sun/pdfview/";
		
		private String path;
		private int index;
		private boolean ending;
		private net.rim.device.api.i18n.ResourceBundle resources;
		
		public ResourcesDefaultImpl(String relPath, boolean abs)
		{
			this.path = abs ? relPath : ORG_PATH + relPath;
			this.index = abs ? 0 : ORG_PATH.length();
			if((!relPath.endsWith(".")) || (!relPath.endsWith("/")))
			{
				this.path += "/";
				this.ending = true;
			}
			this.path = this.path.replace('.', '/');
		}
		
		public String getName()
		{
			String name = this.path.substring(this.index);
			if(ending)
			{
				name = name.substring(0, name.length() - 1);
			}
			return name;
		}
		
		public InputStream getStream(String name)
		{
			return getClass().getResourceAsStream(path + name);
		}
		
		private boolean setupResources()
		{
			if(this.resources == null)
			{
				String name = path + "Resources";
				if(name.startsWith("/"))
				{
					name = name.substring(1);
				}
				name = name.replace('/', '.');
				this.resources = net.rim.device.api.i18n.ResourceBundle.getBundle(name);
			}
			return this.resources != null;
		}
		
		public String getString(long ID)
		{
			if(setupResources())
			{
				return this.resources.getString((int)ID);
			}
			throw new UnsupportedOperationException();
		}
		
		public String[] getStringArray(long ID)
		{
			if(setupResources())
			{
				return this.resources.getStringArray((int)ID);
			}
			throw new UnsupportedOperationException();
		}
		
		//Formatting system uses built in printf functionality from LCMS (this is the only thing that Resources relies on)
		public String getFormattedString(long ID, Object[] args)
		{
			StringBuffer buffer = new StringBuffer(256);
			int len = littlecms.internal.helper.Utility.sprintf(buffer, getString(ID), args);
			return buffer.deleteCharAt(len).toString(); //Get rid of the null-char and return
		}
	}
}
