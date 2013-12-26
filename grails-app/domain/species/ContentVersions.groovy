package species

import java.util.Date;

import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import species.auth.SUser;
import groovy.sql.Sql;
import grails.util.GrailsNameUtils;

class ContentVersions {

    boolean isValidated;
    boolean isSummary;
    SUser user;
    Date createdOn = new Date();
    String content;

    static constraints = {
        isValidated(nullable:false);
        isSummary(nullable:false);
        user (nullable:false);
        createdOn (nullable:false);
        content (nullable:false);
	}

    static mapping = {
		content type:"text";
	}

}
