package org.glycoinfo.WURCSFramework.wurcsgraph.util.visitor;


public interface WURCSVisitable {
    public void accept (WURCSVisitor a_objVisitor) throws WURCSVisitorException;

}
