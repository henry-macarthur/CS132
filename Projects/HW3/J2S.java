import cs132.minijava.*;
import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.*;

public class J2S {
    public static void main(String [] args) {
        try {   
            Goal root = new MiniJavaParser(System.in).Goal();
            Visitor v = new Visitor();

            ClassVisitor cv = new ClassVisitor();
            Context c = new Context();

            root.accept(cv, c);
            c.load_parent_methods();
            //System.out.println(c.class_to_methods);

            System.out.println(root.accept(v, c));
        } catch (Exception e) {
            System.out.println("Error!");
        }
    }
}