package dat.controller;

import java.util.List;

import javax.annotation.Resource;

import org.jboss.logging.Logger;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import dat.App;
import dat.domain.VirtualTable;
import dat.domain.WorkPackage;
import dat.service.ReferenceService;
import dat.service.WorkPackageService;
import dat.util.Constant;
import dat.vo.PkgPageBean;
import dat.vo.Response;


@RestController
@RequestMapping("/pkg")
public class WorkPackageController {
	
	@Resource(name="workPackageServiceImpl")
	private WorkPackageService wpService;
	
	
	private Logger logger = Logger.getLogger(getClass());
	/**
	 * 根据id返回工作包
	 * @param id
	 * @return
	 */
	@GetMapping({"/{id}"})
	public Response get(@PathVariable(required=false) String id){
		Response response = wpService.getPackage(id);
		return response;
	}
	
	/**
	 * 获取指定ID的数据包包含的数据表
	 * @param id
	 * @return
	 */
	@GetMapping("/tab/{id}")
	public Response getTables(@PathVariable String id){
		return wpService.getTables(id);
	}
	
	@GetMapping("/tab/col/{id}")
	public Object getTablesAndColumns(@PathVariable String id) throws Exception{
		List<VirtualTable> tables = wpService.getTablesAndColumns(id);
		JSONArray jsonArray = new JSONArray();
		ObjectMapper objectMapper = new ObjectMapper();
		for (VirtualTable table : tables) {
			String json = objectMapper.writeValueAsString(table);
			String columns = objectMapper.writeValueAsString(table.getColumns());
			JSONObject object = JSON.parseObject(json);
			JSONArray array = JSON.parseArray(columns);
			object.put("columns", array);
			jsonArray.add(object);
		}
		String jsonString = JSON.toJSONString(new Response(Constant.SUCCESS_CODE,"查询成功"));
		JSONObject object = JSON.parseObject(jsonString);
		object.put("tables", jsonArray);
		String writeValueAsString = objectMapper.writeValueAsString(App.getContext().getBean(ReferenceService.class).findByPkgId(id));
		object.put("refs", JSON.parseArray(writeValueAsString));
		return object.toJSONString();
	}
	
	@GetMapping()
	public Response getTables(PkgPageBean pageBean){
		Response pkgs = wpService.getPkgs(pageBean);
		return pkgs;
	}
	
	/**
	 * 添加数据包
	 * @param pg
	 * @return
	 */
	@PostMapping()
	public Response add(WorkPackage pg){
		logger.info(pg);
		return wpService.add(pg);
	}
	
	/**
	 * 修改数据包信息
	 * @param pkg 修改后的数据包
	 * @return
	 */
	public Response update(WorkPackage pkg){
		return wpService.add(pkg);
	}
	
	/**
	 * 为ID为pid的数据包添加一个ID为tid的数据，不会将ID为tid的数据表指定到pid的数据包中，而是
	 * 建立一个虚拟数据表，将tid中的数据字段存放到虚拟数据表中
	 * @param pid
	 * @param tid
	 * @return
	 */
	@PutMapping("/addTab")
	public Response addTab(PkgAddTable pkg){
		logger.debug(pkg);
		return wpService.addTab(pkg.getPid(),pkg.getTids().stream().toArray(String[]::new));
	}
	
	/**
	 * 为pid对应的数据包移除ID为tid的虚拟数据表
	 * @param pid	数据包id
	 * @param tid	虚拟数据表ID
	 * @return
	 */
	@DeleteMapping("/rm/{pid}/{tid}")
	public Response rm(@PathVariable String pid,@PathVariable String tid){
		return wpService.remove(pid,tid);
	}
	
	
	@GetMapping("/extractor/{id}")
	public Response extractor(@PathVariable String id){
		Response res = wpService.updateIndex(id);
		return res;
	}
	
	
	
	
	/**
	 * 数据源dsid中pid没有添加的数据表
	 * @return
	 */
	@GetMapping("/not")
	public Response notAddTable(ExcludeTable excludetable){
		return wpService.getTables(excludetable);
	}
	
	
	public static class ExcludeTable{
		private String pkgid;
		private String dsid;
		private Integer page=1;
		private Integer limit=10;
		
		public ExcludeTable() {
			super();
		}
		public ExcludeTable(String pkgid, String dsid) {
			super();
			this.pkgid = pkgid;
			this.dsid = dsid;
		}
		public String getPkgid() {
			return pkgid;
		}
		public void setPkgid(String pkgid) {
			this.pkgid = pkgid;
		}
		public String getDsid() {
			return dsid;
		}
		public void setDsid(String dsid) {
			this.dsid = dsid;
		}
		public Integer getPage() {
			return page;
		}
		public void setPage(Integer page) {
			this.page = page;
		}
		public Integer getLimit() {
			return limit;
		}
		public void setLimit(Integer size) {
			this.limit = size;
		}
		
		
	}

	public static class PkgAddTable{
		private String pid;
		private List<String> tids;
		public PkgAddTable(String pid, List<String> tids) {
			super();
			this.pid = pid;
			this.tids = tids;
		}
		public PkgAddTable() {
			super();
			// TODO Auto-generated constructor stub
		}
		public String getPid() {
			return pid;
		}
		public void setPid(String pid) {
			this.pid = pid;
		}
		public List<String> getTids() {
			return tids;
		}
		public void setTids(List<String> tids) {
			this.tids = tids;
		}
		@Override
		public String toString() {
			return "PkgAddTable [pid=" + pid + ", tids=" + tids + "]";
		}
		
	}
}
