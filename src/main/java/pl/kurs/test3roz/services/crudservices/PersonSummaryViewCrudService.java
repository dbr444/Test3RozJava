package pl.kurs.test3roz.services.crudservices;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.kurs.test3roz.filters.GetPersonFilter;
import pl.kurs.test3roz.views.PersonSummaryView;
import pl.kurs.test3roz.repositories.PersonSummaryViewRepository;
import pl.kurs.test3roz.repositories.specifications.PersonSummaryViewSpecifications;

@Service
public class PersonSummaryViewCrudService extends GenericCrudService<PersonSummaryView, PersonSummaryViewRepository> {

    private final ModelMapper modelMapper;

    public PersonSummaryViewCrudService(PersonSummaryViewRepository repository, ModelMapper modelMapper) {
        super(repository);
        this.modelMapper = modelMapper;
    }

    public Page<PersonSummaryView> findFiltered(GetPersonFilter filter, Pageable pageable) {
        return repository.findAll(PersonSummaryViewSpecifications.withFilter(filter), pageable);
    }
}