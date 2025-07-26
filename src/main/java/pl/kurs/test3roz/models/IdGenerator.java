package pl.kurs.test3roz.models;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.LongStream;

@Component
@AllArgsConstructor
@Getter
public class IdGenerator {

    private final EntityManager em;

    @Transactional
    public Long nextId() {
        IdSequence seq = em.find(IdSequence.class, "person", LockModeType.PESSIMISTIC_WRITE);
        long current = seq.getNextVal();
        seq.setNextVal(current + 1);
        return current;
    }

    @Transactional
    public List<Long> nextIds(int count) {
        IdSequence seq = em.find(IdSequence.class, "person", LockModeType.PESSIMISTIC_WRITE);
        long start = seq.getNextVal();
        seq.setNextVal(start + count);
        return LongStream.range(start, start + count).boxed().toList();
    }
}
