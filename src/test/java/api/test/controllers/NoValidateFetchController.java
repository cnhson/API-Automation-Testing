package api.test.controllers;

import java.util.function.BiConsumer;

import org.apache.commons.lang3.function.TriConsumer;

import api.test.entities.FakeChainEntity;
import api.test.entities.IndependentEntity;
import api.test.enums.ModeEnum;
import api.test.interfaces.DataFetchInterface;
import api.test.utilities.ExcelFetchUtil;
import api.test.utilities.GoogleDriveFetchUtil;
import api.test.utilities.GoogleSheetsFetchUtil;
import api.test.utilities.PropertyUtil;
import io.restassured.response.Response;

public class NoValidateFetchController {

	private PropertyUtil pu = new PropertyUtil();
	private GoogleDriveFetchUtil gdfu = new GoogleDriveFetchUtil();
	private Integer currentCellIndex = 1; // Cell index start from 1, Array index start from 0
	private Boolean isRunning = true;
	private ModeEnum modeChosen;

	public NoValidateFetchController() {
		try {
			Boolean switchMode = Boolean.parseBoolean(pu.getPropAsString("AUTO_SWITCH_MODE"));
			this.modeChosen = ModeEnum.ONLINE;

			if (switchMode) {
				Boolean isModified = gdfu.checkIfRecentlyModified();
				if (isModified == null) {
					gdfu.downloadSpreadSheetAsync().thenRun(() -> {
						System.out.println("---Download completed---");
					});
					System.out.println(
							"Switch to (ONLINE) mode since can't get the lastest modified timestamp from local file");
				} else if (isModified == true) {
					gdfu.downloadSpreadSheetAsync().thenRun(() -> {
						System.out.println("---Download completed---");
					});
					System.out.println("Switch to (ONLINE) mode since new modified timestamp found");
				} else {
					System.out.println("Switch to (OFFLINE) mode because it haven't been modified");
					this.modeChosen = ModeEnum.OFFLINE;
				}
			}
		}
		catch (Exception e) {
			System.err.println("Error while adjusting mode: " + e.getMessage());
			System.err.println("Switch to (ONLINE) mode");
			this.modeChosen = ModeEnum.ONLINE;
		}
	}

	public DataFetchInterface sheetNameFetching(String sheetName) {
		switch (this.modeChosen) {
		case OFFLINE:
			return new ExcelFetchUtil(sheetName);
		case ONLINE:
			return new GoogleSheetsFetchUtil(sheetName);
		default:
			return new ExcelFetchUtil(sheetName);
		}
	}

	public Boolean getIsRunning() {
		return this.isRunning;
	}

	public void setIsRunning(Boolean isRunning) {
		this.isRunning = isRunning;
	}

	public void currentCellIndexIncrease() {
		this.currentCellIndex += 1;
	}

	public Integer getCurrentCellIndex() {
		return this.currentCellIndex;
	}

	public void loopFetchingDataTest(String apiSheetName, String accountSheetName,
			TriConsumer<FakeChainEntity, String, Object[]> innerFunction) {
		try {
			// AuthenUtil au = new AuthenUtil();
			//
			DataFetchInterface apiIE = this.sheetNameFetching(apiSheetName);
			// DataFetchInterface accountIE = this.sheetNameFetching(accountSheetName);
			//
			// accountIE.fetchingData(1);
			// String[] accountInfo = accountIE.getRowCellsData(1, "USERNAME", "PASSWORD");
			// accountIE.fetchClose();
			Object[] groupInfo = new Object[2];
			String groupDes = "";
			Integer groupId = 1;
			FakeChainEntity fce;
			while (isRunning) {

				if (apiIE.isFetchingEnd(this.getCurrentCellIndex())) {
					System.out.println("Fetching end");
					apiIE.fetchClose();
					isRunning = false;
					fce = null;
				} else {
					apiIE.fetchingData(this.currentCellIndex);

					String[] result = apiIE.getRowCellsData(this.getCurrentCellIndex(), "ID", "DESCRIPTION",
							"PATH", "QUERY_PARAM", "SUCCESS_RESPONSE", "LOGIN_REQUIRED");
					if (result[0] == null || result[0].isEmpty()) {
						System.out.println("Empty line found -> New Group");
						fce = null;
						groupId++;
					} else {
						String id = result[0];
						if (!result[1].isEmpty() && groupDes != result[1]) {
							groupDes = result[1];
						}

						String reqUrl = result[2];
						String payload = result[3];
						String expectedRes = result[4];
						String authenRequire = result[5];

						fce = new FakeChainEntity(id, reqUrl, false, payload, authenRequire, null, null);
					}
					this.currentCellIndexIncrease();
				}
				groupInfo[0] = groupId;
				groupInfo[1] = groupDes;
				innerFunction.accept(fce, this.modeChosen.name(), groupInfo);

			}
		}
		catch (Exception e) {
			System.err.println("Error in NoValidateFetchController (index:" + this.getCurrentCellIndex()
					+ "): " + e.getMessage());
		}
	}
}
