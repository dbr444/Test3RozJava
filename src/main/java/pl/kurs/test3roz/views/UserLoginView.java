package pl.kurs.test3roz.views;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import org.hibernate.annotations.Subselect;

@Subselect("SELECT * FROM person_management.user_login_view")
@Entity
@Getter
public class UserLoginView {

    @Id
    private String email;

    private String password;

    private String type;
}