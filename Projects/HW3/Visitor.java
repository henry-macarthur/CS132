import cs132.minijava.*;
import java.util.*;
import cs132.minijava.visitor.*;
import cs132.minijava.syntaxtree.*;

//will add the second paramater another time, keep it basic for now
class Visitor extends GJDepthFirst<String, Context> {
    int k = 6;
    String current_class = "";
    String current_method = "";
    String arr_access = "";
    
    enum State {
        CLASS, 
        METHOD, 
        OTHER,
        CLASSALLOCATION,
        SEND,
        MAIN,
    }

    State current_state = State.OTHER;

    Hashtable<String, ArrayList<String>> class_to_methods =
        new Hashtable<String, ArrayList<String>>();
    Hashtable<String, ArrayList<String>> class_to_parameters =
        new Hashtable<String, ArrayList<String>>();
    Hashtable<String, String> id_to_type = 
        new Hashtable<String, String>();
    String allocated_type = "";

    @Override
    public String visit(Goal n, Context c) {
        String main = n.f0.accept(this, c);
        String rest = "";

        for(int i = 0; i < n.f1.size(); i++) {
            rest += n.f1.elementAt(i).accept(this, c);
        }
        

        String final_value = (main + rest);
        return final_value;
    }

    @Override 
    public String visit(TypeDeclaration n, Context c) {
        return n.f0.accept(this, c);
    }

    @Override 
    public String visit(Statement n, Context c) {
        String res = n.f0.accept(this, c);
        //System.out.println(res);
        return res;
    }

    @Override
    public String visit(Expression n, Context c) {
        String result = n.f0.accept(this, c);
        // System.out.println(result);
        return result;
    }

    @Override 
    public String visit(PlusExpression n, Context c) {
        String lhs = "t" + k;
        k++;
        String op1 = "t" + k;
        String exp1 = n.f0.accept(this, c);
        String op2 = "t" + k;
        String exp2 = n.f2.accept(this, c);
        return exp1 + exp2 + lhs + " = " + 
            op1 + " + " + op2 + "\n";
    }

    @Override 
    public String visit(TimesExpression n, Context c) {
        String lhs = "t" + k;
        k++;
        String op1 = "t" + k;
        String exp1 = n.f0.accept(this, c);
        String op2 = "t" + k;
        String exp2 = n.f2.accept(this, c);
        return exp1 + exp2 + lhs + " = " + 
            op1 + " * " + op2 + "\n";
    }

    @Override 
    public String visit(MinusExpression n, Context c) {
        String lhs = "t" + k;
        k++;
        String op1 = "t" + k;
        String exp1 = n.f0.accept(this, c);
        String op2 = "t" + k;
        String exp2 = n.f2.accept(this, c);
        return exp1 + exp2 + lhs + " = " + 
            op1 + " - " + op2 + "\n";
    }

    @Override 
    public String visit(CompareExpression n, Context c) {
        String lhs = "t" + k;
        k++;
        String op1 = "t" + k;
        String exp1 = n.f0.accept(this, c);
        String op2 = "t" + k;
        String exp2 = n.f2.accept(this, c);
        return exp1 + exp2 + lhs + " = " + 
            op1 + " < " + op2 + "\n";
    }

    @Override 
    public String visit(AndExpression n, Context c) {
        int oldk = k;
        String lhs = "t" + k;
        k++;
        String op1 = "t" + k;
        String exp1 = n.f0.accept(this, c);
        String op2 = "t" + k;
        String exp2 = n.f2.accept(this, c);

        //add both of the values together, check if its 0
        String check_a0 = "if0 " + op1 + " goto else" + oldk + "\n";
        String check_a1 = "if0 " + op2 + " goto else" + oldk + "\n";
        String elselabel = "else" + oldk + ":\n";
        String assign_true = "t" + oldk + " = 1\n" + "goto if" + oldk + "_end\n";
        String assign_false = "t" + oldk + " = 0\n" + "goto if" + oldk + "_end\n";
        String final_if = exp1 + exp2 + check_a0 + check_a1 + assign_true + elselabel + assign_false + "if" + oldk + "_end:\n";

        String op3 = "t" + k + " = " + op1 + " + " + op2 + "\n";
        //String assign_true = "t" + oldk + " = 1\n" + "goto if" + oldk + "_end\n";
        // /String assign_false = "else" + oldk + ":\n" + "t" + oldk + " = 0\n" + "if" + oldk + "_end:\n";
        String if_statement = "if0 t" + k + " goto else" + oldk + "\n" +  assign_true + assign_false;
        
       
        return final_if;//exp1 + exp2 + op3 + if_statement;
    }

