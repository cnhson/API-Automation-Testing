package api.test.main;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import api.test.controllers.IndepedentFetchController;
import api.test.main.type.IndependentTest;

public class IndependentTestFactory {

    @DataProvider(name = "API_INDEPENDENT")
    @Factory
    public Object[] createInstance() {

        List<Object> instancesList = new ArrayList<>();
        IndepedentFetchController ifc = new IndepedentFetchController();

        ifc.loopFetchingDataTest("VICK_API_INDEPENDENT", "VICK_TEST_ACCOUNT", (apiEntity, accountEntity) -> {
            instancesList.add(
                    new IndependentTest(apiEntity, accountEntity));
        });
        return instancesList.toArray(new Object[0]);
    }

}
