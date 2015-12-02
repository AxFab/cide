package models;

import javax.persistence.Entity;
import play.data.validation.*;
import play.db.jpa.Model;

@Entity
public class User extends Model {
	
	@Required
	public String username;
	
	@Required
	@Email
	public String email;
	
	@Required
	@Password
	public String password;
	
	@Required
	public String firstname;
	
	@Required
	public String lastname;
}
