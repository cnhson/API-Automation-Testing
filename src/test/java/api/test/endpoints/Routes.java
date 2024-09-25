package api.test.endpoints;

import api.test.utilities.PropertyUtil;

public class Routes {

	// private String tlDomain;
	// private String subDomain;
	// private String slDomain;
	private String fullDomain;
	private PropertyUtil pu = new PropertyUtil();

	public Routes() {
		try {
			String subDomain = pu.getPropAsString("SUBDOMAIN");
			String slDomain = pu.getPropAsString("SLDOMAIN");
			String tlDomain = pu.getPropAsString("TLDOMAIN");
			fullDomain = "https://" + subDomain + slDomain + tlDomain;
		}
		catch (Exception e) {
			System.err.println("[Routes] Error while getting domain: " + e.getMessage());
		}
	}

	public String getFullDomain() {
		return fullDomain;
	}
}
