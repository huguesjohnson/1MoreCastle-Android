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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.onemorecastle.adapters.NewsArrayAdapter;
import com.onemorecastle.adapters.PodcastArrayAdapter;
import com.onemorecastle.feed.FeedEntry;
import com.onemorecastle.feed.FeedParser;
import com.onemorecastle.util.SerializationHelper;
import com.onemorecastle.util.Utils;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

public class MainActivity extends SherlockFragmentActivity implements OnScrollListener{
	private final static String TAG="MainActivity";
	private TabHost tabHost;
	private ArrayList<FeedEntry> feedEntryList;
	private ListView listViewAllArticles;
	private ListView listViewAllPodcasts;
    //needed to receive messages when a podcast is done downloading
	private DownloadReceiver downloadReceiver;
    private IntentFilter downloadReceiverfilter;
    //variables to support paging
    private int feedPage=1;
    private int MAX_FEED_PAGE=5;
	//menu constants
	private final static int MENU_REFRESH=0;
	private final static int MENU_MANAGE_DOWNLOADS=1;
	private final static int MENU_ABOUT=2;

	private class ParseFeedTask extends AsyncTask<Integer,String,Void>{
		private ArrayList<FeedEntry> newEntries;
		private int page=1;
		
		public Void doInBackground(Integer... params){
			if((params.length>0)&&(params[0]!=null)){
				page=params[0].intValue();
			}
        	HttpURLConnection httpConnection=null;
        	InputStream httpIn=null;
        	try{
				Log.d(TAG,"ParseFeedTask.doInBackground");
        		long startTime=System.currentTimeMillis();
        		URL url=new URL(getResources().getString(R.string.rss_url)+page);
                httpConnection=(HttpURLConnection)url.openConnection();
                httpConnection.setConnectTimeout(10000);
        		long endTime=System.currentTimeMillis();
                Log.d(TAG,"ParseFeedTask.doInBackground - Time to open connection="+(endTime-startTime)+"ms");
                startTime=endTime;
                httpIn=httpConnection.getInputStream();
        		endTime=System.currentTimeMillis();
                Log.d(TAG,"ParseFeedTask.doInBackground - Time to get input stream="+(endTime-startTime)+"ms");
                startTime=endTime;
                newEntries=FeedParser.parseFeed(httpIn);
        		endTime=System.currentTimeMillis();
                Log.d(TAG,"ParseFeedTask.doInBackground - Time to parse feed="+(endTime-startTime)+"ms");
                httpIn.close();
        		httpConnection.disconnect();
            }catch(Exception x){
    			Log.e(TAG,"ParseFeedTask.doInBackground",x);
            	//in case the IOException is thrown by one of the HttpURLConnection operations
            	if(httpConnection!=null){
            		httpConnection.disconnect();
            	}
            }
    		return(null);
	     }

	     @SuppressWarnings("unchecked")
		public void onPostExecute(Void result){
    		try{
    			if(page==1){
    				if((newEntries!=null)&&(newEntries.size()>0)){
 	        			Log.d(TAG+".ParseFeedTask.onPostExecute","newEntries.size()="+newEntries.size());
    					if((feedEntryList!=null)&&(feedEntryList.size()>0)){
	 	    				//has the feed been updated?
    						if(newEntries.get(0).equals(feedEntryList.get(0))){
    	 	        			Log.d(TAG+".ParseFeedTask.onPostExecute","newEntries.get(0).equals(feedEntryList.get(0) is true");
    	 	        			//nothing has changed
    	 	    				hideProgress();
    	 	    				//state of downloaded podcasts may have changed externally, force update
    	 	    				((BaseAdapter)listViewAllPodcasts.getAdapter()).notifyDataSetChanged(); 
    						}else{
    	 	        			Log.d(TAG+".ParseFeedTask.onPostExecute","newEntries.get(0).equals(feedEntryList.get(0) is false");
    	 	    				//the feed has been updated with new stuff
    	 	        			feedPage=1;
    	 	        			feedEntryList=newEntries;
    	 	        			updateUI();
    	 	        			SerializationHelper.serializeObject(newEntries,"feed.obj");
    						}
	 	    			}else{
	 	        			Log.d(TAG+".ParseFeedTask.onPostExecute","feedEntryList is null or empty");
	 	        			feedPage=1;
	 	    				feedEntryList=newEntries;
	 	        			updateUI();
	 	        			SerializationHelper.serializeObject(newEntries,"feed.obj");
	 	    			}
	 	    		}else{ //some kind of problem getting the episodes, revert to most recent good copy
	 	        		ArrayList<FeedEntry> cachedEntries=(ArrayList<FeedEntry>)SerializationHelper.readObject("feed.obj");
	 	        		if(cachedEntries!=null){
	 	        			feedPage=1;
	 	        			feedEntryList=cachedEntries;
	 	        			updateUI();
	 	        		}else{
	 	        			throw(new Exception("Unable to connect to server. Please check your internet connectivity and retry."));
	 	        		}
	 	    		}
    			}else{ //page>1
    				if((newEntries!=null)&&(newEntries.size()>0)){
    					//append these to the lists
    					if((feedEntryList!=null)&&(feedEntryList.size()>0)){
    						NewsArrayAdapter newsAdapter=(NewsArrayAdapter)listViewAllArticles.getAdapter();
    						PodcastArrayAdapter podcastAdapter=(PodcastArrayAdapter)listViewAllPodcasts.getAdapter();
    						//add new stuff
    						//TODO - this was added late to support infinite scrolling and should be cleaned-up
    						int size=newEntries.size();
    						for(int index=0;index<size;index++){
    							FeedEntry entry=newEntries.get(index);
    							if(entry.isPodcast()){
    								//check if this podcast has been downloaded
    								try{
    									String dlPath=Utils.getDownloadPath();
    									if((new File(dlPath)).exists()){
    										dlPath+="/"+entry.getFileName();
    										entry.setDownloaded((new File(dlPath)).exists());
    									}
    								}catch(Exception x){
    									Log.e(TAG,"updateUI - checking if podcast was downloaded",x);
    								}
    								podcastAdapter.add(entry);
    							}else{
   									newsAdapter.add(entry);
    							}
    						}
    						//notify lists that they've been changed
	 	    				hideProgress();
	 	    			}else{
	 	        			throw(new Exception("Somehow feedEntryList is null or empty - feedPage="+feedPage));
	 	    			}
	 	    		}else{
	 	    			Log.d(TAG+".ParseFeedTask.onPostExecute","Reached the end of the feed or something bad happened");
	 	    		}
    			}
	 		}catch(Exception x){
				Log.e(TAG,"DownloadEpisodeListTask.onPostExecute",x);
			}finally{
				//TODO - this is redundant in most flows
 				hideProgress();
			}
    	}	
	}	
	
