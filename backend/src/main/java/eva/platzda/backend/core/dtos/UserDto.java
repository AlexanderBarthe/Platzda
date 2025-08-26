package eva.platzda.backend.core.dtos;

import eva.platzda.backend.core.models.Restaurant;
import eva.platzda.backend.core.models.User;

import java.util.List;

/**
 * Dto for wrapping Restaurants as their ids
 */
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private List<Long> flags; // nur IDs

    /**
     * No Args constructor
     */
    public UserDto() {}

    /**
     * All Args Constructor
     * @param id ID of the user
     * @param name Name of the user
     * @param email Email Adress of the user
     * @param flags
     */
    public UserDto(Long id, String name, String email, List<Long> flags) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.flags = flags;
    }

    /**
     * Converts a User entity into a UserDto.
     *
     * @param user User entity
     * @return UserDto with mapped values
     */
    public static UserDto fromObject(User user) {
        List<Long> flagIds = user.getFlags().stream()
                .map(Restaurant::getId)
                .toList();
        return new UserDto(user.getId(), user.getName(), user.getEmail(), flagIds);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Long> getFlags() {
        return flags;
    }

    public void setFlags(List<Long> flags) {
        this.flags = flags;
    }

}
