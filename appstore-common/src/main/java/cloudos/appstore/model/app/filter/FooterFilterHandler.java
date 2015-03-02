package cloudos.appstore.model.app.filter;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static org.cobbzilla.util.string.StringUtil.empty;

@Slf4j
public abstract class FooterFilterHandler extends AppFilterHandlerBase {

    public static final String CLOSE_BODY_TAG = "</body>";

    protected abstract String getFooter();

    @Override public String apply(String document, Map<String, Object> scope) {
        if (empty(document)) return document;
        int index = document.lastIndexOf(CLOSE_BODY_TAG);
        if (index == -1) {
            log.warn("No "+CLOSE_BODY_TAG+" found");
            return document;
        }

        return document.substring(0, index) + getFooter() + CLOSE_BODY_TAG + document.substring(index + CLOSE_BODY_TAG.length());
    }

}
