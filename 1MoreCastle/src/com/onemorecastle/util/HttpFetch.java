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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public abstract class HttpFetch{
	private final static int MAX_WIDTH=640;
	private final static int MAX_HEIGHT=320;
	
	//see - http://stackoverflow.com/questions/4414839/bitmapfactory-decodestream-returns-null-without-exception
	//public static InputStream fetch(String address) throws MalformedURLException,IOException{
//	    HttpGet httpRequest=new HttpGet(URI.create(address));
//	    HttpClient httpclient=new DefaultHttpClient();
//	    HttpResponse response=(HttpResponse)httpclient.execute(httpRequest);
//	    HttpEntity entity=response.getEntity();
//	    BufferedHttpEntity bufHttpEntity=new BufferedHttpEntity(entity);
//	    InputStream instream=bufHttpEntity.getContent();
//	    return(instream);
//	}
	
	public static Bitmap fetchBitmap(String imageAddress,int sampleSize) throws MalformedURLException,IOException{
		Bitmap bitmap=null;
		DefaultHttpClient httpclient=null;
		try{
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters,2000);
			HttpConnectionParams.setSoTimeout(httpParameters,5000);
			HttpConnectionParams.setSocketBufferSize(httpParameters,512);
			HttpGet httpRequest=new HttpGet(URI.create(imageAddress));
			httpclient=new DefaultHttpClient();
			httpclient.setParams(httpParameters);
			HttpResponse response=(HttpResponse)httpclient.execute(httpRequest);
			HttpEntity entity=response.getEntity();
			BufferedHttpEntity bufHttpEntity=new BufferedHttpEntity(entity);
			InputStream instream=bufHttpEntity.getContent();
			BitmapFactory.Options options=new BitmapFactory.Options();
		    //first decode with inJustDecodeBounds=true to check dimensions
		    options.inJustDecodeBounds=true;
			options.inSampleSize=sampleSize;
			BitmapFactory.decodeStream(instream,null,options);
		    //decode bitmap with inSampleSize set
		    options.inSampleSize=calculateInSampleSize(options,MAX_WIDTH,MAX_HEIGHT);
			options.inPurgeable=true;
			options.inInputShareable=true;
			options.inDither=true;
		    options.inJustDecodeBounds=false;
			//response=(HttpResponse)httpclient.execute(httpRequest);
			//entity=response.getEntity();
			//bufHttpEntity=new BufferedHttpEntity(entity);
			instream=bufHttpEntity.getContent();
		    bitmap=BitmapFactory.decodeStream(instream,null,options);
		    //close out stuff
		    try{
			    instream.close();
			    bufHttpEntity.consumeContent();
			    entity.consumeContent();
		    }finally{
			    instream=null;
			    bufHttpEntity=null;
			    entity=null;
		    }
		}catch(Exception x){
			Log.e("HttpFetch.fetchBitmap",imageAddress,x);
		}finally{
			httpclient=null;
		}
	    return(bitmap);
	}
	
	//method from Google's documentation on loading large bitmaps
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        // Calculate ratios of height and width to requested height and width
        final int heightRatio = Math.round((float) height / (float) reqHeight);
        final int widthRatio = Math.round((float) width / (float) reqWidth);

        // Choose the smallest ratio as inSampleSize value, this will guarantee
        // a final image with both dimensions larger than or equal to the
        // requested height and width.
        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
    }

    return inSampleSize;
}
}