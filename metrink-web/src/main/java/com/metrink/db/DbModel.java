package com.metrink.db;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Map;

import javax.persistence.EntityNotFoundException;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sop4j.dbutils.EntityUtils;
import com.sop4j.dbutils.QueryRunner;
import com.sop4j.dbutils.handlers.BeanHandler;

/**
 * A Wicket model that is backed by an SQL object.
 *
 * @param <T> the type of the entity.
 * @param <I> the type of the entity's ID.
 */
public class DbModel<T, I> implements IModel<T> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(DbModel.class);

    private final QueryRunner queryRunner;
    private final Class<T> clazz;
    private T entity;
    private I id;
    private final String idColumn;
    private final String selectStmt;

    protected DbModel(final QueryRunner queryRunner, final Class<T> entityType, final T entity) {
        this.queryRunner = queryRunner;
        this.clazz = entityType;
        this.entity = entity;
        this.id = EntityUtils.getId(clazz, entity);

        final Map<String, String> idColumns = EntityUtils.getIdColumns(entityType);
        idColumn = idColumns.keySet().toArray(new String[0])[0];

        selectStmt = MessageFormat.format("select * from {0} where {1} = :{1}", EntityUtils.getTableName(entityType), idColumn);
    }

    protected DbModel(final QueryRunner queryRunner, final Class<T> entityType, final I id, final String idColumn) throws SQLException {
        this.queryRunner = queryRunner;
        this.clazz = entityType;
        this.id = id;
        this.idColumn = idColumn;

        selectStmt = MessageFormat.format("select * from {0} where {1} = :{1}", EntityUtils.getTableName(entityType), idColumn);

        this.entity = queryRunner.query(selectStmt).bind(idColumn, id).execute(new BeanHandler<T>(entityType));
    }

    @Override
    public void detach() {
        // we have an entity and an id
        if(entity != null && EntityUtils.getId(clazz, entity) != null) {
            LOG.debug("Detatching entity {}", entity.toString());
            // so save the id and null out the entity
            id = EntityUtils.getId(clazz, entity);
            entity = null;
        }
    }

    @Override
    public T getObject() {
        // if we don't have an entity but do have an id, load it up
        if(entity == null && id != null) {
            try {
                LOG.debug("Loading entity from DB");
                entity = queryRunner.query(selectStmt).bind(idColumn, id).execute(new BeanHandler<T>(clazz));

                // if the load fails, then throw an exception
                if(null == entity) {
                    throw new EntityNotFoundException("Entity of type " + clazz +
                                                      " and id " + id + " could not be loaded");
                }
            } catch(SQLException e) {
                LOG.error("Error reading entity from DB: {}", e.getMessage());
                throw new WicketRuntimeException(e);
            }
        }

        return entity;
    }

    @Override
    public void setObject(final T entity) {
        if(EntityUtils.getId(clazz, entity) != null) {
            LOG.debug("Setting existing entity");
            this.id = EntityUtils.getId(clazz, entity);
            this.entity = null;
        } else {
            LOG.debug("Setting new entity");
            this.entity = entity;
            this.id = null;
        }
    }

    /**
     * Factory class for creating {@link DbModel}s.
     */
    public static class DbModelFactory implements Serializable {

        private static final long serialVersionUID = 1L;
        private final QueryRunner queryRunner;

        @Inject
        public DbModelFactory(final QueryRunner queryRunner) {
            this.queryRunner = queryRunner;
        }

        public <T, I> DbModel<T, I> create(final Class<T> entityType, final T entity) {
            return new DbModel<T, I>(queryRunner, entityType, entity);
        }

        public <T, I> DbModel<T, I> create(final Class<T> entityType, final I id, final String idColumn) throws SQLException {
            return new DbModel<T, I>(queryRunner, entityType, id, idColumn);
        }
    }

}
