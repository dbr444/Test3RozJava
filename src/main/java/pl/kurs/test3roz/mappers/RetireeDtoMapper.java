package pl.kurs.test3roz.mappers;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import pl.kurs.test3roz.dto.PersonDto;
import pl.kurs.test3roz.dto.RetireeDto;
import pl.kurs.test3roz.models.people.Retiree;

@Component
@AllArgsConstructor
public class RetireeDtoMapper implements IPersonDtoMapper<Retiree> {

    private final ModelMapper modelMapper;

    @Override
    public PersonDto map(Retiree retiree) {
        return modelMapper.map(retiree, RetireeDto.class);
    }

    @Override
    public Class<Retiree> getSupportedClass() {
        return Retiree.class;
    }
}