    @Override
    public String visit(NotExpression n, Context c) { 
        //if expr = 0 then we set the value to 1, 
        //if expr != 0, we set the value to 0

        String op0 = "t" + k;
        int oldk = k;
        k++;
        String op1 = "t" + k;
        String expr1 = n.f1.accept(this, c);
        String if_statement = expr1 + "if0 " + op1 + " goto else" + oldk + "\n" + op0 + " = 0\ngoto end" + oldk + "\nelse" + oldk + ":\n" + op0 + " = 1\nend" + oldk + ":\n";
        
        return if_statement;
    }

    @Override 
    public String visit(PrimaryExpression n, Context c) {
        return n.f0.accept(this, c);
    }

    @Override 
    public String visit(IntegerLiteral n, Context c) {
        String result = "t" + k + " = " + n.f0.tokenImage + "\n";
        k++; //i think it makes sense for this to go here
        return result;
    }

    @Override 
    public String visit(PrintStatement n, Context c) {
        String result; 
        //f2 -> expression 
        int oldk = k;
        String expr = n.f2.accept(this, c);
        result = expr + "print(t" + oldk + ")\n";
        return result;
    }

    @Override 
    public String visit(TrueLiteral n, Context c) {
        String result = "t" + k + " = " + 1 + "\n";
        k++; //i think it makes sense for this to go here
        return result;
    }

    @Override 
    public String visit(FalseLiteral n, Context c) {
        String result = "t" + k + " = " + 0 + "\n";
        k++; //i think it makes sense for this to go here
        return result;
    }

    @Override 
    public String visit(BracketExpression n, Context c) {
        //k++;
        return n.f1.accept(this, c);
    }

    @Override 
    public String visit(IfStatement n, Context c) {
        //handle expression 
        int label_k = k;
        k++;
        String expr_value = "t" + k;
        String expr_result = n.f2.accept(this, c);
        //handle statement 1
        String statement1 = n.f4.accept(this, c); //fix statements later 
        //handle statement 2 
        String statement2 = n.f6.accept(this, c);

        String result = expr_result + "if0 " +  expr_value + " goto else" + label_k  + "\n" +
            statement1 + "goto end" + label_k + "\n" + "else" + label_k + ":" + "\n" + statement2 +
            "end" + label_k + ":\n";
        return result;
    }

    @Override 
    public String visit(Identifier n, Context c) {
        //need to check to see whether the item is in the class or not first cuz otherweise we get it from the class list
        //lookup the token image 
        String res = "t" + k + " = " + current_class + "_" + n.f0.tokenImage + "\n";
        allocated_type = n.f0.tokenImage; //need to look into the table now 
        if(current_state == State.METHOD) {
        }
        //int a = c.containsSubstr(c.class_to_methods.get(current_class), n.f0.tokenImage);
        
        if(current_state == State.METHOD && !c.method_to_parameters.get(current_method).contains(n.f0.tokenImage)) {
           // && (c.containsSubstr(c.class_to_methods.get(current_class), n.f0.tokenImage) == -1)) {
            String parameter_name = current_class + "-" + n.f0.tokenImage;
            int index = c.class_to_parameters.get(current_class).indexOf(parameter_name);
            String tmp_class = current_class;
            while (c.class_to_parent.containsKey(tmp_class) && index == -1) {
                tmp_class = c.class_to_parent.get(tmp_class);
                parameter_name = tmp_class + "-" + n.f0.tokenImage;

                index = c.class_to_parameters.get(tmp_class).indexOf(parameter_name);

            }
            int offset = ((index + 1) * 4);
            res = "t" + k + " = [" + "this" + " + " + offset + "]" + "\n";
        } 
       
        k += 2;
        //add something next for if its a method call and we have it in the class (i.e. this. )
        return res;
    }

