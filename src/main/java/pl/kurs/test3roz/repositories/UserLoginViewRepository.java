package pl.kurs.test3roz.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.test3roz.views.UserLoginView;
import java.util.Optional;

public interface UserLoginViewRepository extends JpaRepository<UserLoginView, String> {
    Optional<UserLoginView> findByEmail(String email);
}
