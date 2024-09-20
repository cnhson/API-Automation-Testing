package api.test.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TestOrderUtil {

	public static String getPriorityId(Object testInstance) {
		try {
			// Try to get the order field
			Field orderField = testInstance.getClass().getDeclaredField("testId");
			orderField.setAccessible(true);
			return orderField.get(testInstance).toString();
		}
		catch (NoSuchFieldException | IllegalAccessException e) {
			// If order field is not found, try to get the order method
			try {
				Method orderMethod = testInstance.getClass().getMethod("getTestId");
				return orderMethod.invoke(testInstance).toString();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}
}
