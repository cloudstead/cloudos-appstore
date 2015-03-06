package cloudos.appstore.bundler;

import cloudos.appstore.model.app.*;
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
import org.cobbzilla.util.system.CommandResult;
import org.cobbzilla.util.system.CommandShell;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static cloudos.appstore.model.app.AppManifest.CLOUDOS_MANIFEST_JSON;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.mkdirOrDie;

@Slf4j
public class DefaultAppBundler implements AppBundler {

    public static final String APP = "__app__";
    public static final String COOKBOOK = "chef/cookbooks/"+APP+"/";
    public static final String CHEF_DATABAGS = "chef/data_bags/"+APP+"/";
    public static final String CHEF_METADATA = COOKBOOK + "metadata.rb";
    public static final String CHEF_README = COOKBOOK + "README.md";
    public static final String CHEF_RECIPES = COOKBOOK + "recipes/";
    public static final String CHEF_LIBRARIES = COOKBOOK + "libraries/";
    public static final String CHEF_FILES = COOKBOOK + "files/default/";
    public static final String CHEF_TEMPLATES = COOKBOOK + "templates/default/";

    @Override
    public void bundle(BundlerOptions options, AppManifest manifest) throws Exception {

        validate(options, manifest);

        final File outputDir = mkdirOrDie(options.getOutputDir());
        final String outputBase = abs(outputDir) + "/";

        final String name = manifest.getName();
        final AppStyle style = manifest.getStyle();
        if (style == null) throw new IllegalArgumentException("style not defined. use one of: "+Arrays.asList(AppStyle.values()));
        final String styleName = style.name().toLowerCase();
        final String baseDir = abs(options.getManifest().getParentFile()) + "/";

        final Map<String, Object> scope = new HashMap<>();
        scope.put("app", manifest);

        final List<String> templates = new ArrayList<>();
        templates.add(CHEF_METADATA);
        templates.add(CHEF_README);
        templates.add(CHEF_RECIPES + "default.rb");
        templates.add(CHEF_LIBRARIES + "common_lib.rb");
        templates.add(CHEF_LIBRARIES + styleName + "_lib.rb");
        templates.add(CHEF_RECIPES + "lib.rb");
        templates.add(CHEF_RECIPES + "backup.rb");
        templates.add(CHEF_RECIPES + "restore.rb");

        if (manifest.hasRepo()) templates.add(CHEF_LIBRARIES + "install_git_lib.rb");
        if (manifest.hasTarball()) templates.add(CHEF_LIBRARIES + "install_tarball_lib.rb");

        // Copy all files in files/ to recipe/files/default/
        // This will pickup and appname_schema.sql (a here-schema), and anything else the app might need at install-time
        copy(options, manifest, "files");

        // Copy extra recipes and data_bags
        copy(options, manifest, "recipes");
        copy(options, manifest, "data_bags");

        // If there was no validate.rb recipe, but there is a validate.sh script in the files dir, create a validate.rb recipe
        if (!new File(baseDir+"recipes/validate.rb").exists()) {
            templates.add(CHEF_RECIPES + "validate.rb");
            if (new File(baseDir+"files/validate.sh").exists()) {
                manifest.setValidation_script(true);
            }
        }

        if (style == AppStyle.rails) {
            templates.add(CHEF_TEMPLATES + "database.yml.erb");
            templates.add(CHEF_TEMPLATES + "Procfile.erb");
        }

        if (manifest.hasSysinit()) {
            // todo: walk the sysinit values, include only the required templates
            templates.add(CHEF_TEMPLATES + "init.sh.erb");
            templates.add(CHEF_TEMPLATES + "init_wrapper.sh.erb");
        }

        if (manifest.hasDatabase()) {
            templates.add(CHEF_LIBRARIES + "database_lib.rb");
            templates.add(CHEF_LIBRARIES + "database_"+ styleName +"_lib.rb");
        }

        if (manifest.hasUserManagement()) {
            final AppUserManagement userMgmt = manifest.getAuth().getUser_management();
            templates.add(CHEF_TEMPLATES + "rooty_handler.yml.erb");
            copyToTemplates(outputBase, name, baseDir, basename(userMgmt.getExists()));
            copyToTemplates(outputBase, name, baseDir, basename(userMgmt.getCreate()));
            copyToTemplates(outputBase, name, baseDir, basename(userMgmt.getDelete()));
            copyToTemplates(outputBase, name, baseDir, basename(userMgmt.getChange_password()));
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
                return src == null || src.toString().isEmpty() ? "" : new Handlebars.SafeString(src.toString().replace("'", "\\'").replace("\"", "\\\""));
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
            mkdirOrDie(file.getParentFile());

            try (Writer w = new FileWriter(file)) {
                hbs.apply(scope, w);
            }
        }

        if (style == AppStyle.rails) {
            manifest.addLogrotate("@repo/log/*.log");
        }

        final File manifestFile = new File(outputDir, CLOUDOS_MANIFEST_JSON);
        FileUtil.toFile(manifestFile, JsonUtil.toJson(manifest));

        // Put a copy of the manifest under data_bags
        final File manifestCopy = outputFile(outputBase, CHEF_DATABAGS, name, CLOUDOS_MANIFEST_JSON);
        final File databagDir = mkdirOrDie(manifestCopy.getParentFile());
        FileUtils.copyFile(manifestFile, new File(databagDir, CLOUDOS_MANIFEST_JSON));
    }

