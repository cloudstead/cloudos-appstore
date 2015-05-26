package cloudos.appstore.bundler;

import cloudos.appstore.model.app.*;
import cloudos.appstore.model.app.config.AppConfigMetadata;
import cloudos.appstore.model.app.config.AppConfigTranslationsDatabag;
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
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.mkdirOrDie;
import static org.cobbzilla.util.io.StreamUtil.loadResourceAsString;
import static org.cobbzilla.util.string.StringUtil.replaceLast;
import static org.cobbzilla.util.system.CommandShell.execScript;

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
    public static final String CHEF_ATTRIBUTES = COOKBOOK + "attributes/";

    @Override
    public void bundle(BundlerOptions options, AppManifest manifest) throws Exception {

        validate(options, manifest);

        final File outputDir = mkdirOrDie(options.getOutputDir());
        final String outputBase = abs(outputDir) + "/";
        final String name = manifest.getName();
        final AppStyle style = manifest.getStyle();
        final String styleName = style.name().toLowerCase();
        final String baseDir = options.getAppSourceDir();
        final File configMetadataFile = new File(baseDir + "config/" + AppConfigMetadata.CONFIG_METADATA_JSON);

        // If we have a plugin, build it and move it into the right place
        final File pluginJar = buildPlugin(options, manifest);
        if (pluginJar != null) {
            final String filesPath = "/chef/cookbooks/" + name + "/files/default";
            final File filesDir = new File(abs(outputDir) + filesPath);
            mkdirOrDie(filesDir);
            FileUtils.copyFile(pluginJar, new File(abs(outputDir) + filesPath + "/" + AppManifest.PLUGIN_JAR));
        }

        final Map<String, Object> scope = new HashMap<>();
        scope.put("app", manifest);
        if (configMetadataFile.exists()) {
            scope.put("config_metadata", AppConfigMetadata.load(configMetadataFile));
        }

        final List<String> templates = new ArrayList<>();
        templates.add(CHEF_METADATA);
        templates.add(CHEF_README);

        if (style != AppStyle.chef) {
            templates.add(CHEF_RECIPES + "default.rb");
            templates.add(CHEF_LIBRARIES + "common_lib.rb");
            templates.add(CHEF_LIBRARIES + styleName + "_lib.rb");
            templates.add(CHEF_RECIPES + "lib.rb");
            templates.add(CHEF_RECIPES + "backup.rb");
            templates.add(CHEF_RECIPES + "restore.rb");
            templates.add(CHEF_RECIPES + "uninstall.rb");

            if (manifest.hasRepo()) templates.add(CHEF_LIBRARIES + "install_git_lib.rb");
            if (manifest.hasTarball()) templates.add(CHEF_LIBRARIES + "install_tarball_lib.rb");

            // If there was no validate.rb recipe, but there is a validate.sh script in the files dir, create a validate.rb recipe
            if (!new File(baseDir + "recipes/validate.rb").exists()) {
                templates.add(CHEF_RECIPES + "validate.rb");
                if (new File(baseDir + "files/validate.sh").exists()) {
                    manifest.getValidation().addPreScript("@files/validate.sh");
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
                templates.add(CHEF_LIBRARIES + "database_" + styleName + "_lib.rb");
                final AppDatabase database = manifest.getDatabase();
                if (database.getAuto_migration() == null) {
                    database.setAuto_migration(manifest.getStyle().getAutoMigration());
                }
                final Boolean autoMigration = database.getAuto_migration();
                if (!database.hasSchemaVersion() && autoMigration != null && autoMigration) {
                    die("Auto-migration is enabled but no schema_version was specified");
                }
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
                if (webType == null)
                    die("web.type not defined. use one of: " + Arrays.asList(AppWebType.values()));
                templates.add(CHEF_LIBRARIES + webType + "_lib.rb");
                templates.add(CHEF_LIBRARIES + webType + "_" + styleName + "_lib.rb");

                if (webType == AppWebType.apache) {

                    final File vhostFile = new File(baseDir + "templates/apache_vhost.conf.erb");
                    if (vhostFile.exists()) {
                        copyToTemplates(outputBase, name, baseDir, vhostFile.getName());
                    }

                    final AppWebApache apache = manifest.getWeb().getApache();
                    if (apache != null) {
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
                        if (apache.hasMixins()) {
                            for (String mixin : apache.getMixins()) {
                                copyToTemplates(outputBase, name, baseDir, mixin + ".erb");
                            }
                        }
                    }
                }
            }
        }

        // Copy all files in files/ to app/files/default/
        // This will pickup and appname_schema.sql (a here-schema), and anything else the app might need at install-time
        copy(options, manifest, "files");

        // copy all templates in templates/ to app/templates/default
        copy(options, manifest, "templates");

        // Copy extra recipes, attributes and data_bags
        copy(options, manifest, "recipes");
        copy(options, manifest, "attributes");
        copy(options, manifest, "config");

        final String libName = name + "_lib.rb";
        final File libraryFile = new File(baseDir + "libraries/" + libName);
        if (libraryFile.exists()) {
            FileUtils.copyFile(libraryFile, outputFile(outputBase, CHEF_LIBRARIES, name, libName));
        }

        final Handlebars handlebars = getHandlebars();
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
        mkdirOrDie(manifestCopy.getParentFile());
        FileUtils.copyFile(manifestFile, manifestCopy);
    }

    private File buildPlugin(BundlerOptions options, AppManifest manifest) {

        final String baseDir = options.getManifest().getParent();
        final File srcDir = new File(baseDir, "src");
        final File pomFile = new File(baseDir, "pom.xml");
        boolean hasPlugin = srcDir.exists() && srcDir.isDirectory() && pomFile.exists() && pomFile.isFile();

        if (!hasPlugin) return null;

        // do not build waste time building plugin if no source file is newer than the jar
        File pluginJar = findPluginJar(baseDir);
        if (pluginJar != null && pluginJar.exists()) {
            try {
                if (pluginJar.lastModified() > CommandShell.mostRecentFileMod(srcDir)) {
                    log.info("buildPlugin: Plugin jar is newer than most recent source modification, not rebuilding");
                    return pluginJar;
                }
            } catch (Exception e) {
                log.warn("buildPlugin: Error comparing src dir mod times against plugin mod time (assuming we need to build the plugin): "+e);
            }
        }

        // Build the plugin (remove debug options from env, if present)
        final Map<String, String> env = scrubMavenOpts();
        final String mvnOutput = execScript("cd " + abs(baseDir) + " && mvn clean package", env);

        // Find the jar that was built, there should be only one
        pluginJar = findPluginJar(baseDir);
        if (pluginJar == null) die("buildPlugin: Error building plugin jar: "+mvnOutput);

        return pluginJar;
    }

    private File findPluginJar(String baseDir) {
        File jarFile = null;
        for (File artifact : FileUtil.listFiles(new File(baseDir, "target"))) {
            if (artifact.getName().endsWith(".jar")) {
                if (jarFile != null) die("findPluginJar: Multiple jar files produced by plugin build: "+abs(jarFile)+", "+abs(artifact));
                jarFile = artifact;
            }
        }
        return jarFile;
    }

    private Map<String, String> scrubMavenOpts() {
        final Map<String, String> env = new HashMap<>(System.getenv());
        final String mavenOpts = env.get("MAVEN_OPTS");
        if (mavenOpts == null) return env;
        final StringBuilder b = new StringBuilder();
        for (String part : mavenOpts.split("\\s+")) {
            if (!part.startsWith("-Xdebug") && !part.startsWith("-Djava.compiler") && !part.startsWith("-Xrunjdwp")) {
                b.append(part).append(" ");
            }
        }
        if (b.length() > 0) {
            env.put("MAVEN_OPTS", b.toString());
        } else {
            env.remove("MAVEN_OPTS");
        }
        return env;
    }

    public Handlebars getHandlebars() {
        final TemplateLoader loader = new ClassPathTemplateLoader("/bundler/");
        final Handlebars handlebars = new Handlebars(loader);

        handlebars.registerHelper("safe", new Helper<Object>() {
            public CharSequence apply(Object src, Options options) {
                return empty(src) ? "" : new Handlebars.SafeString(src.toString().replace("'", "\\'").replace("\"", "\\\""));
            }
        });
        handlebars.registerHelper("quoted_or_nil", new Helper<Object>() {
            public CharSequence apply(Object src, Options options) {
                return empty(src) ? "nil" : new Handlebars.SafeString("'"+src.toString().replace("'", "\\'")+"'");
            }
        });
        handlebars.registerHelper("dots_to_brackets", new Helper<Object>() {
            public CharSequence apply(Object src, Options options) {
                return empty(src) ? "" : new Handlebars.SafeString("['"+src.toString().replace(".", "']['")+"']");
            }
        });
        handlebars.registerHelper("login_field_for_password", new Helper<Object>() {
            public CharSequence apply(Object src, Options options) {
                if (empty(src)) return "";
                return new Handlebars.SafeString("['"+ replaceLast(src.toString(), "password", "login").replace(".", "']['")+"']");
            }
        });
        handlebars.registerHelper("hash_or_nil", new Helper<Object>() {
            public CharSequence apply(Object src, Options options) {
                if (empty(src)) return "nil";
                if (!(src instanceof Map)) die("hash_or_nil: not a Map (was a "+src.getClass().getName()+"): "+src);
                StringBuilder b = new StringBuilder();
                final Map map = (Map) src;
                for (Object key : map.keySet()) {
                    if (b.length() > 0) b.append(", ");
                    b.append("'").append(key.toString()).append("' => '").append(map.get(key).toString()).append("'");
                }
                b.insert(0, "{ ");
                b.append(" }");
                return new Handlebars.SafeString(b.toString());
            }
        });
        handlebars.registerHelper("ident", new Helper<Object>() {
            public CharSequence apply(Object src, Options options) {
                if (empty(src)) return "";
                final StringBuilder sb = new StringBuilder();
                for (char c : src.toString().toCharArray()) {
                    sb.append(Character.isLetterOrDigit(c) ? c : '_');
                }
                return new Handlebars.SafeString(sb.toString());
            }
        });
        return handlebars;
    }

    private void validate(BundlerOptions options, AppManifest manifest) {

        // must define style
        if (manifest.getStyle() == null) die("style not defined. use one of: "+Arrays.asList(AppStyle.values()));

        // If filters are declared, make sure they can actually be used (based on web mode)
        if (manifest.hasWeb() && manifest.getWeb().hasFilters()) {
            final AppWebMode mode = manifest.getWeb().getMode();
            if (!mode.supportsFilters()) die("Web mode does not support filters: " + mode);
        }

        // If config-metadata.json is defined, ensure it is parseable
        final File configDir = new File(abs(options.getManifest().getParent())+"/config");
        if (configDir.exists()) {
            final File configMetaFile = new File(configDir, AppConfigMetadata.CONFIG_METADATA_JSON);
            if (configMetaFile.exists()) {
                try {
                    final AppConfigMetadata metadata = AppConfigMetadata.loadOrDie(configMetaFile);
                    if (metadata.hasPasswords()) {

                        final File outputDir = mkdirOrDie(options.getOutputDir());
                        final String outputBase = abs(outputDir) + "/";

                        final File autogenPass = outputFile(outputBase, CHEF_FILES, manifest.getName(), "autogen_pass.sh");
                        FileUtil.toFile(autogenPass,
                                loadResourceAsString("bundler/" + CHEF_FILES + "autogen_pass.sh"));
                        CommandShell.chmod(autogenPass, "u+rx");
                    }
                    if (metadata.hasLocaleFields()) {
                        final File outputDir = mkdirOrDie(options.getOutputDir());
                        final String outputBase = abs(outputDir) + "/";

                        final File defaultLocaleNames = outputFile(outputBase, CHEF_DATABAGS, manifest.getName(), "default-locale-names.json");
                        FileUtil.toFile(defaultLocaleNames,
                                loadResourceAsString("bundler/" + CHEF_DATABAGS + "default-locale-names.json"));
                    }

                } catch (Exception e) {
                    die("Invalid " + AppConfigMetadata.CONFIG_METADATA_JSON + " file ("+abs(configMetaFile)+"): " + e, e);
                }
            }

            // If translations are defined, ensure they are parseable
            for (File f : FileUtil.list(configDir)) {
                if (f.getName().startsWith("translations") && f.getName().endsWith(".json")) {
                    try { AppConfigTranslationsDatabag.loadOrDie(f); } catch (Exception e) {
                        die("Invalid translations file " + abs(f) + ": " + e, e);
                    }
                }
            }
        }
    }

    private void copy(BundlerOptions options, AppManifest manifest, String assetType) throws IOException {

        final String name = manifest.getName();
        final String baseDir = abs(options.getManifest().getParentFile()) + "/";
        final File outputDir = options.getOutputDir();
        final String outputBase = abs(outputDir) + "/";

        final File localDir = new File(baseDir, assetType);
        final File chefDir = FileUtil.mkdirOrDie(outputFile(outputBase, getChefDirName(assetType), name));

        if (localDir.exists()) {
            final File[] files = FileUtil.list(localDir);
            for (File f : files) {
                final CommandResult rsync = CommandShell.exec("rsync -avc " + abs(f) + " " + abs(chefDir) + "/");

                if (rsync.getExitStatus() != 0) die("Error copying " + abs(f) + ": " + rsync.getStderr());
            }
        }
    }

    private String getChefDirName(String assetType) {
        switch (assetType) {
            case "files": return CHEF_FILES;
            case "recipes": return CHEF_RECIPES;
            case "libraries": return CHEF_LIBRARIES;
            case "templates": return CHEF_TEMPLATES;
            case "attributes": return CHEF_ATTRIBUTES;
            case "data_bags": case "config": return CHEF_DATABAGS;
        }
        return die("getChefDirName: unknown assetType: "+assetType);
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
