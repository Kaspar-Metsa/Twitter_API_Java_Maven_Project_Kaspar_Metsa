package pakk;


public class MySearchAction implements SearchAction {

	private String keyword;
	
	MySearchAction(String keyword){
		this.keyword = keyword;
	}
	@Override
	public void setSearchKeyword(String keyword) {
		this.keyword = keyword;
	}

	@Override
	public String getSearchKeyword() {
		return keyword;
	}

}
