/*
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; version 2.

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
import java.io.FilenameFilter;

import android.os.Environment;
import android.util.Log;

public abstract class Utils{
	public enum DownloadState{DOWNLOADED,NOT_DOWNLOADED,DOWNLOADING};
	private static String downloadPath=null;
	
	public final static String getDownloadPath(){
		if(downloadPath==null){downloadPath=Environment.getExternalStorageDirectory().getPath()+"/1MoreCastleDownloads";}
		return(downloadPath);
	}
	
	public final static boolean deleteFile(String fileName){
		try{
			String fullPath=getDownloadPath()+"/"+fileName;
			//TODO - delete file into if we decide to store that too
			File f=new File(fullPath);
			return(f.delete());
		}catch(Exception x){
			Log.e("Utils.deleteFile","Error deleting: "+fileName,x);
			return(false);
		}
	}
	
	public final static void deletePartialDownloads(){
		try{
			File dir=new File(Utils.getDownloadPath());
			if((dir==null)||(!dir.isDirectory())){ return; }
			File[] fileList=dir.listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name){
					return(name.toLowerCase().endsWith(".partial"));
				}
			});
			if((fileList==null)||(fileList.length<1)){ return; }
			int length=fileList.length;
			for(int fileIndex=0;fileIndex<length;fileIndex++){
				try{
					fileList[fileIndex].delete();
				}catch(Exception x){
					Log.e("Utils.deletePartialDownloads","deleting a file",x);
				}
			}
		}catch(Exception x){
			Log.e("Utils.deletePartialDownloads",x.getMessage(),x);
		}
	}
	
	public final static boolean isDownloaded(String fileName){
		try{
			String fullPath=getDownloadPath()+"/"+fileName;
			File f=new File(fullPath);
			return(f.exists());
		}catch(Exception x){
			Log.e("Utils.isDownloaded","fileName: "+fileName,x);
			return(false);
		}
	}
	
	public final static DownloadState getDownloadState(String fileName){
		try{
			String fullPath=getDownloadPath()+"/"+fileName;
			File f=new File(fullPath);
			boolean exists=f.exists();
			if(exists){
				return(DownloadState.DOWNLOADED);
			}else{ //check if it's currently being downloaded
				fullPath=getDownloadPath()+"/"+fileName+".partial";
				f=new File(fullPath);
				if(f.exists()){
					return(DownloadState.DOWNLOADING);
				}else{
					return(DownloadState.NOT_DOWNLOADED);
				}
			}
		}catch(Exception x){
			Log.e("Utils.getDownloadState","fileName: "+fileName,x);
			return(DownloadState.NOT_DOWNLOADED);
		}	
	}
	
}