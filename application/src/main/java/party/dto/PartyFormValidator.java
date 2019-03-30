package party.dto;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

/**
 * @author csieflyman
 */
@Slf4j
public class PartyFormValidator implements Validator {

    private static final Pattern IDENTITY_PATTERN = Pattern.compile("^[a-zA-z]([\\w\\_\\-])+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9.!#$%&'*+\\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");

    @Override
    public boolean supports(Class<?> clazz) {
        return PartyForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PartyForm form = (PartyForm) target;
        if (!checkIdentity(form.getIdentity())) {
            errors.rejectValue("identity", "invalid.party.identity", new String[]{form.getIdentity()}, "invalid identity: " + form.getIdentity());
        }
        if (!checkName(form.getName())) {
            errors.rejectValue("name", "invalid.party.name", new String[]{form.getName()}, "invalid name: " + form.getName());
        }
        if (!checkEmail(form.getEmail())) {
            errors.rejectValue("email", "invalid.party.email", new String[]{form.getEmail()}, "invalid email: " + form.getEmail());
        }
    }

    private boolean checkIdentity(String identity) {
        return StringUtils.isNotBlank(identity) && identity.length() <= 30 && IDENTITY_PATTERN.matcher(identity).matches();
    }

    private boolean checkName(String name) {
        return StringUtils.isNotBlank(name) && name.length() <= 30;
    }

    private boolean checkEmail(String email) {
        return StringUtils.isEmpty(email) || (StringUtils.isNotBlank(email) && email.length() <= 80 && EMAIL_PATTERN.matcher(email).matches());
    }
}
