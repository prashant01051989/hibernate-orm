/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2010, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.dialect;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.JDBCException;
import org.hibernate.LockOptions;
import org.hibernate.PessimisticLockException;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.PositionSubstringFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.exception.spi.TemplatedViolatedConstraintNameExtracter;
import org.hibernate.exception.spi.ViolatedConstraintNameExtracter;
import org.hibernate.id.SequenceGenerator;
import org.hibernate.internal.util.JdbcExceptionHelper;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.descriptor.sql.BlobTypeDescriptor;
import org.hibernate.type.descriptor.sql.ClobTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

/**
 * An SQL dialect for Postgres
 * <p/>
 * For discussion of BLOB support in Postgres, as of 8.4, have a peek at
 * <a href="http://jdbc.postgresql.org/documentation/84/binary-data.html">http://jdbc.postgresql.org/documentation/84/binary-data.html</a>.
 * For the effects in regards to Hibernate see <a href="http://in.relation.to/15492.lace">http://in.relation.to/15492.lace</a>
 *
 * @author Gavin King
 */
public class PostgreSQL81Dialect extends Dialect {

	public PostgreSQL81Dialect() {
		super();
		registerColumnType( Types.BIT, "bool" );
		registerColumnType( Types.BIGINT, "int8" );
		registerColumnType( Types.SMALLINT, "int2" );
		registerColumnType( Types.TINYINT, "int2" );
		registerColumnType( Types.INTEGER, "int4" );
		registerColumnType( Types.CHAR, "char(1)" );
		registerColumnType( Types.VARCHAR, "varchar($l)" );
		registerColumnType( Types.FLOAT, "float4" );
		registerColumnType( Types.DOUBLE, "float8" );
		registerColumnType( Types.DATE, "date" );
		registerColumnType( Types.TIME, "time" );
		registerColumnType( Types.TIMESTAMP, "timestamp" );
		registerColumnType( Types.VARBINARY, "bytea" );
		registerColumnType( Types.BINARY, "bytea" );
		registerColumnType( Types.LONGVARCHAR, "text" );
		registerColumnType( Types.LONGVARBINARY, "bytea" );
		registerColumnType( Types.CLOB, "text" );
		registerColumnType( Types.BLOB, "oid" );
		registerColumnType( Types.NUMERIC, "numeric($p, $s)" );
		registerColumnType( Types.OTHER, "uuid" );

		registerFunction( "abs", new StandardSQLFunction("abs") );
		registerFunction( "sign", new StandardSQLFunction("sign", StandardBasicTypes.INTEGER) );

		registerFunction( "acos", new StandardSQLFunction("acos", StandardBasicTypes.DOUBLE) );
		registerFunction( "asin", new StandardSQLFunction("asin", StandardBasicTypes.DOUBLE) );
		registerFunction( "atan", new StandardSQLFunction("atan", StandardBasicTypes.DOUBLE) );
		registerFunction( "cos", new StandardSQLFunction("cos", StandardBasicTypes.DOUBLE) );
		registerFunction( "cot", new StandardSQLFunction("cot", StandardBasicTypes.DOUBLE) );
		registerFunction( "exp", new StandardSQLFunction("exp", StandardBasicTypes.DOUBLE) );
		registerFunction( "ln", new StandardSQLFunction("ln", StandardBasicTypes.DOUBLE) );
		registerFunction( "log", new StandardSQLFunction("log", StandardBasicTypes.DOUBLE) );
		registerFunction( "sin", new StandardSQLFunction("sin", StandardBasicTypes.DOUBLE) );
		registerFunction( "sqrt", new StandardSQLFunction("sqrt", StandardBasicTypes.DOUBLE) );
		registerFunction( "cbrt", new StandardSQLFunction("cbrt", StandardBasicTypes.DOUBLE) );
		registerFunction( "tan", new StandardSQLFunction("tan", StandardBasicTypes.DOUBLE) );
		registerFunction( "radians", new StandardSQLFunction("radians", StandardBasicTypes.DOUBLE) );
		registerFunction( "degrees", new StandardSQLFunction("degrees", StandardBasicTypes.DOUBLE) );

		registerFunction( "stddev", new StandardSQLFunction("stddev", StandardBasicTypes.DOUBLE) );
		registerFunction( "variance", new StandardSQLFunction("variance", StandardBasicTypes.DOUBLE) );

		registerFunction( "random", new NoArgSQLFunction("random", StandardBasicTypes.DOUBLE) );

		registerFunction( "round", new StandardSQLFunction("round") );
		registerFunction( "trunc", new StandardSQLFunction("trunc") );
		registerFunction( "ceil", new StandardSQLFunction("ceil") );
		registerFunction( "floor", new StandardSQLFunction("floor") );

		registerFunction( "chr", new StandardSQLFunction("chr", StandardBasicTypes.CHARACTER) );
		registerFunction( "lower", new StandardSQLFunction("lower") );
		registerFunction( "upper", new StandardSQLFunction("upper") );
		registerFunction( "substr", new StandardSQLFunction("substr", StandardBasicTypes.STRING) );
		registerFunction( "initcap", new StandardSQLFunction("initcap") );
		registerFunction( "to_ascii", new StandardSQLFunction("to_ascii") );
		registerFunction( "quote_ident", new StandardSQLFunction("quote_ident", StandardBasicTypes.STRING) );
		registerFunction( "quote_literal", new StandardSQLFunction("quote_literal", StandardBasicTypes.STRING) );
		registerFunction( "md5", new StandardSQLFunction("md5") );
		registerFunction( "ascii", new StandardSQLFunction("ascii", StandardBasicTypes.INTEGER) );
		registerFunction( "char_length", new StandardSQLFunction("char_length", StandardBasicTypes.LONG) );
		registerFunction( "bit_length", new StandardSQLFunction("bit_length", StandardBasicTypes.LONG) );
		registerFunction( "octet_length", new StandardSQLFunction("octet_length", StandardBasicTypes.LONG) );

		registerFunction( "age", new StandardSQLFunction("age") );
		registerFunction( "current_date", new NoArgSQLFunction("current_date", StandardBasicTypes.DATE, false) );
		registerFunction( "current_time", new NoArgSQLFunction("current_time", StandardBasicTypes.TIME, false) );
		registerFunction( "current_timestamp", new NoArgSQLFunction("current_timestamp", StandardBasicTypes.TIMESTAMP, false) );
		registerFunction( "date_trunc", new StandardSQLFunction( "date_trunc", StandardBasicTypes.TIMESTAMP ) );
		registerFunction( "localtime", new NoArgSQLFunction("localtime", StandardBasicTypes.TIME, false) );
		registerFunction( "localtimestamp", new NoArgSQLFunction("localtimestamp", StandardBasicTypes.TIMESTAMP, false) );
		registerFunction( "now", new NoArgSQLFunction("now", StandardBasicTypes.TIMESTAMP) );
		registerFunction( "timeofday", new NoArgSQLFunction("timeofday", StandardBasicTypes.STRING) );

		registerFunction( "current_user", new NoArgSQLFunction("current_user", StandardBasicTypes.STRING, false) );
		registerFunction( "session_user", new NoArgSQLFunction("session_user", StandardBasicTypes.STRING, false) );
		registerFunction( "user", new NoArgSQLFunction("user", StandardBasicTypes.STRING, false) );
		registerFunction( "current_database", new NoArgSQLFunction("current_database", StandardBasicTypes.STRING, true) );
		registerFunction( "current_schema", new NoArgSQLFunction("current_schema", StandardBasicTypes.STRING, true) );
		
		registerFunction( "to_char", new StandardSQLFunction("to_char", StandardBasicTypes.STRING) );
		registerFunction( "to_date", new StandardSQLFunction("to_date", StandardBasicTypes.DATE) );
		registerFunction( "to_timestamp", new StandardSQLFunction("to_timestamp", StandardBasicTypes.TIMESTAMP) );
		registerFunction( "to_number", new StandardSQLFunction("to_number", StandardBasicTypes.BIG_DECIMAL) );

		registerFunction( "concat", new VarArgsSQLFunction( StandardBasicTypes.STRING, "(","||",")" ) );

		registerFunction( "locate", new PositionSubstringFunction() );

		registerFunction( "str", new SQLFunctionTemplate(StandardBasicTypes.STRING, "cast(?1 as varchar)") );

		getDefaultProperties().setProperty(Environment.STATEMENT_BATCH_SIZE, DEFAULT_BATCH_SIZE);
		getDefaultProperties().setProperty( Environment.NON_CONTEXTUAL_LOB_CREATION, "true" );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SqlTypeDescriptor getSqlTypeDescriptorOverride(int sqlCode) {
		SqlTypeDescriptor descriptor;
		switch ( sqlCode ) {
			case Types.BLOB: {
				// Force BLOB binding.  Otherwise, byte[] fields annotated
				// with @Lob will attempt to use
				// BlobTypeDescriptor.PRIMITIVE_ARRAY_BINDING.  Since the
				// dialect uses oid for Blobs, byte arrays cannot be used.
				descriptor = BlobTypeDescriptor.BLOB_BINDING;
				break;
			}
			case Types.CLOB: {
				descriptor = ClobTypeDescriptor.CLOB_BINDING;
				break;
			}
			default: {
				descriptor = super.getSqlTypeDescriptorOverride( sqlCode );
				break;
			}
		}
		return descriptor;
	}

	public String getAddColumnString() {
		return "add column";
	}

	public String getSequenceNextValString(String sequenceName) {
		return "select " + getSelectSequenceNextValString( sequenceName );
	}

	public String getSelectSequenceNextValString(String sequenceName) {
		return "nextval ('" + sequenceName + "')";
	}

	public String getCreateSequenceString(String sequenceName) {
		return "create sequence " + sequenceName; //starts with 1, implicitly
	}

	public String getDropSequenceString(String sequenceName) {
		return "drop sequence " + sequenceName;
	}

	public String getCascadeConstraintsString() {
		return " cascade";
	}
	public boolean dropConstraints() {
		return true;
	}

	public boolean supportsSequences() {
		return true;
	}

	public String getQuerySequencesString() {
		return "select relname from pg_class where relkind='S'";
	}

	public boolean supportsLimit() {
		return true;
	}

	public String getLimitString(String sql, boolean hasOffset) {
		return new StringBuilder( sql.length()+20 )
				.append( sql )
				.append( hasOffset ? " limit ? offset ?" : " limit ?" )
				.toString();
	}

	public boolean bindLimitParametersInReverseOrder() {
		return true;
	}

	public boolean supportsIdentityColumns() {
		return true;
	}

	public String getForUpdateString(String aliases) {
		return getForUpdateString() + " of " + aliases;
	}

	public String getIdentitySelectString(String table, String column, int type) {
		return new StringBuilder().append("select currval('")
			.append(table)
			.append('_')
			.append(column)
			.append("_seq')")
			.toString();
	}

	public String getIdentityColumnString(int type) {
		return type==Types.BIGINT ?
			"bigserial not null" :
			"serial not null";
	}

	public boolean hasDataTypeInIdentityColumn() {
		return false;
	}

	public String getNoColumnsInsertString() {
		return "default values";
	}

	public String getCaseInsensitiveLike(){
		return "ilike";
	}

	@Override
	public boolean supportsCaseInsensitiveLike() {
		return true;
	}

	public Class getNativeIdentifierGeneratorClass() {
		return SequenceGenerator.class;
	}

	public boolean supportsOuterJoinForUpdate() {
		return false;
	}
	
	public boolean useInputStreamToInsertBlob() {
		return false;
	}

	public boolean supportsUnionAll() {
		return true;
	}

	/**
	 * Workaround for postgres bug #1453
	 */
	public String getSelectClauseNullString(int sqlType) {
		String typeName = getTypeName(sqlType, 1, 1, 0);
		//trim off the length/precision/scale
		int loc = typeName.indexOf('(');
		if (loc>-1) {
			typeName = typeName.substring(0, loc);
		}
		return "null::" + typeName;
	}

	public boolean supportsCommentOn() {
		return true;
	}

	public boolean supportsTemporaryTables() {
		return true;
	}

	public String getCreateTemporaryTableString() {
		return "create temporary table";
	}

	public String getCreateTemporaryTablePostfix() {
		return "on commit drop";
	}

	/*public boolean dropTemporaryTableAfterUse() {
		//we have to, because postgres sets current tx
		//to rollback only after a failed create table
		return true;
	}*/

	public boolean supportsCurrentTimestampSelection() {
		return true;
	}

	public boolean isCurrentTimestampSelectStringCallable() {
		return false;
	}

	public String getCurrentTimestampSelectString() {
		return "select now()";
	}

	public boolean supportsTupleDistinctCounts() {
		return false;
	}

	public String toBooleanValueString(boolean bool) {
		return bool ? "true" : "false";
	}

	public ViolatedConstraintNameExtracter getViolatedConstraintNameExtracter() {
		return EXTRACTER;
	}

	/**
	 * Constraint-name extractor for Postgres constraint violation exceptions.
	 * Orginally contributed by Denny Bartelt.
	 */
	private static ViolatedConstraintNameExtracter EXTRACTER = new TemplatedViolatedConstraintNameExtracter() {
		public String extractConstraintName(SQLException sqle) {
			try {
				int sqlState = Integer.valueOf( JdbcExceptionHelper.extractSqlState( sqle )).intValue();
				switch (sqlState) {
					// CHECK VIOLATION
					case 23514: return extractUsingTemplate("violates check constraint \"","\"", sqle.getMessage());
					// UNIQUE VIOLATION
					case 23505: return extractUsingTemplate("violates unique constraint \"","\"", sqle.getMessage());
					// FOREIGN KEY VIOLATION
					case 23503: return extractUsingTemplate("violates foreign key constraint \"","\"", sqle.getMessage());
					// NOT NULL VIOLATION
					case 23502: return extractUsingTemplate("null value in column \"","\" violates not-null constraint", sqle.getMessage());
					// TODO: RESTRICT VIOLATION
					case 23001: return null;
					// ALL OTHER
					default: return null;
				}
			} catch (NumberFormatException nfe) {
				return null;
			}
		}
	};
	
	@Override
	public SQLExceptionConversionDelegate buildSQLExceptionConversionDelegate() {
		return new SQLExceptionConversionDelegate() {
			@Override
			public JDBCException convert(SQLException sqlException, String message, String sql) {
				final String sqlState = JdbcExceptionHelper.extractSqlState( sqlException );

				if ( "40P01".equals( sqlState ) ) { // DEADLOCK DETECTED
					return new LockAcquisitionException( message, sqlException, sql );
				}

				if ( "55P03".equals( sqlState ) ) { // LOCK NOT AVAILABLE
					return new PessimisticLockException( message, sqlException, sql );
				}

				// returning null allows other delegates to operate
				return null;
			}
		};
	}
	
	public int registerResultSetOutParameter(CallableStatement statement, int col) throws SQLException {
		// Register the type of the out param - PostgreSQL uses Types.OTHER
		statement.registerOutParameter(col++, Types.OTHER);
		return col;
	}

	public ResultSet getResultSet(CallableStatement ps) throws SQLException {
		ps.execute();
		return (ResultSet) ps.getObject(1);
	}

	public boolean supportsPooledSequences() {
		return true;
	}

	//only necessary for postgre < 7.4
	//http://anoncvs.postgresql.org/cvsweb.cgi/pgsql/doc/src/sgml/ref/create_sequence.sgml
	protected String getCreateSequenceString(String sequenceName, int initialValue, int incrementSize) {
		return getCreateSequenceString( sequenceName ) + " start " + initialValue + " increment " + incrementSize;
	}
	
	// Overridden informational metadata ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// seems to not really...
//	public boolean supportsRowValueConstructorSyntax() {
//		return true;
//	}


    public boolean supportsEmptyInList() {
		return false;
	}

	@Override
	public boolean supportsExpectedLobUsagePattern() {
		return true;
	}

	@Override
	public boolean supportsLobValueChangePropogation() {
		return false;
	}

	@Override
	public boolean supportsUnboundedLobLocatorMaterialization() {
		return false;
	}

	// locking support
	public String getForUpdateString() {
		return " for update";
	}

	public String getWriteLockString(int timeout) {
		if ( timeout == LockOptions.NO_WAIT )
			return " for update nowait";
		else
			return " for update";
	}

	public String getReadLockString(int timeout) {
		if ( timeout == LockOptions.NO_WAIT )
			return " for share nowait";
		else
			return " for share";
	}

	public boolean supportsRowValueConstructorSyntax() {
		return true;
	}
	
	@Override
	public String getForUpdateNowaitString() {
		return getForUpdateString() + " nowait ";
	}
	
	@Override
	public String getForUpdateNowaitString(String aliases) {
		return getForUpdateString(aliases) + " nowait ";
	}
}