package comp2010.main;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by ntrolls on 04/03/15.
 */
public class Main extends SimpleFileVisitor<Path> {

    @Option(name="-in",usage="Root directory of the input classfiles")
    private String inputRoot;

    @Option(name="-out",usage="Root directory where optimised classfiles will be stored")
    private String outputRoot;

    private void parseArguments(String args[])
    {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(80);
        try{
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java BatchExperiment inputFolder outputFolder");
            parser.printUsage(System.err);
            System.err.println();
            System.exit(-1);
        }
    }

    public static void main(String args[]) throws IOException {
        Main main = new Main();
        main.parseArguments(args);
        Files.walkFileTree(Paths.get(main.inputRoot), main);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path rel = Paths.get(inputRoot).relativize(dir);
        File outputDir = new File(Paths.get(outputRoot, rel.toString()).toString());
        outputDir.mkdirs();
        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if(file.toString().endsWith(".class")){
            ConstantFolder cf = new ConstantFolder(file.toString());
            cf.optimize();

            Path rel = Paths.get(inputRoot).relativize(file);

            cf.write(Paths.get(outputRoot, rel.toString()).toAbsolutePath().toString());
        }
        return super.visitFile(file, attrs);
    }
}
