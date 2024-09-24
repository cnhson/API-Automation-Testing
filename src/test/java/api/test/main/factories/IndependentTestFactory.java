package api.test.main.factories;

import java.util.stream.Stream;

import org.testng.annotations.Factory;

import api.test.controllers.IndepedentFetchController;
import api.test.main.workers.IndependentTestWorker;

public class IndependentTestFactory {

	@Factory
	public Object[] createInstance() {

		IndepedentFetchController ifc = new IndepedentFetchController();
		Stream.Builder<IndependentTestWorker> streamBuilder = Stream.builder();
		String fetchMode = ifc.getFetchMode();
		ifc.loopFetchingDataTest("VICK_API_INDEPENDENT", (apiEntity, accountEntity, testInfo) -> {
			streamBuilder.add(new IndependentTestWorker(apiEntity, accountEntity, testInfo, fetchMode));
		});
		return streamBuilder.build().toArray();

	}

}
