package controllers;

import static util.Util.gen;
import java.io.File;
import java.util.*;
import models.Album;
import models.User;


/**
 * 
 *  A manager class managing album and corresponding directory on disk for an user.
 * 
 * @author Administrator
 *
 */

public class AlbumManager {

	private static final String ROOT_DIR = "public//albums";
	private static final int ALBUM_NAME_LENGTH = 20;
	private static final String DEFAULT_ALBUM_RIGHT = "PUBLIC";

	
	/**
	 *  Create an empty directory storing albums for a newly registered user. 
	 *  
	 * @param email
	 * @return
	 */
	
	public static boolean createUserDirectory(String userDirName) {

		initAlbumDirectory();
		
		String userDirPath = ROOT_DIR + "//" + userDirName;

		return new File(userDirPath).mkdir();
	}

	
	/**
	 *   Checking if the root directory exits, if not, create it.
	 */
	
	private static void initAlbumDirectory() {
		File file = new File(ROOT_DIR);
		if (!file.exists())
			file.mkdir();
	}


	/**
	 *  retrieve all albums for an user identified by an email
	 *  
	 * @param email
	 * @return
	 */
	
	public static List<Album> retrieveAlbums(String email) {
		User user = User.findByEmail(email);
		List<Album> tempList = Album.findAll(user.userId);
		List<Album> albumList = new ArrayList<Album>();
		String albumDirPath;
		for(Album a : tempList){
			albumDirPath = ROOT_DIR + "//" + user.userDirName + "//" + a.id;
			if((new File(albumDirPath)).exists())
				albumList.add(a);
				
		}
		return albumList;
	}
	
	
	/**
	 *  Create a new album for an user and create a directory on disk.
	 *   
	 * @param email
	 * @param albumName
	 * @return
	 */
	
	public static boolean createAlbumDirectory(String email, String albumName) {
		Album album = new Album();
		album.id = gen(ALBUM_NAME_LENGTH);
		album.name = albumName;
		album.photoSum = 0;
		album.right = DEFAULT_ALBUM_RIGHT;
		album.createdTime =  new Date();
		album.user = User.findByEmail(email);
		album.save();
		String albumDirPath = ROOT_DIR + "//" + User.findUserDirName(email) + "//" + album.id;
		return new File(albumDirPath).mkdir();
	}


	/**
	 * Delete an album of an user from db and remove the corresponding directory from disk.
	 * 
	 * @param email
	 * @param albumId
	 * @return
	 */

	public static boolean deleteAlbumDirectory(String email, String albumId) {
		User user = User.findByEmail(email);
		Album album = Album.findAlbumForUser(user.userId, albumId);
		album.delete();
		String albumDirPath = ROOT_DIR + "//" + user.userDirName + "//" + albumId;
		File file = new File(albumDirPath);
		return file.delete();
	}

}
