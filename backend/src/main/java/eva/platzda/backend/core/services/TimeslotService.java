package eva.platzda.backend.core.services;


import eva.platzda.backend.core.models.Timeslot;
import eva.platzda.backend.core.repositories.TimeslotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing Timeslot entities.
 *
 * Provides methods to create, read, update, and delete timeslots,
 * as well as utility functions for filtering free or outdated timeslots.
 */
@Service
public class TimeslotService {

    private final TimeslotRepository timeslotRepository;

    /**
     * Constructs a new TimeslotService with the given repository.
     *
     * @param timeslotRepository Repository for Timeslot entities
     */
    @Autowired
    public TimeslotService(TimeslotRepository timeslotRepository) {
        this.timeslotRepository = timeslotRepository;
    }

    /**
     * Returns all timeslots in the system.
     *
     * @return List of all Timeslot entities
     */
    public List<Timeslot> findAllTimeslots(){return timeslotRepository.findAll();}

    /**
     * Returns all free timeslots that are not assigned to any user.
     *
     * @return List of free Timeslot entities
     */
    public List<Timeslot> findAllFreeTimeslots() {return timeslotRepository.findFreeTimeslots();}

    /**
     * Finds a timeslot by its ID.
     *
     * @param id ID of the timeslot
     * @return Timeslot entity if found, null otherwise
     */
    public Timeslot findById(Long id) {return timeslotRepository.findById(id).orElse(null);}

    /**
     * Creates a new timeslot.
     *
     * @param timeslot Timeslot entity to create
     * @return The created Timeslot entity
     */
    public Timeslot createTimeslot(Timeslot timeslot) {
        timeslot.setId(null);
        return timeslotRepository.save(timeslot);
    }

    /**
     * Updates an existing timeslot.
     *
     * @param timeslot Timeslot entity to update
     * @return The updated Timeslot entity
     */
    public Timeslot updateTimeslot(Timeslot timeslot) {return timeslotRepository.save(timeslot);}

    /**
     * Deletes a timeslot by its ID.
     *
     * @param id ID of the timeslot to delete
     */
    public void deleteTimeslotById(Long id) {timeslotRepository.deleteById(id);}

    /**
     * Deletes all timeslots in the system.
     */
    public void deleteAllTimeslots() {timeslotRepository.deleteAll();}


    /**
     * Deletes all timeslots that start before a specified date.
     *
     * @param date LocalDate; timeslots starting before this date will be removed
     */
    public void deleteTimeslotsBeforeDate(LocalDate date) {
        List<Timeslot> slots = findAllTimeslots().stream().filter(timeslot -> timeslot.getStartTime().toLocalDate().isBefore(date)).toList();
        for (Timeslot slot : slots) {
            deleteTimeslotById(slot.getId());
        }
    }
}
