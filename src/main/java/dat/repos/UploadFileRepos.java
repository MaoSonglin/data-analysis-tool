package dat.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import dat.domain.UploadFile;

public interface UploadFileRepos extends JpaRepository<UploadFile, String>,
		JpaSpecificationExecutor<UploadFile> {

}
