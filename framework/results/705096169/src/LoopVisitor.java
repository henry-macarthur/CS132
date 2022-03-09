// import cs132.IR.SparrowParser;
import cs132.IR.syntaxtree.*;
// import cs132.IR.sparrow.*;
// import cs132.IR.sparrow.visitor.*;
import cs132.IR.visitor.*;
//import cs132.IR.visitor.*;
import java.util.*; 

public class LoopVisitor extends cs132.IR.visitor.GJVoidDepthFirst<Context> { 

    @Override 
    public void visit(LabelWithColon n, Context c) {
        String label_name = n.f0.f0.tokenImage;
        int label_line = n.f0.f0.beginLine;

        c.set_label(label_name, label_line);
    }
    @Override
    public void visit(Goto n, Context c) {
        //need to find the line that this goto goes to 
        //this is where we see if we have a backward loop 
        String label = n.f1.f0.tokenImage;
        int end_line = n.f1.f0.beginLine;
        int begin_line = c.get_label_line(label);
        
        if(begin_line == -1) //this means that we dont have a back edge 
            return;

        ArrayList<Integer> interval = new ArrayList<Integer>();
        interval.add(begin_line); 
        interval.add(end_line); //[begin_line, end_line];
        c.intervals.add(interval);
        
    }

}