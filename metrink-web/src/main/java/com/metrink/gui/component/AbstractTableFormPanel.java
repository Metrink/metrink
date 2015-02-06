package com.metrink.gui.component;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sop4j.dbutils.EntityUtils;
import com.sop4j.dbutils.QueryRunner;

/**
 * An abstract panel that has a table and form.
 *
 * @param <T> the type of the object the form represents.
 */
public abstract class AbstractTableFormPanel<S extends Serializable, T extends Serializable> extends Panel {
    private static final long serialVersionUID = -3136579068845135299L;
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTableFormPanel.class);

    private final Class<T> beanClass;
    private IModel<T> formModel;
    private Form<T> form;
    private Button updateSaveButton;

    /**
     * Constructor.
     * @param beanClass the class of the type objects stored in the form.
     */
    public AbstractTableFormPanel(final String id,
                                  final Class<T> beanClass,
                                  final QueryRunner queryRunner) {
        super(id);
        this.beanClass = beanClass;

        try {
            this.formModel = Model.of(createNewBean());
        } catch (final InstantiationException e) {
            LOG.error("Error creating new bean {}: {}", beanClass, e.getMessage());
            throw new WicketRuntimeException("Error creating new bean");
        }

        form = new Form<T>("form", this.formModel) {
            private static final long serialVersionUID = -3564662582656203263L;

            @Override
            protected void onSubmit() {
                final T bean = getModelObject();

                // decide if we're creating a new one or updating
                if(EntityUtils.getId(beanClass, bean) == null) {
                    try {
                        queryRunner.create(beanClass, bean);
                        LOG.debug("{} created", bean);
                        getSession().success(getCreatedMessage(bean));
                    } catch (SQLException e) {
                        LOG.error("Erorr creating bean: {}", e.getMessage());
                        getSession().error("Error communicating with database");
                    }
                } else { // updating
                    try {
                        queryRunner.update(beanClass, bean);
                        LOG.debug("{} updated", bean);
                        getSession().success(getUpdatedMessage(bean));
                    } catch (SQLException e) {
                        LOG.error("Erorr updating bean: {}", e.getMessage());
                        getSession().error("Error communicating with database");
                    }
                }

                // get any page params
                final PageParameters params = getRedirectPageParameters();

                // just redirect to ourself
                this.setResponsePage(AbstractTableFormPanel.this.getPage().getClass(), params);
            }
        };

        // add in all the form's components
        addFormComponents(form);

        updateSaveButton = new Button("update-button", Model.of("New"));
        form.add(updateSaveButton);

        form.add(new AjaxButton("cancel-button", form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(final AjaxRequestTarget target, final Form<?> submitForm) {
                LOG.debug("Clearing form");

                // set the model to a new object
                try {
                    formModel.setObject(createNewBean());
                } catch (final InstantiationException e) {
                    LOG.error("Error creating new bean: {}", e.getMessage());
                    getSession().error("Error creating new bean: " + e.getMessage());

                    AbstractTableFormPanel.this.setResponsePage(AbstractTableFormPanel.this.getPage().getClass());
                    return;
                }

                form.modelChanged();
                form.clearInput();

                target.add(form);

                // update the button
                updateSaveButton.setModel(Model.of("New"));
                target.add(updateSaveButton);
            }
        }.setDefaultFormProcessing(false));

        add(form);
    }

    /**
     * Callback used to add all the components of the form.
     * @param form the form to add components to.
     */
    protected abstract void addFormComponents(final Form<T> form);

    protected abstract String getCreatedMessage(final T bean);

    protected abstract String getUpdatedMessage(final T bean);

    /**
     * Returns the form's model.
     * @return the form's model.
     */
    protected IModel<T> getFormModel() {
        return formModel;
    }

    /**
     * Allows extending classes the ability to inject any page parameters for the redirect.
     * @return blank {@link PageParameters}.
     */
    protected PageParameters getRedirectPageParameters() {
        return new PageParameters();
    }

    /**
     * Creates a new bean.
     * @return a new bean.
     * @throws InstantiationException if anything goes wrong.
     */
    protected T createNewBean() throws InstantiationException {
        try {
            return beanClass.getConstructor().newInstance();
        } catch (final InstantiationException |
                 IllegalAccessException |
                 IllegalArgumentException |
                 InvocationTargetException |
                 NoSuchMethodException |
                 SecurityException e) {
            // fold it all into one type of exception
            throw new InstantiationException(e.getMessage());
        }
    }

    /**
     * Sets the form to the bean passed in, updating the form via AJAX.
     * @param bean the bean to set the form to.
     * @param target the target of whatever triggers the form fill action.
     */
    protected void setForm(final T bean, final AjaxRequestTarget target) {
        LOG.debug("Setting form to {}", bean);

        // update the model with the new bean
        formModel.setObject(bean);

        // add the form to the target
        target.add(form);

        // update the button
        updateSaveButton.setModel(Model.of("Update"));
        target.add(updateSaveButton);
    }

}
