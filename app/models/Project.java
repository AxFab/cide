package models;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.Model;
import play.mvc.Before;
import core.module.type.SourceFile;

@Entity
public class Project extends Model {

	@Required
	public String name;

	@Required
	public String url;
	
	@Required
	@OneToOne
	public User owner;
	
	@Required
	public Date creationDate;
	
	@Required
	@ManyToMany(cascade = CascadeType.ALL)
    public List<User> users = new ArrayList<User>();

	@Transient
	public File root;

	@Required
	@Unique
	public UUID uuid;

	@Required
	@OneToOne
	public ProjectType type;

	public static Project createProject(String name, String url, User owner, List<User> users, ProjectType type) {
		
		Project project = new Project();
		project.name = name;
		project.url = url;
		project.owner = owner;
		project.type = type;
		project.users.addAll(users);
		project.uuid = UUID.randomUUID();
		project.creationDate = new Date();
		
		return project;
	}
	
	@Override
	public String toString() {
		
		return name; 
	}
}
