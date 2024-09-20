package api.test.utilities;

public class TimeEslapseUtil {

	public static long getTime(Runnable function) {
		long startTime = System.nanoTime();
		function.run();
		return System.nanoTime() - startTime;
	}
}
