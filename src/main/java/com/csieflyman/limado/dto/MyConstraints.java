package com.csieflyman.limado.dto;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author James Lin
 */
public class MyConstraints {

    @Target({FIELD})
    @Retention(RUNTIME)
    @Constraint(validatedBy = EnumValidator.class)
    public @interface Enumerated {
        String message() default EnumValidator.message;
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
        String[] stringValues() default {};
        int[] intValues() default {};
        boolean ignoreCase() default false;
    }

    public static class EnumValidator implements ConstraintValidator<Enumerated, Object>{

        final static public String message = "error.invalid.enum";

        private String[] stringValues;
        private int[] intValues;
        private boolean ignoreCase;

        @Override
        public void initialize(Enumerated constraintAnnotation) {
            this.stringValues = constraintAnnotation.stringValues();
            this.intValues = constraintAnnotation.intValues();
            this.ignoreCase = constraintAnnotation.ignoreCase();
        }

        @Override
        public boolean isValid(Object object, ConstraintValidatorContext constraintContext) {
            boolean isValid = isValid(object);
            if(!isValid) {
                constraintContext.disableDefaultConstraintViolation();
                constraintContext.buildConstraintViolationWithTemplate(
                        String.format("invalid enum value. should be %s", getErrorMessageArgs())).addConstraintViolation();
            }
            return isValid;
        }

        private boolean isValid(Object input) {
            if(input == null)
                return true;

            if(stringValues != null && stringValues.length > 0) {
                for(String validString: stringValues) {
                    if(ignoreCase && validString.equalsIgnoreCase((String)input))
                        return true;
                    else if(!ignoreCase && validString.equals(input))
                        return true;
                }
            }
            else if(intValues != null && intValues.length > 0) {
                for(int validInteger: intValues) {
                    if(validInteger == (Integer)input)
                        return true;
                }
            }
            return false;
        }

        private List<?> getErrorMessageArgs() {
            String joinedValueString = "";
            if(stringValues != null) {
                joinedValueString = Arrays.toString(stringValues);
            }
            else if(intValues != null) {
                joinedValueString = Arrays.toString(intValues);
            }
            return Arrays.asList(joinedValueString);
        }
    }

    @Target({FIELD})
    @Retention(RUNTIME)
    @Constraint(validatedBy = EnumClassValidator.class)
    @play.data.Form.Display(name = "constraint.enum.class")
    public static @interface EnumeratedClass {
        String message() default EnumClassValidator.message;
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
        Class<? extends Enum> value();
    }

    public static class EnumClassValidator extends Constraints.Validator<Object> implements ConstraintValidator<EnumeratedClass, Object>{

        final static public String message = "error.invalid.enum";

        private Class<? extends Enum> enumClass;

        @Override
        public void initialize(EnumeratedClass constraintAnnotation) {
            this.enumClass = constraintAnnotation.value();
        }

        @Override
        public boolean isValid(Object object, ConstraintValidatorContext constraintContext) {
            boolean isValid = isValid(object);
            if(!isValid) {
                constraintContext.disableDefaultConstraintViolation();
                constraintContext.buildConstraintViolationWithTemplate(
                        String.format("invalid enum value. should be %s", getErrorMessageArgs())).addConstraintViolation();
            }
            return isValid;
        }

        @Override
        public boolean isValid(Object input) {
            if(input == null)
                return true;

            for (Field f : enumClass.getFields()) {
                if (f.isEnumConstant() && f.getName().equals(input)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public F.Tuple<String, Object[]> getErrorMessageKey() {
            return new F.Tuple<String, Object[]>(message, new Object[]{getErrorMessageArgs()});
        }

        private List<?> getErrorMessageArgs() {
            String joinedValueString = "";
            if(enumClass != null) {
                joinedValueString = Stream.of(enumClass.getFields()).map(field -> field.getName()).collect(Collectors.joining(","));
            }
            return Arrays.asList(joinedValueString);
        }
    }

    @Target({FIELD})
    @Retention(RUNTIME)
    @Constraint(validatedBy = DateTimeFormatValidator.class)
    @play.data.Form.Display(name = "constraint.dateTimeFormat")
    public static @interface DateTimeFormat {
        String message() default DateTimeFormatValidator.message;
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
        String value();
        boolean isCheck() default false;
        boolean isPast() default false;
    }

    public class DateTimeFormatValidator extends Constraints.Validator<String> implements ConstraintValidator<DateTimeFormat, String> {

        final static public String message = "error.invalid.dateTimeFormat";

        private DateTimeFormatter format;
        private String formatString;
        private boolean isCheck = false;
        private boolean isPast = false;

        @Override
        public void initialize(DateTimeFormat constraintAnnotation) {
            formatString = constraintAnnotation.value();
            format = DateTimeFormatter.ofPattern(formatString);
            isCheck = constraintAnnotation.isCheck();
            isPast = constraintAnnotation.isPast();
        }

        @Override
        public boolean isValid(String object, ConstraintValidatorContext constraintContext) {
            boolean isValid = isValid(object);
            if(!isValid) {
                constraintContext.disableDefaultConstraintViolation();
                constraintContext.buildConstraintViolationWithTemplate(
                        String.format("invalid datetime format. should be %s", formatString)).addConstraintViolation();
            }
            return isValid;
        }

        @Override
        public boolean isValid(String dateTimeString) {
            if(dateTimeString == null)
                return true;

            try {
                // FIXME how to compare datetime without timezone
                if(isCheck) {
                    if(isPast) {
                        return ZonedDateTime.parse(dateTimeString, format).isBefore(ZonedDateTime.now(ZoneId.of("UTC")));
                    }
                    else {
                        return ZonedDateTime.parse(dateTimeString, format).isAfter(ZonedDateTime.now(ZoneId.of("UTC")));
                    }
                }
            } catch (DateTimeParseException e) {
                return false;
            }
            return true;
        }

        @Override
        public F.Tuple<String, Object[]> getErrorMessageKey() {
            return new F.Tuple<String, Object[]>(message, new Object[]{formatString});
        }
    }
}
