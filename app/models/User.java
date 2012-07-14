package models;

import static util.Util.gen;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

@Entity
@Table(name="user")
public class User extends Model{
	
	private static final int USER_DIR_NAME_LENGTH = 10;
	private static final String ROOT_DIR = "albums";
	
    @Id
    @Column(name="USER_ID")
	public int userId;
	
	@Constraints.Required
	@Formats.NonEmpty
	public String email;
	
	@Column(name="DISPLAY_NAME")
	public String displayName;
	
	@Constraints.Required
	@Formats.NonEmpty
	public String password;
	
	@Column(name="USER_DIR_NAME")
	@Constraints.Required
	public String userDirName;
	
	@Column(name="PROFILE_IMG_URL")
	public String profileImageUrl;
	
	@Column(name="SELF_DES")
	public String selfDescription;
	
    @Formats.DateTime(pattern="yyyy-MM-dd hh:mm:ss")
    @Column(name="CREATED_TIME")
	public Date createdTime;
    
    @Column(name="GENDER")
    public String gender;
	
	public static Model.Finder<String,User> find = new Model.Finder(String.class, User.class);
	
	public User(String email, String password){
		this.email = email;
		this.password = password;
	}
	
    public static User authenticate(String email, String password) {
        return find.where()
                .eq("email", email)
                .eq("password", password)
                .findUnique();
    }
	
    public static User RegisterUser(String email, String password){
    	
    	User registeredUser = new User(email, password);
    	int sum = find.all().size();
    	registeredUser.userId = ++sum;
		String userDirName = email.substring(0, email.indexOf('@')) + gen(USER_DIR_NAME_LENGTH);
    	registeredUser.userDirName = userDirName;
    	registeredUser.save();
    	return registeredUser;
    	
    }
    
    /**
     * Retrieve a User from email.
     */
    public static User findByEmail(String email) {
        return find.where().eq("email", email).findUnique();
    }
    
    /**
     * Retrieve all users.
     */
    public static List<User> findAll() {
        return find.all();
    }
    
    /**
     * Retrieve user directory name by email.
     */
    public static String findUserDirName(String email) {
        return find.where()
        		.eq("email", email)
        		.findUnique()
        		.userDirName;
    }
}
