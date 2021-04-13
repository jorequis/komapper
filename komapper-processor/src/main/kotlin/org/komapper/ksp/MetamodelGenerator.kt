package org.komapper.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Nullability
import java.io.PrintWriter
import java.time.ZonedDateTime

private const val Assignment = "org.komapper.core.dsl.metamodel.Assignment"
private const val EntityMetamodel = "org.komapper.core.dsl.metamodel.EntityMetamodel"
private const val EmptyEntityMetamodel = "org.komapper.core.dsl.metamodel.EmptyEntityMetamodel"
private const val EmptyPropertyMetamodel = "org.komapper.core.dsl.metamodel.EmptyPropertyMetamodel"
private const val Identity = "org.komapper.core.dsl.metamodel.Assignment.Identity"
private const val Sequence = "org.komapper.core.dsl.metamodel.Assignment.Sequence"
private const val PropertyDescriptor = "org.komapper.core.dsl.metamodel.PropertyDescriptor"
private const val PropertyMetamodel = "org.komapper.core.dsl.metamodel.PropertyMetamodel"
private const val PropertyMetamodelImpl = "org.komapper.core.dsl.metamodel.PropertyMetamodelImpl"
private const val Clock = "java.time.Clock"
private const val EntityDescriptor = "__EntityDescriptor"

internal class EntityMetamodelGenerator(
    private val entity: Entity,
    private val packageName: String,
    private val entityTypeName: String,
    private val simpleName: String,
    private val w: PrintWriter
) : Runnable {

    private val constructorParamList = listOf(
        "table: String = \"${entity.table.name}\"",
        "catalog: String = \"${entity.table.catalog}\"",
        "schema: String = \"${entity.table.schema}\""
    ).joinToString(", ")

    override fun run() {
        w.println("package $packageName")
        w.println()
        w.println("// generated at ${ZonedDateTime.now()}")
        w.println("@Suppress(\"ClassName\", \"PrivatePropertyName\")")
        w.println("class $simpleName private constructor($constructorParamList) : $EntityMetamodel<$entityTypeName> {")
        w.println("    private val __tableName = table")
        w.println("    private val __catalogName = catalog")
        w.println("    private val __schemaName = schema")

        entityDescriptor()

        propertyMetamodels()

        klass()
        tableName()
        catalogName()
        schemaName()

        idAssignment()
        idProperties()
        versionProperty()
        createdAtProperty()
        updatedAtProperty()
        properties()
        instantiate()
        incrementVersion()
        updateCreatedAt()
        updateUpdatedAt()
        companionObject()

        w.println("}")

        utils()
    }

    private fun entityDescriptor() {
        w.println("    private object $EntityDescriptor {")
        for (p in entity.properties) {
            val getter = "{ it.$p }"
            val setter = "{ e, v -> e.copy($p = v) }"
            val nullable = if (p.nullability == Nullability.NULLABLE) "true" else "false"
            val assignment = when (val kind = p.idGeneratorKind) {
                is IdGeneratorKind.Identity -> {
                    "$Identity<$entityTypeName, ${p.typeName}>(${p.typeName}::class, $setter)"
                }
                is IdGeneratorKind.Sequence -> {
                    val paramList = listOf(
                        "${p.typeName}::class",
                        setter,
                        "\"${kind.name}\"",
                        "\"${kind.catalog}\"",
                        "\"${kind.schema}\"",
                        "${kind.incrementBy}",
                    ).joinToString(", ")
                    "$Sequence<$entityTypeName, ${p.typeName}>($paramList)"
                }
                else -> "null"
            }
            w.println("        val $p = $PropertyDescriptor<$entityTypeName, ${p.typeName}>(${p.typeName}::class, \"$p\", \"${p.column.name}\", $getter, $setter, $nullable, $assignment)")
        }
        w.println("    }")
    }

    private fun propertyMetamodels() {
        for (p in entity.properties) {
            w.println("    val $p by lazy { $PropertyMetamodelImpl(this, $EntityDescriptor.$p) }")
        }
    }

    private fun klass() {
        w.println("    override fun klass() = $entityTypeName::class")
    }

    private fun tableName() {
        w.println("    override fun tableName() = __tableName")
    }

    private fun catalogName() {
        w.println("    override fun catalogName() = __catalogName")
    }

    private fun schemaName() {
        w.println("    override fun schemaName() = __schemaName")
    }

    private fun idAssignment() {
        val p = entity.properties.firstOrNull { it.idGeneratorKind != null }
        w.print("    override fun idAssignment(): $Assignment<$entityTypeName>? = ")
        if (p != null) {
            w.println("$p.idAssignment")
        } else {
            w.println("null")
        }
    }

    private fun idProperties() {
        val idNameList = entity.idProperties.joinToString { it.toString() }
        w.println("    override fun idProperties(): List<$PropertyMetamodel<$entityTypeName, *>> = listOf($idNameList)")
    }

    private fun versionProperty() {
        w.println("    override fun versionProperty(): $PropertyMetamodel<$entityTypeName, *>? = ${entity.versionProperty}")
    }

    private fun createdAtProperty() {
        w.println("    override fun createdAtProperty(): $PropertyMetamodel<$entityTypeName, *>? = ${entity.createdAtProperty}")
    }

    private fun updatedAtProperty() {
        w.println("    override fun updatedAtProperty(): $PropertyMetamodel<$entityTypeName, *>? = ${entity.updatedAtProperty}")
    }

    private fun properties() {
        val nameList = entity.properties.joinToString(",\n        ", prefix = "\n        ") { it.toString() }
        w.println("    override fun properties(): List<$PropertyMetamodel<$entityTypeName, *>> = listOf($nameList)")
    }

    private fun instantiate() {
        val argList = entity.properties.joinToString(",\n        ", prefix = "\n        ") { p ->
            val nullability = if (p.nullability == Nullability.NULLABLE) "?" else ""
            "$p = __m[$p] as ${p.typeName}$nullability"
        }
        w.println("    override fun instantiate(__m: Map<$PropertyMetamodel<*, *>, Any?>) = $entityTypeName($argList)")
    }

    private fun incrementVersion() {
        val body = if (entity.versionProperty == null) {
            "__e"
        } else {
            "${entity.versionProperty}.setter(__e, ${entity.versionProperty}.getter(__e)!!.inc())"
        }
        w.println("    override fun incrementVersion(__e: $entityTypeName): $entityTypeName = $body")
    }

    private fun updateCreatedAt() {
        val property = entity.createdAtProperty
        val body = if (property == null) {
            "__e"
        } else {
            "$property.setter(__e, ${property.typeName}.now(__c))"
        }
        w.println("    override fun updateCreatedAt(__e: $entityTypeName, __c: $Clock): $entityTypeName = $body")
    }

    private fun updateUpdatedAt() {
        val property = entity.updatedAtProperty
        val body = if (property == null) {
            "__e"
        } else {
            "$property.setter(__e, ${property.typeName}.now(__c))"
        }
        w.println("    override fun updateUpdatedAt(__e: $entityTypeName, __c: $Clock): $entityTypeName = $body")
    }

    private fun companionObject() {
        w.println("    companion object {")
        w.println("        val alias = $simpleName()")
        w.println("        fun newAlias($constructorParamList) = $simpleName(table, catalog, schema)")
        w.println("    }")
    }

    private fun utils() {
        if (entity.declaration.hasCompanionObject()) {
            w.println("")
            w.println("val $entityTypeName.Companion.alias get() = $simpleName.alias")
            w.println("fun $entityTypeName.Companion.newAlias($constructorParamList) = $simpleName.newAlias(table, catalog, schema)")
        }
    }
}

