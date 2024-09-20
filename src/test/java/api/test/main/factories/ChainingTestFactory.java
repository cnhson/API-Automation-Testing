package api.test.main.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.testng.annotations.Factory;

import api.test.controllers.ChainingFetchController;
import api.test.controllers.NoValidateFetchController;
import api.test.entities.ChainingEntity;
import api.test.entities.FakeChainEntity;
import api.test.main.workers.ChainingTestWorker;
import api.test.main.workers.FakeIndependentTestWorker3;

public class ChainingTestFactory {

	// @Factory
	// public Object[] createInstance() {

	// List<ChainingTest> instancesList = new ArrayList<>();
	// ChainingFetchController cfc = new ChainingFetchController();

	// cfc.loopFetchingDataTest("VICK_API_CHAINING", "VICK_TEST_ACCOUNT", (apiEntity, accountEntity,
	// fetchMode) -> {
	// instancesList.add(new ChainingTest(apiEntity, accountEntity, fetchMode));

	// });

	// return instancesList.toArray(new Object[0]);
	// }

	@Factory
	public Object[] createInstance() {

		// ChainingFetchController cfc = new ChainingFetchController();
		// Stream.Builder<ChainingTestWorker> streamBuilder = Stream.builder();

		// cfc.loopFetchingDataTest("VICK_API_CHAINING", "VICK_TEST_ACCOUNT", (apiEntity, accountEntity,
		// fetchMode) -> {
		// streamBuilder.add(new ChainingTestWorker(apiEntity, accountEntity, fetchMode));
		// });

		// return streamBuilder.build().toArray();

		ChainingFetchController cfc = new ChainingFetchController();
		String fetchMode = cfc.getFetchMode();
		List<List<ChainingEntity>> fakeEntityListContainer = new ArrayList<>(1);
		fakeEntityListContainer.add(new ArrayList<>());
		Stream.Builder<ChainingTestWorker> streamBuilder = Stream.builder();
		cfc.loopFetchingDataTest("VICK_API_CHAINING", "VICK_TEST_ACCOUNT",
				(apiEntity, accountEntity, group) -> {
					if (apiEntity == null) {
						streamBuilder.add(new ChainingTestWorker(fakeEntityListContainer.get(0),
								accountEntity, group, fetchMode));
						fakeEntityListContainer.set(0, new ArrayList<>());
					} else {
						fakeEntityListContainer.get(0).add(apiEntity);
					}
				});

		return streamBuilder.build().toArray();
	}

}
