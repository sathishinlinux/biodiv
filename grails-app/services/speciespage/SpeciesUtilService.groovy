package speciespage

import species.participation.UserPermission;
import grails.converters.JSON;
import grails.converters.XML;
import grails.web.JSONBuilder;
import species.auth.Role;
import species.participation.UserToken;

class SpeciesUtilService {

    def activityFeedService;
    static transactional = true

    def serviceMethod() {

    }
    
    //
    //RELATED TO PARTICIPATION
    
    List getAllParents(obj) {
        
    }

    List getAllChildren(obj) {
    
    }

    List getCurator(obj) {
        List allParents = getAllParents(obj)
        allParents << obj;
        List res = []
        List rowRes = []
        allParents.each { par ->
            rowRes = UserPermission.findAllWhere(role: "Curator", objectType: par.class.getCanonicalName(), objectId: par.id )
            rowRes.each {
                res << it.user
            }
        }
        return res
    }

    List getContributor(obj) {
        List allParents = getAllParents(obj)
        allParents << obj;
        List res = []
        List rowRes = []
        allParents.each { par ->
            rowRes = UserPermission.findAllWhere(role: "Contributor", objectType: par.class.getCanonicalName(), objectId: par.id )
            rowRes.each {
                res << it.user
            }
        }
        return res
    }

    boolean isCurator(user, obj) {
        def res = null
        res = UserPermission.findWhere(user:user, objectType: obj.class.getCanonicalName(), objectId: obj.id, role: "Curator" )
        if(res!= null) {
            return true
        }
        List allParents = getAllParents(obj)
        allParents.each { par -> 
            res = UserPermission.findAllWhere(role: "Curator", objectType: par.class.getCanonicalName(), objectId: par.id, user:user )
            if(res != null) {
                return true
            }
        
        } 
        return false
    }

    boolean isContributor(user) {
        def res = null
        res = UserPermission.findWhere(user:user, objectType: obj.class.getCanonicalName(), objectId: obj.id, role: "Curator" )
        if(res!= null) {
            return true
        }
        List allParents = getAllParents(obj)
        allParents.each { par -> 
            res = UserPermission.findAllWhere(role: "Contributor", objectType: par.class.getCanonicalName(), objectId: par.id, user:user )
            if(res != null) {
                return true
            }
        
        } 
        return false
    }

    List curatorFor(user) {
        List res = UserPermission.findAllWhere(user:user, role: "Curator" )
        def resources = []
        def obj
        res.each {
            obj = activityFeedService.getDomainObject(it.objectType, it.id)
            resources.add(obj)
        }
        return resources
    }

    List contributorFor(user) {
        List res = UserPermission.findAllWhere(user:user, role: "Contributor" )
        def resources = []
        def obj
        res.each {
            obj = activityFeedService.getDomainObject(it.objectType, it.id)
            resources.add(obj)
        }
        return resources    
    }

    def historyOfCurator(user, spFieldId = null) {
        //NOT SURE OF PARAMS
    }

    def historyOfContributor(user, spFieldId = null) {
        //NOT SURE OF params
    }

    
    //BASED ON CONVENTIONAL DB
    //@Secured(['ROLE_USER'])
    def requestCuratorship(selectedNodes) {
        def type,id,obj,allParents, curators;
		def user = springSecurityService.currentUser;
		if(user) {
            /////////////////////
            def curatorFor = curatorFor(user)
            if(selectedNodes) {
                selectedNodes.each { sn ->
                    type = sn.get("type")
                    id = sn.get("id").toLong()
                    obj = activityFeedService.getDomainObject(type, id);
                    allParents = getAllParents(obj)
                    allParents<<obj
                    if(curatorFor) {
                        if(curatorFor.intersect(allParents)) {
                        //he is already curator of a parent node, no need to add for child node
                        }
                        else {
                        //add for this new node
                        //get all curators for this & higher nodes and send them mail to verify
                            curators = getCurator(obj)
                            curators.each { cu ->
                                log.debug "Sending email to curator ${cu}"
                                def userToken = new UserToken(username: user."$usernameFieldName", controller:'species', action:'confirmCuratorshipRequest', params:['nodeType':type,'nodeId':id.toString(), 'userId':user.id.toString(), 'role':UserPermissionRoleType.ROLE_SPECIES_CURATOR.value()]);
                                userToken.save(flush: true)
                                emailConfirmationService.sendConfirmation(cu.email,
                                "Please confirm Curatorship",  [curator:cu, object:obj, cu:true, user: user, domain:Utils.getDomainName(request), view:'/emailtemplates/requestPermission'], userToken.token);

                            }
                        }
                    }
                    else {
                        //simply add for all nodes
                        curators = getCurator(obj)
                        curators.each { cu ->
                            log.debug "Sending email to curator ${cu}"
                            def userToken = new UserToken(username: user."$usernameFieldName", controller:'species', action:'confirmCuratorshipRequest', params:['nodeType':type, 'nodeId':id.toString(), 'role':UserPermissionRoleType.ROLE_SPECIES_CURATOR.value()]);
                            userToken.save(flush: true)
                            emailConfirmationService.sendConfirmation(cu.email,
                            "Please confirm Curatorship",  [curator:cu,object:obj, cu:true, user: user, domain:Utils.getDomainName(request), view:'/emailtemplates/requestPermission'], userToken.token);

                        }

                    }
                }
                render (['success':true, 'statusComplete':true, 'shortMsg':'Sent request', 'msg':'Sent request to curators for confirmation.'] as JSON);
			    return;
            }
            else {
                return
            }

            //////////////////////////
			
		}
		render (['success':true,'statusComplete':false, 'shortMsg':'Please login', 'msg':'Please login to confirm request.'] as JSON);

	}

