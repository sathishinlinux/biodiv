package species;

import java.util.List;
import java.util.Map;

import org.grails.taggable.*
import groovy.text.SimpleTemplateEngine
import groovy.xml.MarkupBuilder;
import groovy.xml.StreamingMarkupBuilder;
import groovy.xml.XmlUtil;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MultipartHttpServletRequest
import grails.plugin.springsecurity.SpringSecurityUtils;

import grails.converters.JSON;
import grails.converters.XML;

import grails.plugin.springsecurity.annotation.Secured
import grails.util.Environment;
import species.participation.RecommendationVote.ConfidenceType
import species.participation.Flag.FlagType
import species.utils.ImageType;
import species.utils.ImageUtils
import species.utils.Utils;
import species.groups.SpeciesGroup;
import species.groups.UserGroup;
import species.groups.UserGroupController;
import species.Habitat;
import species.Species;
import species.Resource;
import species.BlockedMails;
import species.Resource.ResourceType;
import species.auth.SUser;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList
import species.participation.Featured
import species.participation.ResourceRedirect


import static org.springframework.http.HttpStatus.*;

abstract class AbstractObjectController {
    
    def utilsService;
    def observationService;

    def related() {
        def relatedObv = observationService.getRelatedObservations(params).relatedObv;
        return formatRelatedResults(relatedObv, params);
    }

    protected formatRelatedResults(relatedObv, params) {
        if(params.filterProperty != 'bulkUploadResources') {
            if(relatedObv) {
                if(relatedObv.observations)
                    relatedObv.observations = observationService.createUrlList2(relatedObv.observations, observationService.getIconBasePath(params.controller));
            } else {
                log.debug "no related observations"
            }
            def model = utilsService.getSuccessModel("", null, OK.value(), relatedObv)
            withFormat {
                json { render model as JSON }
                xml { render model as XML }
            }
        } else {
            def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
            List urlList = []
            for(param in relatedObv.observations){
                def res = param['observation'];
                def item = [:]
                item.id = res.id
                if(res.type == ResourceType.IMAGE) {
                    item.imageLink = res.thumbnailUrl(config.speciesPortal.usersResource.serverURL)//thumbnailUrl(iconBasePath)
                } else if(res.type == ResourceType.VIDEO) {
                    item.imageLink = res.thumbnailUrl()
                } else if(res.type == ResourceType.AUDIO) {
                    item.imageLink = config.grails.serverURL+"/images/audioicon.png"
                } 
                item.url = "/resource/bulkUploadResources"
                item.title = param['title']
                item.type = 'resource'
                if(param.inGroup) {
                    item.inGroup = param.inGroup;
                } 
                urlList << item;
            }
            relatedObv.observations = urlList
            def model = utilsService.getSuccessModel("", null, OK.value(), relatedObv)
            withFormat {
                json { render model as JSON }
                xml { render model as XML }
            }
        }
    }
	
	def getTargetInstance(Class clazz, id){
		if( id instanceof String){
			id = id.trim().toLong()
		}
		def instance = clazz.get(id)
		
		if(!instance || (instance.hasProperty('isDeleted') && instance.isDeleted)){
			instance = new ResourceRedirect().fetchTargetInstance(clazz.canonicalName, id)
		}
		
		return instance
	}

}