    @Override 
    public String visit(AssignmentStatement n, Context c) {
        //lets just asume this is the most basic type for now
        //deal with array assignmnment and member variable stuff later
        int k_old = k;

        allocated_type = "";
        String expression = n.f2.accept(this, c);

        String assignment = current_class + "_" + n.f0.f0.tokenImage + " = " + "t" + k_old + "\n";
        String result = expression + assignment;
        //want to look to see if the token is not in the parameter list or local scope
        if(current_state == State.METHOD && !c.method_to_parameters.get(current_method).contains(n.f0.f0.tokenImage)) {
            String parameter_name = current_class + "-" + n.f0.f0.tokenImage;
            String tmp_class = current_class;
            //we need to get the index of the variable in the list, do plus 1 and then * 4 to access it 
            int index = c.class_to_parameters.get(current_class).indexOf(parameter_name);

            while (c.class_to_parent.containsKey(tmp_class) && index == -1) {

                tmp_class = c.class_to_parent.get(tmp_class);
            
                parameter_name = tmp_class + "-" + n.f0.f0.tokenImage;
                
                index = c.class_to_parameters.get(tmp_class).indexOf(parameter_name);
                

            }
            int offset = ((index + 1) * 4);
            result += ("[this + " + offset + "] = t" + k_old + "\n"); 
        }

        if(!allocated_type.equals("") && c.class_to_methods.containsKey(allocated_type)) {
            id_to_type.put(n.f0.f0.tokenImage, allocated_type);
        }

        return result;
    }

    @Override 
    public String visit(Block n, Context c) {
        String result = "";
        for(int i = 0; i < n.f1.size(); i++) {
            result += n.f1.elementAt(i).accept(this, c);
        }

        return result;
    }

    @Override 
    public String visit(WhileStatement n, Context c) {
        /*
        f2 -> expr 
        f4 -> statement 

        resolve the statement first 
        */
        int k_label = k;
        k++; //increment for the while expression 
        String expression_code = n.f2.accept(this, c);
        int s_code = k;
        String statement_code = n.f4.accept(this, c);

        String result =  "loop" + k_label + ":\n" + expression_code + "if0 t" + (k_label + 1) + " goto end" + k_label + "\n"
            + statement_code + "goto loop" + k_label + "\n" + "end" + k_label + ":\n";
        return result;
    } 

    @Override 
    public String visit(ArrayAllocationExpression n, Context c) {
        int old_k = k;
        k = k + 3;
        String expression_result = n.f3.accept(this, c);
        String k1 = "t" + (old_k + 1);
        String k2 = "t" + (old_k + 2);
        String k3 = "t" + (old_k + 3);
        String k4 = "t" + (old_k + 4);
        String k5 = "t" + (old_k + 5);
        k += 2;

        int numDigits = String.valueOf(old_k).length() - 0;

        String length = expression_result.substring(4 + numDigits, expression_result.length() - 1);
        int sz = 0;
        String v1 = "";
        String v2 = "";
        String a1 = "";
        String a2 = "";
        try {
            sz = Integer.parseInt(length);
            v1 = "" + (sz + 1);
            v2 = "" + ((sz + 1) * 4);
        } catch (Exception e) {
            a1 = k4 + " = " + 1 + "\n";
            a2 = k5 + " = " + 4 + "\n";
            
            v1 = "t" + (old_k + 3) + " + " +  k4;
            v2 = "" + k1 + " * " + k5;
        }  
        //int sz = Integer.parseInt(length);
        //String k4 = "t" + (old_k + 4);

        String k1_allocation = k1 + " = " + v1 + "\n";
        String k2_allocation = k2 + " = " + v2 + "\n";
        String k_allocation = "t" + old_k + " = " + "alloc(" + k2 + ")\n";
        String arr_length = "[t" + old_k + " + 0] = " + k3 + "\n"; 
        String set_zero = k1 + " = 0";
        //this does all of the array allocation / length 
        String result = expression_result + a1 + a2 + k1_allocation + k2_allocation + k_allocation + set_zero + "\n" + arr_length;
        //now I need to add default allocation to 0

        for(int i = 1; i <= sz; i++) {
            result += "[t" + (old_k) + " + " + (i * 4) + "] = " + k1 + "\n"; 
        }
        
        
        return result;
    }

