package controllers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.api.libs.json.JsArray;
import play.data.Form;
import play.mvc.*;

import play.libs.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import static play.libs.Json.toJson;

import views.html.*;
import weibo4j.Timeline;
import weibo4j.Weibo;
import weibo4j.http.ImageItem;
import weibo4j.model.Status;
import models.Album;
import models.Photo;
import models.User;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

import controllers.Application.Login;

@Security.Authenticated(Secured.class)
public class MainProgram extends Controller {

	/*
	 *  Form Bean Account
	 */
	
    public static class Account {
        
        public String email;
        public String password;
        public String selfDescription;
        public String displayName;
        public Date createdTime;
        public String gender;
        
        /*
        public String validate() {
        	User user = User.authenticate(email, password);
            if(user == null) {
                return "Invalid username or password";
            }
            else {
            	displayName = user.displayName;
            	return null;
            }
        }
        */
    }
	
	
	/**
	 *  Goto index page
	 * 
	 * @return
	 */
	
	public static Result index() {
		return redirect(routes.MainProgram.home());
	}

	/**
	 *  Goto album page displaying all created albums for user logged in.
	 * 
	 * @return
	 */
	
	public static Result home() {
		List<Album> albumList = AlbumManager.retrieveAlbums(session("email"));
		return ok(home.render(albumList));
	}
	
	/**
	 *  Display account page
	 * 
	 * @return
	 */
	
	public static Result account(String tabKey) {
		System.out.println("tabKey: " + tabKey );
		Form<Account> accountForm = form(Account.class).bindFromRequest();
		User user = User.findByEmail(session("email"));
		Account aAccount = new Account();
		aAccount.email = user.email;
		aAccount.createdTime = user.createdTime;
		aAccount.gender = user.gender;
		aAccount.selfDescription = user.selfDescription;
		aAccount.displayName = user.displayName;
		accountForm = accountForm.fill(aAccount);
		return ok(account.render(accountForm, tabKey));
	}
	
	
	/**
	 *  Update account information
	 */
	
	public static Result updateAccountInfo(){
		Form<Account> accountForm = form(Account.class).bindFromRequest();
		if (accountForm.hasErrors())
			return badRequest(account.render(accountForm,null));
		
		User user = User.findByEmail(session("email"));
		String pwd = accountForm.get().password;
		if(!pwd.isEmpty()){
			user.password = pwd;
			user.save();
			flash("success", "Your password has been changed");
			return redirect(routes.MainProgram.account("password"));
		}
		user.displayName = accountForm.get().displayName;
		user.selfDescription = accountForm.get().selfDescription;
		user.gender = accountForm.get().gender;
		user.save();
		flash("success", "Your account information has been saved");
		return redirect(routes.MainProgram.account("prof"));
	}
	
	/**
	 *  Goto album page displaying all albums
	 * 
	 * @return
	 */
	
	public static Result album() {
		return redirect(routes.MainProgram.home());
	}

	/**
	 *  Create a new album
	 * 
	 * @param albumName
	 * @return
	 */
	public static Result addAlbum(String albumName) {

		if(AlbumManager.createAlbumDirectory(session("email"), albumName)){
			return redirect(routes.MainProgram.home());
		}
		else
			return internalServerError();
	}
	
	
	/**
	 *  Delete an album
	 * 
	 * @param albumId
	 * @return
	 */
	
	public static Result deleteAlbum(String albumId) {
		if(AlbumManager.deleteAlbumDirectory(session("email"), albumId)){
			return redirect(routes.MainProgram.home());
		}
		else
			return internalServerError();
	}

	/**
	 *  Goto photo page displaying photos for selected album
	 * 
	 * @param albumId
	 * @return
	 */
	
	public static Result albumPhotos(String albumId){
		List<Photo> photoList = PhotoManager.retrieveAlbumPhotos(albumId);
		session("albumid", albumId);
		String email = session("email");
		return ok(photo.render(User.findByEmail(email), photoList));
	}
	
	
	public static Result uploadPage() {
		if(session("albumid")==null || session("albumid").isEmpty())
			return redirect(routes.MainProgram.album());
		return ok(upload.render());
	}

	public static Result view() {
		return ok(view.render());
	}
	
	
	/**
	 *   Upload photos
	 * @return
	 */
	
