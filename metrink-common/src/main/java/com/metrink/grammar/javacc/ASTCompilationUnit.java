/* Generated By:JJTree: Do not edit this line. ASTCompilationUnit.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.metrink.grammar.javacc;

public
class ASTCompilationUnit extends com.metrink.grammar.BaseNode {
  public ASTCompilationUnit(int id) {
    super(id);
  }

  public ASTCompilationUnit(MetrinkParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public com.metrink.grammar.ASTWalker.WalkType jjtAccept(MetrinkParserVisitor visitor, Boolean data) throws com.metrink.grammar.MetrinkParseException {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=42c4cd084a7f72ec04d736970d8805e9 (do not edit this line) */