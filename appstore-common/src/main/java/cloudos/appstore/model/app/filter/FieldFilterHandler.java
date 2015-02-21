package cloudos.appstore.model.app.filter;

import lombok.Getter;
import lombok.Setter;

import static org.cobbzilla.util.string.StringUtil.empty;

public class FieldFilterHandler extends ElementFilterHandler {

    @Getter @Setter private String name;

    public String getElement() {
        if (!empty(super.getElement())) return super.getElement();
        return "input";
    }

    public String getMatch_attr_name() {
        if (!empty(super.getMatch_attr_name())) return super.getMatch_attr_name();
        return "name";
    }

    public String getMatch_attr_value() {
        if (!empty(super.getMatch_attr_value())) return super.getMatch_attr_value();
        if (empty(name)) throw new IllegalStateException("Neither name nor match_attr_value was set");
        return name;
    }

    public String getReplace_attr() {
        if (!empty(super.getReplace_attr())) return super.getReplace_attr();
        return "value";
    }

}
