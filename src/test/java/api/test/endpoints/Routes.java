package api.test.endpoints;

import api.test.utilities.PropertyUtil;

public class Routes {

    private String tlDomain;
    private String subDomain;
    private String slDomain;
    private String fullDomain;
    private PropertyUtil pu = new PropertyUtil();

    public Routes() {
        try {
            subDomain = pu.getPropAsString("SUBDOMAIN");
            slDomain = pu.getPropAsString("SLDOMAIN");
            tlDomain = pu.getPropAsString("TLDOMAIN");
            fullDomain = "https://" + subDomain + slDomain + tlDomain;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getFullDomain() {
        return fullDomain;
    }
}
