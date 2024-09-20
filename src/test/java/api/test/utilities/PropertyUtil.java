package api.test.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtil {

	private Properties properties = new Properties();

	public PropertyUtil() {
		try (InputStream inputStream = PropertyUtil.class.getClassLoader()
				.getResourceAsStream("config.properties")) {
			if (inputStream != null) {
				properties.load(inputStream);
			} else {
				throw new IOException("config.properties not found in folder: src/test/resources ");
			}
			inputStream.close();

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getPropAsString(String propertyKey) {
		try {
			return properties.getProperty(propertyKey);
		}
		catch (Exception e) {
			System.err.println("\n[PropertyUtil] Error while getting (String) property: " + e.getMessage());
			return null;
		}

	}

	public Integer getPropAsInt(String propertyKey) {
		try {
			return Integer.parseInt(properties.getProperty(propertyKey).trim());
		}
		catch (Exception e) {
			System.err.println("\n[PropertyUtil] Error while getting (Integer) property: " + e.getMessage());
			return null;
		}

	}
}