	//TODO - maybe think of a better name for this, this method is full refresh of both lists
	private void updateUI(){
		try{
			if((this.feedEntryList==null)||(this.feedEntryList.size()<1)){
				ProgressBar progressBarUpdating=(ProgressBar)findViewById(R.id.ProgressBarUpdating);
				progressBarUpdating.setVisibility(View.GONE);
			}else{ 
				//figure out what's news and what's a podcast
				ArrayList<FeedEntry> newsEntries=new ArrayList<FeedEntry>();
				ArrayList<FeedEntry> podcastEntries=new ArrayList<FeedEntry>();
				int size=this.feedEntryList.size();
				for(int index=0;index<size;index++){
					FeedEntry entry=this.feedEntryList.get(index);
					if(entry.isPodcast()){
						//check if this podcast has been downloaded
						try{
							String dlPath=Utils.getDownloadPath();
							if((new File(dlPath)).exists()){
								dlPath+="/"+entry.getFileName();
								entry.setDownloaded((new File(dlPath)).exists());
							}
						}catch(Exception x){
							Log.e(TAG,"updateUI - checking if podcast was downloaded",x);
						}
						podcastEntries.add(entry);
					}else{
						newsEntries.add(entry);
					}
				}
				//hide the progress fields
				this.hideProgress();
				//populate the lists articles & podcasts
				NewsArrayAdapter newsApapter=new NewsArrayAdapter(this.getApplicationContext(),R.layout.listitem_article,newsEntries);
				ListView listViewAllArticles=(ListView)findViewById(R.id.ListViewAllArticles);
				listViewAllArticles.setAdapter(newsApapter); 
				PodcastArrayAdapter podcastApapter=new PodcastArrayAdapter(this.getApplicationContext(),R.layout.listitem_podcast,podcastEntries);
				this.listViewAllPodcasts.setAdapter(podcastApapter);
			}
		}catch(Exception x){
			Log.e(TAG,"updateUI"+x);
			this.showErrorDialog(x);
		}
	}
	
	private void hideProgress(){
		try{
			LinearLayout progressLayout=(LinearLayout)findViewById(R.id.LinearLayoutProgress);
			progressLayout.setVisibility(View.GONE);
		}catch(Exception x){
			Log.e(TAG,"hideProgress",x);
			showErrorDialog(x);
		}
	}
	
