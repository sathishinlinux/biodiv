
import  species.groups.UserGroup
import species.participation.Checklists
import groovy.sql.Sql
import species.*
import grails.converters.JSON




def migrate(){
	def ugs = ctx.getBean("userGroupService");
	def ds = ctx.getBean('dataSource')
	Sql sql = Sql.newInstance(ds)
	String query = ''' select o.id as oid from observation as o, user_group_observations as ugo where o.id = ugo.observation_id and o.is_checklist = true and ugo.user_group_id = '''; 
	UserGroup.list(sort:"id", order:"asc").each{ UserGroup ug ->
		def objectIds = sql.rows(query + ug.id).oid.collect{'' + it}.join(',')
		def ugids = '' + ug.id
		println "================== " + ugids
		println "====================== " + objectIds
		def m = [author:"1", objectIds:objectIds, submitType:'post', userGroups:ugids, pullType :'bulk', 'objectType':Checklists.class.getCanonicalName() ]
		ugs.updateResourceOnGroup(m)
	}
}

def mergeSynonym(){
	def ns = ctx.getBean("namelistUtilService");
	//File file = new File("/home/sandeept/namesync/thomas/duplicatestodelete.csv")
	File file = new File("/home/sandeept/namesync/thomas/duplicatecolidstomergeSynonyms.csv")
	//File file = new File("/apps/git/biodiv/namelist/after-migration/toDelete.csv");
	
	def lines = file.readLines();
	println "============ started =========="
//	try {
//		ns.mergeSynonym(385677,358206, true)
//	}catch(e){
//		println e.printStackTrace()
//	}
	def failCountList = []
	int i=0;
	lines.each { line ->
		if(i++ == 0) return;
		def arr = line.split(',');
		println '--------------' + arr
		if(arr.length > 1){
			def toDelete = Long.parseLong(arr[0].trim())
			def fullDelete = (arr[1].trim().toLowerCase() == 'yes') ? true : false
			if(!fullDelete)
				return
	        def toMove = Long.parseLong(arr[2].trim())
			println "====================  " + toDelete + "   toMove " + toMove + "    fullDelete " + fullDelete
			try {
				ns.mergeSynonym(toDelete, toMove, fullDelete)
			}catch(e){
				failCountList << arr
				println e.printStackTrace()
			}
		}else{
			println '  leaving arr ' + arr
		}
	}
	println "=========== failed list " + failCountList
	println "=========== tatal failed " + failCountList.size()

}


def mergeAcceptedName(){
	def ns = ctx.getBean("namelistUtilService");
	File file = new File("/home/sandeept/namesync/thomas/duplicatestodelete.csv")
	//File file = new File("/apps/git/biodiv/namelist/after-migration/toDelete.csv");
	
	def lines = file.readLines();
	println "============ started =========="
//	try {
//		ns.mergeAcceptedName(385677,358206, true)
//	}catch(e){
//		println e.printStackTrace()
//	}
	def failCountList = []
	int i=0;
	lines.each { line ->
		if(i++ == 0) return;
		def arr = line.split(',');
		println '--------------' + arr
		if(arr.length > 1){
			def toDelete = Long.parseLong(arr[0].trim())
			def fullDelete = (arr[1].trim().toLowerCase() == 'yes') ? true : false
			if(!fullDelete)
				return
	        def toMove = Long.parseLong(arr[2].trim())
			println "====================  " + toDelete + "   toMove " + toMove + "    fullDelete " + fullDelete
			try {
				ns.mergeAcceptedName(toDelete, toMove, fullDelete)
			}catch(e){
				failCountList << arr
				println e.printStackTrace()
			}
		}else{
			println '  leaving arr ' + arr
		}
	}
	println "=========== failed list " + failCountList
	println "=========== tatal failed " + failCountList.size()

}

def sync(){
	def startDate = new Date()
	println "============ strated "
	def s = ctx.getBean("namesLoaderService");
	s.syncNamesAndRecos(false, false)
	println "========done=== start date " + startDate + "  " + new Date()
}



def buildTree(){
	def startDate = new Date()
	def s = ctx.getBean("namesIndexerService");
	s.rebuild()
	println "========done=== start date " + startDate + "  " + new Date()
}

def snap(){
	//XXX to do
 	def tt = new species.TaxonController()
	tt.createIBPHierarchyForDirtylist()
}



