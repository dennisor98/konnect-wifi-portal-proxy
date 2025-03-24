package net.sasakonnect.wifi_portal.constants;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GlobalPermissionsConstants {
	public static class CreateRole extends PermissionEntry {
		public final static String PERMISSION = "can.create.role";
		public final static String DESCRIPTION = "Create Role";
		@Override
		public String getPERMISSION() {
			return PERMISSION;
		}

		@Override
		public String getDESCRIPTION() {
			return DESCRIPTION;
		}

	}
	
	public static class CanGetRoles extends PermissionEntry {
		public final static String PERMISSION = "can.get.roles";
		public final static String DESCRIPTION = "View Roles";
		@Override
		public String getPERMISSION() {
			return PERMISSION;
		}

		@Override
		public String getDESCRIPTION() {
			return DESCRIPTION;
		}

	}
	
	public static class CanEditRole extends PermissionEntry {
		public final static String PERMISSION = "can.edit.role";
		public final static String DESCRIPTION = "Edit System Role";
		@Override
		public String getPERMISSION() {
			return PERMISSION;
		}

		@Override
		public String getDESCRIPTION() {
			return DESCRIPTION;
		}

	}
	
	public static class CanAssignRole extends PermissionEntry {
		public final static String PERMISSION = "can.assign.role";
		public final static String DESCRIPTION = "Assign Role";
		@Override
		public String getPERMISSION() {
			return PERMISSION;
		}

		@Override
		public String getDESCRIPTION() {
			return DESCRIPTION;
		}

	}
	
	public static class DeleteRole extends PermissionEntry {
		public final static String PERMISSION = "can.delete.role";
		public final static String DESCRIPTION = "Allows a user to delete a system role";
		@Override
		public String getPERMISSION() {
			return PERMISSION;
		}

		@Override
		public String getDESCRIPTION() {
			return DESCRIPTION;
		}

	}
	
	public static class CanGetSubscriptions extends PermissionEntry {
		public final static String PERMISSION = "can.get.subscriptions";
		public final static String DESCRIPTION = "View Subscriptions";
		@Override
		public String getPERMISSION() {
			return PERMISSION;
		}

		@Override
		public String getDESCRIPTION() {
			return DESCRIPTION;
		}
		
	}
	
	public static class CanGetTransactions extends PermissionEntry {
		public final static String PERMISSION = "can.get.transactions";
		public final static String DESCRIPTION = "View Transactions";
		@Override
		public String getPERMISSION() {
			return PERMISSION;
		}

		@Override
		public String getDESCRIPTION() {
			return DESCRIPTION;
		}
		
	}
	
	public static class CanEditSubscriptions extends PermissionEntry {
		public final static String PERMISSION = "can.edit.subscriptions";
		public final static String DESCRIPTION = "Edit Subscriptions";
		@Override
		public String getPERMISSION() {
			return PERMISSION;
		}

		@Override
		public String getDESCRIPTION() {
			return DESCRIPTION;
		}
		
	}
	
	public static class CanDeleteSubscriptions extends PermissionEntry {
		public final static String PERMISSION = "can.delete.subscriptions";
		public final static String DESCRIPTION = "Delete Subscriptions";
		@Override
		public String getPERMISSION() {
			return PERMISSION;
		}

		@Override
		public String getDESCRIPTION() {
			return DESCRIPTION;
		}
		
	}
	
	public static class CanGetUsers extends PermissionEntry{
		public final static String PERMISSION = "can.get.users";
		public final static String DESCRIPTION = "View Users";
		@Override
		public String getPERMISSION() {
			return PERMISSION;
		}

		@Override
		public String getDESCRIPTION() {
			return DESCRIPTION;
		}
	}
	
	public static class CanManageApp extends PermissionEntry{
		public final static String PERMISSION = "can.manage.app";
		public final static String DESCRIPTION = "View Users";
		@Override
		public String getPERMISSION() {
			return PERMISSION;
		}

		@Override
		public String getDESCRIPTION() {
			return DESCRIPTION;
		}
	}
	
	
	
	public static class CanAddUser extends PermissionEntry{
		public final static String PERMISSION = "can.create.user";
		public final static String DESCRIPTION = "Add User";
		@Override
		public String getPERMISSION() {
			return PERMISSION;
		}

		@Override
		public String getDESCRIPTION() {
			return DESCRIPTION;
		}
	}
	
	
	public static class CanManagePayment extends PermissionEntry{
		public final static String PERMISSION = "can.manage.payment";
		public final static String DESCRIPTION = "Manage Payment";
		@Override
		public String getPERMISSION() {
			return PERMISSION;
		}

		@Override
		public String getDESCRIPTION() {
			return DESCRIPTION;
		}
	}
	
	
	
	public static Map<String, String> scan() {
		Class<?>[] innerClasses = GlobalPermissionsConstants.class.getDeclaredClasses();

		Set<PermissionEntry> permissionEntries = new HashSet<>();

		for (Class<?> innerClass : innerClasses) {
			// Check if the class extends PermissionEntry
			if (PermissionEntry.class.isAssignableFrom(innerClass)) {
				try {
					// Create an instance of the inner class
					PermissionEntry entryInstance = (PermissionEntry) innerClass.getDeclaredConstructor().newInstance();
					permissionEntries.add(entryInstance);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return permissionEntries.stream()
				.collect(Collectors.toMap(PermissionEntry::getPERMISSION, PermissionEntry::getDESCRIPTION));
	}

}
