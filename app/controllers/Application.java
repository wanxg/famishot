package controllers;


import java.io.*;
import java.net.*;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;

import models.*;

import play.*;
import play.mvc.*;
import play.data.*;
import views.html.*;
import weibo4j.Users;
import weibo4j.Weibo;
import weibo4j.model.WeiboException;


public class Application extends Controller {
	
	private static final String WEIBO_CALLBACK_URL = "http://95.222.246.113:9000/weiboLogin";
	
    // Login Form Bean Class
    
    public static class Login {
        
        public String email;
        public String password;
        public String nickname;
        
        public String validate() {
        	User user = User.authenticate(email, password);
            if(user == null) {
                return "Invalid username or password";
            }
            else {
            	nickname = user.displayName;
            	return null;
            }
        }
        
    }
    
    // Signup Form Bean Class
    
    public static class Signup {
    	
    	public String semail;
    	public String spassword;
    	public String srepassword;
    	
    	public String validate(){
    		if(spassword!=null && spassword.equals(srepassword))
    			return null;
    		else
    			return "Confirmation password incorrect";
    	}
    	
    }
    
    /**
     *  Login Action
     *  
     */
    
	public static Result login() {

		return ok(login.render(form(Login.class), form(Signup.class)));
	}
	
	
    /**
     *  Weibo Login
     *  
     */
    
	public static Result weiboLogin(String weiboId) {
		
		String email = weiboId+"@weibo.com";
		
		if(User.authenticate(email, "")==null){
			User loggedUser = User.RegisterUser(email, "");
			AlbumManager.createUserDirectory(loggedUser.userDirName);
		}
		session("email", email);
		return redirect(routes.MainProgram.index());
	}
	
    /**
     *  Weibo Login
     *  
     */
    
	public static Result weiboLogin(String code, String error) {
		
		System.out.println("Code: " + code);
		System.out.println(error);
		
		if("".equals(code))
			return redirect(routes.MainProgram.index());
		else{
			
			try {

					String urlString = "https://api.weibo.com/oauth2/access_token?client_id=3867543857&client_secret=a476c51660bddc628106af0dc7d15fc6&grant_type=authorization_code&redirect_uri="+ WEIBO_CALLBACK_URL + "&code=" + code;
					//URLEncoder.encode(urlString, "utf-8");
					URL url = new URL(urlString);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Accept", "application/json");
					conn.setRequestProperty("Content-Type", "application/json");
					if (conn.getResponseCode() != 200) {
						throw new RuntimeException("Failed : HTTP error code : "
								+ conn.getResponseCode());
					}
					BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
					String output;
					String access_token = null;
					String uid = null;
					System.out.println("Output from Server .... \n");
					while ((output = br.readLine()) != null) {
						ObjectMapper mapper = new ObjectMapper(); 
						JsonFactory factory = mapper.getJsonFactory(); 
						JsonParser jp = factory.createJsonParser(output); 
						JsonNode actualObj = mapper.readTree(jp); 
						access_token = actualObj.get("access_token").getTextValue();
						uid = actualObj.get("uid").getTextValue();
					}
					
					if(access_token != null)
						session("access_token", access_token);

					System.out.println("access_token: " + access_token);
					String email=null;
					
					if(uid!=null){
						email = uid+"@weibo.com";
					}
					if(User.authenticate(email, "")==null){
						User loggedUser = User.RegisterUser(email, "");
						Weibo weibo = new Weibo();
						weibo.setToken(access_token);
						weibo4j.Users um = new Users();
						weibo4j.model.User user = um.showUserById(uid);
						System.out.println(user.toString());
						AlbumManager.createUserDirectory(loggedUser.userDirName);
					}
					session("email", email);
					
					Weibo weibo = new Weibo();
					weibo.setToken(access_token);
					weibo4j.Users um = new Users();
					weibo4j.model.User user = um.showUserById(uid);
					System.out.println(user.toString());
					
					conn.disconnect();
			
			 }catch (MalformedURLException e) {
				 
				e.printStackTrace();
		 
			 } catch (IOException e) {
		 
				e.printStackTrace();
		 
			  } catch (WeiboException e) {
					e.printStackTrace();
				}
			return redirect(routes.MainProgram.index());
		}
	}
	
	
	
	/**
	 *  Validate user
	 */

	public static Result authenticate() {
	
		
		Form<Login> loginForm = form(Login.class).bindFromRequest();
		Form<Signup> signupForm = form(Signup.class).bindFromRequest();

		if (loginForm.hasErrors())
			return badRequest(login.render(loginForm,signupForm));

		else {
			session("email", loginForm.get().email);
			session("nickname", loginForm.get().nickname);
			return redirect(routes.MainProgram.index());
		}

	}
	
	
	/**
	 *   Ajax request verifying email address
	 *   
	 */
	
	public static Result verifyEmail(String inputEmail){
		if(User.findByEmail(inputEmail) !=null)
			return ok("<font color='#cc0000'>&nbsp;&nbsp;&nbsp;Email already exits</font>");
		else	
			return ok("<font color='#ACF52A'>&nbsp;&nbsp;&nbsp;Email address valid</font>");
	}
	
	
	/**
	 *   Sign up
	 *   
	 */
	
	public static Result createAccount(){
		
		Form<Signup> signupForm = form(Signup.class).bindFromRequest();
		String email = signupForm.get().semail;
		String password = signupForm.get().spassword;
		if(signupForm.hasErrors())
			return badRequest("Not OK");
		else{
			User loggedUser = User.RegisterUser(email, password);
			session("email",email);
			session("nickname",loggedUser.displayName);
			AlbumManager.createUserDirectory(loggedUser.userDirName);
			return redirect(routes.MainProgram.index());
		}
	}
	
	
    /**
     * Logout and clean the session.
     */
    public static Result logout() {
        session().clear();
        flash("success", "You've been logged out");
        return redirect(routes.Application.login());
    }

}