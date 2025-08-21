package eva.platzda.backend.core.dtos;

import eva.platzda.backend.core.models.Timeslot;

import java.time.LocalDateTime;

public class TimeslotDto {

    private Long id;
    private LocalDateTime starttime;
    private LocalDateTime endtime;
    private Long userid;
    private Long table;

    public TimeslotDto(Long id, LocalDateTime starttime, LocalDateTime endtime,Long userid, Long table) {
        this.id = id;
        this.starttime = starttime;
        this.endtime = endtime;
        this.userid = userid;
        this.table = table;
    }

    public static TimeslotDto toDto(Timeslot timeslot){
        if(timeslot==null) return null;

        return new TimeslotDto(
                timeslot.getId(),
                timeslot.getStartTime(),
                timeslot.getEndTime(),
                timeslot.getUser().getId(),
                timeslot.getTable().getId()
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
