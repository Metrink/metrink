/* Generated By:JJTree: Do not edit this line. ASTStringLiteral.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.metrink.grammar.javacc;

public
class ASTStringLiteral extends com.metrink.grammar.BaseNode {
  public ASTStringLiteral(int id) {
    super(id);
  }

  public ASTStringLiteral(MetrinkParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public com.metrink.grammar.ASTWalker.WalkType jjtAccept(MetrinkParserVisitor visitor, Boolean data) throws com.metrink.grammar.MetrinkParseException {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=e509175fe728e04e9d368784a3c5aed8 (do not edit this line) */
