import cs132.IR.sparrowv.visitor.*;
import cs132.IR.sparrowv.*;
import java.util.*;

public class Converter implements cs132.IR.sparrowv.visitor.ArgRetVisitor<Context,String> {
    String current_function = "";
    int counter = 0;
    public String visit(cs132.IR.sparrowv.Add n, Context c) {
        String ret = "";
        String lhs = n.lhs.toString();
        String arg1 = n.arg1.toString();
        String arg2 = n.arg2.toString();
        String add = "add " + lhs + ", " + arg1 + ", " + arg2 + "\n";
        ret = add;  
        return ret;
    }

    public String visit(cs132.IR.sparrowv.Alloc n, Context c) {
        String ret = "";
        String register = n.lhs.toString();
        String size = n.size.toString();
        String mv_size = "mv a0, " + size + "\n";
        String alloc_call = "jal alloc\n";
        String mv_alloc = "mv " + register + ", a0\n";
        ret = mv_size + alloc_call + mv_alloc;
        return ret;
    }

    public String visit(cs132.IR.sparrowv.Block n, Context c) {
        String ret = "";
        
        for(int i = 0; i < n.instructions.size(); i++) {
            ret += n.instructions.get(i).accept(this, c);
        }

        String return_value = n.return_id.toString();
        int ret_offset = c.get_stack_offset(current_function, return_value);
        String move_ret_to_register = "lw a0, " + ret_offset + "(fp)\n";
        ret += move_ret_to_register;
        return ret;
    }
    // []
    public String visit(cs132.IR.sparrowv.Call n, Context c) {
        String ret = "";
        //step 1, move fp to make space for arguments 
        int argument_size = n.args.size() * 4;
        String allocate_space_for_arguments = ""; 
        if(argument_size > 0) {
            
            ret += "li t6, " + argument_size + "\n";
            ret += allocate_space_for_arguments = "sub sp, sp, " + "t6" + "\n";
            //load each argument 
            for(int i = 0; i < n.args.size(); i++) {
                String id = n.args.get(i).toString();
                int offset = c.get_stack_offset(current_function, id);
                String store_offset_register = "lw t6, " + offset + "(fp)\n";
                String store_parameter = "sw t6, " + (i*4) + "(sp)\n";
                ret += (store_offset_register + store_parameter);
            }
        }
        //now handle the call 
        String calle_register = n.callee.toString();
        String lhs = n.lhs.toString();
        String function_call = "jalr " + calle_register + "\n";
        ret += function_call;
        if(argument_size > 0) {
            ret += "sw fp, -" + argument_size + "(sp)\n";
        }
        String move_ret_val = "mv " + lhs + ", a0\n";
        ret += move_ret_val;

        //am gonna have to store return value (a0) here    
         
        //have to check if there are parameters and move them onto the stack 
        //the parameters should go in the next 2 memory slots 
        return ret;
    }

    public String visit(cs132.IR.sparrowv.ErrorMessage n, Context c) {
        String ret = "";
        String msg = n.msg; 
        String load_msg;
        if(msg.equals("null pointer")) {
            load_msg = "la a0, msg_0\n";
        } else {
            load_msg = "la a0, msg_1\n";
        }
        String jump_error = "j error\n";
        ret = load_msg + jump_error;
        return ret;
    }

    public String visit(cs132.IR.sparrowv.FunctionDecl n, Context c) {
        String ret = "";
        current_function = n.functionName.name;
        //formalParameters
        //block 
        //store old frame pointer
        String store_old_frame = "sw fp, -8(sp)\n";
        String set_new_fp = "mv fp, sp\n";
        int stack_size = c.get_stack_size(current_function); 
        String load_stack_size = "li t6, " + stack_size + "\n";
        String allocate_stack_frame = "sub sp, sp, t6\n";
        String store_ret_address = "sw ra, -4(fp)\n";
        String callee_convention = store_old_frame + set_new_fp + load_stack_size + allocate_stack_frame + store_ret_address; 
        String function_label = ".globl " + current_function + "\n" + current_function + ":\n";
        String function_body = n.block.accept(this, c);
        String set_sp = "addi sp, sp, " + stack_size + "\n";
        String set_ra = "lw ra, -4(fp)\n";
        String set_fp = "lw fp, -8(fp)\n";
        String jump_parent = "jr ra\n";
        String callee_end = set_sp + set_ra + set_fp + jump_parent;
        ret =  function_label + callee_convention + function_body + callee_end + "\n";
        //functionName 
        //parent?
        return ret;
    }

    public String visit(cs132.IR.sparrowv.Goto n, Context c) {
        String ret = "";
        String label = current_function + "_" + n.label.toString();
        String goto_statement = "jal " + label + "\n";
        ret = goto_statement;
        return ret;
    }

    public String visit(cs132.IR.sparrowv.IfGoto n, Context c) {
        String ret = "";
        String condition_register = n.condition.toString();
        //since labels are now global, need to prepend the current function_name to it
        String dest_label = current_function + "_" + n.label.toString(); 
        String to_distant_label = dest_label + "_1_" + counter;
        String branch_eq = "beqz " + condition_register + ", " + to_distant_label + "\n"; //jump to indirect label
        String to_normal_execution_label = dest_label + "_2_" + counter;
        String jump_normal = "jal " + to_normal_execution_label;
        String jump_distant = "jal " + dest_label + "\n";
        //branch
        //jump to normal 
        //jump to distant label 
        //jump distant
        //normal label 
        String if_goto = branch_eq + jump_normal + "\n" + to_distant_label + ":\n" + jump_distant + to_normal_execution_label + ":\n";
        ret = if_goto;
        counter++;
        return ret;
    }

