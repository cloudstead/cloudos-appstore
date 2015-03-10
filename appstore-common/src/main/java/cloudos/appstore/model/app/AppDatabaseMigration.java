package cloudos.appstore.model.app;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.io.StreamUtil;
import org.cobbzilla.util.json.JsonUtil;
import org.jgrapht.Graph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.string.StringUtil.empty;

@NoArgsConstructor @AllArgsConstructor @Accessors(chain=true) @Slf4j
public class AppDatabaseMigration {

    public static final Pattern FILENAME_PATTERN = Pattern.compile("([^_]+_+)?([^,_]+)[,_]+([^,_\\.]+)(\\.(.+))?");

    @Getter @Setter private String from;
    @Getter @Setter private String to;
    @Getter @Setter private String file;

    public AppDatabaseMigration(String from, String to) {
        this(from, to, null);
    }

    public static AppDatabaseMigration fromFile (File f) {
        final String name = f.getName();
        final Matcher matcher = FILENAME_PATTERN.matcher(name);
        if (!matcher.find()) {
            log.warn("Invalid migration file (returning null): "+f.getName());
            return null;
        }
        return new AppDatabaseMigration(matcher.group(2), matcher.group(3), abs(f));
    }

    public static List<AppDatabaseMigration> fromDir (String dir) { return fromDir(new File(dir)); }

    public static List<AppDatabaseMigration> fromDir (File dir) {
        final List<AppDatabaseMigration> migrations = new ArrayList<>();
        for (File f : FileUtil.list(dir)) {
            AppDatabaseMigration m = fromFile(f);
            if (m != null) migrations.add(m);
        }
        return migrations;
    }

    public static Graph getGraph(List<AppDatabaseMigration> available) {
        final Graph graph = new DirectedAcyclicGraph<>(AppDatabaseMigration.class);
        for (AppDatabaseMigration m : available) {
            graph.addVertex(m.from);
            graph.addVertex(m.to);
            graph.addEdge(m.from, m.to, m);
        }
        return graph;
    }

    public static List findShortestPath(AppDatabaseMigration target, Graph graph) {
        try {
            return new DijkstraShortestPath<>(graph, target.from, target.to).getPathEdgeList();
        } catch (Exception e) {
            log.error("findShortestPath: "+e, e);
            return null;
        }
    }

    public static List<AppDatabaseMigration> findShortestPath(AppDatabaseMigration target,
                                                              List<AppDatabaseMigration> available) {
        return findShortestPath(target, getGraph(available));
    }

    public static void main (String[] args) throws Exception {
        final String from = args[0];
        final String to = args[1];
        final String dir = args[2];
        final String mode = (args.length > 2) ? args[3] : null;

        final List<AppDatabaseMigration> path = findShortestPath(new AppDatabaseMigration(from, to), fromDir(dir));

        if (empty(mode) || mode.equals("json")) {
            System.out.println(JsonUtil.toJson(path));

        } else if (mode.equals("sql")) {
            for (AppDatabaseMigration m : path) {
                @Cleanup final InputStream in = new FileInputStream(m.getFile());
                StreamUtil.copyLarge(in, System.out);
            }

        } else if (mode.equals("files")) {
            for (AppDatabaseMigration m : path) {
                System.out.println(abs(m.getFile()));
            }
        }
    }

}
