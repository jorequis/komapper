package org.komapper.dialect.mariadb.r2dbc

import kotlinx.coroutines.runBlocking
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.Column
import org.komapper.core.dsl.metamodel.Join
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.Table
import org.komapper.core.dsl.query.*
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.tx.core.CoroutineTransactionOperator
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionProperty

@Suppress("UNCHECKED_CAST")
operator fun <EXTERIOR : Any> Map<ColumnExpression<*, *>, Any?>.get(column: Column<EXTERIOR, *, *>): EXTERIOR = this[column.metamodel] as EXTERIOR

fun <JOIN_TYPE : Any> List<Join<JOIN_TYPE>>.contains(table: Table<*>): Boolean = this.any { join -> join.table == table }

suspend fun <R> R2dbcDatabase.transaction(transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED, transactionProperty: TransactionProperty = EmptyTransactionProperty, block: suspend R2dbcDatabase.(CoroutineTransactionOperator) -> R) = withTransaction(transactionAttribute, transactionProperty) { block(it) }

fun <R> R2dbcDatabase.transactionBlocking(transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED, transactionProperty: TransactionProperty = EmptyTransactionProperty, block: suspend R2dbcDatabase.(CoroutineTransactionOperator) -> R) = runBlocking { transaction(transactionAttribute, transactionProperty, block) }

suspend fun <ENTITY : Any> R2dbcDatabase.createTable(table: Table<ENTITY>, withForeignKeys: Boolean) {
    runQuery { QueryDsl.create(listOf(table), withForeignKeys) }
}

suspend fun <ENTITY : Any> R2dbcDatabase.createMissingProperties(dbName: String, table: Table<ENTITY>) {
    val columns = getTableColumns(dbName = dbName, table = table)
    val indexes = getTableIndexes(dbName = dbName, table = table)
    if (table.shouldCreateMissingProperties(columns, indexes))
        runQuery { QueryDsl.createMissingProperties(table, columns, indexes) }
}

suspend fun <ENTITY : Any> R2dbcDatabase.createTableOrMissingProperties(dbName: String, table: Table<ENTITY>, withForeignKeys: Boolean) {
    if (!exists(dbName = dbName, table = table))
        createTable(table, withForeignKeys)
    else
        createMissingProperties(dbName = dbName, table = table)
}

suspend fun <ENTITY : Any> Table<ENTITY>.createIn(database: R2dbcDatabase, withForeignKeys: Boolean) = database.runQuery { QueryDsl.create(listOf(this@createIn), withForeignKeys) }

fun <ENTITY : Any, RESULT> Table<ENTITY>.select(block: SelectQueryBuilder<ENTITY, Int, Table<ENTITY>>.() -> Query<RESULT>): Query<RESULT> = QueryDsl.from(metamodel = this).block()

@Suppress("UNCHECKED_CAST")
fun <ENTITY : Any, RESULT> Table<ENTITY>.selectAll(): Query<RESULT> = QueryDsl.from(metamodel = this) as Query<RESULT>

@Suppress("UNCHECKED_CAST")
fun <ENTITY : Any, RESULT, COLUMN_TYPE : Any> Table<ENTITY>.join(joins: List<Join<COLUMN_TYPE>>): Query<RESULT> {
    val joinsProperties = joins.flatMap { join -> join.table.properties() }

    val query = QueryDsl.from(metamodel = this) as EntitySelectQuery<Table<*>>
    val queryWithJoins = joins.fold(initial = query) { acc, element -> acc.innerJoin(element.table) { element.columnA eq element.columnB } }
    return queryWithJoins.select(*(this.properties() + joinsProperties).toTypedArray()) as Query<RESULT>
}

@Suppress("UNCHECKED_CAST")
suspend fun <ENTITY : Any, COLUMN_TYPE : Any> R2dbcDatabase.selectAllFrom(table: Table<*>, joins: List<Join<COLUMN_TYPE>>): List<ENTITY> {
    val joinsProperties = joins.flatMap { join -> join.table.properties() }

    val query = QueryDsl.from(metamodel = table) as EntitySelectQuery<Table<*>>
    val queryWithJoins = joins.fold(initial = query) { acc, element -> acc.innerJoin(element.table) { element.columnA eq element.columnB } }
    val records = runQuery { queryWithJoins.select(*(table.properties() + joinsProperties).toTypedArray()) } as List<RecordImpl>
    return records.map { record -> table.newJoinEntity(recordImpl = record) as ENTITY }
}

suspend fun <ENTITY : Any> R2dbcDatabase.insertInto(table: Table<ENTITY>, block: InsertQueryBuilder<ENTITY, Int, Table<ENTITY>>.() -> Query<ENTITY>) = runQuery { QueryDsl.insert(table).block() }

suspend fun <ENTITY : Any> R2dbcDatabase.updateWhere(table: Table<ENTITY>, block: UpdateQueryBuilder<ENTITY, Int, Table<ENTITY>>.() -> Query<Long>) = runQuery { QueryDsl.update(table).block() }

suspend fun <ENTITY : Any> R2dbcDatabase.updateInto(table: Table<ENTITY>, block: UpdateQueryBuilder<ENTITY, Int, Table<ENTITY>>.() -> Query<ENTITY>) = runQuery { QueryDsl.update(table).block() }

fun <ENTITY : Any> SelectQueryBuilder<ENTITY, Int, Table<ENTITY>>.orderBy(column: Column<*, *, ENTITY>): EntitySelectQuery<ENTITY> = orderBy(column.metamodel)

private suspend fun <ENTITY : Any> R2dbcDatabase.getTableColumns(dbName: String, table: Table<ENTITY>): List<String> {
    return runQuery {
        QueryDsl.fromTemplate("SELECT * FROM information_schema.columns WHERE table_schema = '$dbName' AND table_name = '${table.tableName}'").select { row -> row.getNotNull<String>("column_name") }
    }
}

suspend fun <ENTITY : Any> R2dbcDatabase.exists(dbName: String, table: Table<ENTITY>): Boolean {
    return runQuery {
        QueryDsl.fromTemplate("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$dbName' AND table_name = '${table.tableName}'").select { row -> row.getNotNull<Boolean>("COUNT(*)") }.single()
    }
}

private suspend fun <ENTITY : Any> R2dbcDatabase.getTableIndexes(dbName: String, table: Table<ENTITY>): List<String> {
    val indexes = runQuery {
        QueryDsl.fromTemplate("SELECT * FROM information_schema.statistics WHERE table_schema = '$dbName' AND table_name = '${table.tableName}' GROUP BY index_name").select { row -> row.getNotNull<String>("index_name") }
    }.toMutableList()
    indexes.remove("PRIMARY")
    return indexes
}
