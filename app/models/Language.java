package models;

import java.util.List;
import javax.persistence.*;

import core.model.FileExtension;

import play.data.validation.Unique;
import play.db.jpa.Model;

public abstract class Language extends Model {

	/*
	 * The discriminator sets which language is implemented This cannot be
	 * modified
	 */
	@Unique
	public String name;
	
	@ManyToMany(cascade = CascadeType.ALL)
	public List<FileExtension> extensions;
}
