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

package com.onemorecastle.feed;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class FeedParserHandler extends DefaultHandler{
	private final static String TAG="FeedParserHandler";
	private final static String TAG_ITEM="item";
	private final static String TAG_TITLE="title";
	private final static String TAG_ENCLOSURE="enclosure";
	private final static String TAG_DESCRIPTION="description";
	private final static String TAG_PUBDATE="pubDate";
	private final static String TAG_LINK="link";
	private final static String TAG_CREATOR="creator";
	private final static String TAG_CONTENT_ENCODED="encoded";
	private final static String ATTRIBUTE_TYPE="type";
	private final static String ATTRIBUTE_URL="url";
	private final static String TYPE_IMAGE="image/jpg";
	private final static String TYPE_AUDIO_MPEG="audio/mpeg";
    private boolean readThis=false;
    private FeedEntry currentEntry=null;
    private ArrayList<FeedEntry> entryList=new ArrayList<FeedEntry>();
    private StringBuilder characters=new StringBuilder();

    public FeedParserHandler(){
    	super();
    }
    
    public ArrayList<FeedEntry> getFeedEntryList(){
    	return(this.entryList);
    }
	
	@Override
	public void startDocument() throws SAXException{
		super.startDocument();
	}

    @Override
    public void characters(char[] ch,int start,int length) throws SAXException{
        super.characters(ch,start,length);
        if(readThis){
        	this.characters.append(ch,start,length);
    	}
    }
 
	@Override
	public void startElement(String uri,String localName,String qName,Attributes attributes) throws SAXException{
		super.startElement(uri,localName,qName,attributes);
		try{
			String tagName=localName;
			if((tagName==null)||(tagName.length()<1)){
				tagName=qName;
			}
			if(tagName.equalsIgnoreCase(TAG_ITEM)){
				this.currentEntry=new FeedEntry();
			}
			if(this.currentEntry!=null){
				if(tagName.equalsIgnoreCase(TAG_ENCLOSURE)){
					String enclosureType=attributes.getValue(ATTRIBUTE_TYPE);
					String enclosureUrl=attributes.getValue(ATTRIBUTE_URL);
					if(enclosureType.equalsIgnoreCase(TYPE_IMAGE)){
						this.currentEntry.setFeaturedImageLink(enclosureUrl);
					}else if(enclosureType.equalsIgnoreCase(TYPE_AUDIO_MPEG)){
						this.currentEntry.setEnclosureLink(enclosureUrl);
					}else{
						Log.e(TAG,"startElement - unsupported type: "+enclosureType);
					}
				}else if(tagName.equalsIgnoreCase(TAG_TITLE)){
					readThis=true;
					this.characters.setLength(0);
				}else if(tagName.equalsIgnoreCase(TAG_DESCRIPTION)){
					readThis=true;
					this.characters.setLength(0);
				}else if(tagName.equalsIgnoreCase(TAG_CONTENT_ENCODED)){
					readThis=true;
					this.characters.setLength(0);
				}else if(tagName.equalsIgnoreCase(TAG_PUBDATE)){
					readThis=true;
					this.characters.setLength(0);
				}else if(tagName.equalsIgnoreCase(TAG_LINK)){
					readThis=true;
					this.characters.setLength(0);
				}else if(tagName.equalsIgnoreCase(TAG_CREATOR)){
					readThis=true;
					this.characters.setLength(0);
				}
			}
		}catch(Exception x){
			Log.e(TAG,"startElement",x);
		}
	}

	@Override
	public void endElement(String uri,String localName,String qName) throws SAXException{
		super.endElement(uri,localName,qName);
		try{
			String tagName=localName;
			if((tagName==null)||(tagName.length()<1)){
				tagName=qName;
			}
			if(this.currentEntry!=null){
				if(tagName.equalsIgnoreCase(TAG_ITEM)){
						this.entryList.add(this.currentEntry);
				}else if(tagName.equalsIgnoreCase(TAG_TITLE)){
					this.currentEntry.setTitle(this.characters.toString());
					readThis=false;
				}else if(tagName.equalsIgnoreCase(TAG_DESCRIPTION)){
					this.currentEntry.setDescription(this.characters.toString());
					readThis=false;
				}else if(tagName.equalsIgnoreCase(TAG_CONTENT_ENCODED)){
					this.currentEntry.setContentEncoded(this.characters.toString());
					readThis=false;
				}else if(tagName.equalsIgnoreCase(TAG_PUBDATE)){
					this.currentEntry.setPubDate(this.characters.toString());
					readThis=false;
				}else if(tagName.equalsIgnoreCase(TAG_LINK)){
					this.currentEntry.setLink(this.characters.toString());
					readThis=false;
				}else if(tagName.equalsIgnoreCase(TAG_CREATOR)){
					this.currentEntry.setCreator(this.characters.toString());
					readThis=false;
				}
			}			
		}catch(Exception x){
			Log.e(TAG,"endElement",x);
		}
	}    
}