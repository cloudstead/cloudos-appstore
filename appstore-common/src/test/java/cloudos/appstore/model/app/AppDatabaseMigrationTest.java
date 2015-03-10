package cloudos.appstore.model.app;

import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.wizard.util.SpringUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AppDatabaseMigrationTest {

    private File migrationsDir;

    @Before public void setUp () throws Exception {
        migrationsDir = FileUtil.createTempDir(getClass().getName());
        SpringUtil.copyResources("/migrations/*.sql", migrationsDir);
    }

    @After public void teardown () throws Exception { FileUtils.deleteQuietly(migrationsDir); }

    public static MigrationTest[] TESTS = {
            new MigrationTest(1, 2, new int[][] { {1, 2} }),
            new MigrationTest(1, 3, new int[][] { {1, 2}, {2, 3} }),
            new MigrationTest(1, 5, new int[][] { {1, 4}, {4, 5} }),
            new MigrationTest(2, 5, new int[][] { {2, 3}, {3, 4}, {4, 5} }),
            new MigrationTest(2, -1, null),
    };

    @Test public void testMigrationPathResolution () throws Exception {
        final List<AppDatabaseMigration> migrations = AppDatabaseMigration.fromDir(migrationsDir);

        for (MigrationTest test : TESTS) {
            final AppDatabaseMigration target = new AppDatabaseMigration(test.from(), test.to());
            final List<AppDatabaseMigration> path = AppDatabaseMigration.findShortestPath(target, migrations);
            test.validate(path);
        }
    }

    @AllArgsConstructor
    private static class MigrationTest {
        private int from, to, path[][];
        public String from() { return String.valueOf(from); }
        public String to() { return String.valueOf(to); }

        public void validate(List<AppDatabaseMigration> path) {
            if (this.path == null) {
                assertNull(path);
                return;
            }
            assertEquals(this.path.length, path.size());
            for (int i=0; i<this.path.length; i++) {
                final AppDatabaseMigration step = path.get(i);
                assertEquals(String.valueOf(this.path[i][0]), step.getFrom());
                assertEquals(String.valueOf(this.path[i][1]), step.getTo());
                assertNotNull(step.getFile());
            }
        }
    }
}
