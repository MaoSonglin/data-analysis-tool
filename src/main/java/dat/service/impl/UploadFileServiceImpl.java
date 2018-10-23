package dat.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dat.domain.Response;
import dat.domain.UploadFile;
import dat.repos.UploadFileRepos;
import dat.service.UploadFileService;
import dat.util.Constant;

@Service
public class UploadFileServiceImpl implements UploadFileService {

	@Autowired UploadFileRepos fileRepos;
	
	@Transactional
	public Response save(UploadFile uploadFile) {
		uploadFile.generateId();
		UploadFile save = fileRepos.save(uploadFile);
		return new Response(Constant.SUCCESS_CODE,"保存成功",save);
	}

}