def dmp(){
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(500);
	def sql = new Sql(dataSource)
	
	String sqlStr = "select * from tmp_duplicates  where c > 1 order by rank desc, name asc"
	def taxons = sql.rows(sqlStr);
	int i = 0
	new File("/tmp/accepted_duplicates.csv").withWriter { out ->
		out.println "id|name|status|position|paths"
		taxons.each { t ->
			def tds = TaxonomyDefinition.findAllByNameAndRank(t.name, t.rank)
			println i++
			tds.each {  td -> 
			if(!td.isDeleted){
				def hNames = []
				def trs = TaxonomyRegistry.findAllByTaxonDefinition(td)
				trs.each { tr ->
					hNames << tr.path.split("_").collect{TaxonomyDefinition.read(Long.parseLong(it)).name}.join("->")
				}
				out.println td.id + "|" + td.name + "|" + td.status + "|" + td.position + "|" + hNames.join('#')
			}
			}
			
		}
	}
	
}

def splitTreeExport(){
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(500);
	def sql = new Sql(dataSource)
	
	//String sqlStr = "select taxon_definition_id  as id from tmp_mul_ibp_hier  where c > 1"
	String sqlStr = "select id from taxonomy_definition where id in (160240, 161749, 4414, 170120, 127543, 159416, 170257, 160983, 156734, 167433, 162764, 132778, 146902, 5693, 71886, 161011, 160201, 166095, 87087, 82121, 122152, 176805, 273654, 137733, 168253, 69648, 162621, 276697, 276974, 420941, 277742) "
	
	def taxons = sql.rows(sqlStr);
	int i = 0
	int totalSizeC = 0
	int totalhier = 0
	
	new File("/tmp/names_with_childinfo.csv").withWriter { out ->
		//out.println "id|name|status|position|rank|colId|paths|nextChilds"
		taxons.each { t ->
			def tdf = TaxonomyDefinition.get(t.id)
			println i++	
			//tds.each {  td ->
			//if(!td.isDeleted){
				def hNames = []
				def nextChild = []
				def trs = TaxonomyRegistry.findAllByTaxonDefinitionAndClassification(tdf, Classification.read(265799))
				totalhier += trs.size()
				trs.each { tr ->
					hNames << (tr.path + "#" + tr.path.split("_").collect{TaxonomyDefinition.read(Long.parseLong(it)).name}.join("->"))
					def cTrs =  TaxonomyRegistry.findAllByParentTaxon(tr)
					totalSizeC += cTrs.size()
					cTrs.each { ctr ->
						nextChild << (ctr.path + "#" + ctr.path.split("_").collect{TaxonomyDefinition.read(Long.parseLong(it)).name}.join("->") + ":" + ctr.taxonDefinition.matchId)
					}
				}
				out.println tdf.id + "|" + tdf.name + "|" + tdf.status + "|" + tdf.position + "|"+ tdf.rank  + "|" +  tdf.matchId + "|"+  hNames.join('#') + "|" + nextChild.join('$') 
			//}
			//}
			
		}
	}
	
	println "===========================  tatal child count " + totalSizeC + "  total hier " + totalhier + "  total names " + i
	
}

def dropRawHir(){
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(500);
	def sql = new Sql(dataSource)
	
	String sqlStr = "select id from taxonomy_definition where status = 'ACCEPTED' and position = 'RAW'"
	def taxons = sql.rows(sqlStr);
	int i = 0
	def trids = [:]
	//def tridsMap = [:]
	taxons.each { t ->
			println  t.id +  "  count " + i++ 
			//sql.executeUpdate(" delete from taxonomy_registry_suser where taxonomy_registry_contributors_id = " + tr.id )
			//sql.executeUpdate(" delete from taxonomy_registry where id in (select id from taxonomy_registry where classification_id = 265799 and path like '%" + t.id + "%')")
		    sql.executeUpdate(" delete from taxonomy_registry where id in ( select id from taxonomy_registry where classification_id = 265799 and (path like '%\\_" + t.id + "\\_%' or path like '" + t.id + "\\_%' or path like '%\\_" + t.id + "'  or path like '" + t.id + "'))" )
			
			
//			String s = "select id from taxonomy_registry where classification_id = 265799 and (path like '%\\_" + t.id + "\\_%' or path like '" + t.id + "\\_%' or path like '%\\_" + t.id + "'  or path like '" + t.id + "')" 
//			def ids = sql.rows(s).collect{it.id}
//            println s
////			println ids
//			trids.putAt(t.id, ids)	
			
	}
//	println trids.size()
//	new File("/home/sandeept/name-mig/d_map.txt").withWriter { out ->
//		  out.println trids as JSON
//	  }
//	println '-------------------------------'
//	println trids
//	println '-------------------------------'
	
	
}
 
