package com.beaudry.ed.picture.replacer;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * Controller class to retrieve settings and populate the PictureReplacerSettings bean.
 * 
 * Format of file:
 * <FlickrBloggerPicReplacer>
 * 	<FlickrUserID></FlickrUserID>
 * 	<FlickrAPIKey></FlickrAPIKey>
 * 	<FlickrSecretKey></FlickrSecretKey>
 * 	<ClientID></ClientID>
 * 	<ClientSecret></ClientSecret>
 * 	<BlogID></BlogID>
 * </FlickrBloggerPicReplacer>
 * @author EdBeaudry
 *
 */
public class PictureReplacerSettingsController {

	private static String FLICKR_USER_ID = "FlickrUserID";
	private static String FLICKR_API_KEY = "FlickrAPIKey";
	private static String FLICKR_SECRET_KEY = "FlickrSecretKey";
	private static String CLIENT_ID = "ClientID";
	private static String CLIENT_SECRET = "ClientSecret";
	private static String BLOG_ID = "BlogID";

	public static PictureReplacerSettings GetPictureReplacerSettings(String path) {
		PictureReplacerSettings picReplacerSettings = new PictureReplacerSettings();

		try {
			File file = new File(path);
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(file);
			doc.getDocumentElement().normalize();
			 
			picReplacerSettings.setFlickrAPIKey(doc.getElementsByTagName(FLICKR_API_KEY).item(0).getTextContent());
			picReplacerSettings.setFlickrUserID(doc.getElementsByTagName(FLICKR_USER_ID).item(0).getTextContent());
			picReplacerSettings.setFlickrSecretKey(doc.getElementsByTagName(FLICKR_SECRET_KEY).item(0).getTextContent());
			picReplacerSettings.setClientID(doc.getElementsByTagName(CLIENT_ID).item(0).getTextContent());
			picReplacerSettings.setClientSecret(doc.getElementsByTagName(CLIENT_SECRET).item(0).getTextContent());
			picReplacerSettings.setBlogID(doc.getElementsByTagName(BLOG_ID).item(0).getTextContent());
			

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return picReplacerSettings;
	}

}
