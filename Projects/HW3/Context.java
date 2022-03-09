import java.util.*;

class Context { 
    Hashtable<String, ArrayList<String>> class_to_methods =
        new Hashtable<String, ArrayList<String>>();
    Hashtable<String, ArrayList<String>> class_to_parameters =
        new Hashtable<String, ArrayList<String>>();
    Hashtable<String, String> class_to_parent = 
        new Hashtable<String, String>();
    Hashtable<String, ArrayList<String>> method_to_parameters = 
        new Hashtable<String, ArrayList<String>>();
    Hashtable<String, Hashtable<String, String>> class_method_parameter = 
        new Hashtable<String, Hashtable<String, String>>();
    Hashtable<String, String> method_to_type = 
        new Hashtable<String, String>();
    Hashtable<String, ArrayList<String>> class_param_type 
        = new Hashtable<String, ArrayList<String>>();

    public int containsSubstr(ArrayList<String> input, String key) {
        for(int i = 0; i < input.size(); i++) {
            if(input.get(i).substring(input.get(i).indexOf("_") + 1).equals(key)) {
                return i;
            }
        }
        return -1;
    }

    public void helper(Set<String> visited, String current) {
        
        if(visited.contains(current)) {
            return; //dont need to load any of the parent info if we already have
        }
        if(!class_to_parent.containsKey(current)) { //this is the base case
            return; 
        }

        //make sure the parent loads all of its parent info.
        helper(visited, class_to_parent.get(current));

        ArrayList<String> local_methods = 
            class_to_methods.get(current);
        ArrayList<String> local_parameters 
            = class_to_parameters.get(current);
        ArrayList<String> parent_methods = 
            class_to_methods.get(class_to_parent.get(current));
        ArrayList<String> parent_parameters = 
            class_to_parameters.get(class_to_parent.get(current));
        ArrayList<String> parent_types = 
            class_param_type.get(class_to_parent.get(current));
        ArrayList<String> cur_types = 
            class_param_type.get(current);

        //check parent method to see if the child has overriden it, if not, add it!

        for(int i = parent_methods.size() - 1; i >= 0; i--) {
            String cur_method = parent_methods.get(i);
            String cur_method2 = cur_method.substring(cur_method.indexOf("_") + 1);
            if(containsSubstr(local_methods, cur_method2) == -1) {
                local_methods.add(0, cur_method);
            } else if (containsSubstr(local_methods, cur_method2) != -1) {
                local_methods.add(0, local_methods.get(containsSubstr(local_methods, cur_method2)));
            }
            
        }

        for(int i = parent_parameters.size() - 1; i >= 0; i--) {
            String cp = parent_parameters.get(i);
            if(!local_parameters.contains(cp)) {
                local_parameters.add(0, cp);
                cur_types.add(0, parent_types.get(i));

            }
        }
        //Collections.reverse(local_methods);
        visited.add(current);
    }

    //this method is gonna load all the parent class methods for a 
    public void load_parent_methods() {
        Set<String> visited = new HashSet<String>();
        Set<String> leaves = class_to_parent.keySet();
        for(String leaf : leaves) {
            helper(visited, leaf);
        }
    }
}