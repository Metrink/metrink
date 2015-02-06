package com.metrink.grammar;

import com.metrink.grammar.javacc.MetrinkParser;
import com.metrink.grammar.javacc.SimpleNode;

public class BaseNode extends SimpleNode {

    private String image;

    public BaseNode(int i) {
        super(i);
    }

    public BaseNode(MetrinkParser p, int i) {
        super(p, i);
    }

    public String getImage() {
        return image;
    }

    public void setImage(final String image) {
        this.image = image;
    }
}