    private void refreshLists(boolean fullRefresh){
		try{
			LinearLayout progressLayout=(LinearLayout)findViewById(R.id.LinearLayoutProgress);
			//check if we're already checking
			if(progressLayout.getVisibility()==View.VISIBLE){
				return;
			}
			progressLayout.setVisibility(View.VISIBLE);
			ProgressBar progressBarUpdating=(ProgressBar)findViewById(R.id.ProgressBarUpdating);
			progressBarUpdating.setVisibility(View.VISIBLE);
			TextView textViewUpdating=(TextView)findViewById(R.id.TextViewUpdating);
			if(fullRefresh){
				textViewUpdating.setText("Checking for new articles and podcasts...");
				(new ParseFeedTask()).execute(Integer.valueOf(1));
			}else{
				this.feedPage++;
				textViewUpdating.setText("Loading previous articles and podcasts...");
				(new ParseFeedTask()).execute(Integer.valueOf(feedPage));
			}
		}catch(Exception x){
			Log.e(TAG,"refreshLists",x);
			this.showErrorDialog(x);
		}
	}
    
    private void openArticle(int position){
    	NewsArrayAdapter adapter=(NewsArrayAdapter)listViewAllArticles.getAdapter();
		Intent launchIntent=new Intent(Intent.ACTION_VIEW,Uri.parse(adapter.getItem(position).getLink()));
		startActivity(launchIntent);
    }
    
    private void podcastClicked(int position){
    	PodcastArrayAdapter adapter=(PodcastArrayAdapter)listViewAllPodcasts.getAdapter();
    	FeedEntry podcastEntry=adapter.getItem(position);
    	boolean downloaded=podcastEntry.isDownloaded();
    	if(!downloaded){//TODO - prevent this weird condition from happening
    		downloaded=Utils.isDownloaded(podcastEntry.getFileName());
    	}
    	if(downloaded){
			Intent intent=new Intent(Intent.ACTION_VIEW);
			Uri uri=Uri.parse("file:///"+Utils.getDownloadPath()+"/"+podcastEntry.getFileName());
			intent.setDataAndType(uri,"audio/mpeg3");
			startActivity(intent);	
    	}else{
			if(SerializationHelper.checkMediaState()){
				String fileName=podcastEntry.getFileName();
				Log.d(TAG,"podcastClicked - fileName="+fileName);
				File destinationDir=new File(Utils.getDownloadPath());
				if(!destinationDir.exists()){
					boolean success=destinationDir.mkdirs();
					Log.d(TAG,"podcastClicked - result of destinationDir.mkdirs() is "+success);
				}
				Intent downloadEpisodeIntent=new Intent(getApplicationContext(),DownloadPodcastIntentService.class);
				downloadEpisodeIntent.putExtra("fileName",podcastEntry.getFileName());
				downloadEpisodeIntent.putExtra("episodeTitle",podcastEntry.getTitle());
				downloadEpisodeIntent.putExtra("downloadUrl",podcastEntry.getEnclosureLink());
				startService(downloadEpisodeIntent);
			}
    	}
    }
    
