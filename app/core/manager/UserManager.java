/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.manager;

import core.interfaces.IPlugin;
import core.type.NetworkMessage;
import data.Event;
import java.util.List;
import javax.persistence.EntityManager;
import models.User;
import org.hibernate.dialect.FirebirdDialect;
import play.db.jpa.JPA;
import play.libs.Codec;
import play.Logger;

public class UserManager implements IPlugin {

	@Override
	public String id() {
		return "Cide.UserManager";
	}

	@Override
	public void start() {
	}

	@Override
	public void onNetworkMessage(NetworkMessage msg) {
		
		Logger.debug("User Manager received an event");

		if (msg.body.message.equals("createUser"))
		{
			
		}
		else if (msg.body.message.equals("getUser"))
		{

		}
		else if (msg.body.message.equals("isValidLogin"))
		{

		}
	}

	public void onEvent(Event evt, Object data) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public void createUser(String firstname, String lastname, String username, String email, String password) {
		
		User user = new User();
		user.username = username;
		user.firstname = firstname;
		user.lastname = lastname;
		user.email = email;
		user.password = Codec.hexMD5(password);
			
		EntityManager em = JPA.em();
		em.persist(user);
	}
	
	public User getUser(String username) {
		return User.find("username = ?", username).first();
	}
	
	public boolean isValidLogin(String username, String password) {
		return ((User.find("username = ? AND password = ?", username, Codec.hexMD5(password))).fetch().size() ==1);
	}
	
}
