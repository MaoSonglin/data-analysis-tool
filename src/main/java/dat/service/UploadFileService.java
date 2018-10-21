package dat.service;

import dat.domain.UploadFile;
import dat.pojo.Response;

public interface UploadFileService {

	Response save(UploadFile uploadFile);

}
