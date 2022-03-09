package typechecker;
import cs132.minijava.*;
import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.*;
import java.util.*;

public class SymbolVisitor extends GJDepthFirst<Boolean, SymbolTable> {


    @Override
    public Boolean visit(Goal n, SymbolTable t) {

        boolean main = n.f0.accept(this, t);
        if(!main) {
            return false;
        }
        int items = t.types.size() - 3;
        for(int i = 3; i < t.types.size(); i++) {
            int index = t.types_order.indexOf(t.types.get(i)) - 3;
            //System.out.println(index);
            boolean tm = 
                n.f1.elementAt(index).accept(this, t);
            if(!tm) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean visit(ArrayType n, SymbolTable t) {
        t.id_type = "array";
        return true;
    }

    @Override
    public Boolean visit(BooleanType n, SymbolTable t) {
        t.id_type = "boolean";
        return true;
    }

    @Override
    public Boolean visit(IntegerType n, SymbolTable t) {
        t.id_type = "int";
        return true;
    }

    @Override
    public Boolean visit(Identifier n, SymbolTable t) {
        t.id_type = n.f0.tokenImage;
        return t.types.contains(n.f0.tokenImage);
    }
    @Override 
    public Boolean visit(MainClass n, SymbolTable t) {
        //fill this in later, i think we just need to check to make sure the name isnt 
        //already being used tbh
        String id = "main";
        t.current_method.push(id);
        Hashtable<String, Hashtable<String, String>> outer = 
            new Hashtable<String, Hashtable<String, String>>();
        Hashtable<String, String> tmp = new Hashtable<String, String>();
        outer.put(id, tmp);
        //t.method_to_table.get(t.current_class_name)
        t.method_to_table.put(t.current_class_name, outer); //ok!

        for(int i = 0; i < n.f14.size(); i++)
        {
            Boolean check_var = n.f14.elementAt(i).accept(this, t);
            if(check_var == false) {
                t.current_method.pop();
                return false;
            }
        }
        t.current_method.pop();
        return true;
    }

    @Override 
    public Boolean visit(TypeDeclaration n, SymbolTable t) {
        Boolean ret = n.f0.accept(this, t);
        return ret;
    }

    //need to check to make sure u cant have dups
    @Override 
    public Boolean visit(ClassDeclaration n, SymbolTable t) {
        // can deal with the whole class thing later, lets just focus on the variables for now!
        // each class however should have its own scoping 
        boolean lhs = true;
        //need to check the class specific local variables
        t.class_parameters.clear();
        t.current_class_name = n.f1.f0.tokenImage; //store the current class name
        t.current_state = "class";
        
        for(int i = 0; i < n.f3.size(); i++) {
            boolean tmp = n.f3.elementAt(i).accept(this, t);
            if(!tmp) {
                t.class_parameters.clear();
                return false;
            }
        }
        t.class_to_id.put(t.current_class_name, t.class_parameters); //might have to do some stuff with inheriting parent data?
        Hashtable<String, ArrayList<String>> method_to_params
                = new Hashtable<String, ArrayList<String>>();
        Hashtable<String, String> method_to_type
                = new Hashtable<String, String>();
        t.class_method_param.put(t.current_class_name, method_to_params);
        t.class_method_type.put(t.current_class_name, method_to_type);
        //t.current_class_name = ""; //might need a better way to say that we are checking class variables
        
        for(int i = 0; i < n.f4.size(); i++) {
            boolean tmp = n.f4.elementAt(i).accept(this, t);
            if(!tmp) {
                t.class_parameters.clear();
                return false;
            }
        }
        Hashtable<String, String> tmp_table = new Hashtable<String, String>();
        tmp_table.putAll(t.class_parameters);
        t.class_to_id.put(t.current_class_name, tmp_table);
        t.class_parameters.clear();
        return true; 
    }

    @Override 
    public Boolean visit(FormalParameterList n, SymbolTable t) {
        Boolean lhs = n.f0.accept(this, t);
        if(n.f1.present()) {
            for(int i = 0; i < n.f1.size(); i++) {
                Boolean rhs = n.f1.elementAt(i).accept(this, t);
                if(!rhs) {
                    return false;
                }
            }
        } 

        return lhs;
    }

    @Override 
    public Boolean visit (FormalParameterRest n, SymbolTable t) {
        return n.f1.accept(this, t);
    }

    @Override 
    public Boolean visit(FormalParameter n, SymbolTable t) {
        int param_type = n.f0.f0.which;
        String param_id = n.f1.f0.tokenImage;
        if(t.method_to_table.get(t.current_class_name).get(t.current_method.peek()).containsKey(n.f1.f0.tokenImage)) {
            return false;
        } else {
            n.f0.accept(this, t);
            t.method_to_table.get(t.current_class_name).get(t.current_method.peek()).put(n.f1.f0.tokenImage, t.id_type);//t.num_to_type.get(n.f0.f0.which));
            t.insert_method_to_param(t.current_class_name, t.current_method.peek(), t.id_type); //t.num_to_type.get(n.f0.f0.which));
        }

        return true;
    }

    @Override
    public Boolean visit(MethodDeclaration n, SymbolTable t) {
        
        //need to create a new hash table for each new method we see, we should 
        //dont allow duplicate methods
        String id = n.f2.f0.tokenImage;
        Boolean tp = n.f1.accept(this, t);
        String type = t.id_type;//t.num_to_type.get(n.f1.f0.which); //can we have a class return type?
        String prev_state = t.current_state;

        t.current_state = "method";
        if(t.class_method_type.get(t.current_class_name).containsKey(id) && !prev_state.equals("child_class")) { //method_to_table
            return false;
        } else {
            Hashtable<String, String> local_table
                = new Hashtable<String, String>();
            Hashtable<String, Hashtable<String, String>> outer
                = new Hashtable<String, Hashtable<String, String>>();
            outer.put(id, local_table);
            if(!t.method_to_table.containsKey(t.current_class_name)) {
                t.method_to_table.put(t.current_class_name, outer); 
            } else {
                t.method_to_table.get(t.current_class_name).put(id, local_table);
            }

            t.current_method.push(id); //keep track of the current method
            //copy over the class variables.
            if(t.class_method_param.get(t.current_class_name).containsKey(id) && !prev_state.equals("child_class")) {
                //duplicate method name!
                t.current_method.pop();
                return false;
            }
            
            t.class_method_type.get(t.current_class_name).put(id, type);
            ArrayList<String> temp_list = new ArrayList<String>();
            t.class_method_param.get(t.current_class_name).put(id, temp_list);
            //need to check the parameter list
            if(n.f4.present()) { //parameter list is present
                Boolean tmp = n.f4.accept(this, t);
                if(!tmp) {
                    t.current_method.pop();
                    return false;
                }
            } //builds the method -> parameters relationship
            //now need to check our current method with the parent class 
            if(prev_state.equals("child_class")) {
                String cur_key = t.current_class_name; //t.child_to_parent.get(id);
                while(t.child_to_parent.containsKey(cur_key)) {
                    Hashtable<String, ArrayList<String>> parent_data = 
                        new Hashtable<String, ArrayList<String>>();
                    cur_key = t.child_to_parent.get(cur_key);
                    if(t.class_method_param.containsKey(cur_key)) {
                        parent_data.putAll(t.class_method_param.get(cur_key));
                    }
                    //parent_data.putAll(t.class_method_param.get(cur_key)); //copy over the parents hash table
                    if(parent_data.containsKey(id)) {   //check to see if the current method is already defined in a parent class
                        ArrayList<String> dup_parameter_list = parent_data.get(id);
                        ArrayList<String> cur_parameter_list = t.class_method_param.get(t.current_class_name).get(id);
                        if(!dup_parameter_list.equals(cur_parameter_list)) {
                            return false; //overloading is not allowed!
                        }
                    }
                    break;
                }
                
            }
            
            //might need to check if the method has variables?
            for(int i = 0; i < n.f7.size(); i++) {
                Boolean tmp_val = 
                    n.f7.elementAt(i).accept(this, t);
                if(tmp_val == false) {
                    t.current_method.pop();
                    return false;
                }
            }

            t.load_parameters(id);
            
            
            t.current_method.pop(); //move up from this method
            t.current_state = prev_state;
            return true;
        }
    }

    @Override
    public Boolean visit(Type n, SymbolTable t) {
        return n.f0.accept(this, t);
    }

    @Override
    public Boolean visit(VarDeclaration n, SymbolTable t) {
        //map type to id
        // 0 -> int []
        // 1 -> boolean 
        // 2 -> int
        if(!n.f0.accept(this, t)) {
            return false;
        } //need to load all of the parent member variables
        
        if(t.current_state.contains("class")) {
            if(t.class_parameters.containsKey(n.f1.f0.tokenImage)) {
                return false;
            } else {
                t.class_parameters.put(n.f1.f0.tokenImage, t.id_type);
                return true;
            }
        } //this means that we are currently adding class variables
        if(t.current_state.equals("child_state") && t.method_to_table.get(t.current_class_name).get(t.current_method.peek()).containsKey(n.f1.f0.tokenImage)) { //dont want to redefine it in the same context
            return false;
        }
        if(t.method_to_table.get(t.current_class_name).get(t.current_method.peek()).containsKey(n.f1.f0.tokenImage)) {
            return false;
        }
        t.method_to_table.get(t.current_class_name).get(t.current_method.peek()).put(n.f1.f0.tokenImage, t.id_type);
        //t.table.put(n.f1.f0.tokenImage, t.num_to_type.get(n.f0.f0.which));
        return true; 
    }

    //need to make sure I cant have dups
    @Override 
    public Boolean visit(ClassExtendsDeclaration n, SymbolTable t) {
        /* 
        f1 -> identifier
        f3 -> parent identifier
        f5 -> var declaration 
        f6 -> method declaration 
        */  
        // Hashtable<String, String> local_table
        // = new Hashtable<String, String>();
        //     Hashtable<String, Hashtable<String, String>> outer
        //         = new Hashtable<String, Hashtable<String, String>>();
        //     //outer.put(id, local_table);
        // t.method_to_table.put(t.current_class_name, outer); 
        String class_name = n.f1.f0.tokenImage;
        if(t.class_method_param.containsKey(class_name)) {
            return false;
        }
        t.current_class_name = class_name;
        String parent_class_name = n.f3.f0.tokenImage;
        if(!t.types.contains(parent_class_name)) {
            return false;
        }
        
        //check to make sure there isnt a cycle here
        String cur_key = parent_class_name;
        while(t.child_to_parent.containsKey(cur_key)) {
            if(class_name.equals(t.child_to_parent.get(cur_key))) {
                return false; //means we have found a extending cycle!
            }
            cur_key = t.child_to_parent.get(cur_key);
        }
        //t.child_to_parent.put(class_name, parent_class_name);

        t.current_state = "child_class";

        t.class_parameters.clear();
        for(int i = 0; i < n.f5.size(); i++) {
            boolean tmp = n.f5.elementAt(i).accept(this, t);
            if(!tmp) {
                return false;
            }
        }

        Hashtable<String, ArrayList<String>> method_to_params
                = new Hashtable<String, ArrayList<String>>();
        Hashtable<String, String> method_to_type
                = new Hashtable<String, String>();
        t.class_method_param.put(t.current_class_name, method_to_params);
        t.class_method_type.put(t.current_class_name, method_to_type);

        for(int i = 0; i < n.f6.size(); i++) {
            boolean tmp = n.f6.elementAt(i).accept(this, t);
            if(!tmp) {
                return false; 
            }
        }
        String cur_key2 = t.child_to_parent.get(t.current_class_name);
        t.class_method_param.get(t.current_class_name).putAll(t.class_method_param.get(cur_key2));
                    if(t.class_method_type.containsKey(cur_key2)) {
                        t.class_method_type.get(t.current_class_name).putAll(t.class_method_type.get(cur_key2));
                    }
                    if(t.method_to_table.containsKey(cur_key2)) {
                        //System.out.println(t.method_to_table);
                        if(t.method_to_table.containsKey(t.current_class_name)) {
                            Set<String> ks = t.method_to_table.get(cur_key2).keySet();
                            for(String key : ks) {
                                if(!t.method_to_table.get(t.current_class_name).containsKey(key)) {
                                    t.method_to_table.get(t.current_class_name).put(key, t.method_to_table.get(cur_key2).get(key));
                                } else {
                                    //merge the two 
                                    t.method_to_table.get(t.current_class_name).get(key)
                                        .putAll(t.method_to_table.get(cur_key2).get(key));

                                }
                            }
                            //t.method_to_table.get(t.current_class_name).putAll(t.method_to_table.get(cur_key2));
                        } else {
                            t.method_to_table.put(t.current_class_name, t.method_to_table.get(cur_key2));
                        }
                    }

        t.class_parameters.clear();
        return true;
    }
}
