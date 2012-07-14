package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

@Entity
@Table(name="photo")
public class Photo extends Model{
	
	@Id
	@Column(name="PHOTO_ID")
	public String id;
	
	@Column(name="PHOTO_NAME")
	@Constraints.Required
	public String name;
	
	@Column(name="PHOTO_DES")
	public String description;
	
    @Formats.DateTime(pattern="yyyy-MM-dd hh:mm:ss")
    @Column(name="UPLOAD_TIME")
    @Constraints.Required
	public Date uploadedTime;
    
    @Column(name="PHOTO_RIGHT")
    @Constraints.Required
    public String right;
	
	@ManyToOne
	@JoinColumn (name="ALBUM_ID", referencedColumnName="ALBUM_ID")
	public Album album;
	
	@ManyToOne
	@JoinColumn (name="USER_ID", referencedColumnName="USER_ID")
	public User user;
	
    @Column(name="PHOTO_TYP")
    @Constraints.Required
	public String type;
	
    
    @Column(name="PHOTO_PATH")
    @Constraints.Required
	public String path;
    
    
    @Column(name="PHOTO_SMLPATH")
    @Constraints.Required
	public String smallPath;
    
	public static Model.Finder<String,Photo> find = new Model.Finder(String.class, Photo.class);

    /**
     * Retrieve all photos of an album.
     */
    public static List<Photo> findPhotosOfAlbum(String albumId) {
        return find.where().eq("album.id", albumId).findList();
    }
	
    
    /**
     * Retrieve all photos of an user.
     */
    public static List<Photo> findPhotosOfUser(String userId) {
        return find.where().eq("user.userId", userId).findList();
    }
    
    /**
     * Retrieve all photos of an user.
     */
    public static Photo findPhotoOfUser(int userId, String photoId) {
        return find.where()
    			.eq("user.userId", userId)
    			.eq("id", photoId)
    			.findUnique();
    }
}
