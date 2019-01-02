package dat.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import dat.domain.User;

public interface UserRepository extends JpaRepository<User, String>,JpaSpecificationExecutor<User> {
	
	List<User> findByUsername(String username);
}
