package eva.platzda.backend.core.repositories;

import eva.platzda.backend.core.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByName(String name);
}