internal class EmptyEntityMetamodelGenerator(
    private val classDeclaration: KSClassDeclaration,
    private val packageName: String,
    private val simpleQualifiedName: String,
    private val fileName: String,
    private val w: PrintWriter
) : Runnable {
    private val constructorParamList = listOf(
        "table: String = \"\"",
        "catalog: String = \"\"",
        "schema: String = \"\""
    ).joinToString(", ")

    override fun run() {
        w.println("package $packageName")
        w.println()
        w.println("// generated at ${ZonedDateTime.now()}")
        w.println("@Suppress(\"ClassName\")")
        w.println("class $fileName : $EmptyEntityMetamodel<$simpleQualifiedName>() {")
        val parameters = classDeclaration.primaryConstructor?.parameters
        if (parameters != null) {
            for (p in parameters) {
                w.println("    val $p = $EmptyPropertyMetamodel<$simpleQualifiedName, ${p.type.resolve().declaration.qualifiedName?.asString()}>()")
            }
        }
        w.println("    companion object {")
        w.println("        val alias = $fileName()")
        w.println("        fun newAlias($constructorParamList) = $fileName()")
        w.println("    }")
        w.println("}")
        if (classDeclaration.hasCompanionObject()) {
            w.println("")
            w.println("val $simpleQualifiedName.Companion.alias get() = $fileName.alias")
            w.println("fun $simpleQualifiedName.Companion.newAlias($constructorParamList) = $fileName.newAlias(table, catalog, schema)")
        }
    }
}
