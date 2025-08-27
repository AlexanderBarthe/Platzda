package eva.platzda.backend.core.dtos;

import eva.platzda.backend.core.models.Timeslot;

import java.time.LocalDateTime;

/**
 * Dto for timelots
 */
public class TimeslotDto {

    private Long id;
    private LocalDateTime starttime;
    private LocalDateTime endtime;
    private Long userid;
    private Long table;

    /**
     * Creates a new TimeslotDto.
     *
     * @param id ID of the timeslot
     * @param starttime Start time of the timeslot
     * @param endtime End time of the timeslot
     * @param userid ID of the user that booked the timeslot
     * @param table ID of the reserved table
     */
    public TimeslotDto(Long id, LocalDateTime starttime, LocalDateTime endtime,Long userid, Long table) {
        this.id = id;
        this.starttime = starttime;
        this.endtime = endtime;
        this.userid = userid;
        this.table = table;
    }

    /**
     * Converts a Timeslot entity into a TimeslotDto.
     *
     * @param timeslot Timeslot entity
     * @return TimeslotDto with mapped values
     */
    public static TimeslotDto fromObject(Timeslot timeslot){
        if(timeslot==null) return null;

        Long userId = (timeslot.getUser() == null) ? null : timeslot.getUser().getId();
        Long tableId = (timeslot.getTable() == null) ? null : timeslot.getTable().getId();

        return new TimeslotDto(
                timeslot.getId(),
                timeslot.getStartTime(),
                timeslot.getEndTime(),
                userId,
                tableId
        );
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStarttime() {
        return starttime;
    }

    public void setStarttime(LocalDateTime starttime) {
        this.starttime = starttime;
    }

    public LocalDateTime getEndtime() {
        return endtime;
    }

    public void setEndtime(LocalDateTime endtime) {
        this.endtime = endtime;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public Long getTable() {
        return table;
    }

    public void setTables(Long table) {
        this.table = table;
    }
}
