package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class ProjectOption extends Model {

	@Required
	public String option;
	
	@Required
	public String value;
}