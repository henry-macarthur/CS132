package typechecker;
import cs132.minijava.*;
import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.*;
import java.util.*;

//purpose of this is legit just to add all class as identifiers 
public class TypeVisitor extends GJDepthFirst<Boolean, SymbolTable> {
    @Override 
    public Boolean visit(Goal n, SymbolTable t) {
        for(int i = 0; i < n.f1.size(); i++) {
            boolean tm = 
                n.f1.elementAt(i).accept(this, t);
            if(!tm) {
                return false;
            }
        }
        //check to see if there is a cycle
        //can do the reordering or something?
        ArrayList<String> tm = new ArrayList<String>();
        while(tm.size() < t.types.size()) {
            for(int i = 0; i < t.types.size(); i++) {
                
                if(!tm.contains(t.types.get(i)) && (!t.child_to_parent.containsKey(t.types.get(i)) || tm.contains(t.child_to_parent.get(t.types.get(i))))) {
                    tm.add(t.types.get(i));
                }
            }
        }
        t.types_order = t.types;
        t.types = tm;

      
        return true;
    }

    @Override 
    public Boolean visit(TypeDeclaration n, SymbolTable t) {
        return n.f0.accept(this, t);
    }

    @Override
    public Boolean visit(ClassDeclaration n, SymbolTable t) {
        if(t.types.contains(n.f1.f0.tokenImage))
            return false;
        t.types.add(n.f1.f0.tokenImage);
        return t.types.contains(n.f1.f0.tokenImage);
    }

    @Override 
    public Boolean visit(ClassExtendsDeclaration n, SymbolTable t) {
        String child = n.f1.f0.tokenImage;
        String parent = n.f3.f0.tokenImage;
        t.child_to_parent.put(n.f1.f0.tokenImage, n.f3.f0.tokenImage);
        String cur_key = child;
        if(t.types.contains(n.f1.f0.tokenImage))
            return false;
        if(child.equals(parent)) {
            return false;
        }
        while(t.child_to_parent.contains(cur_key)) {
            cur_key = t.child_to_parent.get(cur_key);
            if(child.equals(cur_key)) {
                return false;
            }
        }
        t.types.add(n.f1.f0.tokenImage);
        return t.types.contains(n.f1.f0.tokenImage);
    }

}