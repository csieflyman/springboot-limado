package com.csieflyman.limado.dto.party;

import com.csieflyman.limado.dto.Form;
import com.csieflyman.limado.model.Party;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.Set;
import java.util.UUID;

/**
 * @author csieflyman
 */
public class PartyForm<T extends Party> extends Form<T> {

    public UUID id = UUID.randomUUID();

    public Long version;

    @NotNull
    public String type;

    @NotNull
    @Size(max = 30)
    @Pattern(regexp = "^[a-zA-z]([\\w\\_\\-])+$")
    public String identity;

    @NotBlank
    @Size(max = 30)
    public String name;

    @NotNull
    @Email
    public String email;

    public Boolean enabled = true;

    @Valid
    public Set<Party> parents;

    @Valid
    public Set<Party> children;
}
