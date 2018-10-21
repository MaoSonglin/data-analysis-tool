package dat.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dat.domain.User;

public interface UserRepository extends JpaRepository<User, String> {
	
	List<User> findByUsername(String username);
}
