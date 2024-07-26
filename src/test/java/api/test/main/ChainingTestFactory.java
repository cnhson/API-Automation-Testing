package api.test.main;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import api.test.controllers.ChainingFetchController;
import api.test.main.type.ChainingTest;

public class ChainingTestFactory {

    @DataProvider(name = "API_CHAINING")
    @Factory
    public Object[] createInstance() {

        List<Object> instancesList = new ArrayList<>();
        ChainingFetchController cfc = new ChainingFetchController();

        cfc.loopFetchingDataTest("VICK_API_CHAINING", "VICK_TEST_ACCOUNT", (apiEntity, accountEntity) -> {
            instancesList.add(
                    new ChainingTest(apiEntity, accountEntity));
        });
        return instancesList.toArray(new Object[0]);
    }

}
