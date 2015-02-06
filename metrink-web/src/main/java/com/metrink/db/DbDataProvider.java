package com.metrink.db;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sop4j.dbutils.EntityUtils;
import com.sop4j.dbutils.QueryRunner;
import com.sop4j.dbutils.handlers.BeanListHandler;
import com.sop4j.dbutils.handlers.ScalarHandler;

public class DbDataProvider<T extends Serializable> extends SortableDataProvider<T, String> {

    private static final Logger LOG = LoggerFactory.getLogger(DbDataProvider.class);
    private static final long serialVersionUID = 1L;

    private final QueryRunner queryRunner;
    private final Class<T> type;
    private final SortParam<String> defaultSort;

    @Inject
    public DbDataProvider(final QueryRunner queryRunner,
                          @Assisted final Class<T> type,
                          @Assisted final SortParam<String> defaultSort) {
        this.queryRunner = queryRunner;
        this.type = type;
        this.defaultSort = defaultSort;
    }

    public List<?> fetchItemList(long first, long count) {
        final SortParam<String> param = getSort() == null ? defaultSort : getSort();
        final StringBuilder sb = new StringBuilder("select * from ");

        sb.append(EntityUtils.getTableName(type));
        sb.append(" order by ");
        sb.append(param.getProperty());
        sb.append(param.isAscending() ? " " : " desc ");
        sb.append("limit ");
        sb.append(first);
        sb.append(", ");
        sb.append(count);

        try {
            return queryRunner.query(sb.toString()).execute(new BeanListHandler<>(type));
        } catch (SQLException e) {
            LOG.error("Error reading from db: {}", e.getMessage());
            throw new WicketRuntimeException(e);
        }
    }

    @Override
    public IModel<T> model(T arg) {
        return new Model<T>(arg);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<? extends T> iterator(long first, long count) {
        return (Iterator<? extends T>)fetchItemList(first, count).iterator();
    }

    @Override
    public long size() {
        try {
            return queryRunner.query("select count(*) from " + EntityUtils.getTableName(type))
                              .execute(new ScalarHandler<Long>());
        } catch (SQLException e) {
            LOG.error("Error getting count from db");
            throw new WicketRuntimeException(e);
        }
    }

    public static class DbDataProviderFactory {
        private final QueryRunner queryRunner;

        @Inject
        public DbDataProviderFactory(final QueryRunner queryRunner) {
            this.queryRunner = queryRunner;
        }

        public <T extends Serializable> DbDataProvider<T> create(final Class<T> type, final SortParam<String> defaultSort) {
            return new DbDataProvider<T>(queryRunner, type, defaultSort);
        }
    }

}
