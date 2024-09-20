package api.test.main.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.testng.annotations.Factory;

import api.test.controllers.NoValidateFetchController;
import api.test.entities.FakeChainEntity;
import api.test.main.workers.FakeIndependentTestWorker3;

public class FakeIndepdentTestFactory3 {

	@Factory
	public Object[] createInstance() {

		NoValidateFetchController nvfc = new NoValidateFetchController();
		List<List<FakeChainEntity>> fakeEntityListContainer = new ArrayList<>(1);
		fakeEntityListContainer.add(new ArrayList<>());
		Stream.Builder<FakeIndependentTestWorker3> streamBuilder = Stream.builder();
		nvfc.loopFetchingDataTest("FAKE_TEST_2", "VICK_TEST_ACCOUNT", (apiEntity, fetchMode, group) -> {
			if (apiEntity == null) {
				streamBuilder.add(new FakeIndependentTestWorker3(fakeEntityListContainer.get(0), group));
				fakeEntityListContainer.set(0, new ArrayList<>());
			} else {
				fakeEntityListContainer.get(0).add(apiEntity);
				System.out.println("here: " + apiEntity.getRequestURL());

			}
		});

		return streamBuilder.build().toArray();

		// NoValidateFetchController nvfc = new NoValidateFetchController();
		// List<FakeChainEntity> fakeEnityList = new ArrayList<>();
		// Stream.Builder<FakeIndependentTestWorker3> streamBuilder = Stream.builder();
		// nvfc.loopFetchingDataTest("FAKE_TEST_2", "VICK_TEST_ACCOUNT", (apiEntity, fetchMode) -> {

		// if (apiEntity.getRequestURL().isEmpty()) {
		// streamBuilder.add(new FakeIndependentTestWorker3(fakeEnityList));
		// fakeEnityList = new ArrayList<>();
		// } else
		// fakeEnityList.add(apiEntity);

		// });
		// return streamBuilder.build().toArray();

	}

}