	public static Result uploadFiles() {
		
		if(session("albumid")==null || session("albumid").isEmpty())
			return redirect(routes.MainProgram.album());
		
		MultipartFormData body = request().body().asMultipartFormData();

		List<FilePart> pictures = body.getFiles();
		String fileName = "";
		long size = 0;
		String absurl = "";
		String url = "";
		List<Map> resultList = new ArrayList<Map>();
		
		for (FilePart pic : pictures) {
			fileName = pic.getFilename();
			String contentType = pic.getContentType();
			File file = pic.getFile();
			
			try{
				PhotoManager.saveUploadedPhoto(file, fileName, session("email"), session("albumid"));
			}catch(IOException e){
				
			}
			
			size = file.length();
			absurl = file.getAbsolutePath();
			url = file.getPath();
			String url1 = file.getPath();
			url = url.substring(0, url.lastIndexOf("\\")) + "\\" + fileName;
			Map<String, String> result = new HashMap<String, String>();
			result.put("name", fileName);
			result.put("size", Long.toString(size));
			result.put("url", url);
			result.put("thumbnail_url", url);
			result.put("delete_url", url);
			result.put("delete_type", "ADD");
			resultList.add(result);
		}


		JsonNode jnode = toJson(resultList);

		return ok(jnode);

	}
	
	/**
	 *   Delete a photo from selected album
	 * @param id
	 * @return
	 */
	public static Result deletePhoto(String id){
		PhotoManager.deletePhoto(session("email"), id);
		String albumId = session("albumid");
		return redirect(routes.MainProgram.albumPhotos(albumId));
	}

	/**
	 *  Update photo title or description using ajax request
	 *   
	 * @return
	 */
	
	public static Result updatePhotoTitleDes(){
		
		String title = "", des="";
		String photoId = null;
		Map<String, String[]> asFormUrlEncoded = request().body().asFormUrlEncoded();
		if (asFormUrlEncoded != null) {
			for (String key : asFormUrlEncoded.keySet()) {
				String[] value = asFormUrlEncoded.get(key);
				if (value.length == 1 && "photoId".equals(key)) 
					photoId = value[0];
				else if (value.length == 1 && "title".equals(key)) 
					title = value[0];
				else if(value.length ==1 && "des".equals(key))
					des = value[0];
			}
		}
		ObjectNode result = Json.newObject();
		System.out.println("id: " + photoId);
		System.out.println("title: " + title);
		System.out.println("Des: " + des);
		PhotoManager.updatePhotoTitleDes(session("email"), photoId, title, des);
		result.put("title", title);
		result.put("des", des);
		return ok(result);
	}
	
	/** post a weibo 
	 * 
	 * @return
	 */
	public static Result postPicToWeibo(){
		String urlString = "https://upload.api.weibo.com/2/statuses/upload.json?cess_token="+session("access_token");
		//URLEncoder.encode(urlString, "utf-8");
		//URL url = new URL(urlString);
		//HttpURLConnection conn = (HttpURLConnection) url.openConnection();		
		try{
			Weibo weibo = new Weibo();
			weibo.setToken(session("access_token"));
			try{
				
				String picUrl = PhotoManager.getBigPhotoURL(session("email"), "3ii91ty9r79ciq02gy0k");
				byte[] content= readFileImage(picUrl);
				System.out.println("content length:" + content.length);
				ImageItem pic=new ImageItem("pic",content);
	
				String s=java.net.URLEncoder.encode( "测试微博","utf-8");
				Timeline tl = new Timeline();
				weibo4j.model.Status status=tl.UploadStatus(s, pic);
	
				System.out.println("Successfully upload the status to ["
						+status.getText()+"].");
			}catch(Exception e1){
				e1.printStackTrace();
			}
		}catch(Exception ioe){
			System.out.println("Failed to read the system input.");
		}
		return ok();
	}
	
	/**
	 *  Read an image file
	 *  
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	
	public static byte[] readFileImage(String filename)throws IOException{
		BufferedInputStream bufferedInputStream=new BufferedInputStream(
				new FileInputStream(filename));
		int len =bufferedInputStream.available();
		byte[] bytes=new byte[len];
		int r=bufferedInputStream.read(bytes);
		if(len !=r){
			bytes=null;
			throw new IOException("读取文件不正确");
		}
		bufferedInputStream.close();
		return bytes;
	}
	
}
