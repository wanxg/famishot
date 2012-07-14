package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import play.data.validation.*;
import play.data.format.*;
import play.db.ebean.Model;

@Entity
@Table(name="album")
public class Album extends Model{
	
	@Id
    @Column(name="ALBUM_ID")
	public String id;
	
	@Constraints.Required
	@Column(name="ALBUM_NAME")
	public String name;
	
    @Formats.DateTime(pattern="yyyy-MM-dd hh:mm:ss")
    @Column(name="CREATE_TIME")
    public Date createdTime;
    
    @Column(name="PHOTO_SUM")
    public int photoSum;

    @Constraints.Required
    @Column(name="ALBUM_RIGHT")
    public String right;
    
    @ManyToOne
    @JoinColumn (name="USER_ID", referencedColumnName="USER_ID")
    public User user;
    
	public static Model.Finder<String,Album> find = new Model.Finder(String.class, Album.class);
    
    /**
     * Retrieve all albums for a user.
     */
    public static List<Album> findAll(int userId) {
        return find.where().eq("user.userId", userId).findList();
    }
    
    
    /**
     * 
     *  Retrieve an album for a user
     */
	
    public static Album findAlbumForUser(int userId, String albumId){
    	return find.where()
    			.eq("user.userId", userId)
    			.eq("id", albumId)
    			.findUnique();
    }
    
    
    /**
     * 
     *  Delete an album for a user
     */
    
    public static void deleteAlbumForUser(int userId, String albumId){
    	findAlbumForUser(userId, albumId).delete();
    }
    
    
	public String validate() {
		if (name != null)
			return null;
		else
			return "Album name emtpy";
	}
	
	public Album(String name){
		this.name = name;
	}
	
	public Album(){
	}
}
