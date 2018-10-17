package com.csieflyman.limado.service;

import com.csieflyman.limado.model.Organization;
import com.csieflyman.limado.model.Party;

/**
 * author flyman
 */
public interface OrganizationService extends PartyService<Organization> {

    void movePartyToOrganization(Party child, Organization organization);
}
