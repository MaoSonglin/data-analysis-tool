package dat.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;

import dat.domain.UploadFile;
import dat.vo.ExcelSheet;
import dat.vo.Response;

public interface UploadFileService {

	Response save(UploadFile uploadFile);

	Response delete(String id);

	String getRealPath(String id);
	
	String getSavePath();
	
	
	default File getFile(String id){
		
		return null;
	}
	
	default File getFile(UploadFile file){
		return null;
	}
	
	default Workbook getWorkbook(String id) throws IOException,FileNotFoundException{
		return null;
	}

	Workbook getWorkbook(UploadFile association) throws FileNotFoundException, IOException;

	List<ExcelSheet> getExcel(String id);

	
	Response getRow(String id, String sheetName, Integer index) throws FileNotFoundException, IOException;
	
}
