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

package com.onemorecastle.adapters;

import java.io.File;
import java.util.ArrayList;

import com.onemorecastle.DownloadPodcastIntentService;
import com.onemorecastle.FullArticleActivity;
import com.onemorecastle.R;
import com.onemorecastle.feed.FeedEntry;
import com.onemorecastle.util.HttpFetch;
import com.onemorecastle.util.ImageCache;
import com.onemorecastle.util.SerializationHelper;
import com.onemorecastle.util.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PodcastArrayAdapter extends ArrayAdapter<FeedEntry>{
	private final static String TAG="NewsArrayAdapter";
	private Context context;
	private int resourceId;
	private ArrayList<FeedEntry> entries;
	private final static int IMAGE_SCALE=1;
	private static Drawable placeholder=null;

	public PodcastArrayAdapter(Context context,int resourceId,ArrayList<FeedEntry> entries){
		super(context,resourceId,(FeedEntry[])entries.toArray(new FeedEntry[entries.size()]));
		this.context=context;
		this.resourceId=resourceId;
		this.entries=entries;
		placeholder=context.getResources().getDrawable(R.drawable.placeholder);
	}
	
	@Override
	public void add(FeedEntry item){
		if(!this.entries.contains(item)){
			this.entries.add(item);
		}
	}

	@Override
	public int getCount(){
	    return(this.entries.size());
	}

	@Override
	public FeedEntry getItem(int position) {
	    return(this.entries.get(position));
	}

	@Override
	public long getItemId(int position){ 
	    return(position);
	}
	
	private static class ViewHolder{
	    public TextView summary;
	    public Button readMoreButton;
	    public Button downloadPlayButton;
	    public ImageView featuredImage;
	    public Button deletePressed;
	    public Button deleteUnPressed;
	    public int position;
	}
		
	@Override
	public View getView(int position,View convertView,ViewGroup parent){
		try{
			final ViewHolder viewHolder;
			final FeedEntry entry=this.entries.get(position);
		    if(convertView==null){
				LayoutInflater inflater=(LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView=inflater.inflate(resourceId,null);
				viewHolder=new ViewHolder();
				viewHolder.position=position;
				//summary
				viewHolder.summary=(TextView)convertView.findViewById(R.id.listitem_podcast_summary);
				String summaryText=entry.getSummary();
				viewHolder.summary.setText(Html.fromHtml(summaryText.replaceAll("<img.+?>", "")));
				//download/play & delete buttons
				viewHolder.downloadPlayButton=(Button)convertView.findViewById(R.id.listitem_podcast_downloadplay);
				viewHolder.deletePressed=(Button)convertView.findViewById(R.id.listitem_podcast_delete_pressed);
				viewHolder.deleteUnPressed=(Button)convertView.findViewById(R.id.listitem_podcast_delete_unpressed);
				//read more button
				viewHolder.readMoreButton=(Button)convertView.findViewById(R.id.listitem_podcast_readmore);
				//image view
				viewHolder.featuredImage=(ImageView)convertView.findViewById(R.id.listitem_podcast_featuredimage);
		        viewHolder.featuredImage.setImageDrawable(placeholder);
				//save the ViewHolder
		        convertView.setTag(viewHolder);
		    }else{
		    	viewHolder=(ViewHolder)convertView.getTag();
		    	if(viewHolder.position!=position){
					String summaryText=entry.getSummary();
					//TODO - do this conversion in the feedentry class
					viewHolder.summary.setText(Html.fromHtml(summaryText.replaceAll("<img.+?>", "")));
			        viewHolder.featuredImage.setImageDrawable(placeholder);
			        viewHolder.position=position;
		    	}
		    }
			//download/play & delete buttons
			Utils.DownloadState downloadState=Utils.getDownloadState(entry.getFileName());
			if(downloadState==Utils.DownloadState.DOWNLOADED){
				viewHolder.downloadPlayButton.setBackgroundResource(R.drawable.play);
				viewHolder.downloadPlayButton.setEnabled(true);
				viewHolder.downloadPlayButton.setOnClickListener(
						new OnClickListener(){
							@Override
							public void onClick(View v){
								try{
									Intent playIntent=new Intent(Intent.ACTION_VIEW);
									Uri uri=Uri.parse("file:///"+Utils.getDownloadPath()+"/"+entry.getFileName());
									playIntent.setDataAndType(uri,"audio/mpeg3");
									playIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									context.startActivity(playIntent);
								}catch(Exception x){
									Log.e(TAG,"downloadPlayButton",x);
								}
							}
						}
				);					
				viewHolder.deleteUnPressed.setVisibility(View.VISIBLE);
				viewHolder.deleteUnPressed.setOnClickListener(
						new android.view.View.OnClickListener(){
							@Override
							public void onClick(View v){
								try{
									viewHolder.deletePressed.setVisibility(View.VISIBLE);
									viewHolder.deleteUnPressed.setVisibility(View.INVISIBLE);
								}catch(Exception x){
									Log.e(TAG,"deleteUnPressed",x);
								}
							}
						}
				);					
				viewHolder.deletePressed.setVisibility(View.INVISIBLE);
				viewHolder.deletePressed.setOnClickListener(
						new OnClickListener(){
							@Override
							public void onClick(View v){
								try{
									Utils.deleteFile(entry.getFileName());
									viewHolder.deletePressed.setVisibility(View.INVISIBLE);
									viewHolder.deleteUnPressed.setVisibility(View.INVISIBLE);
									notifyDataSetChanged();
								}catch(Exception x){
									Log.e(TAG,"deletePressed",x);
								}
							}
						}
				);					
			}else if(downloadState==Utils.DownloadState.NOT_DOWNLOADED){
				viewHolder.downloadPlayButton.setBackgroundResource(R.drawable.download);
				viewHolder.downloadPlayButton.setEnabled(true);
				viewHolder.downloadPlayButton.setOnClickListener(
						new OnClickListener(){
							@Override
							public void onClick(View v){
								try{
									if(SerializationHelper.checkMediaState()){
										String fileName=entry.getFileName();
										Log.d(TAG,"downloadPlayButton podcastClicked - fileName="+fileName);
										File destinationDir=new File(Utils.getDownloadPath());
										if(!destinationDir.exists()){
											boolean success=destinationDir.mkdirs();
											Log.d(TAG,"podcastClicked - result of destinationDir.mkdirs() is "+success);
										}
										Intent downloadEpisodeIntent=new Intent(context,DownloadPodcastIntentService.class);
										downloadEpisodeIntent.putExtra("fileName",entry.getFileName());
										downloadEpisodeIntent.putExtra("episodeTitle",entry.getTitle());
										downloadEpisodeIntent.putExtra("downloadUrl",entry.getEnclosureLink());
										context.startService(downloadEpisodeIntent);
										viewHolder.downloadPlayButton.setBackgroundResource(R.drawable.wait);
										viewHolder.downloadPlayButton.setEnabled(false);
									}								
								}catch(Exception x){
									Log.e(TAG,"downloadPlayButton",x);
								}
							}
						}
				);		
				viewHolder.deletePressed.setVisibility(View.INVISIBLE);
				viewHolder.deleteUnPressed.setVisibility(View.INVISIBLE);
			}else if(downloadState==Utils.DownloadState.DOWNLOADING){
				viewHolder.downloadPlayButton.setBackgroundResource(R.drawable.wait);
				viewHolder.downloadPlayButton.setEnabled(false);
				viewHolder.deletePressed.setVisibility(View.INVISIBLE);
				viewHolder.deleteUnPressed.setVisibility(View.INVISIBLE);
			}
			//featured image
			String featuredImageLink=entry.getFeaturedImageLink();
			if((featuredImageLink!=null)&&(featuredImageLink.length()>0)){
				viewHolder.featuredImage.setTag(featuredImageLink);
				DownloadFeaturedImageTask dlTask=new DownloadFeaturedImageTask();
				dlTask.setImageView(viewHolder.featuredImage);
				dlTask.execute(entry.getFeaturedImageLink());
			}
			//read more button
			viewHolder.readMoreButton.setOnClickListener(
					new OnClickListener(){
						@Override
						public void onClick(View v){
							try{
								Intent activityIntent=new Intent(context,FullArticleActivity.class);
								Bundle bundle=new Bundle();
								bundle.putSerializable("FeedEntry",entry);
								activityIntent.putExtra("FeedEntry",bundle);
								activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								context.startActivity(activityIntent);
							}catch(Exception x){
								Log.e(TAG,"readMoreButton",x);
							}
						}
					}
			);
			return(convertView);
		}catch(Exception x){
			Log.e("TAG","getView",x);
			return(null);
		}
	}
	
	private class DownloadFeaturedImageTask extends AsyncTask<String,Void,Bitmap>{
		private ImageView imageView;
		public void setImageView(ImageView imageView){this.imageView=imageView;}
		private String imageUrl;
		
		public Bitmap doInBackground(String... params){
			try{
				this.imageUrl=params[0];
				Log.d(TAG,"DownloadFeaturedImageTask.doInBackground, imageUrl="+imageUrl);
				Bitmap bitmap=ImageCache.get(imageUrl);
				if(bitmap!=null){return(bitmap);}
        		long startTime=System.currentTimeMillis();
				bitmap=HttpFetch.fetchBitmap(imageUrl,IMAGE_SCALE);
        		long endTime=System.currentTimeMillis();
        		Log.d(TAG,"DownloadFeaturedImageTask.doInBackground, Time to fetch bitmap="+(endTime-startTime)+"ms");
        		if(bitmap!=null)ImageCache.put(imageUrl,bitmap);
                return(bitmap);				
			}catch(Exception x){
				Log.e(TAG,"DownloadFeaturedImageTask.doInBackground",x);
    	    	return(null);
			}
	     }

	     public void onPostExecute(Bitmap result){
	    	 try{
	    		 if(result!=null){
		    		 //check if this image is intended for a different imageview
		    		 if(imageUrl.equals(this.imageView.getTag())){
			    		 this.imageView.setImageBitmap(result);
			    		 result=null;
		    		 }
	    		 }
			}catch(Exception x){
				Log.e(TAG,"DownloadFeaturedImageTask.onPostExecute",x);
			}
	     }
	 }
	
}