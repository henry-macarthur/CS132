import java.util.*; 

public class Context {
    Hashtable<String, Integer> id_to_start = 
        new Hashtable<String, Integer>(); //(func+id) -> begin_line
    Hashtable<String, Integer> id_to_end = 
        new Hashtable<String, Integer>();
    Hashtable<String, Integer> label_to_line = 
        new Hashtable<String, Integer>();
    ArrayList<ArrayList<Integer>> intervals = 
        new ArrayList<ArrayList<Integer>>();
    Stack<String> registers = 
        new Stack<String>();
    Hashtable<String, String> reg_to_id = 
        new Hashtable<String, String>();
    ArrayList<String> dont_allocate = 
        new ArrayList<String>();
    String label = "";
    String has_spill = "";
    // Hashtable<String, String> caller_saved_reg_to_id = 
    //     new Hashtable<String, String>();

    public Context() {
        load_registers(); //initialize our free registers 
    }

    public void set_begin_line(String id, String function_name, int line) {
        String identifier = function_name + "_" + id;
        if(!id_to_start.containsKey(identifier)) {
            id_to_start.put(identifier, line);
        }
    }

    public void set_end_line(String id, String function_name, int line) {
        String identifier = function_name + "_" + id;
        if(!id_to_end.containsKey(identifier)) {
            id_to_end.put(identifier, line);
        } else {
            //if the new use is on a later line then we update its end line 
            int cur_line = id_to_end.get(identifier);
            if(cur_line < line) {
                id_to_end.put(identifier, line);
            }
        }
    }

    public void set_label(String id, int line) {
        label_to_line.put(id, line);
    }

    public int get_label_line(String id) {
        int line = -1; 
        if(label_to_line.containsKey(id)) {
            line = label_to_line.get(id);
        }

        return line;
    }

    public void update_if_loop() {
        Set<String> setOfKeys = id_to_end.keySet();

        for(String key : setOfKeys) { //loop over each key 
            int cur_end = id_to_end.get(key);
            
            //compare it to each interval 
            for(int i = 0; i < intervals.size(); i++) {
                int start_int = intervals.get(i).get(0);
                int end_int = intervals.get(i).get(1);

                if(cur_end > start_int && cur_end < end_int) {
                    id_to_end.put(key, end_int);
                    //update the interval
                }
            }
        }
    }

    //basically just free all of the registers and push them to the stack 
    //as well as their associated ids
    public void load_registers() {
        reg_to_id.clear();
        registers.clear();

        //load all of the registers on the stack 
        // for(int i = 2; i <= 7; i++) {
        //     String cur_reg = "a" + i;
        //     registers.push(cur_reg);
        // }

        for(int i = 1; i <= 11; i++) {
            String cur_reg = "s" + i;
            registers.push(cur_reg);
        }

        //save t0 and t1 for expressions
        for(int i = 2; i <= 5; i++) {
            String cur_reg = "t" + i;
            registers.push(cur_reg);
        }
    }

    public ArrayList<String> addActive(String id, ArrayList<String> cur_active) {
        if(cur_active.size() == 0) {
            cur_active.add(id);
            return cur_active;
        } 
        int i = 0;
        int end = id_to_end.get(id);

        for(; i < cur_active.size(); i++) {
            //find first i that is bigger than the current end interval

            String tmp = cur_active.get(i);
            int tmp_end = id_to_end.get(tmp);

            if(end < tmp_end) {
                break;
            }
        }
        
        if(i == cur_active.size())
            cur_active.add(id);
        else 
            cur_active.add(i, id);
        
        return cur_active;
    }

    public String id_to_reg(String id) {
        Set<String> keySet = reg_to_id.keySet();
        for(String key : keySet) {
            if(reg_to_id.get(key).equals(id)) {
                return key;
            }
        }

        return "";
    }

    public ArrayList<String> expireOldIntervals(String id, ArrayList<String> cur_active, ArrayList<String> live) {
        for(int i = 0; i < cur_active.size(); i++) {
            int endpoint = id_to_end.get(cur_active.get(i)); 
            int startpoint = id_to_start.get(id);
            //System.out.println(endpoint + " " + startpoint);
            if(live.contains(cur_active.get(i)) && endpoint >= startpoint) {
                return cur_active; 
            } else {
                //remove j from active 
                String register = id_to_reg(cur_active.get(i));
                reg_to_id.remove(register);
                //System.out.println("EXPIRE " + cur_active.get(i) + " " + startpoint + " " + endpoint);
                registers.push(register);
                cur_active.remove(i);
                //add register to free pool 
            }
        }

        return cur_active;
    }

