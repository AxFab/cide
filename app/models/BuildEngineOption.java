package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class BuildEngineOption extends Model {

	public BuildEngineOption(String option, String value) {
		
		this.option = option;
		this.value = value;
	}

	@Required
	public String option;
	
	@Required
	public String value;
	
	@Override
	public String toString() {
		
		return value;
	}
}