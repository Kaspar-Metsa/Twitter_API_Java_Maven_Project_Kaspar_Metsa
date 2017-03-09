package pakk;

public class MySortAction implements SortAction {

	private Integer field;
	private Integer order;
	
	MySortAction(){
		field = null;
		order = ORDER_ASCENDING;
	}
	MySortAction(int field, int order){
		setSortField(field);
		setSortOrder(order);
	}
	@Override
	public void setSortField(int field) {
		this.field = field;
	}

	@Override
	public int getSortField() {
		return field;
	}

	@Override
	public void setSortOrder(int order) {
		this.order = order;
	}

	@Override
	public int getSortOrder() {
		return order;
	}

}
