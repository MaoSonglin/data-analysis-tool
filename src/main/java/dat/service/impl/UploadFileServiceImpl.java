package dat.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import dat.domain.UploadFile;
import dat.repos.UploadFileRepos;
import dat.service.UploadFileService;
import dat.util.Constant;
import dat.vo.ExcelSheet;
import dat.vo.Response;

@Service
public class UploadFileServiceImpl implements UploadFileService {

	@Autowired UploadFileRepos fileRepos;
	@Autowired Environment env;
	private Logger log;
	public UploadFileServiceImpl(){
		log = LogManager.getLogger(getClass());
	}
	@Transactional
	public Response save(UploadFile uploadFile) {
		if(uploadFile.getId()==null)
		uploadFile.generateId();
		UploadFile save = fileRepos.save(uploadFile);
		return new Response(Constant.SUCCESS_CODE,"保存成功",save);
	}

	@Override
	@Transactional
	public Response delete(String id) {
		UploadFile one = fileRepos.getOne(id);
		// 获取文件保存的相对路径
		String virtualPath = one.getVirtualPath();
		// 使用request获取web应用的绝对路径
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		String defaultPath = attributes.getRequest().getServletContext().getRealPath("/WEB-INF/upload");
		// 获取环境配置中设置的文件保存路径，使用web路径作为默认值
		String property = env.getProperty("file.upload.savepath", defaultPath);
		// 磁盘上的对应文件对象
		File file = Paths.get(property, virtualPath).toFile();
		log.info("删除文件"+file.getAbsolutePath());
		if(file.isFile()){
			// 删除文件
			boolean b = file.delete();
			if(b){
				fileRepos.delete(one);
				log.info("删除文件成功....");
				return new Response(Constant.SUCCESS_CODE,"删除成功");
			}
			log.info("文件删除失败...");
		}
		return new Response(Constant.ERROR_CODE,"删除失败");
	}
	
	@Override
	public String getRealPath(String id) {
		UploadFile fileInfo = fileRepos.findById(id).get();
		
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = requestAttributes.getRequest();
		
		String virtualPath = fileInfo.getVirtualPath();
		String dir = env.getProperty("file.upload.savepath",request.getServletContext().getRealPath("/WEB-INF/upload"));
		Path absolutePath = Paths.get(dir, virtualPath).toAbsolutePath();
		return absolutePath.toString();
	}
	
	
	@Override
	public String getSavePath() {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = requestAttributes.getRequest();
		
		String dir = env.getProperty("file.upload.savepath",request.getServletContext().getRealPath("/WEB-INF/upload"));
		return dir;
	}
	
	@Override
	public File getFile(String id) {
		UploadFile file = this.fileRepos.findById(id).orElse(null);
		return this.getFile(file);
	}
	
	@Override
	public File getFile(UploadFile file) {
		// 获取HTTPServletRequest对象
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = requestAttributes.getRequest();
		
		// 文件保存目录
		String dir = env.getProperty("file.upload.savepath", request.getServletContext().getRealPath("/WEB-INF/upload"));
		// 文件对象
		File f = Paths.get(dir,file.getVirtualPath()).toFile();
		return f;
	}
	
	@Override
	public Workbook getWorkbook(String id) throws IOException,
			FileNotFoundException {
		UploadFile file = this.fileRepos.findById(id).orElse(null);
		return getWorkbook(file);
	}
	
	@Override
	public Workbook getWorkbook(UploadFile file) throws FileNotFoundException, IOException {
		File uploadFile = getFile(file);
		// 文件在文件保存目录下的虚拟路径
		String virtualPath = file.getVirtualPath();

		if(!uploadFile.isFile()){
			throw new IllegalArgumentException("file '"+uploadFile.getAbsolutePath()+"' is not exist !");
		}
		Workbook workbook = null;
		if (virtualPath.endsWith(".xls")) {
			workbook = new HSSFWorkbook(new FileInputStream(uploadFile));
		} else if (virtualPath.endsWith(".xlsx")) {
			workbook = new XSSFWorkbook(new FileInputStream(uploadFile));
		} else {
			throw new IllegalArgumentException("文件类型不匹配:" + virtualPath);
		}
		return workbook;
	}
	@Override
	public List<ExcelSheet> getExcel(String id) {
		try (Workbook workbook = getWorkbook(id);) {
			List<ExcelSheet> list = new ArrayList<>();
			for(int i = 0 , number = workbook.getNumberOfSheets(); i < number; i++){
				// 获取工作簿
				Sheet sheetAt = workbook.getSheetAt(i);
				// 工作簿名称
				String sheetName = sheetAt.getSheetName();
				
				// 保存工作簿内容的javabean
				ExcelSheet excelSheet = new ExcelSheet();
				int lastRowNum = sheetAt.getLastRowNum();
				// 如果工作簿中的数据量小于等于0
				if(lastRowNum <= 0)
					continue;
				// 获取第一行数据
				Row row = sheetAt.getRow(0);
				
				// 存放栏位名称的数组
				excelSheet.setColumnNames(new ArrayList<>());
				// 存放栏位类型的数组
				excelSheet.setTypes(new ArrayList<>());
				excelSheet.setLengths(new ArrayList<>());
				for(short index = row.getFirstCellNum(), lastCellNum = row.getLastCellNum(); index < lastCellNum; index++){
					// 获取单元格
					Cell cell = row.getCell(index);
					if(cell != null){
						String value = cell.toString();
						excelSheet.getColumnNames().add(value);
					}else{
						excelSheet.getColumnNames().add(null);
					}
					excelSheet.getTypes().add("varchar");
					excelSheet.getLengths().add(255);
				}
				
				excelSheet.setSheetName(sheetName);
				excelSheet.setFieldNameRow(1);
				excelSheet.setFirstDataRow(2);
				excelSheet.setDatePattern("YMD");
				excelSheet.setDateSegmentation("/");
				excelSheet.setTimeSegmentation(":");
				excelSheet.setDateTimeSort("0");
				list.add(excelSheet);
			}
			return list;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public Response getRow(String id, String sheetName, Integer index) throws FileNotFoundException, IOException {
		try(Workbook workbook = getWorkbook(id);){
			Sheet sheet = workbook.getSheet(sheetName);
			Row row2 = sheet.getRow(index-1);
			List<String> list = new ArrayList<>();
			for (Cell cell : row2) {
				String value = cell.toString();
				list.add(value);
			}
			log.debug(list);
			return new Response(Constant.SUCCESS_CODE,"查询成功",list);
		}
	}
}
