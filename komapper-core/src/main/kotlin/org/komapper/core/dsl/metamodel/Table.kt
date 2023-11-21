package org.komapper.core.dsl.metamodel

import org.komapper.core.dsl.expression.Operand
import java.time.Clock
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

abstract class Table<ENTITY : Any>(val tableName: String) : EntityMetamodel<ENTITY, Int, Table<ENTITY>> {

    private var propertyDescriptors: List<PropertyDescriptor<ENTITY, *, *>>? = null
    private var propertyMetamodels: List<PropertyMetamodel<ENTITY, *, *>>? = null
    private var propertyMetamodelsColumnsMap: Map<PropertyMetamodel<ENTITY, *, *>, Column<*, *, ENTITY>>? = null

    open val id: Column<Int, Int, ENTITY>? = null
    private var idMetamodel: PropertyMetamodel<ENTITY, Int, Int>? = null
    private var idGenerator: IdGenerator.AutoIncrement<ENTITY, Int>? = null
    private val idProperties: MutableList<PropertyMetamodel<ENTITY, Int, Int>> = mutableListOf()

    private val foreignKeyList: MutableList<ForeignKey> = mutableListOf()

    private var initialized = false

    abstract fun toEntity(propertyMap: Map<Column<*, *, ENTITY>, Any?>): ENTITY

    override fun foreignKeys(): List<ForeignKey> = foreignKeyList

    override fun uniqueKeys(): List<UniqueKey> = emptyList()

    override fun indexes(): List<Index> = emptyList()

    override fun alwaysQuote(): Boolean = true

    override fun catalogName(): String = ""

    override fun convertToId(generatedKey: Long): Int? = generatedKey.toInt()

    override fun createdAtAssignment(c: Clock): Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? {
        throw Exception("Not yet implemented: createdAtAssignment")
    }

    override fun createdAtProperty(): PropertyMetamodel<ENTITY, *, *>? {
        throw Exception("Not yet implemented: createdAtProperty")
    }

    override fun declaration(): EntityMetamodelDeclaration<Table<ENTITY>> = {}

    override fun disableSequenceAssignment(): Boolean {
        throw Exception("Not yet implemented: disableSequenceAssignment")
    }

    override fun idGenerator(): IdGenerator<ENTITY, Int>? {
        if (!initialized) initialize()
        return idGenerator
    }

    override fun idProperties(): List<PropertyMetamodel<ENTITY, *, *>> {
        //throw Exception("Not yet implemented: idProperties")
        if (!initialized) initialize()
        return idProperties
    }

    override fun extractId(e: ENTITY): Int {
        throw Exception("Not yet implemented: extractId")
    }

    override fun klass(): KClass<ENTITY> {
        throw Exception("Not yet implemented: klass")
    }

    override fun newEntity(m: Map<PropertyMetamodel<*, *, *>, Any?>): ENTITY {
        //throw Exception("Not yet implemented: newEntity")
        return toEntity(m.mapKeys { propertyMetamodelsColumnsMap!![it.key]!! })
    }

    override fun properties(): List<PropertyMetamodel<ENTITY, *, *>> {
        //throw Exception("Not yet implemented: properties")
        if (!initialized) initialize()
        return propertyMetamodels!!
    }

    override fun preUpdate(e: ENTITY, c: Clock): ENTITY {
        return e
    }

    override fun preInsert(e: ENTITY, c: Clock): ENTITY {
        return e
    }

    override fun postUpdate(e: ENTITY): ENTITY {
        return e
    }

    override fun newMetamodel(table: String, catalog: String, schema: String, alwaysQuote: Boolean, disableSequenceAssignment: Boolean, declaration: EntityMetamodelDeclaration<Table<ENTITY>>): Table<ENTITY> {
        throw Exception("Not yet implemented: newMetamodel")
    }

    override fun schemaName(): String = ""

    override fun tableName(): String = tableName

