package eva.platzda.backend.core.controllers;

import eva.platzda.backend.core.dtos.TimeslotDto;
import eva.platzda.backend.core.models.Timeslot;
import eva.platzda.backend.core.services.TableService;
import eva.platzda.backend.core.services.TimeslotService;
import eva.platzda.backend.core.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/slot")
public class TimeslotController {

    private UserService userService;
    private TableService tableService;
    private TimeslotService timeslotService;

    @Autowired
    public TimeslotController(UserService u, TableService ta, TimeslotService ti) {
        this.userService = u;
        this.tableService = ta;
        this.timeslotService = ti;
    }

    @GetMapping
    public ResponseEntity<List<TimeslotDto>> getAllTimeslots() {
        List<Timeslot> timeslots = timeslotService.findAllTimeslots();
        List<TimeslotDto> timeslotDtos = timeslots.stream()
                .map(TimeslotDto::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(timeslotDtos);
    }

}
