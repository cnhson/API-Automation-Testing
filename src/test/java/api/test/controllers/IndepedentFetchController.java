package api.test.controllers;

import java.util.function.BiConsumer;

import org.apache.commons.lang3.function.TriConsumer;

import api.test.entities.AccountEntity;
import api.test.entities.IndependentEntity;
import api.test.enums.ModeEnum;
import api.test.interfaces.DataFetchInterface;
import api.test.utilities.ExcelFetchUtil;
import api.test.utilities.GoogleDriveFetchUtil;
import api.test.utilities.GoogleSheetsFetchUtil;
import api.test.utilities.PropertyUtil;

public class IndepedentFetchController {

	private PropertyUtil pu = new PropertyUtil();
	private GoogleDriveFetchUtil gdfu = new GoogleDriveFetchUtil();
	private Integer currentCellIndex = 1; // Cell index start from 1, Array index start from 0
	private Boolean isRunning = true;
	private ModeEnum modeChosen;
	private String sheetName;

	public IndepedentFetchController() {
	}

	public void setFetchSheetName(String sheetName) {
		try {
			this.sheetName = sheetName;
			Boolean switchMode = Boolean.parseBoolean(pu.getPropAsString("AUTO_SWITCH_MODE"));
			this.modeChosen = ModeEnum.ONLINE;

			if (switchMode) {
				Boolean isModified = gdfu.checkIfRecentlyModified();
				if (isModified == null) {
					gdfu.downloadSpreadSheetAsync().thenRun(() -> {
						System.out.println("---Download completed [" + sheetName + "]---");
					});
					System.out.println(
							"Switch to (ONLINE) mode since can't get the lastest modified timestamp from local file");
				} else if (isModified == true) {
					gdfu.downloadSpreadSheetAsync().thenRun(() -> {
						System.out.println("---Download completed [" + sheetName + "]---");
					});
					System.out.println("Switch to (ONLINE) mode since new modified timestamp found");
				} else {
					System.out.println("Switch to (OFFLINE) mode because it haven't been modified");
					this.modeChosen = ModeEnum.OFFLINE;
				}
			} else
				System.out.println("Default (ONLINE) mode");

		}
		catch (Exception e) {
			System.err.println("Error while adjusting mode: " + e.getMessage());
			System.err.println("Switch to (ONLINE) mode");
			this.modeChosen = ModeEnum.ONLINE;
		}
	}

	private DataFetchInterface sheetNameFetching() {

		switch (this.modeChosen) {
		case OFFLINE:
			return new ExcelFetchUtil(this.sheetName);
		case ONLINE:
			return new GoogleSheetsFetchUtil(this.sheetName);
		default:
			return new ExcelFetchUtil(this.sheetName);
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

	public String getFetchMode() {
		return this.modeChosen.name();
	}

	public void loopFetchingDataTest(TriConsumer<IndependentEntity, AccountEntity, Object[]> innerFunction) {
		try {

			DataFetchInterface apiIE = this.sheetNameFetching();
			//
			String username = pu.getPropAsString("USERNAME");
			String password = pu.getPropAsString("PASSWORD");
			AccountEntity ae = new AccountEntity(username, password);

			Object[] testInfo = new Object[2];
			String testDes = "";
			Integer testId;
			IndependentEntity ie = null;
			while (isRunning) {

				if (apiIE.isFetchingEnd(this.getCurrentCellIndex())) {
					System.out.println("[IndependentFetchController] Fetching end");
					apiIE.fetchClose();
					isRunning = false;
					break;
				} else {
					apiIE.fetchingData(this.getCurrentCellIndex());
					String[] result = apiIE.getRowCellsData(this.getCurrentCellIndex(), "ID", "PATH",
							"QUERY_PARAM", "SUCCESS_RESPONSE", "LOGIN_REQUIRED");
					String id = result[0];
					String reqUrl = result[1];
					String payload = result[2];
					String expectedRes = result[3];
					String authenRequire = result[4];

					testId = Integer.valueOf(id);
					ie = new IndependentEntity(id, reqUrl, payload, expectedRes, "", authenRequire);

					this.currentCellIndexIncrease();
				}
				testInfo[0] = testId;
				testInfo[1] = testDes;
				innerFunction.accept(ie, ae, testInfo);

			}
		}
		catch (Exception e) {
			System.err.println("Error in IndependentFetchController (index:" + this.getCurrentCellIndex()
					+ "): " + e.getMessage());
		}
	}
}
