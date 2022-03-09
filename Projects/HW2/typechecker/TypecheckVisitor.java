package typechecker; 
import cs132.minijava.*;
import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.*;
import java.util.*;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;

public class TypecheckVisitor extends GJDepthFirst<String, SymbolTable> {
    
    @Override
    public String visit(Goal n, SymbolTable t) {
        //will need to add more here
        String pass = n.f0.accept(this, t);
        String types = n.f1.accept(this, t);
        if(!pass.equals("fail") && !types.equals("fail")) {
            return "";
        }
        return "fail";
    }

    @Override 
    public String visit(MainClass n, SymbolTable t) {
        t.current_method.clear();
        t.current_class_name = "";
        t.current_method.push("main");
        String f1 = n.f14.accept(this, t);
        if(f1.equals("fail")) {
            return "fail";
        }
        String f2 = n.f15.accept(this, t);
        if(f2.equals("fail")) {
            return "fail";
        }
        t.current_method.pop();
        return "";
    }

    @Override 
    public String visit(NodeListOptional n, SymbolTable t) {
        for(int i = 0; i < n.size(); i++) {
            if(n.elementAt(i).accept(this, t).equals("fail")) {
                return "fail";
            }
        }
        return "";
    }

    @Override
    public String visit(TimesExpression n, SymbolTable t) {

        if(n.f0.accept(this, t).equals("int") && n.f2.accept(this, t).equals("int")) {
            return "int";
        } else {
            return "fail";
        }
    }
    
    @Override
    public String visit(PlusExpression n, SymbolTable t) {
        String lhs = n.f0.accept(this, t);
        String rhs = n.f2.accept(this, t);

        if(lhs.equals("int") && rhs.equals("int")) {
            return "int";
        } else {
            return "fail";
        }
    }

    @Override 
    public String visit(PrimaryExpression n, SymbolTable t) {
        return n.f0.accept(this, t);
    }

    @Override
    public String visit(CompareExpression n, SymbolTable t) {
        if(n.f0.accept(this, t).equals("int") && n.f2.accept(this, t).equals("int")) {
            return "boolean";
        } else {
            return "fail";
        }
    }
    
    @Override
    public String visit(MinusExpression n, SymbolTable t) {
        if(n.f0.accept(this, t).equals("int") && n.f2.accept(this, t).equals("int")) {
            return "int";
        } else {
            return "fail";
        }
    }
    
    @Override
    public String visit(AndExpression n, SymbolTable t) {
        if(n.f0.accept(this, t).equals("boolean") && n.f2.accept(this, t).equals("boolean")) {
            return "boolean";
        } else {
            return "fail";
        }
    } 

    @Override 
    public String visit(ArrayLookup n, SymbolTable t) {
        if(n.f0.accept(this, t).equals("array") && n.f2.accept(this, t).equals("int")) {
            return "int";
        } else {
            return "fail";
        }
    }

    @Override 
    public String visit(ArrayLength n, SymbolTable t) {
        if(n.f0.accept(this, t).equals("array")) {
            return "int";
        } else {
            return "fail";
        }
    }

    @Override
    public String visit(VarDeclaration n, SymbolTable t) {
        return "";
    }

    @Override 
    public String visit(IntegerType n, SymbolTable t) {
        return "int";
    }

    @Override 
    public String visit(BooleanType n, SymbolTable t) {
        return "boolean";
    }

    @Override 
    public String visit(ArrayType n, SymbolTable t) {
        return "array";
    }

    @Override 
    public String visit(IntegerLiteral n, SymbolTable t) {
        return "int";
    }

    @Override 
    public String visit(TrueLiteral n, SymbolTable t) {
        return "boolean";
    }

    @Override 
    public String visit(FalseLiteral n, SymbolTable t) {
        return "boolean";
    }

    @Override 
    public String visit(NotExpression n, SymbolTable t) {
        if(n.f1.accept(this, t).equals("boolean")) {
            return "boolean";
        }
        return "fail";
    }

    @Override 
    public String visit(BracketExpression n, SymbolTable t) {
        return n.f1.accept(this, t);
    }

    @Override 
    public String visit(ArrayAllocationExpression n, SymbolTable t) {
        String l = n.f3.accept(this, t);
        if(l.equals("int")) {
            return "array";
        }
        return "fail";
    }

