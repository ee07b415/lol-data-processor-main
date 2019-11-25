package apiparser.model

import com.google.api.services.bigquery.model.{
  TableFieldSchema,
  TableRow,
  TableSchema
}
import com.google.cloud.bigquery.{
  Field,
  FieldValueList,
  InsertAllRequest,
  LegacySQLTypeName,
  Schema
}
import java.sql.Timestamp
import java.util

trait BaseModel {
  def readFromFieldList(row: FieldValueList): Unit = {
    this.getClass.getDeclaredFields
      .foreach(field => {
        field.setAccessible(true)

        if (field.getType.isAssignableFrom(classOf[String]))
          field.set(this, row.get(field.getName).getStringValue)
        else if (field.getType.isAssignableFrom(classOf[Byte]))
          field.set(this, row.get(field.getName).getBytesValue)
        else if (field.getType
                   .isAssignableFrom(classOf[Integer]) || field.getType
                   .isAssignableFrom(classOf[Int]))
          field.set(this, Math.toIntExact(row.get(field.getName).getLongValue))
        else if (field.getType.isAssignableFrom(classOf[Boolean]))
          field.set(this, row.get(field.getName).getBooleanValue)
        else if (field.getType.isAssignableFrom(classOf[Float]))
          field.set(this, row.get(field.getName).getDoubleValue.toFloat)
        else if (field.getType.isAssignableFrom(classOf[Double]))
          field.set(this, row.get(field.getName).getDoubleValue)
        else if (field.getType.isAssignableFrom(classOf[Long]))
          field.set(this, row.get(field.getName).getLongValue)
        else if (field.getType.isAssignableFrom(classOf[Timestamp]))
          field.set(this,
                    new Timestamp(row.get(field.getName).getTimestampValue))
      })
  }

  def makeTableSchema: Schema = {
    val fields: Array[java.lang.reflect.Field] = this.getClass.getDeclaredFields
    val fieldList: util.List[Field] = new util.ArrayList[Field]

    for (field <- fields) {
      field.setAccessible(true)
      if (field.getType.isAssignableFrom(classOf[String]))
        fieldList.add(Field.of(field.getName, LegacySQLTypeName.STRING))
      else if (field.getType.isAssignableFrom(classOf[Byte]))
        fieldList.add(Field.of(field.getName, LegacySQLTypeName.BYTES))
      else if (field.getType.isAssignableFrom(classOf[Integer]) || field.getType
                 .isAssignableFrom(classOf[Int]))
        fieldList.add(Field.of(field.getName, LegacySQLTypeName.INTEGER))
      else if (field.getType.isAssignableFrom(classOf[Boolean]))
        fieldList.add(Field.of(field.getName, LegacySQLTypeName.BOOLEAN))
      else if (field.getType.isAssignableFrom(classOf[Float]))
        fieldList.add(Field.of(field.getName, LegacySQLTypeName.FLOAT))
      else if (field.getType.isAssignableFrom(classOf[Double]))
        fieldList.add(Field.of(field.getName, LegacySQLTypeName.FLOAT))
      else if (field.getType.isAssignableFrom(classOf[Long]))
        fieldList.add(Field.of(field.getName, LegacySQLTypeName.INTEGER))
      else if (field.getType.isAssignableFrom(classOf[Timestamp]))
        fieldList.add(Field.of(field.getName, LegacySQLTypeName.TIMESTAMP))
      else fieldList.add(Field.of(field.getName, LegacySQLTypeName.STRING))
    }
    Schema.of(fieldList)
  }

  def makeBigQueryTableSchema: TableSchema = {
    val fields: Array[java.lang.reflect.Field] = this.getClass.getDeclaredFields
    val tableFields = new util.ArrayList[TableFieldSchema]

    for (field <- fields) {
      field.setAccessible(true)

      if (field.getType.isAssignableFrom(classOf[String]))
        tableFields.add(
          new TableFieldSchema()
            .setName(field.getName)
            .setType(LegacySQLTypeName.STRING.name()))
      else if (field.getType.isAssignableFrom(classOf[Byte]))
        tableFields.add(
          new TableFieldSchema()
            .setName(field.getName)
            .setType(LegacySQLTypeName.BYTES.name()))
      else if (field.getType.isAssignableFrom(classOf[Integer]) || field.getType
                 .isAssignableFrom(classOf[Int]))
        tableFields.add(
          new TableFieldSchema()
            .setName(field.getName)
            .setType(LegacySQLTypeName.INTEGER.name()))
      else if (field.getType.isAssignableFrom(classOf[Boolean]))
        tableFields.add(
          new TableFieldSchema()
            .setName(field.getName)
            .setType(LegacySQLTypeName.BOOLEAN.name()))
      else if (field.getType.isAssignableFrom(classOf[Float]))
        tableFields.add(
          new TableFieldSchema()
            .setName(field.getName)
            .setType(LegacySQLTypeName.FLOAT.name()))
      else if (field.getType.isAssignableFrom(classOf[Double]))
        tableFields.add(
          new TableFieldSchema()
            .setName(field.getName)
            .setType(LegacySQLTypeName.FLOAT.name()))
      else if (field.getType.isAssignableFrom(classOf[Long]))
        tableFields.add(
          new TableFieldSchema()
            .setName(field.getName)
            .setType(LegacySQLTypeName.INTEGER.name()))
      else if (field.getType.isAssignableFrom(classOf[Timestamp]))
        tableFields.add(
          new TableFieldSchema()
            .setName(field.getName)
            .setType(LegacySQLTypeName.TIMESTAMP.name()))
      else
        tableFields.add(
          new TableFieldSchema()
            .setName(field.getName)
            .setType(LegacySQLTypeName.STRING.name()))
    }

    new TableSchema().setFields(tableFields)
  }

