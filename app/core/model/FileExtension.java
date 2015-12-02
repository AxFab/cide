package core.model;

import javax.persistence.Entity;

import play.data.validation.Unique;

@Entity
public class FileExtension extends play.db.jpa.Model {

	@Unique
	String extension;
}
