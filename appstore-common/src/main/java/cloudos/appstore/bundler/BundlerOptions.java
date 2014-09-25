package cloudos.appstore.bundler;

import lombok.Getter;
import lombok.Setter;
import org.kohsuke.args4j.Option;

import java.io.File;

public class BundlerOptions {

    public static final File pwd = new File(System.getProperty("user.dir"));

    public static final String USAGE_MANIFEST = "The manifest file for the app. Required.";
    public static final String OPT_MANIFEST = "-m";
    public static final String LONGOPT_MANIFEST = "--manifest";
    @Option(name=OPT_MANIFEST, aliases=LONGOPT_MANIFEST, usage=USAGE_MANIFEST, required=true)
    @Getter @Setter private File manifest;

    public static final String USAGE_OUTPUT_DIR = "The output directory to build the bundle and leave the zipfile. Default is the current directory";
    public static final String OPT_OUTPUT_DIR = "-o";
    public static final String LONGOPT_OUTPUT_DIR = "--output";
    @Option(name=OPT_OUTPUT_DIR, aliases=LONGOPT_OUTPUT_DIR, usage=USAGE_OUTPUT_DIR)
    @Getter @Setter private File outputDir = pwd;

}
