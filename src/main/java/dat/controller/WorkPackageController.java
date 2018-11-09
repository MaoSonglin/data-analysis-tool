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

import dat.domain.PkgPageBean;
import dat.domain.Response;
import dat.domain.WorkPackage;
import dat.service.WorkPackageService;


@RestController
@RequestMapping("/pkg")
public class WorkPackageController {
	
	@Resource(name="workPackageServiceImpl")
	private WorkPackageService wpService;
	
	private Logger log = Logger.getLogger(getClass());
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
		log.info(pg);
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
		log.debug(pkg);
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
	
	/**
	 * 数据源dsid中pid没有添加的数据表
	 * @return
	 */
	@GetMapping("/{pid}/{dsid}")
	public Response notAddTable(@PathVariable String pid,@PathVariable String dsid){
		ExcludeTable excludeTable = new ExcludeTable(pid,dsid);
		return wpService.getTables(excludeTable);
	}
	
	public static class ExcludeTable{
		private String pkgid;
		private String dsid;
		
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
