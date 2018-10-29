package dat.service.impl;

import java.io.File;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import dat.domain.Response;
import dat.domain.UploadFile;
import dat.repos.UploadFileRepos;
import dat.service.UploadFileService;
import dat.util.Constant;

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
		uploadFile.generateId();
		UploadFile save = fileRepos.save(uploadFile);
		return new Response(Constant.SUCCESS_CODE,"保存成功",save);
	}

	@Override
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

}