    @Override 
    public String visit(ArrayLookup n, Context c) {
        int old_k = k;
        k = k + 11;

        String expression1 = n.f0.accept(this, c);

        int e1k = k;
        String expression2 = n.f2.accept(this, c);

        String t1 = "t" + (old_k + 1);
        String t2 = "t" + (old_k + 2);
        String t0 = "t" + old_k;
        String t3 = "t" + (old_k + 3);
        String t4 = "t" + (old_k + 4);
        String t5 = "t" + (old_k + 5);
        String t6 = "t" + (old_k + 6);
        String t7 = "t" + (old_k + 7);
        String t8 = "t" + (old_k + 8);
        String t9 = "t" + (old_k + 9);
        String t10 = "t" + (old_k + 10);
        String t11 = "t" + (old_k + 11);

        String tk = "t" + e1k;

        String e3 = t3 + " = " + t11 + " + " + t2 + "\n";
        t1 = t1 + " = t" + e1k + " + " + t9 + "\n";
        t2 = t2 + " = t" + (old_k + 1) + " * " + t10 + "\n";
        String assignt9 = t9 + " = " + 1 + "\n";
        String assignt10 = t10 + " = " + 4 + "\n";
        
        //String array_bounds0 = t8 + " = " + 
        String array_bounds1 = t4 + " = " + "1\n";
        String array_bounds11 = t8 + " = " + "2\n";
        String array_bounds12 = t8 + " = " + t4 + " - " + t8 + "\n";
        String array_bounds2 = t5 + " = " + t8 + " < " + tk + "\n";
        String array_bounds3 = "if0 " + t5 + " goto Error" + old_k + "\n";
        String array_bounds4 = t6 + " = " + "[" + t11 + " + 0]\n";
        String array_bounds5 = t7 + " = " + tk + " < " + t6 + "\n";
        String array_bounds6 = "if0 " + t7 + " goto Error" + old_k + "\n";
        String arraybounds = array_bounds1 + array_bounds11 + array_bounds12 + array_bounds2 + array_bounds3 + array_bounds4 + array_bounds5 +array_bounds6;
        //String rest_label = "Code" + 

        t0 = t0 + " = [" + t3 + " + 0]\n";
        String ret_label = "end" + old_k + "\n";
        String result = expression1 + expression2 + assignt9 + assignt10 + t1 + t2  + e3 + arraybounds + t0 + "goto " + ret_label + "Error" + old_k + ":\n error(\"array index out of bounds"  + "\")\n"
            + "end" + old_k + ":\n";

        return result;
    }

    @Override 
    public String visit(ArrayAssignmentStatement n, Context c) {
        //id[e2] = e3
        //e2
        //e3
        //code for the rest 
        //if the array is a class variable, need to change this

        int old_k = k;
        k = k+10;
        String ak = "t" + k; //this is the offset actually
        //i need to get the actual array by resolving the array itself
        String e0 = n.f0.accept(this, c);
        int k_after = k;
        String e1 = n.f2.accept(this, c);
        int k2 = k;
        String e2 = n.f5.accept(this, c);
        
        //this is what i was looking for i guess, we cant assign to just the name, have to see if its in class scope! 
        String t1 = "t" + (old_k + 1);
        String t2 = "t" + (old_k + 2);
        String t3 = "t" + (old_k + 3);
        String t4 = "t" + (old_k + 4);
        String t5 = "t" + (old_k + 5);
        String t6 = "t" + (old_k + 6);
        String t7 = "t" + (old_k + 7);
        String t8 = "t" + (old_k + 8);
        String t9 = "t" + (old_k + 9);
        String t10 = "t" + (k_after);
        String tk = "t" + k2;
        String t11 = "t" + k;
        k++;
       // String t11 = "t" + k;
        //k++;
        String s11 = t9 + " = 1\n";
        String s1 = t1 + " = " + t10 + " + " + t9 + "\n";//1 \n";
        String s12 = t9 + " = 4\n";
        String s2 = t2 + " = " + t1 + " * " + t9 + "\n";//4 \n";
        //String s4 = t11 + " = ";
        //how need to check to see if the array is in the c
        String s3 = t3 + " = " + ak + " + " + t2 + "\n";
        String arraybounds0 = t11 + " = [" + ak + " + 0]\n"; 
        String array_bounds1 = t4 + " = " + "1\n";
        String array_bounds11 = t8 + " = 2\n"; 
        String array_bounds12 = t8 + " = " + t4 + " - " + t8 + "\n";
        String array_bounds2 = t5 + " = " + t8 + " < " + tk + "\n";
        String array_bounds3 = "if0 " + t5 + " goto Error" + old_k + "\n";
        String array_bounds4 = t6 + " = " + "[" + ak + " + 0]\n"; //is token image ok or do i need to change this?
        String array_bounds5 = t7 + " = " + t10+ " < " + t11 + "\n";
        String array_bounds6 = "if0 " + t7 + " goto Error" + old_k + "\n";
        String arraybounds = arraybounds0 + array_bounds1 + array_bounds11 + array_bounds12 + array_bounds2 + array_bounds3 + array_bounds4 + array_bounds5 +array_bounds6;

        String s4 = "[" + t3 + " + 0]" + " = t" + k2 + "\n";
        String ret_label = "end" + old_k + "\n";
        String result =  e0 + e1  + e2 + s11 + s1 + s12 + s2 + s3 + arraybounds + s4 + "goto " + ret_label + "Error" + old_k + ":\n error(\"array index out of bounds"  + "\")\n"
            + "end" + old_k + ":\n";
        return result;
    }

