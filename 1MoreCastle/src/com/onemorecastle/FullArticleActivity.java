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

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.onemorecastle.feed.FeedEntry;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.TextView;

public class FullArticleActivity extends SherlockActivity{
	private final static String TAG="FullArticleActivity";
	FeedEntry entry=null;
	
	//menu constants
	private final static int MENU_BACK=0;
	private final static int MENU_SHARE=1;
	private final static int MENU_ABOUT=2;

	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try{
        	//setup the action bar
        	getSupportActionBar().setIcon(R.drawable.logo);
        	getSupportActionBar().setTitle("");
        	getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff424542));
        	//get the article that was selected
        	Intent intent=getIntent();
        	this.entry=(FeedEntry)intent.getBundleExtra("FeedEntry").getSerializable("FeedEntry");
            setContentView(R.layout.activity_fullarticle);
            //title
            TextView fullArticleTitle=(TextView)findViewById(R.id.activity_fullarticle_title);
            fullArticleTitle.setText(this.entry.getTitle());
            //webview
            WebView fullArticleBody=(WebView)findViewById(R.id.activity_fullarticle_body);
            fullArticleBody.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
            String contentEncoded=this.entry.getContentEncoded();
            int indexOf=contentEncoded.indexOf("<div id=\"hidden_android\">");
            if(indexOf>0){
            	contentEncoded=contentEncoded.substring(0,indexOf);
            }
            //thanks to http://stackoverflow.com/questions/8572818/utf-8-encoding-on-webview-and-ics
            fullArticleBody.loadDataWithBaseURL(null, "<?xml version='1.0' encoding='utf-8'?><html>"+contentEncoded+"</html>", "text/html", "UTF-8",null);
        }catch(Exception x){
			Log.e(TAG,"onCreate",x);
        }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		try{
			menu.add(0,MENU_BACK,MENU_BACK,"Back").setIcon(R.drawable.ic_menu_back);
			if(this.entry!=null){
				menu.add(0,MENU_SHARE,MENU_SHARE,"Share").setIcon(R.drawable.ic_menu_share);
			}
			menu.add(0,MENU_ABOUT,MENU_ABOUT,"About").setIcon(R.drawable.ic_menu_info_details);
			return(true);
		}catch(Exception x){
			Log.e(TAG,"onCreateOptionsMenu",x);
			return(false);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		try{
			switch(item.getItemId()){
			case MENU_BACK:{
				this.finish();
				return(true);
			}
			case MENU_ABOUT:{
				this.showAboutDialog();
				return(true);
			}
			case MENU_SHARE:{
				try{
					Intent shareIntent=new Intent(android.content.Intent.ACTION_SEND);				
					shareIntent.setType("text/plain");
					shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,this.entry.getTitle());
					shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,this.entry.getTitle()+" "+this.entry.getLink());
					startActivity(Intent.createChooser(shareIntent,"Share via"));
				}catch(Exception x){
					Log.e(TAG,"onOptionsItemSelected - case MENU_SHARE",x);
					return(false);
				}
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
