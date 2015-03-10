/*
 * Created on 08.05.2005
 *
 */
package net.sf.ojts.castor.persist.keygen;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.persist.spi.OJTSExtendedKeyGenerator;
import org.exolab.castor.persist.spi.PersistenceFactory;
import org.exolab.castor.persist.spi.QueryExpression;
import org.exolab.castor.util.Messages;

/**
 * @author cs
 *
 */
public class MaxOrKeepKeyGenerator implements OJTSExtendedKeyGenerator {
    
    protected static final BigDecimal ONE = new BigDecimal( 1 );
    protected static final int minimum_generated_key_ = 500000;
    protected static final BigDecimal bd_minimum_generated_key_ = new BigDecimal(500000);

    protected final int                sql_type_;
    protected final PersistenceFactory factory_;
    
    /**
     * Initialize the MAX key generator.
     */
    public MaxOrKeepKeyGenerator( PersistenceFactory factory, int sql_type ) throws MappingException {
        factory_ = factory;
        sql_type_ = sql_type;
        if ( sql_type_ != Types.INTEGER && sql_type_ != Types.NUMERIC && sql_type_ != Types.DECIMAL && sql_type_ != Types.BIGINT)
            throw new MappingException( Messages.format( "mapping.keyGenSQLType", getClass().getName(), new Integer( sql_type_ ) ) );
    }
    
    public boolean isKeyGeneratorUsed(Object actualid) {
        if(actualid instanceof Integer) {
            int id = ((Integer) actualid).intValue();
            if(id > 0)
                return false;
        }
        return true;
    }

    /**
     * Generate a new key for the specified table as "MAX(primary_key) + 1".
     *
     * If there is no records in the table, then the value 1 is returned.
     *
     * @param conn An open connection within the given transaction
     * @param tableName The table name
     * @param primKeyName The primary key name
     * @param props A temporary replacement for Principal object
     * @return A new key
     * @throws PersistenceException An error occured talking to persistent
     *  storage
     */
    public Object generateKey( Connection conn, String tableName, String primKeyName, Properties props ) throws PersistenceException {
        Object identity = props.get("identity");
        if(null != identity) {
            if(identity instanceof Integer) {
                int id = ((Integer) identity).intValue();
                if(id > 0)
                    return identity;
            }
        }
        
        String sql;
        PreparedStatement stmt = null;
        ResultSet rs;
        QueryExpression query;
        identity = null;

        try {
            query = factory_.getQueryExpression();
            if ( factory_.getFactoryName().equals( "mysql" ) ) {
                // Create SQL sentence of the form
                // "SELECT MAX(pk) FROM table"
                query.addSelect("MAX(" + factory_.quoteName(primKeyName)+ ")");
                query.addTable(tableName);

                // SELECT without lock
                sql = query.getStatement( false );
            } else {
                // Create SQL sentence of the form
                // "SELECT pk FROM table WHERE pk=(SELECT MAX(t1.pk) FROM table t1)"
                // with database-dependent keyword for lock
                query.addColumn( tableName, primKeyName);
                query.addCondition( tableName, primKeyName, QueryExpression.OpEquals,
                        "(SELECT MAX(t1." + factory_.quoteName(primKeyName) + ") FROM " + factory_.quoteName(tableName) + " t1)");

                // SELECT and put lock on the last record
                sql = query.getStatement( true );
            }

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            if ( rs.next() ) {
                if ( sql_type_ == Types.INTEGER )
                    identity = new Integer( Math.max(rs.getInt( 1 ) + 1, minimum_generated_key_));
                else if ( sql_type_ == Types.BIGINT )
                    identity = new Long( Math.max(rs.getLong( 1 ) + 1,  minimum_generated_key_));
                else {
                    BigDecimal max = rs.getBigDecimal( 1 );
                    if (max == null) {
                        identity = bd_minimum_generated_key_;
                    } else {
                        identity = max.add( ONE );
                        if(max.compareTo(bd_minimum_generated_key_) < 0)
                            identity = bd_minimum_generated_key_;
                    }
                }
            } else {
                if ( sql_type_ == Types.INTEGER )
                    identity = new Integer( minimum_generated_key_ );
                else if ( sql_type_ == Types.BIGINT )
                    identity = new Long( minimum_generated_key_ );
                else
                    identity = bd_minimum_generated_key_;
            }
        } catch ( SQLException ex ) {
            throw new PersistenceException( Messages.format(
                    "persist.keyGenSQL", ex.toString() ), ex );
        } finally {
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException ex ) {
                }
            }
        }
        if (identity == null) {
            throw new PersistenceException( Messages.format("persist.keyGenOverflow", getClass().getName() ) );
        }
        return identity;
    }


    /**
     * Style of key generator: BEFORE_INSERT, DURING_INSERT or AFTER_INSERT ?
     */
    public final byte getStyle() {
        return BEFORE_INSERT;
    }


    /**
     * Gives a possibility to patch the Castor-generated SQL statement
     * for INSERT (makes sense for DURING_INSERT key generators)
     */
    public final String patchSQL( String insert, String primKeyName ) throws MappingException {
        return insert;
    }


    /**
     * Is key generated in the same connection as INSERT?
     */
    public boolean isInSameConnection() {
        return true;
    }

}
