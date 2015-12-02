package models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.Model;

@Entity
public class BuildEngine extends Model {
	
	@Unique
	public String name;

	@Required
	public String description;
	
	public BuildEngine(String name, String description) {
		
		this.name = name;
		this.description = description;
	}
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	public List<BuildEngineOption> options = new ArrayList<BuildEngineOption>();
	
	//public abstract List<ProcessBuilder> build(Project project, List<File> files) throws Exception;
	//public abstract List<ProcessBuilder> clean(Project project, List<File> files) throws Exception;
}
