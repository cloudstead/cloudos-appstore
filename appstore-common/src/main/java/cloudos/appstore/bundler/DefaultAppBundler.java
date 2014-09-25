package cloudos.appstore.bundler;

import cloudos.appstore.model.app.AppManifest;
import cloudos.appstore.model.app.AppStyle;
import cloudos.appstore.model.app.AppWebApache;
import cloudos.appstore.model.app.AppWebType;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.json.JsonUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@Slf4j
public class DefaultAppBundler implements AppBundler {

    public static final String APP = "__app__";
    public static final String COOKBOOK = "chef/cookbooks/"+APP+"/";
    public static final String CHEF_METADATA = COOKBOOK + "metadata.rb";
    public static final String CHEF_README = COOKBOOK + "README.md";
    public static final String CHEF_RECIPES = COOKBOOK + "recipes/";
    public static final String CHEF_LIBRARIES = COOKBOOK + "libraries/";
    public static final String CHEF_FILES = COOKBOOK + "files/default/";
    public static final String CHEF_TEMPLATES = COOKBOOK + "templates/default/";

    @Override
    public void bundle(BundlerOptions options, AppManifest manifest) throws Exception {

        final File outputDir = options.getOutputDir();
        final String outputBase = outputDir.getAbsolutePath() + "/";
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IllegalArgumentException("Error creating output dir: "+ outputBase);
        }

        final String name = manifest.getName();
        final AppStyle style = manifest.getStyle();
        if (style == null) throw new IllegalArgumentException("style not defined. use one of: "+Arrays.asList(AppStyle.values()));
        final String styleName = style.name().toLowerCase();
        final String baseDir = options.getManifest().getParentFile().getAbsolutePath() + "/";

        final Map<String, Object> scope = new HashMap<>();
        scope.put("app", manifest);

        final List<String> templates = new ArrayList<>();
        templates.add(CHEF_METADATA);
        templates.add(CHEF_README);
        templates.add(CHEF_RECIPES + "default.rb");
        templates.add(CHEF_LIBRARIES + "common_lib.rb");
        templates.add(CHEF_LIBRARIES + styleName + "_lib.rb");

        if (manifest.hasRepo()) templates.add(CHEF_LIBRARIES + "install_git_lib.rb");
        if (manifest.hasTarball()) templates.add(CHEF_LIBRARIES + "install_tarball_lib.rb");

        if (style == AppStyle.rails) {
            templates.add(CHEF_TEMPLATES + "database.yml.erb");
            templates.add(CHEF_TEMPLATES + "Procfile.erb");
        }

        if (manifest.hasSysinit()) templates.add(CHEF_TEMPLATES + "init.sh.erb");

        if (manifest.hasDatabase()) {
            templates.add(CHEF_LIBRARIES + "database_lib.rb");
            templates.add(CHEF_LIBRARIES + "database_"+ styleName +"_lib.rb");

            if (manifest.getDatabase().hasHere_schema()) {
                final String schema = manifest.getDatabase().getHere_schema();
                final File outputSchemaFile = outputFile(outputBase, CHEF_FILES, name, schema);
                FileUtils.copyFile(new File(baseDir + "files/" + schema), outputSchemaFile);
            }
        }

