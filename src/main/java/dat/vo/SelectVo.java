package dat.vo;

import lombok.Data;

@Data
public class SelectVo {
	
	private String graphId;
	
	private String select;
	
	private String where;
	
	private String groupBy;
	
	private String orderBy;
}