def addColhir(){
	def nlSer = ctx.getBean("namelistUtilService");
	
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(500);
	def sql = new Sql(dataSource)
	
	String sqlStr = "select id from taxonomy_definition where  position = 'WORKING' and status = 'ACCEPTED' and is_deleted = false and id not in ( select distinct(td.id) from taxonomy_definition as td left outer join taxonomy_registry tr on td.id = tr.taxon_definition_id where td.position = 'WORKING' and td.status = 'ACCEPTED' and tr.classification_id = 265799 and tr.id is not  null)"
	def taxons = sql.rows(sqlStr);
	int i = 0
	taxons = taxons.collect { TaxonomyDefinition.get(it.id) }
	
	// to download file if not exist
	nlSer.downloadColXml(taxons)
	
	taxons.each { td ->
		nlSer.updateIBPNameWithCol(td, td.matchId)
	}
	println "============= done "
}

def createDuplicateName(){
	def nlSer = ctx.getBean("namelistUtilService");
	
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(500);
	
	//File file = new File("/home/sandeept/namesync/thomas/to_split.csv");
	File file = new File("/apps/git/biodiv/namelist/after-migration/to_split.csv");
	
	def lines = file.readLines();
	int i=0;
	def ibpId = null
	boolean hasId = false
	lines.each { line ->
		if(i++ == 0) return;
		def arr = line.split(',');
		println " arr ->>> " + arr
		if(arr.length > 1){
			if(arr[0] && (arr[0].trim() != '')){
				ibpId = Long.parseLong(arr[0].trim())
				hasId = true
			}else{
				hasId = false
			}
			
			def colId = arr[1].trim()
			def path = arr[2].trim()
			
			if(!hasId){
				def td = TaxonomyDefinition.get(ibpId)
				nlSer.createDuplicateNameWithNewColId(colId)
				nlSer.addExistingHir(td, colId, path)
			}
			else{
				println "updating only col id"
				def otd = TaxonomyDefinition.get(ibpId)
				otd.matchId = colId
				if(!otd.save(flush:true)){
					otd.errors.allErrors.each { println it }
				}
			}
		}else{
			println '  leaving arr ' + arr
		}
	}
	println "============= done "
}


def updateColId(){
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(500);
	def sql = new Sql(dataSource)
	
	//File file = new File("/home/sandeept/namesync/thomas/validation_fixes.csv");
	File file = new File("/apps/git/biodiv/namelist/after-migration/validation_fixes.csv");
	
	def lines = file.readLines();
	int i=0;
	lines.each { line ->
		if(i++ == 0) return;
		def arr = line.split(',');
		println " arr ->>> " + arr
		def ibpId = arr[0]//Long.parseLong(arr[0].trim())
		def colId = arr[1]
		sql.executeUpdate(" update taxonomy_definition set match_id = '" + colId + "' where id = " + ibpId);
	}
}
	

def createInputFile(){
	def nlSer = ctx.getBean("namelistUtilService");
	//nlSer.generateStatsInput(new File("/home/sandeept/name-mig/kk_ac_names.csv"))
	nlSer.verifyAcceptedNamesAndColPath(new File("/home/sandeept/name-mig/kk_ac_names.csv"), new File("/home/sandeept/name-mig/kk_ac_in.csv"))
	
}


def copyIbpHir(){
	def nlSer = ctx.getBean("namelistUtilService");
	nlSer.copyIBPHirAsCOL()
	
}

def addIBPHirToRawNames(){
	def nlSer = ctx.getBean("namelistUtilService");
	nlSer.addIBPHirToRawNames()
	
}


//dmp()
//splitTreeExport()

//dropRawHir()
//addColhir()
//createDuplicateName()
//mergeAcceptedName()
//mergeSynonym()
//updateColId()

//createInputFile()
//copyIbpHir()
addIBPHirToRawNames()