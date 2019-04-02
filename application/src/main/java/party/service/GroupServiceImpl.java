package party.service;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import party.dao.PartyDao;
import party.model.Group;
import party.model.Party;
import party.model.PartyType;

import java.util.Collection;

/**
 * @author csieflyman
 */
@Slf4j
@Service("group")
public class GroupServiceImpl extends PartyServiceImpl<Group> implements GroupService {

    @Autowired
    public GroupServiceImpl(@Qualifier("partyDao") PartyDao<Group> partyDao) {
        super(partyDao);
    }

    @Override
    public void addParents(Group child, Collection<Party> parents) {
        Preconditions.checkArgument(child != null, "child must not be null");
        Preconditions.checkArgument(parents != null, "parents must not be null");

        if (parents.isEmpty())
            return;
        if (parents.stream().anyMatch(parent -> parent.getType() == PartyType.USER || parent.getType() == PartyType.OU)) {
            throw new IllegalArgumentException(String.format("group %s can't add user or organization parent %s", child, parents));
        }

        super.addParents(child, parents);
    }
}
