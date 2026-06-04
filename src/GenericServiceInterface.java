import java.util.List;
import java.util.Optional;

public interface GenericServiceInterface<T> {
    void creeaza(T entitate);

    Optional<T> citesteDupaId(int id);

    List<T> citesteToate();

    void actualizeaza(T entitate);

    void sterge(int id);
}
