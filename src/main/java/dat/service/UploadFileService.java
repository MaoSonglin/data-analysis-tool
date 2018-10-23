package dat.service;

import dat.domain.Response;
import dat.domain.UploadFile;

public interface UploadFileService {

	Response save(UploadFile uploadFile);

}
