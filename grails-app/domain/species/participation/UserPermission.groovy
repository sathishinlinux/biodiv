package species.participation

import species.auth.SUser;
import species.auth.Role;

class UserPermission {

    SUser user;
    String objType;
    long objId;
    Role role
    Date createdOn = new Date();

    public enum UserPermissionRoleType {
		ROLE_SPECIES_CURATOR("ROLE_SPECIES_CURATOR"),
		ROLE_SPECIES_CONTRIBUTOR("ROLE_SPECIES_CONTRIBUTOR"),

		private String value;

		UserPermissionRoleType(String value) {
			this.value = value;
		}

		String value() {
			return this.value;
		}

		static def toList() {
			return [ ROLE_SPECIES_CURATOR, ROLE_SPECIES_CONTRIBUTOR ]
		}

		public String toString() {
			return this.value();
		}
	
	}


    static constraints = {
    }
}
