package com.metrink.gui;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.value.IValueMap;

public class StyleModifier extends Behavior implements IClusterable {

    private static final long serialVersionUID = 1L;
    
    private static final String ATTRIBUTE = "style";
    private final String property;
    private final String value;

    /**
     * Updates or adds the given property to the style attribute.
     * @param property the CSS property to modify.
     * @param value the CSS value to add/replace.
     */
    public StyleModifier(final String property, final String value) {
        this.property = property;
        this.value = value;
    }
    
    /**
     * Removes a given property from the style attribute.
     * @param property the CSS property to remove.
     */
    public StyleModifier(final String property) {
        this(property, null);
    }
    
    @Override
    public final void onComponentTag(Component component, ComponentTag tag)
    {
        if(tag.getType() != TagType.CLOSE && isEnabled(component)) {
            final IValueMap attributes = tag.getAttributes();
            final String styleAttribute = attributes.getString(ATTRIBUTE);
            
            if(styleAttribute != null) {
                // need to parse the style attribute
                final String[] propVals = styleAttribute.split(" *; *");
                final StringBuffer newValue = new StringBuffer(styleAttribute.length());
                boolean added = false;
                
                for(String pv:propVals) {
                    if(pv.startsWith(property)) {
                        if(value != null) {
                            newValue.append(property);
                            newValue.append(": ");
                            newValue.append(value);
                            newValue.append(";");
                            added = true;
                        }
                    } else {
                        newValue.append(pv);
                        newValue.append(";");
                    }
                }

                // if we're looking to add the property
                // but it wasn't found, then add it
                if(value != null && !added) {
                    newValue.append(property);
                    newValue.append(": ");
                    newValue.append(value);
                    newValue.append(";");
                }

                attributes.put(ATTRIBUTE, newValue.toString());
            } else {
                if(value != null) {
                    // just need to add it
                    attributes.put(ATTRIBUTE, property + ": " + value + ";");
                }
            }
        }
    }
}
