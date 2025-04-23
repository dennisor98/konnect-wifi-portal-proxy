package net.sasakonnect.wifi_portal.config;

public class DataSourceContextHolder {
	private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

	public static void set(String dataSourceType) {
		CONTEXT.set(dataSourceType);
	}

	public static String get() {
		return CONTEXT.get();
	}

	public static void clear() {
		CONTEXT.remove();
	}
}
