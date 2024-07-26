package api.test.main;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import api.test.controllers.NoValidateFetchController;

public class NoValidateTestFactory {

    @DataProvider(name = "FAKE_TEST_2")
    @Factory
    public Object[] createInstance() {

        List<Object> instancesList = new ArrayList<>();
        NoValidateFetchController nvfcu = new NoValidateFetchController();

        nvfcu.loopFetchingDataTest("FAKE_TEST_2", "VICK_TEST_ACCOUNT", (entity, res) -> {
            // instancesList.add(
            // new IndependentTest(entity.getFetchMode(), entity.getRequestURL(), res,
            // entity.getSuccessResponse()));
        });
        return instancesList.toArray(new Object[0]);
    }

}
