package net.sasakonnect.wifi_portal.constants;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GlobalPermissionsConstants {
	public static class CreateRole extends PermissionEntry {
		public final static String PERMISSION = "can.create.role";
		public final static String DESCRIPTION = "Allows a user to create a system role";
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
		public final static String DESCRIPTION = "Can get all user roles";
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
		public final static String DESCRIPTION = "Allows a user to edit a system role";
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
