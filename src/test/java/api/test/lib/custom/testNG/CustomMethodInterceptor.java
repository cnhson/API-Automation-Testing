package api.test.lib.custom.testNG;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import api.test.main.workers.ChainingTestWorker;
import api.test.utilities.TestOrderUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CustomMethodInterceptor implements IMethodInterceptor {

    // Execute test methods sequentially
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        Map<String, IMethodInstance> orders = new TreeMap<String, IMethodInstance>();
        for (IMethodInstance iMethodInstance : methods) {
            String order = TestOrderUtil.getPriorityId(iMethodInstance.getInstance());
            orders.put(order, iMethodInstance);
        }
        List<IMethodInstance> tests = new ArrayList<IMethodInstance>(orders.size());
        for (String order : orders.keySet()) {
            IMethodInstance test = orders.get(order);
            tests.add(test);
        }
        return tests;
    }
}
