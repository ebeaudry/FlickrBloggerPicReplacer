package com.beaudry.ed.picture.replacer;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.Size;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.blogger.Blogger;
import com.google.api.services.blogger.Blogger.Posts.List;
import com.google.api.services.blogger.Blogger.Posts.Patch;
import com.google.api.services.blogger.BloggerScopes;
import com.google.api.services.blogger.model.Page;
import com.google.api.services.blogger.model.PageList;
import com.google.api.services.blogger.model.Post;
import com.google.api.services.blogger.model.PostList;

public class PictureReplacer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		PictureReplacerSettings settings = PictureReplacerSettingsController
				.GetPictureReplacerSettings(args[0]);
		Flickr flickr = new Flickr(settings.getFlickrAPIKey(),
				settings.getFlickrSecretKey(), new REST());
		int converted = 0;
		Blogger blogger = InitiateBlogger(settings.getClientID(),
				settings.getClientSecret());
		try {
			replaceInPosts(settings.getBlogID(), blogger, flickr, converted);
			replaceInPages(settings.getBlogID(), blogger, flickr, converted);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Total Converted:  " + converted);

		System.out.println("Done!");
	}

	private static HttpURLConnection setupConnection(String apiString)
			throws MalformedURLException, IOException, ProtocolException {
		URL url;
		HttpURLConnection httpURLConnection;
		url = new URL(apiString);
		httpURLConnection = (HttpURLConnection) url.openConnection();
		httpURLConnection.setRequestMethod("GET");
		httpURLConnection.setInstanceFollowRedirects(false);
		return httpURLConnection;
	}

	/**
	 * Replaces pictures in a specified post
	 * 
	 * @param blogger
	 * @param blogID
	 * @param post
	 * @param flickr
	 */
	private static void ReplacePictures(Blogger blogger, String blogID,
			Post post, Flickr flickr, int converted) {
		String content = post.getContent();
		String title = post.getTitle();
		if (content != null) {
			Document doc = Jsoup.parse(content);

			Elements images = doc.select("img[src*=flickr]");
			if (images.size() > 0) {
				int changedPostPhotos = imageRename(flickr, title, images, converted);
				if (changedPostPhotos > 0) {
					post.setContent(doc.body().html());
					// The request action.
					updatePhotos(blogger, blogID, post);
				}
			}
		}

	}

	/**
	 * Renames the name of the image
	 * 
	 * @param flickr
	 * @param title
	 * @param images
	 * @return
	 */
	private static int imageRename(Flickr flickr, String title, Elements images, int converted) {
		int changedPostPhotos = 0;
		for (Element image : images) {
			HttpURLConnection httpURLConnection = null;

			try {
				httpURLConnection = setupConnection(image.attr("src"));
				if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					changedPostPhotos = replaceImageName(flickr, title,
							changedPostPhotos, image, converted);
				}

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (httpURLConnection != null) {
					httpURLConnection.disconnect();
				}
				httpURLConnection = null;

			}
		}
		return changedPostPhotos;
	}

	/**
	 * @param flickr
	 * @param title
	 * @param changedPostPhotos
	 * @param image
	 * @return
	 */
	private static int replaceImageName(Flickr flickr, String title,
			int changedPostPhotos, Element image, int converted) {
		System.out.println("Image is incorrect in " + title
				+ "; Source Name = " + image.attr("src"));
		String imageText = image.attr("src").substring(0,
				image.attr("src").lastIndexOf('.'));
		image.attr("src").substring(image.attr("src").lastIndexOf('.'));
		String[] imageSource = imageText.substring(
				imageText.lastIndexOf('/') + 1).split("_");
		PhotosInterface photo = flickr.getPhotosInterface();
		try {
			Collection<Size> photos = photo.getSizes(imageSource[0]);
			boolean changed = false;
			for (Size photoSize : photos) {
				String[] src = photoSize.getSource()
						.substring(0, photoSize.getSource().lastIndexOf('.'))
						.split("_");
				image.attr("src").substring(
						photoSize.getSource().lastIndexOf('.'));
				if (imageSource.length > 2 && src.length > 2 && src[1] != null
						&& !src[1].equals(imageSource[1])
						&& src[2].equals(imageSource[2])) {
					imageText.split("_");
					image.attr("src", photoSize.getSource());
					System.out.println("Image is changed in " + title
							+ "; Source Name = " + image.attr("src"));
					changed = true;
					converted++;
					changedPostPhotos++;

				} else if (imageSource.length == 2 && src.length == 2
						&& src[1] != null && !src[1].equals(imageSource[1])) {
					imageText.split("_");
					image.attr("src", photoSize.getSource());
					System.out.println("Image is changed in " + title
							+ "; Source Name = " + image.attr("src"));
					changed = true;
					converted++;
					changedPostPhotos++;
				}

			}
			if (!changed) {
				System.out.println("Image was not changed");
			}
		} catch (FlickrException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return changedPostPhotos;
	}

	/**
	 * Updates the photos on a post and saves the post.
	 * 
	 * @param blogger
	 * @param blogID
	 * @param post
	 */
	private static void updatePhotos(Blogger blogger, String blogID, Post post) {
		Patch postsUpdateAction;
		try {
			Post patchedPost = new Post();
			patchedPost.setId(post.getId());
			patchedPost.setContent(post.getContent());

			postsUpdateAction = blogger.posts().patch(blogID, post.getId(),
					patchedPost);

			// Restrict the result content to just the data we need.
			postsUpdateAction
					.setFields("author/displayName,content,published,title,url,id");

			// This step sends the request to the server.
			Post postOutput = postsUpdateAction.execute();

			// Now we can navigate the response.
			System.out.println("Title: " + postOutput.getTitle());
			System.out.println("Author: "
					+ postOutput.getAuthor().getDisplayName());
			System.out.println("Published: " + postOutput.getPublished());
			System.out.println("URL: " + postOutput.getUrl());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Updates the photos on the specified Page
	 * 
	 * @param blogger
	 * @param blogID
	 * @param page
	 */
	private static void updatePhotos(Blogger blogger, String blogID, Page page) {
		com.google.api.services.blogger.Blogger.Pages.Patch pageUpdateAction;

		try {
			Page patchedPage = new Page();
			patchedPage.setId(page.getId());
			patchedPage.setContent(page.getContent());

			pageUpdateAction = blogger.pages().patch(blogID, page.getId(),
					patchedPage);

			// Restrict the result content to just the data we need.
			pageUpdateAction
					.setFields("author/displayName,content,published,title,url,id");

			// This step sends the request to the server.
			Page pageOutput = pageUpdateAction.execute();

			// Now we can navigate the response.
			System.out.println("Title: " + pageOutput.getTitle());
			System.out.println("Author: "
					+ pageOutput.getAuthor().getDisplayName());
			System.out.println("Published: " + pageOutput.getPublished());
			System.out.println("URL: " + pageOutput.getUrl());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Initializes the blogger
	 * 
	 * @param clientId
	 * @param clientSecret
	 * @return blogger with authorization credentials
	 */
	private static Blogger InitiateBlogger(String clientId, String clientSecret) {
		// Configure the Java API Client for Installed Native App
		HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
		JsonFactory JSON_FACTORY = new JacksonFactory();
		// Configure the Installed App OAuth2 flow.
		Credential credential;
		Blogger blogger = null;
		try {
			credential = authorize(HTTP_TRANSPORT, JSON_FACTORY,
					new LocalServerReceiver(), clientId, clientSecret,
					Arrays.asList(BloggerScopes.BLOGGER));

			// Construct the Blogger API access facade object.
			Blogger.Builder builder = new Blogger.Builder(HTTP_TRANSPORT,
					JSON_FACTORY, credential);
		//	builder.setApplicationName("Blogger-PostsSearch-Snippet/1.0");
			builder.setApplicationName("FlickrBloggerPicReplacer/1.0");

			blogger = builder.setHttpRequestInitializer(credential).build();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return blogger;
	}

	/**
	 * Searches for all pictures in posts on a Blog
	 * 
	 * @param blogID
	 * @param blogger
	 * @param flickr
	 * @throws IOException
	 */
	private static void replaceInPosts(String blogID, Blogger blogger,
			Flickr flickr, int converted) throws IOException {
		// The request action.
		List postsSearchAction = blogger.posts().list(blogID);

		// Restrict the result content to just the data we need.
		postsSearchAction.setFields("items(content,id,title),nextPageToken");

		// This step sends the request to the server.
		PostList posts = postsSearchAction.execute();

		// Now we can navigate the response.
		int postCount = 0;
		int pageCount = 0;

		// Now we can navigate the response.
		while (posts.getItems() != null && !posts.getItems().isEmpty()) {
			postCount += posts.getItems().size();
			for (Post post : posts.getItems()) {
				ReplacePictures(blogger, blogID, post, flickr, converted);
			}

			// Pagination logic
			String pageToken = posts.getNextPageToken();
			if (pageToken == null || ++pageCount >= 5) {
				break;
			}
			System.out.println("-- Next page of posts");
			postsSearchAction.setPageToken(pageToken);
			posts = postsSearchAction.execute();

		}
		System.out.println("Total Posts:  " + postCount);
	}

	/**
	 * Replaces the pictures in all Pages
	 * 
	 * @param blogID
	 * @param blogger
	 * @param flickr
	 * @throws IOException
	 */
	private static void replaceInPages(String blogID, Blogger blogger,
			Flickr flickr, int converted) throws IOException {
		// The request action.
		com.google.api.services.blogger.Blogger.Pages.List postsSearchAction = blogger
				.pages().list(blogID);// (BLOG_ID, "flickr");

		// Restrict the result content to just the data we need.
		postsSearchAction.setFields("items(content,id,title)");

		// This step sends the request to the server.
		PageList pages = postsSearchAction.execute();

		// Now we can navigate the response.
		int pageCount = 0;

		// Now we can navigate the response.
		if (pages.getItems() != null && !pages.getItems().isEmpty()) {
			pageCount += pages.getItems().size();
			for (Page page : pages.getItems()) {
				ReplacePictures(blogger, blogID, page, flickr, converted);
			}
		}
		System.out.println("Total Pages:  " + pageCount);
	}

	/**
	 * Replaces pictures in a specified page
	 * 
	 * @param blogger
	 * @param blogID
	 * @param page
	 * @param flickr
	 */
	private static void ReplacePictures(Blogger blogger, String blogID,
			Page page, Flickr flickr, int converted) {
		String content = page.getContent();
		String title = page.getTitle();
		if (content != null) {
			Document doc = Jsoup.parse(content);

			Elements images = doc.select("img[src*=flickr]");
			if (images.size() > 0) {
				int changedPostPhotos = imageRename(flickr, title, images, converted);
				if (changedPostPhotos > 0) {
					page.setContent(doc.body().html());
					// The request action.
					updatePhotos(blogger, blogID, page);
				}
			}
		}

	}

	public static Credential authorize(HttpTransport transport,
			JsonFactory jsonFactory, VerificationCodeReceiver receiver,
			String clientId, String clientSecret, Collection<String> scopes)
			throws Exception {
		try {
			String redirectUri = receiver.getRedirectUri();
			// redirect to an authorization page
			GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
					transport, jsonFactory, clientId, clientSecret, scopes)
					.build();
			browse(flow.newAuthorizationUrl().setRedirectUri(redirectUri)
					.build());
			// receive authorization code and exchange it for an access token
			String code = receiver.waitForCode();
			GoogleTokenResponse response = flow.newTokenRequest(code)
					.setRedirectUri(redirectUri).execute();
			// store credential and return it
			return flow.createAndStoreCredential(response, null);
		} finally {
			receiver.stop();
		}
	}

	/**
	 * Open a browser at the given URL. \ Taken from OAuth2Native.java
	 */
	private static void browse(String url) {
		// first try the Java Desktop
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Action.BROWSE)) {
				try {
					desktop.browse(URI.create(url));
					return;
				} catch (IOException e) {
					// handled below
				}
			}
		}
		// Next try rundll32 (only works on Windows)
		try {
			Runtime.getRuntime().exec(
					"rundll32 url.dll,FileProtocolHandler " + url);
			return;
		} catch (IOException e) {
			// handled below
		}
		try {
			Runtime.getRuntime().exec(new String[] { "google-chrome", url });
			return;
		} catch (IOException e) {
			// handled below
		}
		// Finally just ask user to open in their browser using copy-paste
		System.out.println("Please open the following URL in your browser:");
		System.out.println("  " + url);
	}

}
