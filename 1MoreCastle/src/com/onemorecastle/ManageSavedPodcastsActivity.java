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
import java.io.FilenameFilter;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.onemorecastle.adapters.SavedPodcastArrayAdapter;
import com.onemorecastle.util.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ManageSavedPodcastsActivity extends SherlockActivity{
	private final static String TAG="ManageSavedEpisodesActivity";
	//menu constants
	private final static int MENU_ABOUT=2;
	private final static int MENU_BACK=0;
	private final static int MENU_REFRESH=1;
	private final static int MENU_PLAY=0;
	private final static int MENU_DELETE=1;
	//reference to the files being display
	private File[] fileList;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		try{
			menu.add(0,MENU_BACK,MENU_BACK,"Back").setIcon(R.drawable.ic_menu_back);
			menu.add(0,MENU_REFRESH,MENU_REFRESH,"Refresh").setIcon(R.drawable.ic_menu_refresh);
			menu.add(0,MENU_ABOUT,MENU_ABOUT,"About").setIcon(R.drawable.ic_menu_info_details);
			return(true);
		}catch(Exception x){
			Log.e(TAG,"onCreateOptionsMenu",x);
			return(false);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected( com.actionbarsherlock.view.MenuItem item){
		try{
			switch(item.getItemId()){
			case MENU_BACK:{this.finish();return(true);}
			case MENU_REFRESH:{this.refreshFileList();return(true);}
			case MENU_ABOUT:{this.showAboutDialog();return(true);}
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
	protected void onResume(){
		super.onResume();
		try{
			//load the episode list
			this.refreshFileList();
		}catch(Exception x){
			Log.e(TAG,"onResume",x);
			showErrorMessage(x);
		}
	}
		
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try{
        	//setup the action bar
        	getSupportActionBar().setIcon(R.drawable.logo);
        	getSupportActionBar().setTitle("");
        	getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff424542));	        
        	//set the rest of the view
        	setContentView(R.layout.activity_managedownloads);
	        ListView listViewSavedEpisodes=(ListView)findViewById(R.id.ListViewSavedPodcasts);
	        listViewSavedEpisodes.setOnItemClickListener(episodeClickListener);
	        registerForContextMenu(listViewSavedEpisodes);
		}catch(Exception x){
			Log.e(TAG,"onCreate");
			showErrorMessage(x);
		}
    }
	
	OnItemClickListener episodeClickListener=new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent,View view,int position,long id){
			try{
				playEpisode(fileList[position]);
			}catch(Exception x){
				Log.e(TAG,"episodeClickListener: position="+position,x);
				showErrorMessage(x);
			}
		}
	};
	
	@Override
	public void onCreateContextMenu(ContextMenu menu,View view,ContextMenuInfo menuInfo){
		try{
			if(view.getId()==R.id.ListViewSavedPodcasts){
				AdapterView.AdapterContextMenuInfo info=(AdapterView.AdapterContextMenuInfo)menuInfo;
				menu.setHeaderTitle(fileList[info.position].getName());
				menu.add(Menu.NONE,MENU_PLAY,MENU_PLAY,"Play");
				menu.add(Menu.NONE,MENU_DELETE,MENU_DELETE,"Delete");
			}
		}catch(Exception x){
			Log.e(TAG,"onCreateContextMenu",x);
		}
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item){
		try{
			AdapterView.AdapterContextMenuInfo info=(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
			File f=fileList[info.position];
			int menuItemIndex=item.getItemId();
			if(menuItemIndex==MENU_PLAY){
				playEpisode(f);
			}else if(menuItemIndex==MENU_DELETE){
				if(f.delete()){
					refreshFileList();
				}else{
					try{
				        new AlertDialog.Builder(this)
				   		.setTitle(R.string.app_name)
				   		.setMessage("Can't delete "+f.getName()+" for some reason.")
				   		.setPositiveButton("OK",null)
				   		.show();	
					}catch(Exception reallyBadTimes){
						Log.e(TAG,"showErrorDialog",reallyBadTimes);
					}
				}
			}
		}catch(Exception x){
			Log.e(TAG,"onContextItemSelected",x);
		}
		  return(true);
	}
	
	private void playEpisode(File f){
		try{
			Intent intent=new Intent(Intent.ACTION_VIEW);
			Uri uri=Uri.parse("file:///"+f.getAbsolutePath());
			intent.setDataAndType(uri,"audio/mpeg3");
			startActivity(intent);
		}catch(Exception x){
			Log.e(TAG,"playEpisode",x);
			showErrorMessage(x);
		}
	}
	
	private void refreshFileList(){
		try{
			File dir=new File(Utils.getDownloadPath());
			ListView listViewSavedEpisodes=(ListView)findViewById(R.id.ListViewSavedPodcasts);
			if((dir==null)||(!dir.isDirectory())){
				listViewSavedEpisodes.setVisibility(View.GONE);
				throw(new Exception("No saved episodes."));
			}else{
				this.fileList=dir.listFiles(new FilenameFilter() {
			        @Override
			        public boolean accept(File dir, String name) {
			            return(name.toLowerCase().endsWith(".mp3"));
			        }
			    });
				if((this.fileList==null)||(this.fileList.length<1)){
					listViewSavedEpisodes.setVisibility(View.GONE);
					throw(new Exception("No saved episodes."));
				}
				listViewSavedEpisodes.setVisibility(View.VISIBLE);
				SavedPodcastArrayAdapter adapter=new SavedPodcastArrayAdapter(this.getApplicationContext(),R.layout.listitem_savedpodcast,this.fileList);
				listViewSavedEpisodes.setAdapter(adapter);
			}
		}catch(Exception x){
			Log.e(TAG,"refreshFileList",x);
			showErrorMessage(x);
		}
	}
	
	private void showErrorMessage(Exception x){
		try{
			TextView textViewManageSavedEpisodesError=(TextView)findViewById(R.id.TextViewManageSavedPodcastsError);
			textViewManageSavedEpisodesError.setText(x.getMessage());
			textViewManageSavedEpisodesError.setVisibility(View.VISIBLE);
		}catch(Exception reallyBadTimes){
			Log.e(TAG,"showErrorMessage",reallyBadTimes);
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

}