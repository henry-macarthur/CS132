package typechecker;
import java.util.*; 

public class SymbolTable {
    ArrayList<String> types = new ArrayList<String>();
    ArrayList<String> types_order = new ArrayList<String>();
    Hashtable<String, String> table = new Hashtable<String, String>(); //can delete later
    Hashtable<Integer, String> num_to_type = new Hashtable<Integer, String>();
    Hashtable<String, Hashtable<String, Hashtable<String, String>>> method_to_table = 
        new Hashtable<String, Hashtable<String, Hashtable<String, String>>>();
    //Hashtable<String, 
    Stack<String> current_method = new Stack<String>();
    Hashtable<String, Hashtable<String, ArrayList<String>>> class_method_param = 
        new Hashtable<String, Hashtable<String, ArrayList<String>>>();
    Hashtable<String, Hashtable<String, String>> class_method_type = 
        new Hashtable<String, Hashtable<String, String>>();
    ArrayList<String> current_inputs = new ArrayList<String>();
    Hashtable<String, String> child_to_parent = new Hashtable<String, String>();
    Hashtable<String, Hashtable<String, String>> class_param = 
        new Hashtable<String, Hashtable<String, String>>();
    //Stack<String> current_class = new Stack<String>();
    String current_class_name = "";
    String current_state = "";
    String id_type = "";
    //this will keep track of a class formal parameters
    //for a new class we clear it, fill it, then copy over its data 
    //to the current hash table
    Hashtable<String, String> class_parameters = new Hashtable<String, String>();
    Hashtable<String, Hashtable<String, String>> class_to_param 
        = new Hashtable<String, Hashtable<String, String>>();
    Hashtable<String, Hashtable<String, String>> class_to_id = new Hashtable<String, Hashtable<String, String>>();

    public SymbolTable() {
        num_to_type.put(0, "array");
        num_to_type.put(1, "boolean");
        num_to_type.put(2, "int");
        types.add("array");
        types.add("boolean");
        types.add("int");
    }

    public Boolean load_parameters(String method) {
        Set<String> keys = class_parameters.keySet();
        // for(String key : keys) {
        //     //check to make sure there arent duplicates again
        //     if(method_to_table.get(current_class_name).get(method).containsKey(key)) {
        //         return false; //we have a duplicate :(
        //     } // do a single pass, just to check
        // } // do a single pass, just to check
        // System.out.println("************");
        // System.out.println(method);
        // System.out.println(class_parameters);
        // System.out.println("************");
        for(String key : keys) { //no duplicates, just copy over
            String type = class_parameters.get(key); //get the type for the id
            if(!method_to_table.get(current_class_name).get(method).containsKey(key)) {
                method_to_table.get(current_class_name).get(method).put(key, type);
            } 
        }

        return true;
    }

    public void insert_method_to_param(String class_name, String method_name, String type) {
        if(!class_method_param.get(class_name).containsKey(method_name)) {
            ArrayList<String> tmp = new ArrayList<String>();
            class_method_param.get(class_name).put(method_name, tmp);
        } 
        class_method_param.get(class_name).get(method_name).add(type);
    }

    public boolean is_subtype(String child, String parent) {
        if(child.equals(parent)) return true;
        while(child_to_parent.containsKey(child)) {
            child = child_to_parent.get(child);
            if(child.equals(parent)) {
                return true;
            }
        }
        return false;
    }
}