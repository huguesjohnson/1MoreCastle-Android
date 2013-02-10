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

import java.util.ArrayList;

import com.onemorecastle.FullArticleActivity;
import com.onemorecastle.R;
import com.onemorecastle.feed.FeedEntry;
import com.onemorecastle.util.HttpFetch;
import com.onemorecastle.util.ImageCache;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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

public class NewsArrayAdapter extends ArrayAdapter<FeedEntry>{
	private final static String TAG="NewsArrayAdapter";
	private Context context;
	private int resourceId;
	private ArrayList<FeedEntry> entries;
	private final static int IMAGE_SCALE=1;
	private static Drawable placeholder=null;
	
	public NewsArrayAdapter(Context context,int resourceId,ArrayList<FeedEntry> entries){
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
	    public TextView title;
	    public TextView summary;
	    public Button readMoreButton;
	    public ImageView featuredImage;
	    public int position;
	}
	
	@Override
	public View getView(int position,View convertView,ViewGroup parent){
		try{
			ViewHolder viewHolder=null;
		    final FeedEntry entry=this.entries.get(position);
		    if(convertView==null){
				LayoutInflater inflater=(LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView=inflater.inflate(resourceId,null);
				viewHolder=new ViewHolder();
				viewHolder.position=position;
				//title
				viewHolder.title=(TextView)convertView.findViewById(R.id.listitem_article_title);
				viewHolder.title.setText(entry.getTitle());
				//summary
				viewHolder.summary=(TextView)convertView.findViewById(R.id.listitem_article_summary);
				String summaryText=entry.getSummary();
				//TODO - do this conversion in the feedentry class
				viewHolder.summary.setText(Html.fromHtml(summaryText.replaceAll("<img.+?>", "")));
				//read more button
				viewHolder.readMoreButton=(Button)convertView.findViewById(R.id.listitem_article_readmore);
		        //image
		        viewHolder.featuredImage=(ImageView)convertView.findViewById(R.id.listitem_article_image);
		        viewHolder.featuredImage.setImageDrawable(placeholder);
		        //save the ViewHolder
		        convertView.setTag(viewHolder);
		    }else{
		    	viewHolder=(ViewHolder)convertView.getTag();
		    	if(viewHolder.position!=position){
					viewHolder.title.setText(entry.getTitle());
					String summaryText=entry.getSummary();
					//TODO - do this conversion in the feedentry class
					viewHolder.summary.setText(Html.fromHtml(summaryText.replaceAll("<img.+?>", "")));
			        viewHolder.featuredImage.setImageDrawable(placeholder);
			        viewHolder.position=position;
		    	}
		    }
		    //setup button listener
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
			//load featured image in the background
			String featuredImageLink=entry.getFeaturedImageLink();
			if((featuredImageLink!=null)&&(featuredImageLink.length()>0)){
				DownloadFeaturedImageTask dlTask=new DownloadFeaturedImageTask();
				viewHolder.featuredImage.setTag(featuredImageLink);
				dlTask.setImageView(viewHolder.featuredImage);
				dlTask.execute(entry.getFeaturedImageLink());
			}
			return(convertView);
		}catch(Exception x){
			Log.e("NewsArrayAdapter.getView",x.getMessage(),x);
			return(null);
		}
	}
	
	private class DownloadFeaturedImageTask extends AsyncTask<String,Void,Bitmap>{
		private ImageView imageView;
		public void setImageView(ImageView imageView){this.imageView=imageView;}
		private String imageUrl;
		
		public Bitmap doInBackground(String... params){
			try{
				imageUrl=params[0];
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
	    			 }
	    		 }
			}catch(Exception x){
				Log.e(TAG,"DownloadFeaturedImageTask.onPostExecute",x);
			}
	     }
	 }
	
}