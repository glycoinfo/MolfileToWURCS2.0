package org.glycoinfo.WURCSFramework.wurcsglycan.util.visitor;


public interface WURCSVisitable {
    public void accept (WURCSVisitor a_objVisitor) throws WURCSVisitorException;

}