    @Override
    public String visit(ArrayLength n, Context c) {
        int old_k = k;
        k++;
        String e = n.f0.accept(this, c);
        String rest = "t" + old_k + " = [t" + (old_k + 1) + " + 0]\n";
        String result = e + rest;
        return result;
    }

    @Override
    public String visit(VarDeclaration n, Context c) {
        String adder = "";
        if(current_state == State.CLASS) {

            class_to_parameters.get(current_class).add(n.f1.f0.tokenImage);
            //have to map class member variables to type then should be gtg!
            String tm = current_class + "-";
            tm += n.f1.f0.tokenImage;
            int indexx = c.class_to_parameters.get(current_class).indexOf(tm);
            //System.out.println(c.class_param_type.get(current_class).get(indexx));
            String tp = c.class_param_type.get(current_class).get(indexx);
            //System.out.println(c.class_to_parameters.get(current_class).get(current_class + "-" + n.f1.f0.tokenImage));
            //System.out.println(n.f1.f0.tokenImage);
            id_to_type.put(n.f1.f0.tokenImage, tp);
            
        } else if (current_state == State.METHOD) {
            
            c.method_to_parameters.get(current_method).add(n.f1.f0.tokenImage);
            String tmp_method = current_method.substring(current_method.indexOf("_") + 1);
            adder = current_class + "_";//.substring(0, current_class.indexOf("_")) + "_";
            id_to_type.put(n.f1.f0.tokenImage, c.class_method_parameter.get(current_class).get(tmp_method+"-"+n.f1.f0.tokenImage));
            //add each local variable to the local method scope object
        } else if (current_state == State.MAIN) {
            //id_to_type.put(n.f1.f0.tokenImage
            //System.out.println(c.class_method_parameter.get("Main").get("Main-" + n.f1.f0.tokenImage));
            adder = "Main_";
            String type = c.class_method_parameter.get("Main").get("Main-" + n.f1.f0.tokenImage);
            //String tmp_method = current_method.substring(current_method.indexOf("_") + 1);
            id_to_type.put(n.f1.f0.tokenImage, type);
            
        }
        //should just need to create the mapping as there is no need to convert declarations to 
        //sparrow as it will be handled within the expression?
        return adder + n.f1.f0.tokenImage + " = 0\n" + n.f1.accept(this, c);
    }
    @Override 
    public String visit(ClassDeclaration n, Context c) {
        
        current_state = State.CLASS;
        current_class = n.f1.f0.tokenImage;
        String result = "\n";

        //initialize the mapping from each class to their method / parameters
        class_to_methods.put(current_class, new ArrayList<String>());
        class_to_parameters.put(current_class, new ArrayList<String>());
        //f0 -> class 
        //f1 -> identifier 
        //f3 -> var declaration 
        //f4 -> method declaration
        //will basically just have to build a list of methods and local variables in each class
        //so that when we create each object we can make the array structure to represent the current class 
        String methods = "";
        String throwaway = "";
        for(int i = 0; i < n.f3.size(); i++) {
            throwaway += n.f3.elementAt(i).accept(this, c);
        }

        for(int i = 0; i < n.f4.size(); i++) {
            methods += n.f4.elementAt(i).accept(this, c);
        }
        
        return (result +methods);
    }

