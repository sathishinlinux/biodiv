package content.eml

import java.util.Date;

import species.License;
import species.Metadata
import species.auth.SUser;
import species.groups.UserGroup;
import species.groups.SpeciesGroup;
import species.Habitat;
import org.grails.taggable.Taggable;
import org.grails.rateable.*
/**
 * eml-literature module
 * http://knb.ecoinformatics.org/software/eml/eml-2.1.1/eml-literature.html
 * http://knb.ecoinformatics.org/software/eml/eml-2.1.1/index.html
 *
 */
class Document extends Metadata implements Taggable, Rateable {
	
	def grailsApplication
	def activityFeedService
	def documentService
	
	public enum DocumentType {
		Report("Report"),
		Poster("Poster"),
		Proposal("Proposal"),
		Miscellaneous("Miscellaneous"),

		private String value;
		

		DocumentType(String value) {
			this.value = value;
		}

		public String value() {
			return this.value;
		}
	}

	DocumentType type

	String title
	SUser author;
	
	UFile uFile   //covers physical file formats
	String uri
	
	String notes // <=== description
	String contributors;
	String attribution;
	
	License license
	String doi
	
	//source holder(i.e project, group)
	Long sourceHolderId;
	String sourceHolderType;

	//XXX uncmment it before migration
	Coverage coverage //<== extending metadata now	//Coverage Information
	
	//Date createdOn  <=== dateCreated
	//Date lastRevised <=== lastUpdated
	
	boolean deleted
	
	boolean agreeTerms = false	
	
	static transients = [ 'deleted' ]

	
	static constraints = {
		title nullable:false, blank:false
		uFile validator : {val, obj -> 
			if(!(val || obj.uri))
				return 'fileOrUrl.validator.invalid' 
		},nullable:true
		uri validator : {val, obj -> 
			val || obj.uFile
		},nullable:true
		contributors nullable:true
		attribution nullable:true	
		sourceHolderId nullable:true
		sourceHolderType nullable:true
		author nullable:true
		notes nullable:true
		doi nullable:true
		license nullable:true
		
		agreeTerms nullable:true
		
		//coverage related extended from metadata
		placeName(nullable:true)
		reverseGeocodedName(nullable:true)
		fromDate(nullable: true)
		group nullable:true
		habitat nullable:true
	}
	
	static hasMany = [speciesGroups:SpeciesGroup, habitats:Habitat, userGroups:UserGroup]
	static belongsTo = [SUser, UserGroup]
	
	static mapping = {
		notes type:"text"
	}
	
	
	def getOwner() {
		return author;
	}
	
	String toString() {
		return title;
	}
	
	def setSource(parent) {
		this.sourceHolderId = parent.id
		this.sourceHolderType = parent.class.getCanonicalName()
	}
	
	def fetchSource(){
		if(sourceHolderId && sourceHolderType){
			return grailsApplication.getArtefact("Domain",sourceHolderType)?.getClazz()?.read(sourceHolderId)
		}
	}
	
	def beforeDelete(){
		activityFeedService.deleteFeed(this)
	}
	
	def beforeUpdate(){
		if(isDirty('topology')){
			updateLatLong()
		}
	}
	
	def beforeInsert(){
		updateLatLong()
	}
	
	def fetchList(params, max, offset, noLimit=false){
		return documentService.getFilteredDocuments(params, max, offset, noLimit)
	}
}
