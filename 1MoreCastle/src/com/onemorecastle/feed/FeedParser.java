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

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class FeedParser{
	private static SAXParserFactory saxFactory;
	private static SAXParser saxParser;
	
	private synchronized static SAXParserFactory getSaxFactory(){
		if(saxFactory==null){
			saxFactory=SAXParserFactory.newInstance();
		}
		return(saxFactory);
	}
	
	private synchronized static SAXParser getSaxParser() throws Exception{
		if(saxParser==null){
			saxParser=getSaxFactory().newSAXParser();
		}
		return(saxParser);
	}
	
	public static ArrayList<FeedEntry> parseFeed(InputStream in) throws Exception{
		FeedParserHandler handler=new FeedParserHandler();
		getSaxParser().parse(in,handler);
		return(handler.getFeedEntryList());
	}
}