    @Override 
    public String visit(ClassExtendsDeclaration n, Context c) {
        current_state = State.CLASS; 
        current_class = n.f1.f0.tokenImage;

        String result = "\n";

        class_to_methods.put(current_class, new ArrayList<String>());
        class_to_parameters.put(current_class, new ArrayList<String>());

        String throwaway = "";
        for(int i = 0; i < n.f5.size(); i++) {
            throwaway += n.f5.elementAt(i).accept(this, c);
        }
        //System.out.println("AAA");
        String methods = "";
        for(int i = 0; i < n.f6.size(); i++) {
            methods += n.f6.elementAt(i).accept(this, c);
        }
        return (result +methods);
    }

    @Override 
    public String visit(FormalParameter n, Context c) {
        //String idk = n.f0.accept(this, c);
        //String type = n.f0.accept(this, c);
        String parameter = n.f1.f0.tokenImage;
        id_to_type.put(parameter, c.class_method_parameter.get(current_class).get(current_method + "-" + parameter));
        class_to_parameters.get(current_class).add(parameter);
        return current_class + "_" + parameter;
    } 

    @Override
    public String visit(FormalParameterRest n, Context c) {
        return n.f1.accept(this, c);
    }

    @Override 
    public String visit(FormalParameterList n, Context c) {
        String parameter_list = "";
        String current_parameter = " " + n.f0.accept(this, c);
        parameter_list += current_parameter;
        if(n.f1.present()) {
            for(int i = 0; i < n.f1.size(); i++) {
                parameter_list += (" " + n.f1.elementAt(i).accept(this, c));
            }
        }
        return parameter_list;
    }

    @Override
    public String visit(MainClass n, Context c) {
        current_class = "Main";
        current_class = "Main";
        current_state = State.CLASS;
        
        String function_declaration =   
            "func Main() \n"; 
        
        String vars = "";
        current_state = State.MAIN;
        if(n.f14.present()) {
            for(int i = 0; i < n.f14.size(); i++) {
                vars += n.f14.elementAt(i).accept(this, c);
            }
        }
        current_state = State.CLASS;

        String exps = "";
        if(n.f15.present()) {
            for(int i = 0; i < n.f15.size(); i++) {
                exps += n.f15.elementAt(i).accept(this, c);
            }
        }
        
        String ret_label = "main_end";
        String error_label = "\ngoto " + ret_label + "\n"; //+ "Error:\n error(\"error!\")\n";
        String return_statement = "t" + k + " = " + 1 + error_label + ret_label + ":\n" + "return t" + k + "\n";
        k++;
        String result = function_declaration + vars + exps + return_statement;
        return result;

    }


    @Override
    public String visit(Type n, Context c) {
        n.f0.accept(this, c);
        String ret = "";
        return ret;
    }

    @Override 
    public String visit(ArrayType n, Context c) {
        allocated_type = "default";
        return "";
    }

    @Override 
    public String visit(BooleanType n, Context c) {
        allocated_type = "default";
        return "";
    }

    @Override 
    public String visit(IntegerType n, Context c) {
        allocated_type = "default";
        return "";
    }

    // @Override 
    // public String visit(FormalParameter n, Context c) {
    //     id_to_type.put(n.f1.f0.tokenImage, "a");
    //     return "";
    // }

    // @Override 
    // public String visit(FormalParameterList n, Context c) {
    //     String a = n.f0.accept(this, c);
    //     for(int i = 0; i < n.f1.size(); i++) {
    //         String b = n.f1.elementAt(i).accept(this, c);
    //     }
    //     return "";
    // }

