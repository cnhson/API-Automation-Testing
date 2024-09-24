package api.test.main.workers;

import org.testng.annotations.BeforeSuite;

import api.test.utilities.AuthenUtil;
import api.test.utilities.PropertyUtil;

public class AuthenciationWorker {

	@BeforeSuite
	public void login() {
		PropertyUtil pu = new PropertyUtil();
		AuthenUtil au = new AuthenUtil();
		String username = pu.getPropAsString("USERNAME");
		String password = pu.getPropAsString("PASSWORD");
		au.sessionValidPreCheck();
		au.isLoggedIn(username, password);
		System.out.println("Finish validating session !");
	}
}
