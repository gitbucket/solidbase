package io.github.gitbucket.solidbase.migration;

import io.github.gitbucket.solidbase.Solidbase;
import static io.github.gitbucket.solidbase.migration.MigrationUtils.*;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class AntMigration implements Migration {

    private String path;

    public AntMigration(){
        this(null);
    }

    public AntMigration(String path){
        this.path = path;
    }

    @Override
    public void migrate(String moduleId, String version, Map<String, Object> context) throws Exception {
        String path = this.path;
        if(path == null){
            path = moduleId + "_" + version + ".xml";
        }

        Path tempFilePath = Files.createTempFile("solidbase_ant-", ".xml");

        try {
            ClassLoader classLoader = (ClassLoader) context.get(Solidbase.CLASSLOADER);
            String source = MigrationUtils.readResourceAsString(classLoader, path);
            Files.write(tempFilePath, source.getBytes("UTF-8"));

            Project project = new Project();
            project.setProperty("ant.file", path);
            project.init();
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            project.addReference("ant.projectHelper", helper);
            helper.parse(project, tempFilePath.toFile());
            System.out.println(project.getDefaultTarget());
            project.executeTarget(project.getDefaultTarget());

        } finally {
            ignoreException(() -> Files.delete(tempFilePath));
        }
    }

}
