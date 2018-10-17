package com.csieflyman.limado.service;

import com.csieflyman.limado.model.Group;
import com.csieflyman.limado.model.Organization;
import com.csieflyman.limado.model.Party;
import com.csieflyman.limado.model.User;
import com.google.common.base.Preconditions;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * author flyman
 */
@Service("group")
public class GroupServiceImpl extends PartyServiceImpl<Group> implements GroupService {

    @Override
    public void addParents(Group child, Set<Party> parents) {
        Preconditions.checkArgument(child != null, "child must not be null");
        Preconditions.checkArgument(parents != null, "parents must not be null");

        if (parents.isEmpty())
            return;
        if (parents.stream().anyMatch(parent -> parent.getType().equals(User.TYPE) || parent.getType().equals(Organization.TYPE))) {
            throw new IllegalArgumentException(String.format("group %s can't add user or organization parent %s", child, parents));
        }

        super.addParents(child, parents);
    }
}
