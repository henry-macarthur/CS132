import java.io.*; 
import java.util.*; 

public class Parse { 
    private static int token = 0;  //give us the token index
    private static Set<String> terminals = new HashSet<String>();
    private static Set<String> ref_terminals = new HashSet<String>();
    private static Set<String> non_terminals = new HashSet<String>();
    private static ArrayList<String> tokens = new ArrayList<String>();

    public static void load_terminals() {
        terminals.add("\\{");
        terminals.add("\\}");
        terminals.add("System.out.println");
        terminals.add("\\(");
        terminals.add("\\)");
        terminals.add("\\;");
        terminals.add("if");
        terminals.add("else");
        terminals.add("while");
        terminals.add("true");
        terminals.add("false");
        terminals.add("!");
        ref_terminals.add("{");
        ref_terminals.add("}");
        ref_terminals.add("System.out.println");
        ref_terminals.add("(");
        ref_terminals.add(")");
        ref_terminals.add(";");
        ref_terminals.add("if");
        ref_terminals.add("else");
        ref_terminals.add("while");
        ref_terminals.add("true");
        ref_terminals.add("false");
        ref_terminals.add("!");
    }

    public static void load_non_terminals() { 
        non_terminals.add("S ");
        non_terminals.add("L");
        non_terminals.add("E");
    }

    public static void eat(String a) {
        if(tokens.get(token).equals(a)) {
            token++;
        } else {
            //error!
            handle_error("");
        }
    }

    public static void handle_error(String s) {
        System.out.println("Parse error");
        System.exit(0);
    }
    public static void handle_L() {
        if(handle_start()) { //check wether we are at the last element 
            handle_L();
        } else {}
    }

    public static void handle_E() {
        if(tokens.get(token).equals("true")) {
            eat("true");
        } else if(tokens.get(token).equals("false")) {
            eat("false");
        } else if(tokens.get(token).equals("!")) {
            eat("!");
            handle_E();
        } else {
            handle_error("E");
        }
    }

    public static boolean handle_start() {
        if(tokens.get(token).equals("{")) {
            eat("{"); 
            handle_L();
            eat("}");
        } else if(tokens.get(token).equals("System.out.println")) {
            eat("System.out.println");
            eat("(");
            handle_E();
            eat(")");
            eat(";");
        } else if(tokens.get(token).equals("if")) {
            eat("if");
            eat("(");
            handle_E();
            eat(")");
            if(!handle_start()) handle_error("");
            eat("else");
            handle_start();
        } else if(tokens.get(token).equals("while")) {
            eat("while");
            eat("(");
            handle_E();
            eat(")");
            if(!handle_start()) handle_error("");
        } else {
            //error!
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        //load predefined term and non_terms
        load_terminals();
        load_non_terminals();
        //read input into a string
        Scanner sc = new Scanner(System.in);
        String input = new String();
        
        while(sc.hasNextLine()) {
            String s = sc.nextLine().trim();
            for(String item : terminals) {
                String tmp = item + " ";
                s = s.replaceAll(item, tmp);
            }
            for(String item : non_terminals) {
                String tmp = item + " ";
                s = s.replaceAll(item, tmp);
            }
            s = s.replaceAll("\\s{2,}", " ").trim();
            String[] cur_tokens = s.split(" ");
            for(int i = 0; i < cur_tokens.length; i++) {
                if(non_terminals.contains(cur_tokens[i]) || ref_terminals.contains(cur_tokens[i])) {
                    tokens.add(cur_tokens[i]);
                } else {
                    //throw an error and stop since we have an invalid token
                    handle_error("");
                    return;
                }
            }
        } //gives us an array of tokens, checks the validity as well
        if(!handle_start() || token != tokens.size()) //start parsing
            handle_error("");
        System.out.println("Program parsed successfully");
    }
}