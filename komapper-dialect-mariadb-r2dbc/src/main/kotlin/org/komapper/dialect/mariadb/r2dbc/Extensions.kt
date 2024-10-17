package org.komapper.dialect.mariadb.r2dbc

import kotlinx.coroutines.runBlocking
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.metamodel.Column
import org.komapper.core.dsl.metamodel.Table
import org.komapper.core.dsl.query.*
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.tx.core.CoroutineTransactionOperator
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionProperty

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

suspend fun <ENTITY : Any> R2dbcDatabase.selectAllFrom(table: Table<ENTITY>) = runQuery { QueryDsl.from(table) }

suspend fun <ENTITY : Any, RESULT> R2dbcDatabase.selectFrom(table: Table<ENTITY>, block: SelectQueryBuilder<ENTITY, Int, Table<ENTITY>>.() -> Query<RESULT>) = runQuery { QueryDsl.from(table).block() }

suspend fun <ENTITY : Any> R2dbcDatabase.insertInto(table: Table<ENTITY>, block: InsertQueryBuilder<ENTITY, Int, Table<ENTITY>>.() -> Query<ENTITY>) = runQuery { QueryDsl.insert(table).block() }

suspend fun <ENTITY : Any> R2dbcDatabase.updateWhere(table: Table<ENTITY>, block: UpdateQueryBuilder<ENTITY, Int, Table<ENTITY>>.() -> Query<Long>) = runQuery { QueryDsl.update(table).block() }

suspend fun <ENTITY : Any> R2dbcDatabase.updateInto(table: Table<ENTITY>, block: UpdateQueryBuilder<ENTITY, Int, Table<ENTITY>>.() -> Query<ENTITY>) = runQuery { QueryDsl.update(table).block() }

fun <T : Any> SelectQueryBuilder<T, Int, Table<T>>.orderBy(column: Column<*, *, T>): EntitySelectQuery<T> = orderBy(column.metamodel)

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
