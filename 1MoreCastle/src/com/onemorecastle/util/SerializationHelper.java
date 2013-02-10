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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.os.Environment;
import android.util.Log;

public abstract class SerializationHelper{
	private final static String TAG="SerializationHelper";

	public static boolean checkMediaState(){
		String state=Environment.getExternalStorageState();
		if(state.equals(Environment.MEDIA_MOUNTED)){
			return(true);
		}
		if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
			Log.e(TAG,"checkMediaState - SD card is read-only. [state=MEDIA_MOUNTED_READ_ONLY]");
		}else if(Environment.MEDIA_CHECKING.equals(state)){
			Log.e(TAG,"checkMediaState - SD card is being disk-checked. [state=MEDIA_CHECKING]");
		}else if(Environment.MEDIA_BAD_REMOVAL.equals(state)){
			Log.e(TAG,"checkMediaState - SD card was removed before it was unmounted. [state=MEDIA_BAD_REMOVAL]");
		}else if(Environment.MEDIA_NOFS.equals(state)){
			Log.e(TAG,"checkMediaState - SD card is blank or is using an unsupported filesystem. [state=MEDIA_NOFS]");
		}else if(Environment.MEDIA_REMOVED.equals(state)){
			Log.e(TAG,"checkMediaState - SD card is missing. [state=MEDIA_REMOVED]");
		}else if(Environment.MEDIA_SHARED.equals(state)){
			Log.e(TAG,"checkMediaState - SD card is shared. If you are connected to a PC via USB please disconnect and try again. [state=MEDIA_SHARED]");
		}else if(Environment.MEDIA_UNMOUNTABLE.equals(state)){
			Log.e(TAG,"checkMediaState - SD card is present but cannot be mounted. [state=MEDIA_UNMOUNTABLE]");
		}else if(Environment.MEDIA_UNMOUNTED.equals(state)){
			Log.e(TAG,"checkMediaState - SD card is not mounted. [state=MEDIA_UNMOUNTED]");
		}else{
			Log.e(TAG,"checkMediaState - Unknown media state. [state="+state+"]");
		}
		return(false);
	}
	
	public static boolean serializeObject(Serializable sobj,String destinationFileName){
		try{
			if(checkMediaState()){
				File dir=new File(Environment.getExternalStorageDirectory().getPath()+"/1MoreCastleDownloads");
				if(!dir.exists()){
					dir.mkdir();
				}
				File f=new File(dir.getPath()+"/"+destinationFileName);
				if(!f.exists()){
					f.createNewFile();
				}
				FileOutputStream fout=new FileOutputStream(f);
				ObjectOutputStream oout=new ObjectOutputStream(fout);
				oout.writeObject(sobj);
				oout.close();
				return(true);
			}else{
				return(false);
			}
		}catch(Exception x){
			Log.e(TAG,"serializeObject",x);
			if(sobj!=null){
				Log.e(TAG,"sobj="+sobj.toString());
			}
			return(false);
		}
	}
	
	public static Object readObject(String sourceFileName){
		Object sobj=null;
		ObjectInputStream oin=null;
		try{
			checkMediaState();
			File dir=new File(Environment.getExternalStorageDirectory().getPath()+"/1MoreCastleDownloads");
			if(!dir.exists()){
				throw(new Exception(dir.getPath()+" does not exist"));
			}
			FileInputStream fin=new FileInputStream(dir.getPath()+"/"+sourceFileName);
			oin=new ObjectInputStream(fin);
			sobj=oin.readObject();
		}catch(Exception x){
			Log.e(TAG,"readObject",x);
			if(sobj!=null){
				Log.e(TAG,"sobj="+sobj.toString());
			}
		}finally{
			if(oin!=null){
				try{oin.close();}catch(IOException oix){}
			}
		}
		return(sobj);
	}
	
}