        if (manifest.hasWeb()) {
            final AppWebType webType = manifest.getWeb().getType();
            if (webType == null) throw new IllegalArgumentException("web.type not defined. use one of: "+Arrays.asList(AppWebType.values()));
            templates.add(CHEF_LIBRARIES + webType + "_lib.rb");
            templates.add(CHEF_LIBRARIES + webType + "_" + styleName +"_lib.rb");

            if (webType == AppWebType.apache) {
                final AppWebApache apache = manifest.getWeb().getApache();
                if (apache != null) {
                    if (apache.hasVhost()) {
                        final String dirFile = "apache_vhost.conf.erb";
                        copyToTemplates(outputBase, name, baseDir, dirFile);
                    }
                    if (apache.hasDir()) {
                        for (String dir : apache.getDir()) {
                            final String dirFile = "apache_dir_" + dir.replace("@doc_root", "doc_root").replace("/", "_") + ".conf.erb";
                            copyToTemplates(outputBase, name, baseDir, dirFile);
                        }
                    }
                    if (apache.hasLocation()) {
                        for (String loc : apache.getLocation()) {
                            if (loc.isEmpty() || loc.equals("/")) loc = "root";
                            else if (loc.startsWith("/")) loc = "root_" + loc.substring(1);
                            final String locFile = "apache_location_" + loc.replace("/", "_") + ".conf.erb";
                            copyToTemplates(outputBase, name, baseDir, locFile);
                        }
                    }
                    if (apache.hasHtaccess()) {
                        for (String htaccess : apache.getHtaccess()) {
                            final String htaccessFile = "apache_htaccess_" + htaccess.replace("@doc_root", "doc_root").replace("/", "_") + ".conf.erb";
                            copyToTemplates(outputBase, name, baseDir, htaccessFile);
                        }
                    }
                }
            }
        }

        final Map<String, String> manifestTemplates = manifest.getTemplates();
        if (manifest.hasTemplates()) {
            for (String path : manifestTemplates.keySet()) {
                String src = manifestTemplates.get(path);
                if (src.equals("_")) src = new File(path).getName();
                final File outputFile = outputFile(outputBase, CHEF_TEMPLATES, name, src + ".erb");
                FileUtils.copyFile(new File(baseDir + "templates/" + src + ".erb"), outputFile);
            }
        }

        final File messagesFile = new File(baseDir + "files/messages.properties");
        if (messagesFile.exists()) {
            FileUtils.copyFile(messagesFile, outputFile(outputBase, CHEF_FILES, name, messagesFile.getName()));
        }

        final String libName = name + "_lib.rb";
        final File libraryFile = new File(baseDir + "libraries/" + libName);
        if (libraryFile.exists()) {
            FileUtils.copyFile(libraryFile, outputFile(outputBase, CHEF_LIBRARIES, name, libName));
        }

        final TemplateLoader loader = new ClassPathTemplateLoader("/bundler/");
        final Handlebars handlebars = new Handlebars(loader);

        handlebars.registerHelper("safe", new Helper<Object>() {
            public CharSequence apply(Object src, Options options) {
                return src == null || src.toString().isEmpty() ? "" : new Handlebars.SafeString(src.toString().replace("'", "\\'"));
            }
        });
        handlebars.registerHelper("quoted_or_nil", new Helper<Object>() {
            public CharSequence apply(Object src, Options options) {
                return src == null || src.toString().isEmpty() ? "nil" : new Handlebars.SafeString("'"+src.toString().replace("'", "\\'")+"'");
            }
        });
        handlebars.registerHelper("ident", new Helper<Object>() {
            public CharSequence apply(Object src, Options options) {
                if (src == null || src.toString().isEmpty()) return "";
                final StringBuilder sb = new StringBuilder();
                for (char c : src.toString().toCharArray()) {
                    sb.append(Character.isLetterOrDigit(c) ? c : '_');
                }
                return new Handlebars.SafeString(sb.toString());
            }
        });
        for (String template : templates) {
            final Template hbs = handlebars.compile(template);
            final String path = template.replace(APP, name).replace("/", File.separator);
            final File file = new File(outputBase + File.separator + path);
            final File parent = file.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("error creating directory: "+parent.getAbsolutePath());
            }

            try (Writer w = new FileWriter(file)) {
                hbs.apply(scope, w);
            }
        }

        if (style == AppStyle.rails) {
            manifest.addLogrotate("@repo/log/*.log");
        }

        FileUtil.toFile(new File(outputDir, "cloudos-manifest.json"), JsonUtil.toJson(manifest));
    }

    protected void copyToTemplates(String outputBase, String name, String baseDir, String dirFile) throws IOException {
        final File outputFile = outputFile(outputBase, CHEF_TEMPLATES, name, dirFile);
        FileUtils.copyFile(new File(baseDir + "templates/" + dirFile), outputFile);
    }

    protected File outputFile(String base, String path, String appName, String file) {
        return new File(base + path.replace(APP, appName) + file);
    }

}