    public ArrayList<String> spillAtInterval(String id, ArrayList<String> cur_active) {
        String spill = cur_active.get(cur_active.size() - 1);
        int spill_end = id_to_end.get(spill);
        int cur_end = id_to_end.get(id);

        if(spill_end > cur_end && id_to_reg(spill).equals("")) {
            String register = id_to_reg(spill); //reassign this register
            reg_to_id.put(register, id);

            int spill_index = cur_active.indexOf(spill);
            //have to spill here, so spill = register
            //String spill = 
            has_spill = (id + " = " + register);
            cur_active.remove(spill_index);
            cur_active = addActive(id, cur_active);
        } else {
            //spill here 
            //id = whatever value we are assigning
            
        }

        return cur_active;
    }

    public ArrayList<String> getLiveIntervals(int cur_line) {
        ArrayList<String> live_intervals = new ArrayList<String>();
        Set<String> ids = id_to_start.keySet();
        for(String id : ids) { //loop over each id, and get a list of all the current live intervals
            int start = id_to_start.get(id); 
            int end = id_to_end.get(id);

            if(start <= cur_line && end >= cur_line) { //if the current line is contained in the interval, then the id is live
                live_intervals.add(id);
            }
        }

        return live_intervals; //return a list of all ids that are currently live
    }

    public void allocateRegister(int line_number) {
        //System.out.println(line_number + " " + reg_to_id);
        ArrayList<String> active = new ArrayList<String>();
        //going to loop through reg_to_id and add all of the active ids to create our active list
        Set<String> keySet = reg_to_id.keySet();
        for(String id : keySet) {
            active = addActive(reg_to_id.get(id), active);
        }
        ArrayList<String> increasingStart = new ArrayList<String>();

        increasingStart = getLiveIntervals(line_number);

        //now time to sort by increasing start point 
        for(int i = 0; i < increasingStart.size(); i++) {
            for(int j = i+1; j < increasingStart.size(); j++) {
                String id1 = increasingStart.get(i);
                String id2 = increasingStart.get(j);

                int start1 = id_to_start.get(id1);
                int start2 = id_to_start.get(id2);

                if(start2 < start1) {
                    String tmp = id1;
                    increasingStart.set(i, id2);
                    increasingStart.set(j, tmp);
                }
            }
        }
            
        
        for(int i = 0; i < increasingStart.size(); i++) {
            active = expireOldIntervals(increasingStart.get(i), active, increasingStart);
            if(reg_to_id.size() >= 15) {
                //split at interval
                active = spillAtInterval(increasingStart.get(i), active);
            } else {
                //allocate a register 
                //only need to allocate a register if the current id is not mapped to a register
                
                String tmp = id_to_reg(increasingStart.get(i));
                if(tmp.equals("")) {
                    String new_register = registers.pop();
                    reg_to_id.put(new_register, increasingStart.get(i));
                    active = addActive(increasingStart.get(i), active); //update the active list
                    //add i to active
                }
            }
        }
    }

    public String getRegister(String id, int line_number) {
        has_spill = "";
        //i think all i have to do is call allocate register and 
        //then check to see if the current id is in our register map
        //if it is, return its register, otherwise "spill"
        allocateRegister(line_number); //this handles register allocation 
        //check to see if id has been assigned to a register : 
        String register = id_to_reg(id);
        if(register.equals("")) {
            //this means that no register got allocated 
            return "t0";
        }

        if(!has_spill.equals("")) {
            register += (";" + has_spill);
        }

        return register;
    }
    //allocate a register 
    /*
    public String getRegister(String id) {
        
        //simple case, if we already have the register allocated
        // if(dont_allocate.contains(id)) {
        //     return "t0";
        // }
        Set<String> keySet = reg_to_id.keySet();
        for(String key : keySet) {
            String identifier = reg_to_id.get(key);
            if(id.equals(identifier)) {
                return key;
            }
        }

        if(dont_allocate.contains(id)) {
            return "t0";
        }
        //simple case, if we have free registers just pop it off the stack 
        if(registers.size() > 0) {
            String allocated_register = registers.peek();
            registers.pop();
            //int end_line = id_to_end.get(reg_to_id.get());
            reg_to_id.put(allocated_register, id);

            return allocated_register;
        }
        //otherwise we have to do more work :)
        keySet = reg_to_id.keySet();
        int max_end = -1; 
        String max_register = "";
        String max_id = ""; 
        for(String key : keySet) {
            //key -> register
            String identifier = reg_to_id.get(key);
            int current_end = id_to_end.get(identifier);

            if(current_end > max_end) {
                max_end = current_end;
                max_register = key; 
                max_id = identifier;
            }
        }
        //need a case where the register just does not get allocated since its interval is larger 
        int new_end = id_to_end.get(id);
        if(new_end >= max_end) { //dont allocate a register for it, lets just return a temporary 
            return "t0";
        }
        reg_to_id.put(max_register, id);

        max_id = max_id + " = " + max_register + "\n";
        //will pick off the max_id later and assign the max_id to the register value so that 
        //it counts as a store, then we will reassign the register to the new id
        return max_register + ";" + max_id;
    } */

    public String pickRegister(String input) {
        String ret = input;
    
        if(input.indexOf(";") != -1) {
            ret = input.substring(0, input.indexOf(";"));
        } 

        return ret; 
    }

