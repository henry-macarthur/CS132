import typechecker.TypecheckVisitor;
import typechecker.SymbolVisitor;
import typechecker.SymbolTable;
import typechecker.TypeVisitor;
import cs132.minijava.*;
import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.*;


public class Typecheck { 
    public static void main(String [] args) {
        try { 
            Goal root = new MiniJavaParser(System.in).Goal(); 
            SymbolVisitor sv = new SymbolVisitor();
            SymbolTable t = new SymbolTable();
            TypeVisitor tv = new TypeVisitor();

            root.accept(tv, t);

            //System.out.println(root.accept(sv, t));
            if(!root.accept(sv, t)) {
                System.out.println("Type error");
                return;
            }
            TypecheckVisitor visitor = new TypecheckVisitor();
            if(root.accept(visitor, t).equals("fail")) {
                System.out.println("Type error");
                return;
            } else {
                System.out.println("Program type checked successfully");
            }
        } catch (Exception e) {
            System.out.println("Type error");
        }
    }
}