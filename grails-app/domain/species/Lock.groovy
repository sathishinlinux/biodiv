package species

import java.util.Date;

import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import species.auth.SUser;
import groovy.sql.Sql;
import grails.util.GrailsNameUtils;

class Lock {
    def activityFeedService

    SUser user;
    Date takenOn = new Date();

    static constraints = {
        user (nullable:false);
        takenOn (nullable:false);
	}

    private synchronized boolean setLock(type, id, user) {
        def obj = activityFeedService.getDomainObject(type, id);
        if(obj.lock == null) {
            def lock = new Lock(user: user)
            obj.lock = lock
            if(!obj.save(flush:true)) {
                obj.errors.allErrors.each { log.error it }
            }
            return true;
        }
        return false;
    }
    
    private synchronized boolean releaseLock(type, id, user) {
        def obj = activityFeedService.getDomainObject(type, id);
        if(obj.lock != null) {
            try{
                obj.lock.delete(flush:true, failOnError:true)
            }catch (Exception e) {
                e.printStackTrace()
                return false;
            }
            return true;
        }
        return false;
    }
}
