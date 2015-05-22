package cloudos.appstore.model.app.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@Slf4j @NoArgsConstructor @AllArgsConstructor
public class FooterFilterHandler extends AppFilterHandlerBase {

    public static final String CLOSE_BODY_TAG = "</body>";

    @Getter @Setter protected String footer;

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