    @Override
    public String visit(MethodDeclaration n, Context c) {
        
        //f2 -> identifier 
        //f4 -> parameter list 
        //f7 -> variable declaration 
        //f8 -> statements 
        //f10 -> return expression -> return id that the return expression is evaluated to 
         //update the current state
        String method_name = n.f2.f0.tokenImage;
        // if(n.f4.present()) {
        //     n.f4.accept(this, c);
        // }
        class_to_methods.get(current_class).add(method_name);
        current_method = method_name;
        String parameter_list = "";
        current_state = State.OTHER;
        if(n.f4.present()) {
            parameter_list = n.f4.accept(this, c);
        }
        current_method = current_class + "_" + method_name;
        current_state = State.METHOD;
        ArrayList<String> tmp_parameters;
        if(parameter_list.equals("")) {
            tmp_parameters = new ArrayList<String>();
        } else {
            tmp_parameters = new ArrayList<String>(Arrays.asList(parameter_list.substring(1).split(" ")));
        }

        for(int i = 0; i < tmp_parameters.size(); i++) {
            String tmp = tmp_parameters.get(i);
            if(tmp.indexOf("_") != -1) {
                tmp = tmp.substring(tmp.indexOf("_") + 1);
                tmp_parameters.set(i, tmp);
            }
        }

        c.method_to_parameters.put(current_class + "_" + method_name, tmp_parameters);
        String function_declaration = "func " + current_class + "_" + method_name + "(this" + parameter_list + ")\n";
        String function_body = ""; //n.f7.accept(this) + n.f8.accept(this); have to iterate over both of these i think
        String vars = "";
        for(int i = 0; i < n.f7.size(); i++) {
            current_state = State.METHOD;
            vars += n.f7.elementAt(i).accept(this, c);
        }
        
        String exprs = "";
        /*
        for each of the expressions, i need to check to see if they correspond to any of the member variables, 
        so keep a list of all of the ids that have been locally initialized / parameters
        */  

        for(int i = 0; i < n.f8.size(); i++) {
            current_state = State.METHOD;

            exprs += n.f8.elementAt(i).accept(this, c);
            
        }
        
        function_body += (vars + exprs);
        int old_k = k;
        String ret_label = n.f2.f0.tokenImage + "_end";
        String error_label = "goto " + ret_label + "\n"; //+ "Error:\n error(\"error!\")\n";
        String return_statement = n.f10.accept(this, c) + error_label + ret_label + ":\n" + "return t" + old_k + "\n\n";
        current_state = State.METHOD;
        String result = function_declaration + function_body + return_statement;
        
        return result;
    }

    /*
    to finish this i have to work on class extending 
    i have to add the right class name which i will take care of in the other visitor
    after doing so i need to add the parameters, but i need to see if they have to be loaded 
    into temporaries first 
    */

    @Override 
    public String visit(AllocationExpression n, Context c) {
        //we know this is a class allocation
        String result = "";
        //f1 -> identifier 
        allocated_type = n.f1.f0.tokenImage;
        String id_name = n.f1.f0.tokenImage; // we will use this to access into our mappings
        //create a variable to store how many items we need to allocate
        //so we need to check how many member variables and how many functions the class has. 
        //for now do not worry about inheritance, just assume it isnt a thing, add that in later 
        int num_vars = 0; 
        int num_methods = 0; 
        for(int i = 0; i < c.class_to_methods.get(id_name).size(); i++) {
            num_methods++;
        }
        for(int i = 0; i < c.class_to_parameters.get(id_name).size(); i++) {
            num_vars++;
        }

        int num_class_items = 0;
        num_class_items += num_vars;

        if(num_methods > 0)
            num_class_items += 1;
        
        String k0 = "t" + (k + 0);
        String k1 = "t" + (k + 1);
        String k2 = "t" + (k + 2);
        String k3 = "t" + (k + 3);
        String k4 = "t" + (k + 4);
        k = k + 5;
        //alloc fields + 1
        String class_length = k1 + " = " + (num_class_items * 4) + "\n";
        String class_list = k0 + " = " + "alloc(" + k1 + ")\n";
        //add its parameters - do this later
        //alloc method array
        String method_length = k3 + " = " + (num_methods * 4) + "\n";
        String method_list = k4 + " = " + "alloc(" + k3 + ")\n";

        String method_allocation = "";
        String current_allocation = "";
        for(int i = 0; i < c.class_to_methods.get(id_name).size(); i++) {
            current_allocation += ("t" + (k + i) + " = " + "@" + c.class_to_methods.get(id_name).get(i) + "\n");
            method_allocation += "[" + k4  + " + " + (i*4) + "] = " + "t" + (i+k) + "\n";
            //k++;
        }
        k += c.class_to_methods.get(id_name).size();
        //add its parameters - do this later
        //link the two 
        String link = "[" + k0 + " + 0] = " + k4 + "\n";  
        id_to_type.put(k0, n.f1.f0.tokenImage);
        result += (class_length + class_list + method_length + method_list + current_allocation + method_allocation + link);
        return result;
    }