    @Override 
    public String visit(Expression n, SymbolTable t) {
        String ret_val = n.f0.accept(this, t);
        return ret_val;
    }

    @Override 
    public String visit(AssignmentStatement n, SymbolTable t) {
        //check to make sure the identifier and expressions type match! 
        //n.f0 -> identifier 
        //n.f2 -> expression 
        String id_type = t.method_to_table.get(t.current_class_name).get(t.current_method.peek())
                            .get(n.f0.f0.tokenImage);//t.table.get(n.f0.f0.tokenImage);
        
        String expression_type = n.f2.accept(this, t);
        //make sure type(id) = type(expression)
        if(t.is_subtype(expression_type, id_type)) {//id_type.equals(expression_type)) {
            return expression_type; //not needed, can return "" but doesnt matter 
        }
        return "fail";
    }

    @Override
    public String visit(Block n, SymbolTable t) {
        for(int i = 0; i < n.f1.size(); i++) {
            String tm = n.f1.elementAt(i).accept(this, t);
            if(tm.equals("fail")) {
                return "fail";
            }
        }
        return "";
    }

    @Override 
    public String visit(Statement n, SymbolTable t) {
        String accept = n.f0.accept(this, t);
        return accept;
    }

    @Override 
    public String visit(ArrayAssignmentStatement n, SymbolTable t) {
        String t1 = t.method_to_table.get(t.current_class_name).get(t.current_method.peek())
                        .get(n.f0.f0.tokenImage);//t.table.get(n.f0.f0.tokenImage); // array 
        String t2 = n.f2.accept(this, t);   // int 
        String t3 = n.f5.accept(this, t);   // int 

        if(t1.equals("array") && t2.equals("int") && t3.equals("int")) {
            return "";
        } else {
            return "fail";
        }

    }

    @Override 
    public String visit(Identifier n, SymbolTable t) {
        //will most likely have to change this, as i think i should just return the tokenImage but
        //can do this later
        //return n.f0.tokenImage;
        String t1 = n.f0.tokenImage;
        if(t.current_state.equals("method_type")) {
            t1 = n.f0.tokenImage;
        } else {
            t1 = t.method_to_table.get(t.current_class_name).get(t.current_method.peek())
                        .get(n.f0.tokenImage);//t.table.get(n.f0.tokenImage);
        }
        // String t1 = t.method_to_table.get(t.current_class_name).get(t.current_method.peek())
        //                 .get(n.f0.tokenImage);//t.table.get(n.f0.tokenImage);
        if(t1 == null) {
            String tm = t.current_class_name;
            while(t.child_to_parent.containsKey(tm)) {
                tm = t.child_to_parent.get(tm);
                if(t.class_to_id.get(tm).containsKey(n.f0.tokenImage)) {
                    t1 = t.class_to_id.get(tm).get(n.f0.tokenImage);
                    break;
                }
            }
        }
        return t1;
    }


    @Override 
    public String visit(IfStatement n, SymbolTable t) {
        // f2 -> bool 
        // f4, f6 -> type checks 
        String t1 = n.f2.accept(this, t);
        String t2 = n.f4.accept(this, t);
        String t3 = n.f6.accept(this, t);
        if(t1.equals("boolean") && !t2.equals("fail") && !t3.equals("fail")) { 
            return "";
        } else {
            return "fail";
        }
    }

    @Override
    public String visit(WhileStatement n, SymbolTable t) {
        String t1 = n.f2.accept(this, t);
        String t2 = n.f4.accept(this, t);

        if(t1.equals("boolean") && !t2.equals("fail")) {
            return "";
        } else {
            return "fail";
        }
    }

    @Override 
    public String visit(PrintStatement n, SymbolTable t) {
        String t1 = n.f2.accept(this, t);

        if(t1.equals("int")) {
            return "";
        } else {
            return "fail";
        }
    }

    @Override 
    public String visit(Type n, SymbolTable t) { 
        return n.f0.accept(this, t);
    }

