import cs132.minijava.*;
import java.util.*;
import cs132.minijava.visitor.*;
import cs132.minijava.syntaxtree.*;

class ClassVisitor extends GJDepthFirst <String, Context> {
    enum State {
        CLASS, 
        METHOD, 
        OTHER,
        MAIN
    }
    String current_class; 
    String type = "";
    State current_state = State.OTHER;   
    String current_method;


    @Override
    public String visit(ClassDeclaration n, Context c) {
        current_state = State.CLASS;
        current_class = n.f1.f0.tokenImage;
        String result = "";

        //initialize the mapping from each class to their method / parameters
        c.class_to_methods.put(current_class, new ArrayList<String>());
        c.class_to_parameters.put(current_class, new ArrayList<String>());
        c.class_param_type.put(current_class, new ArrayList<String>());
        c.class_method_parameter.put(current_class, new Hashtable<String, String>());


        for(int i = 0; i < n.f3.size(); i++) {
            n.f3.elementAt(i).accept(this, c);
        }
        for(int i = 0; i < n.f4.size(); i++) {
            n.f4.elementAt(i).accept(this, c);
        }
        return result;
    }

    @Override 
    public String visit(VarDeclaration n, Context c) {
        if(current_state == State.CLASS) {
            c.class_to_parameters.get(current_class).add(current_class + "-" + n.f1.f0.tokenImage);
            //type
            n.f0.accept(this, c);
            c.class_param_type.get(current_class).add(type);
        } else if (current_state == State.METHOD) {
            n.f0.accept(this, c); //will set the type variable for us 
            c.class_method_parameter.get(current_class).put(current_method + "-" +n.f1.f0.tokenImage, type);
        } else if(current_state == State.MAIN) {

        }
        // System.out.println(current_class);
        // System.out.println(n.f1.f0.tokenImage);
        //c.class_to_parameters.get(current_class).add(n.f1.f0.tokenImage);
        return "";
    }

    @Override 
    public String visit(ArrayType n, Context c) {
        type = "default";
        return "";
    }

    @Override 
    public String visit(BooleanType n, Context c) {
        type = "default";
        return "";
    }

    @Override 
    public String visit(IntegerType n, Context c) {
        type = "default";
        return "";
    }

    @Override 
    public String visit(Identifier n, Context c) {
        type = n.f0.tokenImage;
        return "";
    }

    @Override 
    public String visit(Type n, Context c) {
        return n.f0.accept(this, c);
    }

    @Override
    public String visit(FormalParameter n, Context c) {
        String parameter = n.f1.f0.tokenImage;
        n.f0.accept(this, c);
        c.class_method_parameter.get(current_class).put(current_method + "-" + parameter, type);
        return "";
    }

    @Override
    public String visit(FormalParameterRest n, Context c) {
        return n.f1.accept(this, c);
    }

    @Override 
    public String visit(FormalParameterList n, Context c) {
        n.f0.accept(this, c);
        if(n.f1.present()) {
            for(int i = 0; i < n.f1.size(); i++) {
                n.f1.elementAt(i).accept(this, c);
            }
        }
        return "";
    }

    @Override
    public String visit(MethodDeclaration n, Context c) {
        //f2 -> identifier 
        //f4 -> parameter list 
        //f7 -> variable declaration 
        //f8 -> statements 
        // /current_state = state
        //f10 -> return expression -> return id that the return expression is evaluated to 
        
        String method_name = n.f2.f0.tokenImage;
        current_method = method_name;
        current_state = State.METHOD;
        if(n.f4.present())
            n.f4.accept(this, c);

        for(int i = 0; i < n.f7.size(); i++) {
            n.f7.elementAt(i).accept(this, c);
        }
        
        String final_name = current_class + "_" + method_name;
        c.class_to_methods.get(current_class).add(final_name);
        String types = n.f1.accept(this, c);
        c.method_to_type.put(final_name, type);
        //System.out.println(c.method_to_type);

        return "";
    }

    @Override 
    public String visit(MainClass n, Context c) {
        current_state = State.METHOD;
        current_class = "Main";
        current_method = "Main";
        c.class_to_methods.put(current_class, new ArrayList<String>());
        c.class_to_methods.get(current_class).add(current_method);
        c.class_to_parameters.put(current_class, new ArrayList<String>());
        c.class_param_type.put(current_class, new ArrayList<String>());
        c.class_method_parameter.put(current_class, new Hashtable<String, String>());
        
        for(int i = 0; i < n.f14.size(); i++) {
            n.f14.elementAt(i).accept(this, c);
        }
        return "";
    }

    @Override 
    public String visit(ClassExtendsDeclaration n, Context c) {

        current_state = State.CLASS;
        current_class = n.f1.f0.tokenImage;
        String parent_class = n.f3.f0.tokenImage;

        c.class_to_parent.put(current_class, parent_class);

        //initialize the mapping from each class to their method / parameters
        c.class_to_methods.put(current_class, new ArrayList<String>());
        c.class_to_parameters.put(current_class, new ArrayList<String>());
        c.class_param_type.put(current_class, new ArrayList<String>());
        c.class_method_parameter.put(current_class, new Hashtable<String, String>());

        for(int i = 0; i < n.f5.size(); i++) {
            n.f5.elementAt(i).accept(this, c);
        }
        for(int i = 0; i < n.f6.size(); i++) {
            n.f6.elementAt(i).accept(this, c);
        }

        return "";
    }

}