	OnItemClickListener selectArticleListener=new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent,View view,int position,long id){
			try{
				openArticle(position);
			}catch(Exception x){
				Log.e(TAG,"onItemClick: position="+position,x);
				showErrorDialog(x);
			}  
		}
	};
	
	OnItemClickListener selectPodcastListener=new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent,View view,int position,long id){
			try{
				podcastClicked(position);
			}catch(Exception x){
				Log.e(TAG,"onItemClick: position="+position,x);
				showErrorDialog(x);
			}
		}
	};
    
	@Override
	protected void onResume(){
		super.onResume();
		try{
			//load the episode list
			this.refreshLists(true);
	        //re-register the for episode downloads
	        registerReceiver(this.downloadReceiver,downloadReceiverfilter);
		}catch(Exception x){
			Log.e(TAG,"onResume",x);
			showErrorDialog(x);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		try{
			setContentView(R.layout.activity_main);
        	//setup the action bar
			ActionBar actionBar=getSupportActionBar();
			actionBar.setIcon(R.drawable.logo);
			actionBar.setTitle("");
			actionBar.setBackgroundDrawable(new ColorDrawable(0xff424542));	
        	//setup the tabs
        	tabHost=(TabHost)findViewById(android.R.id.tabhost);
        	tabHost.setup();
			tabHost.addTab(
					tabHost.newTabSpec("News")
					.setIndicator("",this.getResources().getDrawable(R.drawable.ic_tab_news))
					.setContent(R.id.LinearLayoutNews));
			tabHost.addTab(
					tabHost.newTabSpec("Podcasts")
					.setIndicator("",this.getResources().getDrawable(R.drawable.ic_tab_podcast))
					.setContent(R.id.LinearLayoutPodcasts));
			Resources r=getResources();
			tabHost.getTabWidget().getChildAt(0).setBackgroundColor(r.getColor(R.color.omc_red));
			tabHost.getTabWidget().getChildAt(1).setBackgroundColor(r.getColor(R.color.omc_darkred));
			tabHost.setCurrentTab(0);	
			//update tab background color on change
			tabHost.setOnTabChangedListener(new OnTabChangeListener(){
				@Override
				public void onTabChanged(String tabId){
					Resources r=getResources();
					if(tabHost.getTabWidget().getChildAt(0).isSelected()){
						tabHost.getTabWidget().getChildAt(0).setBackgroundColor(r.getColor(R.color.omc_red));
						tabHost.getTabWidget().getChildAt(1).setBackgroundColor(r.getColor(R.color.omc_darkred));
					}else{
						tabHost.getTabWidget().getChildAt(0).setBackgroundColor(r.getColor(R.color.omc_darkred));
						tabHost.getTabWidget().getChildAt(1).setBackgroundColor(r.getColor(R.color.omc_red));
					}
				} });
			//setup click listeners
	        this.listViewAllArticles=(ListView)findViewById(R.id.ListViewAllArticles);
	        this.listViewAllArticles.setOnItemClickListener(selectArticleListener);
	        this.listViewAllArticles.setOnScrollListener(this);
	        this.listViewAllPodcasts=(ListView)findViewById(R.id.ListViewAllPodcasts);
	        this.listViewAllPodcasts.setOnItemClickListener(selectPodcastListener);
	        this.listViewAllPodcasts.setOnScrollListener(this);
	        //setup receiver for episode downloads
	        this.downloadReceiverfilter=new IntentFilter(DownloadReceiver.RESPONSE);
	        downloadReceiverfilter.addCategory(Intent.CATEGORY_DEFAULT);
	        this.downloadReceiver=new DownloadReceiver(){
	     	   @Override
	    	   public void onReceive(Context context,Intent intent){
	    		   Log.d("DownloadReceiver","onReceive");
	    		   refreshLists(true);
	    	   }
	        };
	        //clean up partial downloads
	        Utils.deletePartialDownloads();
		}catch(Exception x){
			Log.e(TAG,"onCreate",x);
			this.showErrorDialog(x);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		try{
			menu.add(0,MENU_REFRESH,MENU_REFRESH,"Refresh").setIcon(R.drawable.ic_menu_refresh);
			menu.add(0,MENU_MANAGE_DOWNLOADS,MENU_MANAGE_DOWNLOADS,"Manage downloaded podcasts").setIcon(R.drawable.ic_menu_archive);
			menu.add(0,MENU_ABOUT,MENU_ABOUT,"About").setIcon(R.drawable.ic_menu_info_details);
			return(true);
		}catch(Exception x){
			Log.e(TAG,"onCreateOptionsMenu",x);
			return(false);
		}
	}
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem manageDownloadsItem=menu.getItem(MENU_MANAGE_DOWNLOADS);
		String currentTag=tabHost.getCurrentTabTag();
		if(currentTag.equals("Podcasts")){
			manageDownloadsItem.setVisible(true);
		}else{
			manageDownloadsItem.setVisible(false);
		}
		return(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		try{
			switch(item.getItemId()){
			case MENU_REFRESH:{
				this.refreshLists(true);
				return(true);
			}
			case MENU_ABOUT:{
				this.showAboutDialog();
				return(true);
			}
			case MENU_MANAGE_DOWNLOADS:{
				Intent manageSavedEpisodesIntent=new Intent(getApplicationContext(),ManageSavedPodcastsActivity.class);
				startActivityForResult(manageSavedEpisodesIntent,0);
				return(true);
			}
			default:{return(false);}
			}
		}catch(Exception x){
			if(item!=null){
				Log.e(TAG,"onOptionsItemSelected: item.getItemId()="+item.getItemId(),x);
			}else{
				Log.e(TAG,"onOptionsItemSelected: item is null",x);
			}
			return(false);
		}
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		unregisterReceiver(this.downloadReceiver);
	}

	private void showErrorDialog(Exception x){
		try{
	        new AlertDialog.Builder(this)
	   		.setTitle(R.string.app_name)
	   		.setMessage("Error: "+x.getMessage())
	   		.setPositiveButton("Close", null)
	   		.show();	
		}catch(Exception reallyBadTimes){
			Log.e(TAG,"showErrorDialog",reallyBadTimes);
		}
	}

	private void showAboutDialog(){
		try{
	        new AlertDialog.Builder(this)
	   		.setTitle(R.string.app_name)
	   		.setMessage(R.string.about)
	   		.setPositiveButton("Close", null)
	   		.show();	
		}catch(Exception x){
			Log.e(TAG,"showAboutDialog",x);
		}
	}

	@Override
	public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount){
		//bail out of this method as early as possible because it gets called a lot
		if(totalItemCount==0){return;}
		if(this.feedPage>MAX_FEED_PAGE){return;}
		//this is just a sanity check
		if((view.getId()==R.id.ListViewAllArticles)||(view.getId()==R.id.ListViewAllPodcasts)){
            final int lastItem=firstVisibleItem+visibleItemCount;
            if(lastItem>=totalItemCount){
                //last item reached
            	this.refreshLists(false);
            }
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}
	
}