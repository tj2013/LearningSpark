package sql

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkContext, SparkConf}

object Basic {
  case class Cust(id: Integer, name: String, sales: Double, discount: Double, state: String)

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("SQL-Basic").setMaster("local[4]")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    import sqlContext.implicits._

    // create a sequence of case class objects
    // (we defined the case class above)
    val custs = Seq(
      Cust(1, "Widget Co", 120000.00, 0.00, "AZ"),
      Cust(2, "Acme Widgets", 410500.00, 500.00, "CA"),
      Cust(3, "Widgetry", 410500.00, 200.00, "CA"),
      Cust(4, "Widgets R Us", 410500.00, 0.0, "CA"),
      Cust(5, "Ye Olde Widgete", 500.00, 0.0, "MA")
    )
    // make it an RDD and convert to a DataFrame
    val customerDF = sc.parallelize(custs, 4).toDF()

    println("*** See the DataFrame contents")
    customerDF.show()

    println("*** See the first few lines of the DataFrame contents")
    customerDF.show(2)

    println("*** Statistics for the numerical columns")
    customerDF.describe("sales", "discount").show()

    println("*** A DataFrame has a schema")
    customerDF.printSchema()

    //
    // Register with a table name for SQL queries
    //
    customerDF.registerTempTable("customer")

    println("*** Very simple query")
    val allCust = sqlContext.sql("SELECT id, name FROM customer")
    allCust.show()
    println("*** The result has a schema too")
    allCust.printSchema()

    println("*** Very simple query with a filter")
    val californiaCust =
      sqlContext.sql("SELECT id, name, sales FROM customer WHERE state = 'CA'")
    californiaCust.show()
    californiaCust.printSchema()

    println("*** Queries are case sensitive by default, but this can be disabled")

    sqlContext.setConf("spark.sql.caseSensitive", "false")
    //
    // the capitalization of "CUSTOMER" here would normally make the query fail
    // with "Table not found"
    //
    val caseInsensitive =
      sqlContext.sql("SELECT * FROM CUSTOMER")
    caseInsensitive.show()
    sqlContext.setConf("spark.sql.caseSensitive", "true")


    // TODO: CASE .. WHEN ... THEN

    // TODO: When Spark 1.5.0 is released:
    // TODO: current_date, current_timestamp
  }
}
