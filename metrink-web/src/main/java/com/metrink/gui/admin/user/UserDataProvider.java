package com.metrink.gui.admin.user;

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
import com.metrink.metric.User;
import com.metrink.utils.DeserializationUtils;
import com.sop4j.dbutils.EntityUtils;
import com.sop4j.dbutils.QueryExecutor;
import com.sop4j.dbutils.QueryRunner;
import com.sop4j.dbutils.handlers.BeanListHandler;
import com.sop4j.dbutils.handlers.ScalarHandler;

public class UserDataProvider extends SortableDataProvider<User, String> {

    private static final Logger LOG = LoggerFactory.getLogger(UserDataProvider.class);
    private static final long serialVersionUID = 1L;

    @Inject private transient QueryRunner queryRunner;
    private final SortParam<String> defaultSort;
    private final String search;

    @Inject
    public UserDataProvider(final QueryRunner queryRunner,
                            @Assisted final SortParam<String> defaultSort,
                            @Nullable @Assisted final String search) {
        this.queryRunner = queryRunner;
        this.defaultSort = defaultSort;

        // we want to escape any % they have, then convert any * into a % as well
        this.search = (search == null ? null : "%" + search.replace("%", "\\%").replace("*", "%") + "%");
    }

    public List<? extends User> fetchItemList(final long first, final long count) {
        final StringBuilder sb = new StringBuilder("select * from ");
        final SortParam<String> param = getSort() != null ? getSort() : defaultSort;

        sb.append(EntityUtils.getTableName(User.class));

        if(search != null) {
            sb.append(" where username like :search or name like :search");
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

            final List<User> ret = exec.execute(new BeanListHandler<User>(User.class));

            return ret;
        } catch(final SQLException e) {
            LOG.error("Error reading users: {}", e);
            throw new WicketRuntimeException(e);
        }
    }

    @Override
    public IModel<User> model(final User arg) {
        return new Model<User>(arg);
    }

    @Override
    public Iterator<? extends User> iterator(final long first, final long count) {
        return fetchItemList(first, count).iterator(); // need to fix this
    }

    @Override
    public long size() {
        final StringBuilder sb = new StringBuilder("select count(*) from ");

        sb.append(EntityUtils.getTableName(User.class));

        if(search != null) {
            sb.append(" where username like :search or name like :search ");
        }

        try {
            final QueryExecutor exec = queryRunner.query(sb.toString());

            if(search != null) {
                exec.bind("search", search);
            }

            return exec.execute(new ScalarHandler<Long>());
        } catch(final SQLException e) {
            LOG.error("Error reading users: {}", e);
            throw new WicketRuntimeException(e);
        }
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        DeserializationUtils.readObject(in, this);
    }

    public static interface UserDataProviderFactory {
        public UserDataProvider create(final SortParam<String> defaultSort, final String search);
    }

}
