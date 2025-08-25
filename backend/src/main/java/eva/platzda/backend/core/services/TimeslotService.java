package eva.platzda.backend.core.services;


import eva.platzda.backend.core.models.Timeslot;
import eva.platzda.backend.core.repositories.TimeslotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TimeslotService {

    private final TimeslotRepository timeslotRepository;

    @Autowired
    public TimeslotService(TimeslotRepository timeslotRepository) {
        this.timeslotRepository = timeslotRepository;
    }

    /* wat fehlt:
    freie Zeitslots richtig finden, pro tag
    zeitslot buchen
    */
    public List<Timeslot> findAllTimeslots(){return timeslotRepository.findAll();}

    public List<Timeslot> findAllFreeTimeslots() {return timeslotRepository.findFreeTimeslots();}

    public Timeslot findById(Long id) {return timeslotRepository.findById(id).orElse(null);}

    public Timeslot createTimeslot(Timeslot timeslot) {
        timeslot.setId(null);
        return timeslotRepository.save(timeslot);
    }

    public Timeslot updateTimeslot(Timeslot timeslot) {return timeslotRepository.save(timeslot);}

    public void deleteTimeslotById(Long id) {timeslotRepository.deleteById(id);}

    public void deleteAllTimeslots() {timeslotRepository.deleteAll();}

    public void deleteTimeslotsBeforeDate(LocalDate date) {
        List<Timeslot> slots = findAllTimeslots().stream().filter(timeslot -> timeslot.getStartTime().toLocalDate().isBefore(date)).toList();
        for (Timeslot slot : slots) {
            deleteTimeslotById(slot.getId());
        }
    }
}