    public String visit(cs132.IR.sparrowv.LabelInstr n, Context c) {
        String ret = "";
        String label = current_function + "_" + n.label.toString() + ":\n";
        ret = label;
        return ret;
    }

    public String visit(cs132.IR.sparrowv.LessThan n, Context c) {
        String ret = "";
        String lhs = n.lhs.toString();
        String arg1 = n.arg1.toString();
        String arg2 = n.arg2.toString();
        String lt = "slt " + lhs + ", " + arg1 + ", " + arg2 + "\n";
        ret = lt;
        return ret;
    }

    public String visit(cs132.IR.sparrowv.Load n, Context c) {
        String ret = "";
        String lhs = n.lhs.toString();
        String base = n.base.toString();
        int offset = n.offset;// * -1;
        String load = "lw " + lhs + ", "  + offset + "(" + base + ")\n";
        ret = load;
        return ret;
    }

    public String visit(cs132.IR.sparrowv.Move_Id_Reg n, Context c) {
        String ret = "";
        String id = n.lhs.toString();
        String register = n.rhs.toString();
        int offset = c.get_stack_offset(current_function, id);
        //need to do a sw here 
        String store_word = "sw " + register + ", " + offset + "(fp)\n";
        ret = store_word;
        return ret;
    }

    public String visit(cs132.IR.sparrowv.Move_Reg_FuncName n, Context c) {
        String ret = "";
        String register = n.lhs.toString();
        String func_name = n.rhs.toString();
        String load_address = "la " + register + ", " + func_name + "\n";
        ret = load_address;
        return ret;
    }

    public String visit(cs132.IR.sparrowv.Move_Reg_Id n, Context c) {
        String ret = "";
        //store at offset 
        String register = n.lhs.toString();
        String id = n.rhs.toString();
        int offset = c.get_stack_offset(current_function, id);
        String load_word = "lw " + register + ", " + offset + "(fp)\n";
        ret = load_word;
        return ret;
    }

    public String visit(cs132.IR.sparrowv.Move_Reg_Integer n, Context c) {
        String ret = "";
        //load immediate 
        int rhs = n.rhs; //immediate
        String lhs = n.lhs.toString(); //register
        String load_immediate = "li " + lhs + ", " + rhs + "\n";
        ret = load_immediate;
        return ret;
    }

    public String visit(cs132.IR.sparrowv.Move_Reg_Reg n, Context c) {
        String ret = "";
        //move : mv command 
        String rhs = n.rhs.toString();
        String lhs = n.lhs.toString();
        String move = "mv " + lhs + ", " + rhs + "\n";
        ret = move;
        return ret;
    }

    public String visit(cs132.IR.sparrowv.Multiply n, Context c) {
        String ret = "";
        String lhs = n.lhs.toString();
        String arg1 = n.arg1.toString();
        String arg2 = n.arg2.toString();
        String multiply = "mul " + lhs + ", " + arg1 + ", " + arg2 + "\n";
        ret = multiply; 
        return ret;
    }

    public String visit(cs132.IR.sparrowv.Print n, Context c) {
        String ret = "";
        String register = n.content.toString();
        String get_print_register = "mv a1, " + register + "\n";
        String load_print_int = "li a0, @print_int\n";
        ret = get_print_register + load_print_int + "ecall\n";
        get_print_register = "li a1, " + "10" + "\n";
        load_print_int = "li a0, @print_char\n";
        ret += get_print_register + load_print_int + "ecall\n";
        return ret;
    }

    public String visit(cs132.IR.sparrowv.Program n, Context c) {
        String ret = "";
        //program consists of a list of function declarations
        String functions = "";
        for(int i = 0; i < n.funDecls.size(); i++) {
            //recurse onto each function declaration
            functions += n.funDecls.get(i).accept(this, c);
        }

        String equiv_statements = ".equiv @sbrk, 9\n.equiv @print_string, 4\n.equiv @print_char, 11\n.equiv @print_int, 1\n.equiv @exit, 10\n.equiv @exit2, 17\n\n";
        String text = ".text\njal Main\nli a0, @exit\necall\n\n";
        String error = ".globl error\nerror:\nmv a1, a0\nli a0, @print_string\necall\nli a1, 10\nli a0, @print_char\necall\nli a0, @exit\necall\nabort_17:\nj abort_17\n\n";
        String allocate = ".globl alloc\nalloc:\nmv a1, a0\nli a0, @sbrk\necall\njr ra\n\n";
        String data = ".data\n\n";
        String msg_0 = ".globl msg_0\nmsg_0:\n.asciiz \"null pointer\"\n.align 2\n\n";
        String msg_1 = ".globl msg_1\nmsg_1:\n.asciiz \"array index out of bounds\"\n.align 2\n\n";
        ret = equiv_statements + text + functions +  error+ allocate + data + msg_0 + msg_1;
        System.out.println(ret);
        return ret;
    }

    public String visit(cs132.IR.sparrowv.Store n, Context c) {
        String ret = "";
        String base = n.base.toString();
        String rhs = n.rhs.toString();
        int offset = n.offset;// * -1;
        String store = "sw " + rhs + ", " + offset + "(" + base + ")\n";
        ret = store;
        return ret;
    }

    public String visit(cs132.IR.sparrowv.Subtract n, Context c) {
        String ret = "";
        String lhs = n.lhs.toString();
        String arg1 = n.arg1.toString();
        String arg2 = n.arg2.toString();
        String subtract = "sub " + lhs + ", " + arg1 + ", " + arg2 + "\n";
        ret = subtract; 
        return ret;
    }
}