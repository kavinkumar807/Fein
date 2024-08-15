package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Class to create java command line app that generates syntax tree classes
 */
public class GenerateAst {
    public static void main(String[] args) throws IOException{
        if(args.length != 1){
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token operator, Expr right"
        ));
    }

    /**
     * Method to define ast from string
     *
     * @param outputDir String
     * @param baseName String
     * @param types List<String>
     */
    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        // The AST classes
        for(String type: types){
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
    }

    /**
     * Method to define fields subclass and constructor in ast classes
     *
     * @param writer PrintWriter
     * @param baseName String
     * @param className String
     * @param fieldList String
     */
    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println(" static class " + className + " extends " + baseName + " {");

        //constructor
        writer.println("    " + className + "(" + fieldList + ") {");

        //Store parameters in fields
        String[] fields = fieldList.split(", ");
        for(String field : fields){
            String name = field.split(" ")[1];
            writer.println("        this." + name + " = " + name + ";");
        }

        writer.println("    }");

        // Fields
        writer.println();
        for(String field : fields){
            writer.println("    final " + field + ";");
        }
        writer.println("    }");

    }
}