    override fun updatedAtAssignment(c: Clock): Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? {
        throw Exception("Not yet implemented: updatedAtAssignment")
    }

    override fun updatedAtProperty(): PropertyMetamodel<ENTITY, *, *>? {
        throw Exception("Not yet implemented: updatedAtProperty")
    }

    override fun versionAssignment(): Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? {
        throw Exception("Not yet implemented: versionAssignment")
    }

    override fun versionProperty(): PropertyMetamodel<ENTITY, *, *>? {
        throw Exception("Not yet implemented: versionProperty")
    }

    @Suppress("UNCHECKED_CAST")
    private fun initialize() {
        val kClass = Class.forName(this::class.java.name).kotlin
        val kInstance = kClass.objectInstance!!

        var descriptors = mutableListOf<PropertyDescriptor<ENTITY, *, *>>()
        var metamodels = mutableListOf<PropertyMetamodel<ENTITY, *, *>>()
        val metamodelsColumnsMap = mutableMapOf<PropertyMetamodel<ENTITY, *, *>, Column<*, *, ENTITY>>()

        kInstance::class.memberProperties.forEach { property ->
            val kProperty = property as KProperty1<Any, *>
            try {
                val column = kProperty.get(kInstance) as Column<*, *, ENTITY>

                if (column.name == id?.name) {
                    idMetamodel = column.metamodel as PropertyMetamodel<ENTITY, Int, Int>?
                    idGenerator = IdGenerator.AutoIncrement(idMetamodel!!)
                    idProperties.add(idMetamodel!!)

                    descriptors = (listOf(column.descriptor) + descriptors).toMutableList()
                    metamodels = (listOf(column.metamodel) + metamodels).toMutableList()
                    metamodelsColumnsMap[column.metamodel] = column
                } else {
                    descriptors.add(column.descriptor)
                    metamodels.add(column.metamodel)
                    metamodelsColumnsMap[column.metamodel] = column
                }
            } catch (_: Exception) {
            }
        }

        propertyDescriptors = descriptors
        propertyMetamodels = metamodels
        propertyMetamodelsColumnsMap = metamodelsColumnsMap
        initialized = true
    }

    fun addForeignKey(foreignKey: ForeignKey) = foreignKeyList.add(foreignKey)

    // Column definitions
    private fun <TYPE : Any> createColumn(columnName: String, kClass: KClass<TYPE>, getter: (ENTITY) -> TYPE?, setter: (ENTITY, TYPE) -> ENTITY, options: List<Any> = emptyList()): Column<TYPE, TYPE, ENTITY> {
        val descriptor = PropertyDescriptor(kClass, kClass, columnName, columnName, alwaysQuote = false, masking = false, getter, setter, { it }, { it }, nullable = false, defaultValue = null)
        return Column(name = columnName, descriptor = descriptor, metamodel = PropertyMetamodelImpl(owner = this, descriptor, options))
    }

    fun bool(columnName: String, getter: (ENTITY) -> Boolean, setter: (ENTITY, Boolean) -> ENTITY) = createColumn(columnName, Boolean::class, getter, setter)

    fun integer(columnName: String, getter: (ENTITY) -> Int?, setter: (ENTITY, Int) -> ENTITY) = createColumn(columnName, Int::class, getter, setter)

    fun varchar(columnName: String, length: Int, getter: (ENTITY) -> String?, setter: (ENTITY, String) -> ENTITY) = createColumn(columnName, String::class, getter, setter, listOf(length))

    fun datetime(columnName: String, getter: (ENTITY) -> LocalDateTime, setter: (ENTITY, LocalDateTime) -> ENTITY) = createColumn(columnName, LocalDateTime::class, getter, setter)