    @Override 
    public String visit(ExpressionList n, Context c) {
        String result;
        int oldk = k;
        String expr = n.f0.accept(this, c);
        String parameters = "this t" + oldk;

        for(int i = 0; i < n.f1.size(); i++) {
            int tmpk = k;
            String tmp = n.f1.elementAt(i).accept(this, c);
            parameters += (" " + "t" + tmpk);
            expr += tmp;
        }
        result = (expr + parameters);
        return result;
    }

    @Override 
    public String visit(ExpressionRest n, Context c) {
        String result = n.f1.accept(this, c);
        return result;
    }

    @Override 
    public String visit(MessageSend n, Context c) {
        
        String result = "";
        //need to resolve the primary expression first 
        //need to set allocated type if we chain message sends together 
        int originalk = k;
        k += 2;
        int oldk = k;
        String declarations = "";
        State prev_state = current_state;
        String class_type;
        //whatever is in toldk is the id i want to use, i just need to get its type now
        String primary_expr = n.f0.accept(this, c);
        //System.out.println("!!");
        String id_primary = primary_expr.substring(primary_expr.indexOf("_") + 1, primary_expr.length() - 1);

        arr_access = "t" + (originalk + 2);
        String cur_type = current_class;
        if(id_to_type.containsKey(allocated_type)) {
            
            allocated_type = id_to_type.get(allocated_type);
        }

        //time to get method name!
        int method_k = k; //this is the id of the method
        String oldat = allocated_type;
        String method_name = n.f2.accept(this, c);
        String method_reg = method_name.substring(0, method_name.indexOf("=") - 1);
        allocated_type = oldat;
        method_name = method_name.substring(method_name.indexOf("=") + 2, method_name.length() - 1);
        method_name = n.f2.f0.tokenImage;
        int index = 0;
        if(id_to_type.containsKey(allocated_type)) {
            allocated_type = id_to_type.get(allocated_type);
        }
        //System.out.println(allocated_type);
        //System.out.println("-----");
        //System.out.println(allocated_type);
        //System.out.println(method_name);
        
        for(int i = 0; i < c.class_to_methods.get(allocated_type).size(); i++) {

            int dashIndex = c.class_to_methods.get(allocated_type).get(i).indexOf("_") + 1;

            if(c.class_to_methods.get(allocated_type).get(i).substring(dashIndex).equals(method_name)) {
                break;
            }
            index++;
        }
        
        //have the right index, now we need to develop the table we are going to grab info from 
        String t_table = "t" + k;
        String t_access = "t" + (k+1);
        k += 2;
        //need to do a nullptr check here when I access the table
        //need to check to see if the id im calling on is allocated yet or not
        String null_check = "if0 t" + oldk + " goto null" + oldk + "\n";
        String null_label = "null" + oldk + ":\nerror(\"null pointer\")\n";
        String null_jump = "goto null_end"+oldk + "\n";
        String null_end = "null_end" + oldk + ":\n";
        String get_table = t_table + " = [t" + oldk + " + 0]\n";
        String get_func = t_access + " = [" + t_table + " + " + (index * 4) + "]\n";
        String together = primary_expr + null_check + get_table + get_func + null_jump + null_label + null_end; //this gives me the function to call, now its time to call it!

        String expr_list = "";
        String parameters = "";
        if(n.f4.present())
            expr_list = n.f4.accept(this, c);
        
        if(!expr_list.equals("")) {
            parameters += " " + expr_list.substring(expr_list.indexOf("this t") + 5);
        }

        //String parameters = "t" + oldk +  " " + expr_list.substring(expr_list.indexOf("this") + 5);
        
        if(!expr_list.equals("")) {
            expr_list = expr_list.substring(0, expr_list.indexOf("this t"));
        }

        together += expr_list;
        String call_function = "t" + originalk + " = call " + t_access + "(t" + oldk + parameters + ")\n";
        together += call_function;
        
        //System.out.println("***");
        //System.out.println(allocated_type + "_" + method_name);
        //System.out.println()
        if(c.method_to_type.containsKey(allocated_type + "_" + method_name)) {
            allocated_type = c.method_to_type.get(allocated_type + "_" + method_name);
            if(allocated_type.equals("default"))
                allocated_type = "";
        }

        return together;
    }

    @Override
    public String visit(ThisExpression n, Context c) {
        String tk = "t" + k + " = this\n";
        allocated_type = current_class;
        k++;
        return tk;
    }

}