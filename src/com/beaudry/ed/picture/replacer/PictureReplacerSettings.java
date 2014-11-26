package com.beaudry.ed.picture.replacer;
/**
 * 
 */

/**
 * @author EdBeaudry
 *
 */
public class PictureReplacerSettings {
	
	private String flickrUserID = "";
	
	private String flickrAPIKey = "";
	
	private String flickrSecretKey = "";
	
	private String clientID = "";
	
	private String clientSecret = "";
	
	private String blogID = "";

	public String getFlickrUserID() {
		return flickrUserID;
	}

	public void setFlickrUserID(String flickrUserID) {
		this.flickrUserID = flickrUserID;
	}

	public String getFlickrAPIKey() {
		return flickrAPIKey;
	}

	public void setFlickrAPIKey(String flickrAPIKey) {
		this.flickrAPIKey = flickrAPIKey;
	}

	public String getFlickrSecretKey() {
		return flickrSecretKey;
	}

	public void setFlickrSecretKey(String flickrSecretKey) {
		this.flickrSecretKey = flickrSecretKey;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getBlogID() {
		return blogID;
	}

	public void setBlogID(String blogID) {
		this.blogID = blogID;
	}

}