    fun <ENUM_TYPE : Enum<ENUM_TYPE>> enum(columnName: String, kClass: KClass<ENUM_TYPE>, getter: (ENTITY) -> ENUM_TYPE?, setter: (ENTITY, ENUM_TYPE) -> ENTITY): Column<ENUM_TYPE, Int, ENTITY> {
        val descriptor = PropertyDescriptor(kClass, Int::class, columnName, columnName, alwaysQuote = false, masking = false, getter, setter, { wrapEnum(ordinal = it, kClass = kClass) }, ::unwrapEnum, nullable = false, defaultValue = null)
        return Column(name = columnName, descriptor = descriptor, metamodel = PropertyMetamodelImpl(owner = this, descriptor))
    }

    fun <TYPE : Any> reference(columnName: String, kClass: KClass<TYPE>, referenceColumn: Column<TYPE, *, *>, onDelete: ReferenceOption, onUpdate: ReferenceOption, getter: (ENTITY) -> TYPE?, setter: (ENTITY, TYPE) -> ENTITY): Column<TYPE, TYPE, ENTITY> {
        addForeignKey(ForeignKey(name = columnName, referenceColumn = referenceColumn, onDelete = onDelete, onUpdate = onUpdate))
        return createColumn(columnName, kClass, getter, setter)
    }

    // Column options
    fun <ENTITY : Any, EXTERIOR : Any, INTERIOR : Any> Column<EXTERIOR, INTERIOR, ENTITY>.nullable(): Column<EXTERIOR, INTERIOR, ENTITY> {
        val descriptor = PropertyDescriptor(exteriorClass = this.descriptor.exteriorClass, interiorClass = this.descriptor.interiorClass, name = this.descriptor.name, columnName = this.descriptor.columnName, alwaysQuote = this.descriptor.alwaysQuote, masking = this.descriptor.masking, getter = this.descriptor.getter, setter = this.descriptor.setter, wrap = this.descriptor.wrap, unwrap = this.descriptor.unwrap, nullable = true, defaultValue = this.descriptor.defaultValue)
        return Column(name = this.name, descriptor = this.descriptor, metamodel = PropertyMetamodelImpl(owner = this.metamodel.owner, descriptor = descriptor, options = this.metamodel.options))
    }

    fun <ENTITY : Any, EXTERIOR : Any, INTERIOR : Any> Column<EXTERIOR, INTERIOR, ENTITY>.default(value: EXTERIOR): Column<EXTERIOR, INTERIOR, ENTITY> {
        val descriptor = PropertyDescriptor(exteriorClass = this.descriptor.exteriorClass, interiorClass = this.descriptor.interiorClass, name = this.descriptor.name, columnName = this.descriptor.columnName, alwaysQuote = this.descriptor.alwaysQuote, masking = this.descriptor.masking, getter = this.descriptor.getter, setter = this.descriptor.setter, wrap = this.descriptor.wrap, unwrap = this.descriptor.unwrap, nullable = this.descriptor.nullable, defaultValue = value)
        return Column(name = this.name, descriptor = this.descriptor, metamodel = PropertyMetamodelImpl(owner = this.metamodel.owner, descriptor = descriptor, options = this.metamodel.options))
    }

    // Column helpers
    private fun <ENUM_TYPE : Enum<ENUM_TYPE>> wrapEnum(ordinal: Int, kClass: KClass<ENUM_TYPE>): ENUM_TYPE = try {
        kClass.java.enumConstants[ordinal]
    } catch (e: ArrayIndexOutOfBoundsException) {
        throw org.komapper.core.dsl.runner.EnumMappingException(kClass, "ordinal", ordinal, e)
    }

    private fun <ENUM_TYPE : Enum<ENUM_TYPE>> unwrapEnum(it: ENUM_TYPE): Int = it.ordinal

    //

    fun shouldCreateMissingProperties(columns: List<String>, indexes: List<String>): Boolean {
        val hasNewColumns = columns.size != this.properties().size
        val hasNewIndexes = indexes.size != (this.foreignKeys().size + this.uniqueKeys().size + this.indexes().size)
        return hasNewColumns && hasNewIndexes
    }
}
