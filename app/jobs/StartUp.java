package jobs;

import core.PluginManager;
import javax.persistence.EntityManager;
import models.User;
import play.Logger;
import play.db.jpa.JPA;
import play.jobs.*;
import play.libs.Codec;

@OnApplicationStart
public class StartUp extends Job {

	@Override
	public void doJob() {
		Logger.info("Welcome on Cide v0.1 alpha !");
		PluginManager.start();
		
		initDB();
	}
	
	public void initDB()
	{
		EntityManager em = JPA.em();
		
		//Create Users
		User user = new User ();
		
		user.username = "gbrunier";
		user.email = "brunier@ece.fr";
		user.firstname = "Guillaume";
		user.lastname = "Brunier";
		user.password = Codec.hexMD5("0000");
		em.persist(user);
		
		user = new User();
		user.username = "fbavent";
		user.email = "fbavent@ece.fr";
		user.firstname = "Fabien";
		user.lastname = "Bavent";
		user.password = Codec.hexMD5("0000");
		em.persist(user);
	}
}
