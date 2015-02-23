package cloudos.appstore.model.app.filter;

import lombok.Getter;
import lombok.Setter;

import static org.cobbzilla.util.string.StringUtil.safeFunctionName;

public class SubmitFilterHandler extends FooterFilterHandler {

    @Getter @Setter private String id;
    @Getter @Setter private String only_if = "true";
    @Getter @Setter private String not_if = "false";

    @Override public String getFooter () {
        final String func = "__cloudos_submit_form_" + safeFunctionName(id);

        return "<script language=\"JavaScript\">\n" +
                "function "+func+"() {\n" +
                "  if (!("+not_if+") && ("+only_if+")) {\n" +
                "    document.getElementById('"+id+"').submit();\n" +
                "  }\n" +
                "}\n" +
                "if(window.attachEvent) {\n" +
                "    window.attachEvent('onload', "+func+");\n" +
                "} else {\n" +
                "    if(window.onload) {\n" +
                "        var curronload = window.onload;\n" +
                "        var newonload = function() {\n" +
                "            curronload();\n" +
                "            "+func+"();\n" +
                "        };\n" +
                "        window.onload = newonload;\n" +
                "    } else {\n" +
                "        window.onload = "+func+";\n" +
                "    }\n" +
                "}\n" +
                "</script>\n";
    }

}
