package party.dto;

import base.dto.request.AbstractForm;
import base.exception.NotImplementedException;
import base.util.BeanUtils;
import lombok.*;
import party.model.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author csieflyman
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class PartyForm extends AbstractForm<Party> {

    @EqualsAndHashCode.Include
    private UUID id;

    @NotNull
    private PartyType type;

    @NotNull
    @Size(max = 30)
    @Pattern(regexp = "^[a-zA-z]([\\w\\_\\-])+$")
    private String identity;

    @NotBlank
    @Size(max = 30)
    private String name;

    @NotNull
    @Email
    private String email;

    @NotNull
    private Boolean enabled;

    @Valid
    private Set<Party> parents = new HashSet<>();

    @Valid
    private Set<Party> children = new HashSet<>();

    @Override
    public Party toModel() {
        Party party;
        switch (type) {
            case USER:
                party = new User(identity);
                break;
            case OU:
                party = new Organization(identity);
                break;
            case GROUP:
                party = new Group(identity);
                break;
            default:
                throw new NotImplementedException("Undefined PartyType");
        }
        BeanUtils.copyIgnoreNull(this, party);
        return party;
    }
}