    public String pickStack(String input) {
        String ret = "";

        if(input.indexOf(";") != -1) {
            ret = (input.substring(input.indexOf(";") + 1) + "\n");
        }

        return ret;
    }

    public void freeRegister(int current_line) {
        Set<String> keySet = reg_to_id.keySet();
        ArrayList<String> free_registers = new ArrayList<String>();

        for(String key : keySet) {

            String identifier = reg_to_id.get(key);
            int current_end = id_to_end.get(identifier);
            if(current_end <= current_line) { //this should happen at the beginning i guess since we can just use temporaries?
                //free the register
                registers.push(key);
                free_registers.add(key);
            }
        }

        for(int i = 0; i < free_registers.size(); i++) {
            String reg = free_registers.get(i);
            reg_to_id.remove(reg);
        }
    }

    public String isRegister(String func, String id) {
        Set<String> keySet = reg_to_id.keySet();
        for(String key : keySet) {
            String identifier = reg_to_id.get(key);
            String full_id = func + "_" + id;
            if(identifier.equals(full_id)) {
                return key;
            }
        }

        return func + "_"  + id;
    }

    public String callerSaveActiveRegisters(String callRegister) {
        Set<String> keySet = reg_to_id.keySet();
        String ret = "";
        for(String register : keySet) {
            if(register.equals(callRegister))
                continue;
            String current_id = reg_to_id.get(register);
            //current_id = current_id.substring(current_id.indexOf("_") + 1);
            String save_register = current_id + " = " + register + "\n";
            ret += save_register;
        }

        return ret; 
    }

    public String callerRestoreActiveRegisters(String callRegister) {
        Set<String> keySet = reg_to_id.keySet();
        String ret = "";
        for(String register : keySet) {
            if(register.equals(callRegister))
                continue;
            String current_id = reg_to_id.get(register);
            //current_id = current_id.substring(current_id.indexOf("_") + 1);
            String restore = register + " = " + current_id + "\n";
            ret += restore;
        }

        return ret; 
    }

    public boolean isLabel(int current_line) {
        Set<String> keySet = id_to_start.keySet();
        for(String id : keySet) {
            if(label_to_line.get(id) == current_line) {
                label = id;
                return true;
            }
                
        }
        return false;
    }



    public String determineLoopLiveness(int current_line) {
        if(dont_allocate.size() > 0) //only need to deal with outer most loop 
            return "";
        ArrayList<String> ids = new ArrayList<String>();
        int interval_end = 0;
        int interval_begin = 0;
        for(int i = 0; i < intervals.size(); i++) {
            if(current_line == intervals.get(i).get(0)
            && current_line <= intervals.get(i).get(1)) {
                interval_end = intervals.get(i).get(1);
                interval_begin = intervals.get(i).get(0);
                break; //find the end to our interval
            }
        }

        if(interval_end == 0)
            return "";

        Set<String> keySet = id_to_start.keySet();
        for(String id : keySet) {
            int begin_line = id_to_start.get(id);
            int end_line = id_to_end.get(id);

            if(end_line == interval_end ||
            (end_line >= interval_end && begin_line <= interval_begin)) {
                ids.add(id);
            }
        }
        
        //now I just need to loop over this, and for each one, see which one ends the last
        //
        for(int i = 0; i < ids.size(); i++) {
            for(int j = i+1; j < ids.size(); j++) {
                String id1 = ids.get(i);
                String id2 = ids.get(j);

                if(id_to_end.get(id1) > id_to_end.get(id2)) {
                    //swap them 
                    String id3 = id1; 
                    ids.set(i,id2); 
                    ids.set(j,id3);
                }
            }
        }

        //ids is now sorted in ascending end line order
        //can allocate registers for all of them
        //loop over the first 15 registers to mark them as "allocated" and 
        //then store the rest in a list to show that they cant be allocated 
        //one thing to look out for is seeing if a regisger is already allocated 
        ArrayList<String> reserved = new ArrayList<String>();
        if(ids.size() > 15) {
            for(int k = 15; k < ids.size(); k++) {
                String cur_id = ids.get(k);
                reserved.add(cur_id);
            }
        }

        /*
            loop through all the reserved ids and make sure to deallocate them 
            from registers 
        */
        String output = "";
        for(int i = 0; i < reserved.size(); i++) {
            String cur_id = reserved.get(i);
            int index = cur_id.indexOf("_");
            String cl = cur_id.substring(0, index);
            String id = cur_id.substring(index+1);
            if(!cur_id.equals(this.isRegister(cl, id))) { //means its in a register
                //need to deallocate it 
                //if the item is currently in a register, save to stack basically
                //System.out.println(register);
                String register = this.isRegister(cl, id);
                output += (cur_id + " = " + register + "\n");
                reg_to_id.remove(register);
                registers.push(register);
            }
        }
        dont_allocate = reserved;
        return output;
    }
}