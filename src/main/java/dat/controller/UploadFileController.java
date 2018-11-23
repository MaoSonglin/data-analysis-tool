package dat.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dat.domain.UploadFile;
import dat.service.UploadFileService;
import dat.util.Constant;
import dat.vo.Response;

/**
 * @author MaoSonglin
 * 文件上传接受控制类
 */
@RestController
@RequestMapping("/file")
public class UploadFileController {
	
	private static Logger log = LogManager.getLogger(UploadFileController.class);
	
	@Autowired
	Environment env;
	
	@Resource(name="uploadFileServiceImpl")
	UploadFileService fileService;
	
	/**
	 * 上传保存文件
	 * @param file
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method=RequestMethod.POST,value={"/upload"})
	public Response hello(MultipartFile file,HttpServletRequest request) throws IOException{
		if(file == null){
			return new Response(Constant.ERROR_CODE,"您没有上传文件！");
		}
		// 获取配置文件中配置的文件保存路径，如果没有配置，使用servlet容器下的WEB-INF/upload路径
		String realPath = env.getProperty("file.upload.savepath", 
				request.getServletContext().getRealPath("/WEB-INF/upload"));
		// 上传文件的文件按日期分文件夹存放
		String virtualdir =	new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		File file2 = Paths.get(realPath, virtualdir).toFile();
		// 判断目录是否存在
		if(!file2.isDirectory()){
			// 如果目录不存在创建目录
			file2.mkdirs();
		}
		// 获取原名称
		String originalFilename = file.getOriginalFilename();
		// 新建文件
		File file3 = new File(file2,originalFilename);
		int i = 1;
		while(file3.exists()){// 如果文件已经存在
			int of = originalFilename.lastIndexOf(".");
			if(of>-1){
				String prefix = originalFilename.substring(0, of);
				String nextfix = originalFilename.substring(of+1);
				file3 = new File(file2,prefix+"("+(i++)+")."+nextfix);
			}else{
				file3 = new File(file2,originalFilename+"("+(i++)+")");
			}
		}
		log.info("保存文件为:"+file3.getAbsolutePath());
		// 保存文件
		IOUtils.write(file.getBytes(),new FileOutputStream(file3));
		
		// 保存文件数据信息
		String virtualPath = virtualdir+System.getProperty("file.separator")+file3.getName();
		UploadFile uploadFile = new UploadFile();
		uploadFile.setVirtualPath(virtualPath);
		uploadFile.setFileName(file3.getName());
		uploadFile.setAddTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())); 
		uploadFile.setSize(file.getSize());
		Response response = fileService.save(uploadFile);
		
		return response;
	}

	@DeleteMapping("/{id}")
	public Response remove(@PathVariable String id){
		Response response = fileService.delete(id);
		return response;
	}
}
