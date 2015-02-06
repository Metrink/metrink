package com.metrink.gui.alert;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.metrink.alert.AlertBean;
import com.metrink.utils.DeserializationUtils;
import com.sop4j.dbutils.QueryRunner;
import com.sop4j.dbutils.handlers.BeanListHandler;
import com.sop4j.dbutils.handlers.ScalarHandler;

public class AlertDataProvider extends SortableDataProvider<AlertBean, String> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(AlertDataProvider.class);

    @Inject private transient QueryRunner queryRunner;

    @Inject
    public AlertDataProvider(final QueryRunner queryRunner) {
        this.queryRunner = queryRunner;
    }

    @Override
    public Iterator<? extends AlertBean> iterator(final long first, final long count) {
        List<AlertBean> ret = new ArrayList<AlertBean>();

        try {
            ret = queryRunner.query("select * from alerts limit :first,:count")
                             .bind("first", first)
                             .bind("count", count)
                             .execute(new BeanListHandler<AlertBean>(AlertBean.class));
        } catch (final SQLException e) {
            LOG.error("Error querying alert table", e);
        }

        return ret.iterator();
    }

    @Override
    public long size() {
        try {
            return queryRunner.query("select count(*) from alerts").execute(new ScalarHandler<Long>()).longValue();
        } catch (final SQLException e) {
            LOG.error("Error querying alert table: {}", e.getMessage());
        }

        return 0;
    }

    @Override
    public IModel<AlertBean> model(final AlertBean object) {
        return Model.of(object);
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        DeserializationUtils.readObject(in, this);
    }
}
