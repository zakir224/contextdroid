package main.java;

import soot.jimple.toolkits.callgraph.CallGraph;

import java.io.Serializable;

public class CallGraphSerializable implements Serializable {
    public CallGraphSerializable(CallGraph callGraph) {
        this.callGraph = callGraph;
    }

    CallGraph callGraph;
}
