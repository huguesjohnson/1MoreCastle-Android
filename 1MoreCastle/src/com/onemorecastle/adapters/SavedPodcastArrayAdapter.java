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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.onemorecastle.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SavedPodcastArrayAdapter extends ArrayAdapter<File>{
	private Context context;
	private int resourceId;
	private File[] fileList;
	
	public SavedPodcastArrayAdapter(Context context,int resourceId,File[] fileList){
		super(context,resourceId,fileList);
		this.context=context;
		this.resourceId=resourceId;
		this.fileList=fileList;
	}
	
	@Override
	public View getView(int position,View convertView,ViewGroup parent){
		try{
			LayoutInflater inflater=(LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout=inflater.inflate(resourceId,null);
			File f=this.fileList[position];
			TextView title=(TextView)layout.findViewById(R.id.listitem_savedpodcast_title);
			title.setText(f.getName());
			TextView date=(TextView)layout.findViewById(R.id.listitem_savedpodcast_date);
			String simpleDate=new SimpleDateFormat("MM-dd-yyyy",Locale.getDefault()).format(f.lastModified());
			date.setText(simpleDate);
			TextView size=(TextView)layout.findViewById(R.id.listitem_savedpodcast_size);
			size.setText(readableFileSize(f.length()));
			return(layout);
		}catch(Exception x){
			Log.e("SavedEpisodeListArrayAdapter.getView",x.getMessage(),x);
			return(null);
		}
	}
	
	//blatantly stolen from http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
	public static String readableFileSize(long size) {
	    try{
			if(size<=0){return("0");}
		    final String[] units=new String[]{"B","KB","MB","GB","TB"};
		    int digitGroups=(int)(Math.log10(size)/Math.log10(1024));
		    return(new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups))+" "+units[digitGroups]);
	    }catch(Exception x){
			Log.e("SavedEpisodeListArrayAdapter.readableFileSize","size="+size,x);
			return("0");
	    }
	}
}