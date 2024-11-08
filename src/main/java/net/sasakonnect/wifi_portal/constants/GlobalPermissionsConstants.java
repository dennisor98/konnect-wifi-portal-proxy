package net.sasakonnect.wifi_portal.constants;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GlobalPermissionsConstants {
	public static class CreateRole extends PermissionEntry {
		public final String PERMISSION = "can.create.role";
		public final String DESCRIPTION = "Allows a user to create a system role";
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
		public final String PERMISSION = "can.delete.role";
		public final String DESCRIPTION = "Allows a user to delete a system role";
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
