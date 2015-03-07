package cloudos.appstore.model.app.config.validation;

import lombok.AllArgsConstructor;

import java.util.regex.Pattern;

@AllArgsConstructor
public class RegexFieldValidator extends BasicFieldValidator {

    private final Pattern pattern;

    @Override protected boolean isValid(String value) { return super.isValid(value) && pattern.matcher(value).matches(); }

}
