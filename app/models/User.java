package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.ManyToMany;

import com.google.gson.annotations.Expose;

import play.data.validation.*;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.libs.Codec;

@Entity
public class User extends Model {

	@Required
	public String username;

	@Required
	public String firstname;

	@Required
	public String lastname;

	@Required
	@Email
	public String email;

	@Required
	@Password
	public String password;
	
	public boolean admin;
	
	@Required
    @ManyToMany(cascade = CascadeType.ALL, mappedBy="users")
    public List<Project> projects = new ArrayList<Project>();

	/**
	 * Compute the hash of the specified password so a plain-text password is
	 * never stored
	 * 
	 * @param username
	 */
	public void setPassword(String password) {

		this.password = Codec.hexSHA1(password);
	}

	/**
	 * Returns the hash-encoded password
	 * 
	 * @return
	 */
	public String getPassword() {

		return this.password;
	}

	public static User createUser(String firstname, String lastname,
			String username, String email, String password, boolean admin) {

		User user = new User();
		user.firstname = firstname;
		user.lastname = lastname;
		user.username = username;
		user.email = email;
		user.password = password;
		user.admin = admin;
		return user;
	}

	/**
	 * Check if the username/password pair is valid
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public static User isValidLogin(String username, String password) {
		
		return User.find("username = ? AND password = ?", username,
				Codec.hexSHA1(password)).first();
	}
	
	public static User isValidAdminLogin(String username, String password) {
		return User.find("username = ? AND password = ? AND admin = ?", username, Codec.hexSHA1(password), true).first();
	}

	/**
	 * Get a user by username
	 * 
	 * @param username
	 * @return
	 */
	public static User getUser(String username) {
		return User.find("username = ?", username).first();
	}
	
	@Override
	public String toString() {
		
		return username;	
	}

	public boolean isAdmin() {
		return admin;
	}
}