    //@Secured(['ROLE_USER'])
    def requestContributorship(selectedNodes) {
		log.debug params;
        def type,id,obj,allParents, curators;
		def user = springSecurityService.currentUser;
		if(user) {
            /////////////////////
            def contributorFor = contributorFor(user)
            if(selectedNodes) {
                selectedNodes.each { sn ->
                    type = sn.get("type")
                    id = sn.get("id").toLong()
                    obj = activityFeedService.getDomainObject(type, id);
                    allParents = getAllParents(obj)
                    allParents << obj
                    if(contributorFor) {
                        if(contributorFor.intersect(allParents)) {
                        //he is already curator of a parent node, no need to add for child node
                        }
                        else {
                        //add for this new node
                        //get all curators for this & higher nodes and send them mail to verify
                            curators = getCurator(obj)
                            curators.each { cu ->
                                log.debug "Sending email to curator ${cu}"
                                def userToken = new UserToken(username: user."$usernameFieldName", controller:'species', action:'confirmContributorshipRequest', params:['nodeType':type,'nodeId':id.toString(), 'userId':user.id.toString(), 'role':UserPermissionRoleType.ROLE_SPECIES_CONTRIBUTOR.value()]);
                                userToken.save(flush: true)
                                emailConfirmationService.sendConfirmation(cu.email,
                                "Please confirm Contributorship",  [curator:cu, object:obj, cu:false, user: user, domain:Utils.getDomainName(request), view:'/emailtemplates/requestPermission'], userToken.token);

                            }
                        }
                    }
                    else {
                        //simply add for all nodes
                        curators = getCurator(obj)
                        curators.each { cu ->
                            log.debug "Sending email to curator ${cu}"
                            def userToken = new UserToken(username: user."$usernameFieldName", controller:'species', action:'confirmContributorshipRequest', params:['nodeType':type, 'nodeId':id.toString(), 'role':UserPermissionRoleType.ROLE_SPECIES_CONTRIBUTOR.value()]);
                            userToken.save(flush: true)
                            emailConfirmationService.sendConfirmation(cu.email,
                            "Please confirm Contributorship",  [curator:cu, object:obj, cu:false, user: user, domain:Utils.getDomainName(request), view:'/emailtemplates/requestPermission'], userToken.token);

                        }

                    }
                }
                render (['success':true, 'statusComplete':true, 'shortMsg':'Sent request', 'msg':'Sent request to curators for confirmation.'] as JSON);
			    return;
            }
            else {
                return
            }

            //////////////////////////
			
		}
		render (['success':true,'statusComplete':false, 'shortMsg':'Please login', 'msg':'Please login to confirm request.'] as JSON);

	}

    //@PreAuthorize("hasPermission(#userGroupInstance, write)")
    void sendSpeciesInvitation(selectedNodes, members, domain, message=null) {
        members.each { mem ->
            def curatorFor = curatorFor(mem)
            selectedNodes.each { sn ->
                type = sn.get("type")
                id = sn.get("id")
                obj = activityFeedService.getDomainObject(type, id);
                allParents = getAllParents(obj)
                allParents << obj
                if(curatorFor) {
                    if(curatorFor.intersect(allParents)) {
                        //he is already curator of a parent node, no need to add for child node
                    }
                    else {
                        def userToken = new UserToken(username: mem."$usernameFieldName", controller:'species', action:'confirmCuratorInviteRequest', params:['nodeType':type,'nodeId':id.toString(), 'userId':user.id.toString(), 'role':UserPermissionRoleType.ROLE_SPECIES_CURATOR.value()]);
                        userToken.save(flush: true)
                        emailConfirmationService.sendConfirmation(cu.email,
                        "Please confirm Contributorship",  [curator:cu, object:obj, cu:false, user: user, domain:Utils.getDomainName(request), view:'/emailtemplates/requestPermission'], userToken.token);

                    }

                }

            }
        }
    }

    boolean addCurator(user, obj) {
       def role = new Role(authority: UserPermissionRoleType.ROLE_SPECIES_CURATOR.value());
       //insert this obj and delete all its child obj records
       def row = new UserPermission(user: user, role: role, objType: obj.class.getCanonicalName(), objId: obj.id.toLong());
       if(!row.save(flush: true)) {
        return flase
       }
       def res;
       def allChildren = getAllChildren();
       allChildren.each { child ->
            res = UserPermission.findWhere(user:user, role:role, objType: child.class.getCanonicalName(), objId: child.id.toLong())
            try{
                res.delete(flush:true, failOnError:true)
            }catch (Exception e) {
                e.printStackTrace()
                return false
            }
       
       }
       return true;
    }

    boolean addContributor(user, obj) {
       def role = new Role(authority: UserPermissionRoleType.ROLE_SPECIES_CONTRIBUTOR.value());
       //insert this obj and delete all its child obj records
       def row = new UserPermission(user: user, role: role, objType: obj.class.getCanonicalName(), objId: obj.id.toLong());
       if(!row.save(flush: true)) {
        return flase
       }
       def res;
       def allChildren = getAllChildren();
       allChildren.each { child ->
            res = UserPermission.findWhere(user:user, role:role, objType: child.class.getCanonicalName(), objId: child.id.toLong())
            try{
                res.delete(flush:true, failOnError:true)
            }catch (Exception e) {
                e.printStackTrace()
                return false
            }
       
       }
       return true;
    }

}
