package controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

import models.Album;
import models.Photo;
import models.User;
import static org.imgscalr.Scalr.resize;
import static util.Util.gen;

public class PhotoManager {
	
	private static final String ROOT_DIR = "public//albums";
	private static final int PHOTO_ID_LENGTH = 20;
	private static final String DEFAULT_PHOTO_RIGHT = "PUBLIC";
	
	public static List<Photo> retrieveAlbumPhotos(String albumId){
		return Photo.findPhotosOfAlbum(albumId);
	}
	
	public static void saveUploadedPhoto(File file, String fileName, String email, String albumId) throws IOException{
		User user = User.findByEmail(email);
		Album album = Album.findAlbumForUser(user.userId, albumId);
		
		Photo photo = new Photo();
		photo.id = gen(PHOTO_ID_LENGTH);
		photo.uploadedTime = new Date();
		photo.right = DEFAULT_PHOTO_RIGHT;
		photo.user = user;
		photo.album = album;
		photo.name = fileName;
		
		int sum = album.photoSum;
		

		
		String uploadedLocation = user.userDirName + "//" + albumId;
		String smallPhotoLocation = user.userDirName + "//" + albumId + "//" + "small";
		File dir = new File(ROOT_DIR + "//" + uploadedLocation);
		
		if(!dir.exists())
			throw new IOException();
		
		// save uploaded file into corresponding directory
		
		BufferedImage image = ImageIO.read(file);
		String fileExt = fileName.substring(fileName.lastIndexOf('.') + 1);
		
		photo.type = fileExt;
		photo.path = uploadedLocation + "//" + photo.id + "." + fileExt;
		
		File bigPhoto = new File(ROOT_DIR + "//" + photo.path);
		ImageIO.write(image, fileExt, bigPhoto);
		photo.path = photo.path.replaceAll("//", "/");
		
		// save small file
		File smallDir = new File(ROOT_DIR + "//" + smallPhotoLocation);
		
		if (!smallDir.exists())
			smallDir.mkdir();
		
		if(image.getHeight() > image.getWidth())
			image = resize(image, Scalr.Method.SPEED, Scalr.Mode.FIT_TO_HEIGHT,
		               260, Scalr.OP_ANTIALIAS);
		else
			image = resize(image, Scalr.Method.SPEED, Scalr.Mode.FIT_TO_WIDTH,
		               260, Scalr.OP_ANTIALIAS);
		
		photo.smallPath = smallPhotoLocation + "//" + photo.id + "_small." + fileExt;
		
		File smallPhoto = new File(ROOT_DIR + "//" + photo.smallPath);
		ImageIO.write(image, fileExt, smallPhoto);
		photo.smallPath = photo.smallPath.replaceAll("//", "/");
		
		photo.save();
		photo.album.photoSum = ++sum;
		album.save();
	}
	
	public static void deletePhoto(String email, String id){
		User user = User.findByEmail(email);
    	Photo photo = Photo.findPhotoOfUser(user.userId, id);
    	String bigLocation = photo.path.replace("/", "//");
    	String smallLocation = photo.smallPath.replace("/", "//");
    	File big,small;
    	boolean removed = false;
    	
    	if( (big = new File(ROOT_DIR + "//" + bigLocation)).exists()){
    		removed = big.delete() || removed;
    	}
    	
    	if ((small = new File(ROOT_DIR + "//" + smallLocation)).exists()){
    		removed = small.delete() || removed;    		
    	}
    	if (removed)
    		photo.delete();
    	
	}
	
	public static void updatePhotoTitleDes(String email, String photoId, String title, String des){
		User user = User.findByEmail(email);
    	Photo photo = Photo.findPhotoOfUser(user.userId, photoId);
    	photo.name = title;
    	photo.description = des;
    	photo.save();
	}
	
	public static String getBigPhotoURL(String email, String id){
		User user = User.findByEmail(email);
    	Photo photo = Photo.findPhotoOfUser(user.userId, id);
    	String bigLocation = photo.path.replace("/", "//");
    	return ROOT_DIR + "//" + bigLocation;
	}
}
