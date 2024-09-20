package api.test.entities;

public class SuiteInfoDBEntity {
	private Integer id;
	private String mode;
	private String suiteName;
	private Integer passedCount;
	private Integer failedCount;
	private Integer skippedCount;
	private String startDate;
	private String endDate;
	private String totalTime;

	public SuiteInfoDBEntity() {

	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public Integer getPassedCount() {
		return passedCount;
	}

	public void setPassedCount(Integer passedCount) {
		this.passedCount = passedCount;
	}

	public Integer getFailedCount() {
		return failedCount;
	}

	public void setFailedCount(Integer failedCount) {
		this.failedCount = failedCount;
	}

	public Integer getSkippedCount() {
		return skippedCount;
	}

	public void setSkippedCount(Integer skippedCount) {
		this.skippedCount = skippedCount;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(String totalTime) {
		this.totalTime = totalTime;
	}

	public String getSuiteName() {
		return suiteName;
	}

	public void setSuiteName(String suiteName) {
		this.suiteName = suiteName;
	}

}
