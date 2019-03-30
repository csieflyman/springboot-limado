package party.dto;

import java.util.UUID;

/**
 * @author csieflyman
 */
public class PartyCreateForm extends PartyForm{

    public PartyCreateForm() {
        setId(UUID.randomUUID());
    }
}
