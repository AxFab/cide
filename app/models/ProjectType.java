package models;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import org.apache.commons.io.FileUtils;

import play.Logger;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.Model;
import core.common.controller.SettingsManager;

@Entity
public class ProjectType extends Model {

	@Unique
	public String name;
	
	@Required
	public String description;

	//@ManyToMany
	//public List<Language> langs = new ArrayList<Language>();

	@OneToOne
	public BuildEngine buildEngine;
	
	public ProjectType(String name, String description) {
		
		this.name = name;
		this.description = description;
	}

	/**
	 * Copy the files from the templates folder
	 */
	public void initializeProject(Project project) throws IOException {
	
		// Get the CIDE data path
		String rootPath = SettingsManager.getValue("rootPath");
		String projectPath = rootPath + "projects" + File.separator + project.uuid;
		
		// Copying data from the templates directory
		String templatesPath = SettingsManager.getValue("templatesPath");
		templatesPath += File.separator + this.name;
		
		Logger.debug("[ProjectType] Copying templates from '"+templatesPath+"' to '%s'", projectPath);
		
		File templates = new File(templatesPath);
		if(!templates.exists()) {
			throw new IOException("Template directory is not present. You NEED a template directory for each project type!");
		}
		FileUtils.copyDirectory(templates, new File(projectPath));
	}
	/*
	public abstract void generateTemplates();
	public abstract void updateTemplates();
	*/
	
	@Override
	public String toString() {
		
		return this.name;
	}
}
