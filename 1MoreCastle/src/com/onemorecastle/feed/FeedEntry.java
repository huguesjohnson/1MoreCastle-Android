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

import java.io.Serializable;

public class FeedEntry implements Serializable{
	private static final long serialVersionUID=106619886435585767L;
	private String title;
	private String link;
	private String pubDate;
	private String creator;
	private String description;
	private String enclosureLink;
	private String featuredImageLink;
	private String fileName;
	private String contentEncoded;
	
	private boolean hasEnclosure=false;
	private boolean downloaded=false;
	
	@Override
	public boolean equals(Object o){
		if((o!=null)&&(o instanceof FeedEntry)){
			FeedEntry fe=(FeedEntry)o;
			return(fe.getLink().equals(this.link));
		}
		return(false);
	}

	@Override
	public int hashCode() {
		return(this.link.hashCode());
	}

	public String getContentEncoded() {
		return contentEncoded;
	}

	public void setContentEncoded(String contentEncoded) {
		this.contentEncoded = contentEncoded;
	}

	public String getFeaturedImageLink() {
		if((featuredImageLink==null)||(featuredImageLink.length()<1)){
			String contentEncoded=this.getContentEncoded();
			//this solution works for this particular RSS feed and is not a good general solution
			int start=contentEncoded.indexOf("src=\"");
			int end=contentEncoded.indexOf("\"",start+5);
			this.featuredImageLink=contentEncoded.substring(start+5,end);
		}
		return featuredImageLink;
	}

	public void setFeaturedImageLink(String featuredImageLink) {
		this.featuredImageLink = featuredImageLink;
	}

	public String getFileName() {
		return fileName;
	}

	public boolean isDownloaded() {
		return downloaded;
	}

	public void setDownloaded(boolean downloaded) {
		this.downloaded = downloaded;
	}

	public String getSummary(){
		return(this.description);
	}
		
	public boolean isPodcast(){
		return(this.hasEnclosure);
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getPubDate() {
		return pubDate;
	}
	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getEnclosureLink() {
		return enclosureLink;
	}
	public void setEnclosureLink(String enclosureLink) {
		this.enclosureLink = enclosureLink;
		if((enclosureLink!=null)&&(enclosureLink.length()>0)){
			this.hasEnclosure=true;
			this.fileName=enclosureLink.substring(enclosureLink.lastIndexOf('/')+1);
		}
	}
}