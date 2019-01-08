package dat.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.core.env.Environment;

import dat.App;
import dat.domain.UploadFile;

public class ThreadFileReader{
	
	private static ThreadLocal<List<String>> thLines = new ThreadLocal<List<String>>();
	
	private static ThreadLocal<UploadFile> tlFileInfo = new ThreadLocal<UploadFile>();
	
	public static List<String> getLines(UploadFile fileInfo) throws IOException{
		UploadFile uploadFile = tlFileInfo.get();
		if(fileInfo.equals(uploadFile)){
			return thLines.get();
		}else{
			String virtualPath = fileInfo.getVirtualPath();
			String path = App.getContext().getBean(Environment.class).getProperty("file.upload.savepath", "");
			File file = Paths.get(path, virtualPath).toFile();
			if(!file.isFile()){
				throw new IllegalArgumentException("分类文件'"+fileInfo.getFileName()+"'没有找到");
			}
			List<String> lines = FileUtils.readLines(file, Charset.defaultCharset().name());
			thLines.set(lines);
			tlFileInfo.set(fileInfo);
			return lines;
		}
	}

}
