/*
Copyright (C) 2010-2012 Hugues Johnson

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package com.onemorecastle.util;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public abstract class ImageCache{
	   private static int cacheSize=4*1024*1024;
	   private static LruCache<String,Bitmap> bitmapCache=null;
	   
	   private static synchronized LruCache<String,Bitmap> getBitmapCache(){
		   if(bitmapCache==null){
			   bitmapCache=new LruCache<String,Bitmap>(cacheSize){
				   protected int sizeOf(String key,Bitmap value){
					   return(value.getRowBytes()*value.getHeight());
				   }};
		   }
		   return(bitmapCache);
	   }
	   
	   public static synchronized Bitmap get(String imageUrl){
		   return(getBitmapCache().get(imageUrl));
	}

	   public static synchronized void put(String imageUrl,Bitmap bitmap){
		   getBitmapCache().put(imageUrl,bitmap);
	}
}