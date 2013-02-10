/*
1MoreCastle - Mobile application for 1MoreCastle.com
Author - Hugues Johnson (http://HuguesJohnson.com)

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

package com.onemorecastle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.onemorecastle.R;
import com.onemorecastle.util.SerializationHelper;
import com.onemorecastle.util.Utils;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class DownloadPodcastIntentService extends IntentService{
	private final static String TAG="DownloadPodcastIntentService";

	public DownloadPodcastIntentService(){
		super("DownloadPodcastIntentService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent){
		int inProgressIcon=R.drawable.icon_notification_inprogress;
		int completeIcon=R.drawable.icon_notification_complete;
		Notification notification=null;
		NotificationManager notificationManager=null;
		try{
			String fileName=intent.getStringExtra("fileName");
			Log.d(TAG,"DownloadPodcastIntentService.onHandleIntent - fileName="+fileName);
			String episodeTitle=intent.getStringExtra("episodeTitle");
			Log.d(TAG,"DownloadPodcastIntentService.onHandleIntent - episodeTitle="+episodeTitle);
			String downloadUrl=intent.getStringExtra("downloadUrl");
			Log.d(TAG,"DownloadPodcastIntentService.onHandleIntent - downloadUrl="+downloadUrl);
			//create the Notification instance with icon
	        notification=new Notification(inProgressIcon,"Downloading "+episodeTitle,System.currentTimeMillis());
	        notification.flags=notification.flags|Notification.FLAG_ONGOING_EVENT;
	        //get a reference to the application context
	        Context applicationContext=this.getApplicationContext();
	        if(applicationContext==null){ throw(new Exception("DownloadPodcastIntentService.onHandleIntent - applicationContext is null")); }
	        //setup an intent for the notification
	        Intent notificationIntent=new Intent(applicationContext,ManageSavedPodcastsActivity.class); 
	        PendingIntent contentIntent=PendingIntent.getActivity(applicationContext,0,notificationIntent,0);
	        notification.contentIntent=contentIntent;
	        //setup the notification's content view
	        RemoteViews contentView=new RemoteViews(applicationContext.getPackageName(),R.layout.notification_downloadpodcast);
	        notification.contentView=contentView;
	        notification.contentView.setImageViewResource(R.id.ImageViewDownloadPodcastNotificationIcon,inProgressIcon);
	        notification.contentView.setTextViewText(R.id.TextViewDownloadPodcastNotificationMessage,"Downloading "+episodeTitle);
	        notification.contentView.setProgressBar(R.id.ProgressBarDownloadPodcast,100,0,true);
	        //send the notification
	        notificationManager=(NotificationManager)applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
	        if(notificationManager==null){ throw(new Exception("DownloadPodcastIntentService.onHandleIntent - notificationManager is null")); }
	        notificationManager.notify("DownloadPodcastIntentService_Notification",1,notification);
	        //download the episode
			HttpURLConnection urlConnection=null;
			FileOutputStream out=null;
			InputStream in=null;
			if(!SerializationHelper.checkMediaState()){
				throw(new Exception("SD storage is unavailable"));
			}else{
				//check if the download directory exists, create it if not
				File destinationDir=new File(Utils.getDownloadPath());
				if(!destinationDir.exists()){
					boolean success=destinationDir.mkdirs();
					Log.d(TAG,"DownloadPodcastIntentService.onHandleIntent - result of destinationDir.mkdirs() is "+success);
					if(!success){
	            		throw(new Exception("Can't create directory "+destinationDir.getAbsolutePath()));
					}
				}
				//did we already download this file?
				Utils.DownloadState downloadState=Utils.getDownloadState(fileName);
				if(downloadState==Utils.DownloadState.DOWNLOADED){
					throw(new Exception("Hey, you already downloaded "+fileName));
				}else if(downloadState==Utils.DownloadState.DOWNLOADING){
					throw(new Exception("Hey, "+fileName+" is already being downloaded"));
				}
				String downloadPath=destinationDir+"/"+fileName+".partial";
				Log.d(TAG,"DownloadPodcastIntentService.onHandleIntent - downloadPath="+downloadPath);
	            URL url=new URL(downloadUrl);
	            urlConnection=(HttpURLConnection)url.openConnection();
	            urlConnection.setRequestMethod("GET");
	            urlConnection.setDoOutput(false);
	            urlConnection.connect();
	            File f=new File(downloadPath);
            	boolean success=f.createNewFile();
				Log.d(TAG,"DownloadPodcastIntentService.onHandleIntent - result of f.createNewFile() is "+success);
	            out=new FileOutputStream(f);
	            in=urlConnection.getInputStream();
				Log.d(TAG,"DownloadPodcastIntentService.onHandleIntent - urlConnection.getInputStream() was successful");
	            byte[] buffer=new byte[1024];
	            int bytes=0;
	            int bytesRead=0;
	            int updateFrequency=5242880;
	            int updateCounter=0;
	            while((bytes=in.read(buffer))>0){
	                out.write(buffer,0,bytes);
	                bytesRead+=1024;
	                updateCounter+=1024;
	            	//uncomment next line for hyper debugging messages
	            	//Log.d(TAG,bytesRead+"/"+updateCounter + "/" +updateFrequency);
	            	String progressString;
	                if(updateCounter>=updateFrequency){
	                	progressString=" ("+bytesRead+" bytes read)";
	        	        notification.contentView.setTextViewText(R.id.TextViewDownloadPodcastNotificationMessage,"Downloading "+episodeTitle+progressString);
	        	        notificationManager.notify("DownloadPodcastIntentService_Notification",1,notification);
	        	        updateCounter=0;
	                }
	            }
	            //rename the file
	            f.renameTo(new File(destinationDir+"/"+fileName));
	            //OK, all done, let's update the notification again
    	        notification.contentView.setImageViewResource(R.id.ImageViewDownloadPodcastNotificationIcon,completeIcon);
    	        notification.contentView.setTextViewText(R.id.TextViewDownloadPodcastNotificationMessage,"Finished downloading "+episodeTitle);
    	        notification.contentView.setProgressBar(R.id.ProgressBarDownloadPodcast,100,100,false);
    	        notification.icon=completeIcon;
    	        notification.flags=notification.flags|Notification.FLAG_AUTO_CANCEL;
    	        notificationManager.notify("DownloadPodcastIntentService_Notification",1,notification);
			}
		}catch(Exception x){
			Log.e(TAG,x.getMessage(),x);
			//try to update the notification
			try{
				if((notification!=null)&&(notification.contentView!=null)&&(notificationManager!=null)){
	    	        notification.contentView.setImageViewResource(R.id.ImageViewDownloadPodcastNotificationIcon,completeIcon);
	    	        notification.contentView.setTextViewText(R.id.TextViewDownloadPodcastNotificationMessage,"Error: "+x.getMessage());
	    	        notification.contentView.setProgressBar(R.id.ProgressBarDownloadPodcast,100,100,false);
	    	        notification.flags=notification.flags|Notification.FLAG_AUTO_CANCEL;
	    	        notification.icon=completeIcon;
	    	        notificationManager.notify("DownloadPodcastIntentService_Notification",1,notification);
				}
			}catch(Exception reallyBadTimes){
				Log.e(TAG,reallyBadTimes.getMessage(),reallyBadTimes);
			}
		}finally{
			//notify the main activity that we are done
			try{
				Intent broadcastIntent=new Intent();
		        broadcastIntent.setAction(DownloadReceiver.RESPONSE);
		        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		        //broadcastIntent.putExtra(PARAM_OUT_MSG, resultTxt);
		        sendBroadcast(broadcastIntent);				
			}catch(Exception reallyBadTimes){
				Log.e(TAG,reallyBadTimes.getMessage(),reallyBadTimes);
			}
		}
	}
}