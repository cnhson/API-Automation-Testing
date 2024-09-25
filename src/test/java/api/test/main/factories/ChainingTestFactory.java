package api.test.main.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.testng.annotations.Factory;

import api.test.controllers.ChainingFetchController;
import api.test.entities.ChainingEntity;
import api.test.main.workers.ChainingTestWorker;

public class ChainingTestFactory {
	@Factory
	public Object[] createInstance() {
		List<List<ChainingEntity>> fakeEntityListContainer = new ArrayList<>(1);
		fakeEntityListContainer.add(new ArrayList<>());
		Stream.Builder<ChainingTestWorker> streamBuilder = Stream.builder();

		ChainingFetchController cfc = new ChainingFetchController();
		cfc.setFetchSheetName("VICK_API_CHAINING");
		String fetchMode = cfc.getFetchMode();
		cfc.loopFetchingDataTest((apiEntity, accountEntity, group) -> {
			if (apiEntity == null) {
				streamBuilder.add(new ChainingTestWorker(fakeEntityListContainer.get(0), accountEntity, group,
						fetchMode));
				fakeEntityListContainer.set(0, new ArrayList<>());
			} else {
				fakeEntityListContainer.get(0).add(apiEntity);
			}
		});

		return streamBuilder.build().toArray();
	}

}
