package api.test.main.factories;

import java.util.stream.Stream;

import org.testng.annotations.Factory;

import api.test.controllers.IndepedentFetchController;
import api.test.main.workers.FakeIndependentTestWorker;
import api.test.main.workers.IndependentTestWorker;

public class FakeIndepdentTestFactory {

	@Factory
	public Object[] createInstance1() {

		IndepedentFetchController ifc = new IndepedentFetchController();
		Stream.Builder<FakeIndependentTestWorker> streamBuilder = Stream.builder();
		String fetchMode = ifc.getFetchMode();

		ifc.loopFetchingDataTest("FAKE_TEST", "VICK_TEST_ACCOUNT", (apiEntity, accountEntity, testInfo) -> {
			streamBuilder.add(new FakeIndependentTestWorker(apiEntity, accountEntity, testInfo, fetchMode));
		});
		return streamBuilder.build().toArray();

	}
}
