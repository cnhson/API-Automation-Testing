package api.test.interfaces;

public interface DataFetchInterface {

    public void setConfig();

    public void fetchingData(Integer currentIndex);

    public String[] getRowCellsData(Integer rowIndex, String... colNames);

    public Boolean isCellNull(Integer rowIndex, Integer colIndex);

    public void fetchClose();

    public Boolean isFetchingEnd(Integer rowIndex);
}
