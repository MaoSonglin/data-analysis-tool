package dat.service;

import dat.domain.UploadFile;
import dat.vo.Response;

public interface UploadFileService {

	Response save(UploadFile uploadFile);

	Response delete(String id);

}
