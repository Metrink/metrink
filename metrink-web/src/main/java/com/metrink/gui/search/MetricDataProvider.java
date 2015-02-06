package com.metrink.gui.search;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.metrink.metric.MetricId;
import com.metrink.utils.DeserializationUtils;
import com.sop4j.dbutils.EntityUtils;
import com.sop4j.dbutils.QueryExecutor;
import com.sop4j.dbutils.QueryRunner;
import com.sop4j.dbutils.handlers.BeanListHandler;
import com.sop4j.dbutils.handlers.ScalarHandler;

public class MetricDataProvider extends SortableDataProvider<MetricId, String> {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(MetricDataProvider.class);

    @Inject private transient QueryRunner queryRunner;
    private final SortParam<String> defaultSort;
    private final String search;

    @Inject
    public MetricDataProvider(final QueryRunner queryRunner,
                              @Assisted final SortParam<String> defaultSort,
                              @Nullable @Assisted final String search) {
        this.queryRunner = queryRunner;
        this.defaultSort = defaultSort;

        // we want to escape any % they have, then convert any * into a % as well
        this.search = search == null ? null : "%" + search.replace("%", "\\%").replace("*", "%") + "%";
    }

    public List<? extends MetricId> fetchItemList(final long first, final long count) {
        final StringBuilder sb = new StringBuilder("select * from ");
        final SortParam<String> param = getSort() != null ? getSort() : defaultSort;

        sb.append(EntityUtils.getTableName(MetricId.class));

        if(search != null) {
            sb.append(" where device like :search or groupName like :search or name like :search ");
        }

        sb.append(" order by ");
        sb.append(param.getProperty());
        sb.append(param.isAscending() ? " " : " desc ");
        sb.append("limit ");
        sb.append(first);
        sb.append(", ");
        sb.append(count);

        try {
            final QueryExecutor exec = queryRunner.query(sb.toString());

            if(search != null) {
                exec.bind("search", search);
            }

            return exec.execute(new BeanListHandler<MetricId>(MetricId.class));
        } catch(final SQLException e) {
            LOG.error("Error reading metric IDs: {}", e);
            throw new WicketRuntimeException(e);
        }
    }

    @Override
    public IModel<MetricId> model(final MetricId arg) {
        return new Model<MetricId>(arg);
    }

    @Override
    public Iterator<? extends MetricId> iterator(final long first, final long count) {
        return fetchItemList(first, count).iterator(); // need to fix this
    }

    @Override
    public long size() {
        try {
        if(search == null) {
            return queryRunner
                    .query("select count(*) from " + EntityUtils.getTableName(MetricId.class))
                    .execute(new ScalarHandler<Long>());
        } else {
            return queryRunner
                    .query("select count(*) from " + EntityUtils.getTableName(MetricId.class) + "where device like :device or groupName like :group or name like :name")
                    .bind("device", search)
                    .bind("group", search)
                    .bind("name", search)
                    .execute(new ScalarHandler<Long>());
        }
        } catch(final SQLException e) {
            LOG.error("Error getting count of metric_owenrs: {}", e.getMessage());
            throw new WicketRuntimeException(e);
        }
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        DeserializationUtils.readObject(in, this);
    }

    public static interface MetricDataProviderFactory {

        public MetricDataProvider create(final SortParam<String> defaultSort, final String search);
    }

}
