package cloudos.appstore.model.app.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.daemon.ZillaRuntime;

import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@Slf4j @NoArgsConstructor @AllArgsConstructor
public class HeaderFilterHandler extends AppFilterHandlerBase {

    public static final String CLOSE_HEAD_TAG = "</head>";

    @Getter @Setter private String html;

    @Override public String apply(String document, Map<String, Object> scope) {
        if (empty(document)) return document;
        int index = document.lastIndexOf(CLOSE_HEAD_TAG);
        if (index == -1) {
            log.warn("No "+ CLOSE_HEAD_TAG +" found");
            return document;
        }

        return document.substring(0, index) + getHtml() + CLOSE_HEAD_TAG + document.substring(index + CLOSE_HEAD_TAG.length());
    }
}