  def addTableRow(field: java.lang.reflect.Field,
                  rowContent: util.HashMap[String, AnyRef]): Unit = {
    field.setAccessible(true)
    if (field.getType.isAssignableFrom(classOf[String]))
      rowContent.put(field.getName, field.get(this))
    else if (field.getType.isAssignableFrom(classOf[Byte]))
      rowContent.put(field.getName, field.get(this))
    else if (field.getType.isAssignableFrom(classOf[Integer]) || field.getType
               .isAssignableFrom(classOf[Int]))
      rowContent.put(field.getName, field.get(this))
    else if (field.getType.isAssignableFrom(classOf[Boolean]))
      rowContent.put(field.getName, field.get(this))
    else if (field.getType.isAssignableFrom(classOf[Float]))
      rowContent.put(field.getName, field.get(this))
    else if (field.getType.isAssignableFrom(classOf[Double]))
      rowContent.put(field.getName, field.get(this))
    else if (field.getType.isAssignableFrom(classOf[Long]))
      rowContent.put(field.getName, field.get(this))
    else if (field.getType.isAssignableFrom(classOf[Timestamp]))
      rowContent.put(field.getName, field.get(this))
    else
      rowContent.put(field.getName, field.get(this).toString) // Actually we only need this
  }

  def buildTableRow: InsertAllRequest.RowToInsert = {
    val rowContent = new util.HashMap[String, AnyRef]
    val fields = getClass.getDeclaredFields
    try for (field <- fields) {
      addTableRow(field, rowContent)
    } catch {
      case e: IllegalAccessException =>
        e.printStackTrace()
    }
    InsertAllRequest.RowToInsert.of(rowContent)
  }

  def toBigQueryTableRow: TableRow = {
    val tableRow = new TableRow()
    val fields = getClass.getDeclaredFields

    fields
      .foreach(field => {
        field.setAccessible(true)
        if (field.getType.isAssignableFrom(classOf[String]))
          tableRow.set(field.getName, field.get(this))
        else if (field.getType.isAssignableFrom(classOf[Byte]))
          tableRow.set(field.getName, field.get(this))
        else if (field.getType
                   .isAssignableFrom(classOf[Integer]) || field.getType
                   .isAssignableFrom(classOf[Int]))
          tableRow.set(field.getName, field.get(this))
        else if (field.getType.isAssignableFrom(classOf[Boolean]))
          tableRow.set(field.getName, field.get(this))
        else if (field.getType.isAssignableFrom(classOf[Float]))
          tableRow.set(field.getName, field.get(this))
        else if (field.getType.isAssignableFrom(classOf[Double]))
          tableRow.set(field.getName, field.get(this))
        else if (field.getType.isAssignableFrom(classOf[Long]))
          tableRow.set(field.getName, field.get(this))
        else if (field.getType.isAssignableFrom(classOf[Timestamp]))
          tableRow.set(field.getName, field.get(this))
        else
          tableRow
            .set(field.getName, field.get(this).toString) // Actually we only need this
      })
    tableRow
  }

  def fromBigQueryTableRow(tableRow:TableRow): Unit = {

    this.getClass.getDeclaredFields
      .foreach(field => {
        field.setAccessible(true)

        if (field.getType.isAssignableFrom(classOf[String]))
          field.set(this, tableRow.get(field.getName).asInstanceOf[String])
        else if (field.getType.isAssignableFrom(classOf[Byte]))
          field.set(this, tableRow.get(field.getName).asInstanceOf[Byte])
        else if (field.getType.isAssignableFrom(classOf[Integer]) ||
          field.getType.isAssignableFrom(classOf[Int]))
          field.set(this, tableRow.get(field.getName).toString.toInt)
        else if (field.getType.isAssignableFrom(classOf[Boolean]))
          field.set(this, tableRow.get(field.getName).toString.toBoolean)
        else if (field.getType.isAssignableFrom(classOf[Float]))
          field.set(this, tableRow.get(field.getName).toString.toFloat)
        else if (field.getType.isAssignableFrom(classOf[Double]))
          field.set(this, tableRow.get(field.getName).toString.toDouble)
        else if (field.getType.isAssignableFrom(classOf[Long]))
          field.set(this, tableRow.get(field.getName).toString.toLong)
        else if (field.getType.isAssignableFrom(classOf[Timestamp]))
          field.set(this, new Timestamp(tableRow.get(field.getName).toString.toLong))
      })
  }
}