    @Override 
    public String visit(MethodDeclaration n, SymbolTable t) {
        /*
        f1 : type 
        f2 : id
        f4 : formal parameter list 
        f7 : var dec 
        f8 : statement 
        f10 : expression 

        need f10 and f1 to evaluate to the same type
        */
        t.current_state = "method_type";
        String id = n.f2.f0.tokenImage;
        t.current_method.push(id);
        String type = n.f1.accept(this, t);
        t.current_state = "";
        // String type = t.num_to_type.get(n.f1.f0.which);
        //System.out.println("$$$$$$$");
        //System.out.println(n.f1.accept(this, t));
        //get the id next!
        String var_dec = n.f7.accept(this, t);
        String statement = n.f8.accept(this, t);
        String return_expression = n.f10.accept(this, t);
        boolean type_checks = 
            type.equals(return_expression) &&
            !var_dec.equals("fail") && 
            !statement.equals("fail"); 
        t.current_method.pop();
        if(type_checks) {
            return "";
        } else {
            return "fail";
        }
    }

    @Override
    public String visit(AllocationExpression n, SymbolTable t) {
        //just need to make sure the id exists, lets do that later!
        //if(t.class_method_param.containsKey(n.f1.f0.tokenImage))
        if(!t.class_method_type.containsKey(n.f1.f0.tokenImage)) {
            return "fail";
        }
        return n.f1.f0.tokenImage;
    } 

    @Override 
    public String visit(MessageSend n, SymbolTable t) {
        /*
        f0 ->gives us the class name?
        f2 ->gives is the method name 
        f4 ->gives the input parameter list 
        */  
        //will add the subtyping later
        String id_name = n.f0.accept(this, t);
        String method_name = n.f2.f0.tokenImage;
        //might need to check if it exists
        String method_type = t.class_method_type.get(id_name).get(method_name);
        //need to map (class, method) -> type
        //might need to make sure Integer() etc are allowed here 
        //need to make sure they exist, the accept should return the 
        if(!t.class_method_param.containsKey(id_name) || !t.class_method_param.get(id_name).containsKey(method_name)) {
            //class or method dont exist so 
            return "fail";
        }

        t.current_inputs.clear();
        String tmp = "";
        if(n.f4.present()) {
            tmp = n.f4.accept(this, t);
            
            if(tmp.equals("fail") || (t.current_inputs.size() != t.class_method_param.get(id_name).get(method_name).size())) {
                return "fail";
            }
            
            for(int i = 0; i < t.current_inputs.size(); i++) {
                //add subtype later
                if(!t.is_subtype(t.current_inputs.get(i), t.class_method_param.get(id_name).get(method_name).get(i))) {//t.current_inputs.get(i).equals(t.class_method_param.get(id_name).get(method_name).get(i))) {
                    return "fail";
                }
            }
        } else {
            if(t.current_inputs.size() != t.class_method_param.get(id_name).get(method_name).size()) {
                return "fail";
            }
        }
        return method_type;
    }

    @Override 
    public String visit(ExpressionList n, SymbolTable t) {
        String cur = n.f0.accept(this, t);
        if(cur.equals("fail")) {
            return "fail";
        }
        t.current_inputs.add(cur);

        for(int i = 0; i < n.f1.size(); i++) {
            cur = n.f1.elementAt(i).accept(this, t);
            if(cur.equals("fail")) {
                return "fail";
            }
            t.current_inputs.add(cur);
        }
        return "";
    }

    @Override 
    public String visit(ExpressionRest n, SymbolTable t) {
        return n.f1.accept(this, t);
    }

    @Override 
    public String visit(ClassDeclaration n, SymbolTable t) {
        //have to handle id later
        t.current_class_name = n.f1.f0.tokenImage;
        String var = n.f3.accept(this, t);
        String methods = n.f4.accept(this, t);
        if(!var.equals("fail") && !methods.equals("fail")) {
            return "";
        }
        else {
            return "fail"; 
        } 
    }

    @Override 
    public String visit(ClassExtendsDeclaration n, SymbolTable t) {
        t.current_class_name = n.f1.f0.tokenImage;
        String var = n.f5.accept(this, t);
        String methods = n.f6.accept(this, t);

        if(!var.equals("fail") && !methods.equals("fail")) {
            return "";
        }
        else {
            return "fail"; 
        } 
        
    }

    @Override 
    public String visit(TypeDeclaration n, SymbolTable t) {
        return n.f0.accept(this, t);
    }

    @Override 
    public String visit(ThisExpression n, SymbolTable t) {
        //want to return whatever class we are in, is this allowed in the main class? dont 
        //think it is as main class will have no methods 
        //just return the current class name I guess then
        return t.current_class_name;
    }
}
