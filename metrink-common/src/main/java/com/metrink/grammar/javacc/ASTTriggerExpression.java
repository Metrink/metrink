/* Generated By:JJTree: Do not edit this line. ASTTriggerExpression.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.metrink.grammar.javacc;

public
class ASTTriggerExpression extends com.metrink.grammar.BaseNode {
  public ASTTriggerExpression(int id) {
    super(id);
  }

  public ASTTriggerExpression(MetrinkParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public com.metrink.grammar.ASTWalker.WalkType jjtAccept(MetrinkParserVisitor visitor, Boolean data) throws com.metrink.grammar.MetrinkParseException {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=3e02ca3f80741fe8b9a2bb6fe9583979 (do not edit this line) */
