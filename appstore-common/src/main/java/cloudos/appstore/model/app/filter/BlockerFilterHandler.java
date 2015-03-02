package cloudos.appstore.model.app.filter;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockerFilterHandler extends CompoundFilterHandler {

    @Getter @Setter private String frame_css;
    @Getter @Setter private String div_css;
    @Getter @Setter private String div_content;

    @Override protected List<AppFilterHandler> getHandlers(String document, Map<String, Object> scope) {
        final List<AppFilterHandler> handlers = new ArrayList<>();
        handlers.add(new HeaderFilterHandler(getFormattedCss(scope)));
        handlers.add(new FooterFilterHandler(getFormattedHtml(scope)));
        return handlers;
    }

    private String getFormattedCss(Map<String, Object> scope) {
        return render("\n\n<!-- ### START CLOUDOS CSS BLOCK -->\n\n" +
                        "<style type=\"text/css\">\n" +
                        "#__cloudos_blocker { " + getFrame_css() + " } \n" +
                        "#__cloudos_blocker div { " + getDiv_css() + " } \n" +
                        "</style>\n\n<!-- ### END CLOUDOS CSS BLOCK -->\n\n",
                scope);
    }

    private String getFormattedHtml(Map<String, Object> scope) {
        return render("\n\n<!-- ### BEGIN CLOUDOS FOOTER -->\n\n" +
                        "<script language=\"JavaScript\">" +
                        "setTimeout(function(){\n" +
                        "    $(\"#__cloudos_blocker\").hide();\n" +
                        "}, 30000);" +
                        "</script>\n" +
                        "<div id=\"__cloudos_blocker\">\n" +
                        "   <div>"+ getDiv_content()+"</div>\n" +
                        "</div>\n" +
                        "\n<!-- ### END CLOUDOS FOOTER -->\n\n",
                scope);
    }

}