    private void validate(BundlerOptions options, AppManifest manifest) {
        // If filters are declared, make sure they can actually be used (based on web mode)
        if (manifest.hasWeb() && manifest.getWeb().hasFilters()) {
            final AppWebMode mode = manifest.getWeb().getMode();
            if (!mode.supportsFilters()) throw new IllegalArgumentException("Web mode does not support filters: "+mode);
        }
    }

    private void copy(BundlerOptions options, AppManifest manifest, String assetType) throws IOException {

        final String name = manifest.getName();
        final String baseDir = abs(options.getManifest().getParentFile()) + "/";
        final File outputDir = options.getOutputDir();
        final String outputBase = abs(outputDir) + "/";

        final File localDir = new File(baseDir, assetType);
        final File chefDir = FileUtil.mkdirOrDie(outputFile(outputBase, getPath(assetType), name));

        if (localDir.exists()) {
            final File[] files = FileUtil.list(localDir);
            for (File f : files) {
                final CommandResult rsync = CommandShell.exec("rsync -avc " + abs(f) + " " + abs(chefDir) + "/");
                if (rsync.getExitStatus() != 0) {
                    die("Error copying " + assetType + "s (" + abs(f) + "): " + rsync.getStderr());
                }
            }
        }
    }

    private String getPath(String assetType) {
        switch (assetType) {
            case "files": return CHEF_FILES;
            case "recipes": return CHEF_RECIPES;
            case "libraries": return CHEF_LIBRARIES;
            case "templates": return CHEF_TEMPLATES;
            case "data_bags": return CHEF_DATABAGS;
        }
        throw new IllegalArgumentException("getPath: unknown assetType: "+assetType);
    }

    private String basename(String prefix) { return prefix == null ? null : new File(prefix + ".erb").getName(); }

    protected void copyToTemplates(String outputBase, String name, String baseDir, String dirFile) throws IOException {
        if (dirFile != null) {
            final File outputFile = outputFile(outputBase, CHEF_TEMPLATES, name, dirFile);
            FileUtils.copyFile(new File(baseDir + "templates/" + dirFile), outputFile);
        }
    }

    protected File outputFile(String base, String path, String appName) {
        return outputFile(base, path, appName, "");
    }

    protected File outputFile(String base, String path, String appName, String file) {
        return new File(base + path.replace(APP, appName) + file);
    }

}
